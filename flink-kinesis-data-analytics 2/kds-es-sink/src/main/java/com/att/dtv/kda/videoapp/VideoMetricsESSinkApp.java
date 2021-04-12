package com.att.dtv.kda.videoapp;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.att.dtv.kda.converters.SessionStatsToESModelConverter;
import com.att.dtv.kda.converters.SessionStatsToReportConverter;
import com.att.dtv.kda.factory.*;
import com.att.dtv.kda.model.app.ApplicationProperties;
import com.att.dtv.kda.model.app.SessionStatsAggr;
import com.att.dtv.kda.model.app.VideoPlayerStatsProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "metric-stats-es", mixinStandardHelpOptions = true, description = "The app reads  metrics and writes to es")
public class VideoMetricsESSinkApp implements Callable {

    public static final long serialVersionUID = 369739782576481095L;
    private static Logger LOG = LoggerFactory.getLogger(VideoMetricsESSinkApp.class);
    final static GsonBuilder builder = new GsonBuilder();
    final static Gson gson = builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

    @CommandLine.Option(names = {"-g", "--property-group"}, description = "Property Group")
    private String propertyGroupId = "StatsESApp";

    @CommandLine.Option(names = {"-f", "--property-file"}, description = "Property File")
    private String propertyFile = "";

    public static void main(String[] args) {
        new CommandLine(new VideoMetricsESSinkApp()).execute(args);
    }

    @Override
    public Integer call() throws Exception {

        final StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = initRuntimeConfigProperties(streamExecutionEnvironment, propertyGroupId, propertyFile);
        final ApplicationProperties videoPlayerStatsProperties = new KdsEsConfig(properties);
        FrameworkConfigFactory.configureSystem(streamExecutionEnvironment, videoPlayerStatsProperties);


        LOG.warn("Starting Kinesis Analytics App Sample using Streaming API sourceEndpoint {} intervalStatsEndpoint {} region {} systemParallelism {}",
                videoPlayerStatsProperties.getSourceEndpoint(), videoPlayerStatsProperties.getIntervalStatsEndpoint(), videoPlayerStatsProperties.getRegion(),
                streamExecutionEnvironment.getParallelism());

        SinkFunction<Tuple4<String, String, String, String>> elasticSearchSinkOneMinInterval =  SinkFactory.getIntervalStatsSink(videoPlayerStatsProperties);
        //SinkFunction<Tuple3<String, String, String>> elasticSearchSinkLongerInterval =  SinkFactory.getIntervalStatsSink(videoPlayerStatsProperties, false);
        SinkFunction<String> ssdReportSink = SinkFactory.getSessionStatsSink(videoPlayerStatsProperties);
        DataStream<SessionStatsAggr> sessionStream = SourceFactory
                .getInputDataStream(streamExecutionEnvironment, videoPlayerStatsProperties, SessionStatsAggr.class)
                .uid("kds-source-id").name("kds-source");
        SingleOutputStreamOperator<SessionStatsAggr> elasticSearchSink = sessionStream
                .filter(ssa-> !ssa.isReport() && ssa.getInterval()!=null )
                    .uid("kds-filter-es-id").name("kds-filter-es");


        SingleOutputStreamOperator<SessionStatsAggr> ssdStream = sessionStream
                .filter(ssa-> ssa.isReport())
                .uid("kds-filter-ssd-id").name("kds-filter-ssd");

        elasticSearchSink
                .map(getElasticSearchModelMapper(videoPlayerStatsProperties))
                .uid("model-converter-es-id").name("model-converter-es")
                .addSink(elasticSearchSinkOneMinInterval)
                .uid("es-sink-id").name("es-sink-name");



        ssdStream
                .map(getSSDModelMapper())
                .uid("model-converter-ssd-id")
                .name("model-converter-ssd")
                .addSink(ssdReportSink)
                .uid("ssd-sink-id")
                .name("ssd-sink");


        streamExecutionEnvironment.execute();

        return 0;
    }

    private static MapFunction<SessionStatsAggr, Tuple4<String, String, String, String>> getElasticSearchModelMapper(ApplicationProperties videoPlayerStatsProperties) {
        return new MapFunction<SessionStatsAggr, Tuple4<String, String, String, String>>() {
            private static final long serialVersionUID = 4561647090360706L;

            @Override
            public Tuple4<String, String, String, String> map(SessionStatsAggr sessionStatsAggr) throws Exception {
                return Tuple4.of(
                        SessionStatsToESModelConverter.buildId(sessionStatsAggr),
                        SessionStatsToESModelConverter.convertJson(sessionStatsAggr),
                        SessionStatsToESModelConverter.buildSessionSuffix(sessionStatsAggr, videoPlayerStatsProperties.getEsIndexPeriod()),
                        videoPlayerStatsProperties.getIntervalLengths().size() > 1 && sessionStatsAggr.getInterval().getLength()>1? videoPlayerStatsProperties.getLongerIntervalIndexPrefix():videoPlayerStatsProperties.getShorterIntervalIndexPrefix()); //session suffix is responsible for ES index suffix (daily indices)
            }
        };
    }

    private MapFunction<SessionStatsAggr, String> getSSDModelMapper() {
        MapFunction<SessionStatsAggr, String> mf = new MapFunction<SessionStatsAggr, String>() {
            private static final long serialVersionUID = 4561647090360706L;

            @Override
            public String map(SessionStatsAggr value) {
                return SessionStatsToReportConverter.convertJson(value);
            }
        };
        return mf;
    }


    // helper method to return runtime properties for Property Group AppProperties
    private static Properties initRuntimeConfigProperties(StreamExecutionEnvironment env, String propertyGroupId, String propertyFile) throws IllegalArgumentException, IOException {

        Map<String, Properties> applicationProperties;

        if (!propertyFile.isEmpty())
            LOG.debug("Load AppProperties from provided file: {}", propertyFile);
        if (env instanceof LocalStreamEnvironment) {
            if (propertyFile.isEmpty())
                propertyFile = VideoMetricsESSinkApp.class.getClassLoader().getResource("conf/app_properties.json").getPath();
            applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties(propertyFile);
            env.setParallelism(1);
        } else {
            if (propertyFile.isEmpty())
                applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();
            else
                applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties(propertyFile);
        }

        Properties props = applicationProperties.get(propertyGroupId);
        if (props == null || props.size() <= 0) {
            throw new IllegalArgumentException("No such property group found or group have no properties, group id: " + propertyGroupId);
        }
        return props;
    }

}
