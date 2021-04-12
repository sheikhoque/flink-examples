package com.att.dtv.kda.videoapp;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.att.dtv.kda.factory.*;
import com.att.dtv.kda.model.app.VideoPlayerStatsProperties;
import com.att.dtv.kda.process.DataPipelineFlow;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "video-player-stats", mixinStandardHelpOptions = true, description = "The app reads stream of app events and calculates video quality metrics")
public class VideoPlayerStatsApp implements Callable {

    public static final long serialVersionUID = 369739782576481095L;
    private static Logger LOG = LoggerFactory.getLogger(VideoPlayerStatsApp.class);

    @CommandLine.Option(names = {"-g", "--property-group"}, description = "Property Group")
    private String propertyGroupId = "VideoStatsApp";

    @CommandLine.Option(names = {"-f", "--property-file"}, description = "Property File")
    private String propertyFile = "";

    public static void main(String[] args) {
        new CommandLine(new VideoPlayerStatsApp()).execute(args);
    }

    @Override
    public Integer call() throws Exception {

        final StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = initRuntimeConfigProperties(streamExecutionEnvironment, propertyGroupId, propertyFile);
        final VideoPlayerStatsProperties videoPlayerStatsProperties = new VideoPlayerStatsProperties(properties);
        FrameworkConfigFactory.configureSystem(streamExecutionEnvironment, videoPlayerStatsProperties);

        LOG.warn("Starting Kinesis Analytics App using parameters: {}", videoPlayerStatsProperties);


        streamExecutionEnvironment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        streamExecutionEnvironment.getConfig().setAutoWatermarkInterval(videoPlayerStatsProperties.getAutoWatermarkInterval());

        if (videoPlayerStatsProperties.getParallelism() > 0)
            streamExecutionEnvironment.setParallelism(videoPlayerStatsProperties.getParallelism());

        DataPipelineFlow dataPipelineFlow = new DataPipelineFlow(streamExecutionEnvironment, videoPlayerStatsProperties);
        dataPipelineFlow.prepareFlow();

        streamExecutionEnvironment.execute();

        return 0;
    }

    // helper method to return runtime properties for Property Group AppProperties
    private static Properties initRuntimeConfigProperties(StreamExecutionEnvironment env, String propertyGroupId, String propertyFile) throws IllegalArgumentException, IOException {

        Map<String, Properties> applicationProperties;
        String versionNo = "x.x.x";

        if (!propertyFile.isEmpty())
            LOG.debug("Load AppProperties from provided file: {}", propertyFile);
        if (env instanceof LocalStreamEnvironment) {
            if (propertyFile.isEmpty())
                propertyFile = VideoPlayerStatsApp.class.getClassLoader().getResource("conf/app_properties.json").getPath();
            applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties(propertyFile);
        } else {
            versionNo = getVersionNo();
            if (propertyFile.isEmpty())
                applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();
            else
                applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties(propertyFile);
        }

        Properties props = applicationProperties.get(propertyGroupId);
        if (props == null || props.size() <= 0) {
            throw new IllegalArgumentException("No such property group found or group have no properties, group id: " + propertyGroupId);
        }
        props.put("version", versionNo);
        return props;
    }

    public static String getVersionNo() {
        String version = "x.x.x";
        String pomPath = "META-INF/maven/com.att.dtv.kda/player-metrics-app/pom.properties";
        Properties pomProperties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = VideoPlayerStatsApp.class.getClassLoader().getResourceAsStream(pomPath);
            pomProperties.load(inputStream);
        } catch (Exception e) {
            if (inputStream == null)
                LOG.error("Unable to find pom.properties @ " + pomPath, e);
            else {
                LOG.error("Error in loading pom.properties ", e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pomProperties.getProperty("version", version);
    }

}
