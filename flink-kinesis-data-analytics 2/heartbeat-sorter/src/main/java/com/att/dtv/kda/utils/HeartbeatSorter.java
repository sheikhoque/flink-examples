package com.att.dtv.kda.utils;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.att.dtv.kda.flink.GZipHeartbeatOutputFormat;
import com.att.dtv.kda.model.app.Heartbeat;
import com.att.dtv.kda.factory.SupportedProtocol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.FileOutputFormat;
import org.apache.flink.api.common.io.FilePathFilter;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.LocalEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "heartbeat-sorter", mixinStandardHelpOptions = true, description = "The app reads raw Heartbeat events from S3 folder, sort by event timestamp and write to another S3 location in bucketed and compressed form.")
public class HeartbeatSorter implements Callable {

    private static Class<?> thisClass = HeartbeatSorter.class;
    private static Logger LOG = LoggerFactory.getLogger(thisClass);
    private ZoneOffset UTC = ZoneOffset.UTC;
    private ChronoUnit chronoUnit = ChronoUnit.DAYS;

    @CommandLine.Option(names = {"-g", "--property-group"}, description = "Property Group")
    private String propertyGroupId = "Parameters";

    @CommandLine.Option(names = {"-f", "--property-file"}, description = "Property File")
    private String propertyFile = "";

    @CommandLine.Option(names = {"-s", "--start-date"}, description = "Start date")
    private LocalDateTime startDateTime = LocalDateTime.MIN;

    @CommandLine.Option(names = {"-e", "--end-date"}, description = "End date")
    private LocalDateTime endDateTime = LocalDateTime.MAX;

    @CommandLine.Option(names = {"-t", "--session-ttl"}, description = "Session time to live in hours")
    private int sessionTTL = 24; // 24 hours

    public static void main(String[] args) {
        new CommandLine(new HeartbeatSorter()).execute(args);
    }

    public Integer call() throws Exception {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // set up the batch execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setRestartStrategy(RestartStrategies.noRestart());

        Properties props = initRuntimeConfigProperties(env, propertyGroupId, propertyFile);

        List<Path> sourcePaths = new LinkedList<>();

        String inputPath = new SupportedProtocol(new URI(props.getProperty("sourceEndpoint"))).getEndpoint();
        String outputPath = new SupportedProtocol(new URI(props.getProperty("targetEndpoint"))).getEndpoint();

        sourcePaths.add(new Path(inputPath));

        if (props.getProperty("sessionTTL") != null)
            sessionTTL = Integer.valueOf(props.getProperty("sessionTTL"));

        String sourcePathPrefix = "year=!{timestamp:YYYY}/month=!{timestamp:MM}/day=!{timestamp:dd}/";
        if (props.getProperty("filePathPrefix") != null)
            sourcePathPrefix = props.getProperty("filePathPrefix");

        String startDateParam = props.getProperty("startDate");
        String endDateParam = props.getProperty("endDate");
        if (startDateParam != null && endDateParam != null) {
            this.startDateTime = getLocalDateTime(startDateParam);
            this.endDateTime = getLocalDateTime(endDateParam).plus(1, chronoUnit).minusNanos(1); // the whole last hour should be included
            if (this.startDateTime.isAfter(this.endDateTime))
                throw new IllegalArgumentException("Start Date should be less than End Date. Equal is allowed");

            sourcePaths = getFilePath(startDateTime, endDateTime, sessionTTL, inputPath, sourcePathPrefix);
        }

        if (props.getProperty("parallelism") != null)
            env.setParallelism(Integer.valueOf(props.getProperty("parallelism")));
        LOG.warn("Starting HeartbeatsSorter using Batch API sourceEndpoint {} targetEndpoint {} region {} systemParallelism {}",
                props.getProperty("sourceEndpoint"), props.getProperty("targetEndpoint"), props.getProperty("region"), env.getParallelism());

        TextInputFormat textInputFormat = new TextInputFormat(new Path());
        textInputFormat.setNestedFileEnumeration(true);
        textInputFormat.setFilesFilter(FilePathFilter.createDefaultFilter());
        textInputFormat.setFilePaths(sourcePaths.toArray(new Path[]{}));

        DataSet<String> heartbeatDataSet = new DataSource<>(env, textInputFormat, BasicTypeInfo.STRING_TYPE_INFO, "s3Source");

        GZipHeartbeatOutputFormat output = new GZipHeartbeatOutputFormat();
        output.setOutputDirectoryMode(FileOutputFormat.OutputDirectoryMode.ALWAYS);
        output.setWriteMode(FileSystem.WriteMode.OVERWRITE);


        long sessionStartTime = startDateTime.toEpochSecond(UTC) * 1000; //to nano-seconds
        long sessionEndTime = endDateTime.toEpochSecond(UTC) * 1000 + 999; //to nano-seconds

        heartbeatDataSet
                .map(json2Heartbeat).returns(Heartbeat.class).name("json2Heartbeat")
                .filter(hb -> (hb != null) && (hb.getSessionStartTime() >= sessionStartTime && hb.getSessionStartTime() <= sessionEndTime)).name("filterNotNull")
                .partitionByHash(keyBuilder).name("partitionByKey")
                .sortPartition(tsSelector, Order.ASCENDING).name("sortByTime")
                .write(output, outputPath);

        env.execute("HeartbeatsSorter");

        return 0;
    }


    @NotNull
    private LocalDateTime getLocalDateTime(String dateTime) {
        try {
            return LocalDate.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(UTC)).atStartOfDay();
        } catch (DateTimeParseException e) {
            try {
                // If daily format doesn't work, let's try hourly
                chronoUnit = ChronoUnit.HOURS;
                return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd:HH").withZone(UTC));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Incorrect date or datetime format provided, date should be provided in yyyy-MM-dd or yyyy-MM-dd:HH24 format");
            }
        }
    }

    // helper method to return runtime properties for Property Group AppProperties
    private static Properties initRuntimeConfigProperties(ExecutionEnvironment env, String propertyGroupId, String propertyFile) throws IllegalArgumentException, IOException {
        Map<String, Properties> applicationProperties;

        if (!propertyFile.isEmpty())
            LOG.debug("Load AppProperties from provided file: {}", propertyFile);
        if (env instanceof LocalEnvironment) {
            if (propertyFile.isEmpty())
                propertyFile = Heartbeat.class.getClassLoader().getResource("conf/app_properties.json").getPath();
            applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties(propertyFile);
        } else {
            if (propertyFile.isEmpty())
                applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();
            else
                applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties(propertyFile);
        }
        LOG.warn("AppProperties: {} ", applicationProperties);
        Properties props = applicationProperties.get(propertyGroupId);
        if (props == null || props.size() <= 0) {
            throw new IllegalArgumentException("No such property group found or group have no properties, group id: " + propertyGroupId);
        }

        return props;
    }


    final static GsonBuilder builder = new GsonBuilder();
    final static Gson gson = builder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

    final static MapFunction<String, Heartbeat> json2Heartbeat = new MapFunction<String, Heartbeat>() {

        @Override
        public Heartbeat map(String json) {
            Heartbeat hb = null;

            String[] jsons = json.split("(?<=\\})(?=\\{)");

            if (jsons != null && jsons.length > 1) {
                for (String js : jsons) {
                    try {
                        hb = gson.fromJson(js, Heartbeat.class);
                        return hb;
                    } catch (Exception e) {
                        LOG.error("Bad sub json data format: {}", js, e);
                    }
                }
            } else {
                try {
                    hb = gson.fromJson(json, Heartbeat.class);
                    return hb;
                } catch (Exception e) {
                    LOG.error("Bad data format: {}", json, e);
                }
            }
                return null;
        }
    };

    final static KeySelector<Heartbeat, String> keyBuilder = new KeySelector<Heartbeat, String>() {
        private static final long serialVersionUID = 6598119592182548520L;

        @Override
        public String getKey(Heartbeat hb) throws Exception {
            return String.valueOf(hb.getSessionId()).concat(hb.getViewerId());
        }
    };

    final static KeySelector<Heartbeat, Long> tsSelector = new KeySelector<Heartbeat, Long>() {
        private static final long serialVersionUID = 809705083398879065L;

        @Override
        public Long getKey(Heartbeat heartbeat) throws Exception {
            return heartbeat.tsMillis();
        }
    };


    public List<Path> getFilePath(LocalDateTime startDateTime, LocalDateTime endDateTime, int sessionTTL, String sourcePath, String pathPrefix) {

        Map<String, String> values = new HashMap<>();
        StrSubstitutor ss = new StrSubstitutor(values, "!{timestamp:", "}");
        List<Path> sourcePaths = new LinkedList<>();

        for (long i = 0; i < chronoUnit.between(startDateTime, endDateTime.plusHours(sessionTTL)); i++) {
            LocalDateTime dateTime = startDateTime.plus(i, chronoUnit);
            switch (chronoUnit) {
                case HOURS:
                    values.put("HH", String.format("%02d", dateTime.getHour()));
                default:
                    values.put("YYYY", String.format("%04d", dateTime.getYear()));
                    values.put("MM", String.format("%02d", dateTime.getMonthValue()));
                    values.put("dd", String.format("%02d", dateTime.getDayOfMonth()));
            }

            sourcePaths.add(new Path(sourcePath.concat("/").concat(ss.replace(pathPrefix))));
        }

        return sourcePaths;
    }
}