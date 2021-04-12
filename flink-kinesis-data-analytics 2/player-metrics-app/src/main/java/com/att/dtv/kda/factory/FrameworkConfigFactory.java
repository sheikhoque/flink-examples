package com.att.dtv.kda.factory;

import com.att.dtv.kda.model.app.ApplicationProperties;
import com.att.dtv.kda.model.app.VideoPlayerStatsProperties;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FrameworkConfigFactory {
    public static void configureSystem(StreamExecutionEnvironment streamExecutionEnvironment, ApplicationProperties properties) {
        streamExecutionEnvironment.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        configureCheckpointParameters(streamExecutionEnvironment, properties);
    }

    private static void configureCheckpointParameters(StreamExecutionEnvironment streamExecutionEnvironment, ApplicationProperties properties) {
        // if checkpoint is not disabled, leave default otherwise
        if (properties.getCheckpointInterval() > 0) {
            streamExecutionEnvironment.enableCheckpointing(properties.getCheckpointInterval());

            // advanced options:

            // AT_LEAST_ONCE is recommended
            streamExecutionEnvironment.getCheckpointConfig().setCheckpointingMode(properties.getCheckpointMode());

            streamExecutionEnvironment.getCheckpointConfig().setMinPauseBetweenCheckpoints(properties.getMinPauseBetweenCheckpoints());

            streamExecutionEnvironment.getCheckpointConfig().setCheckpointTimeout(properties.getCheckpointTimeout());

            // allow only one checkpoint to be in progress at the same time,  recommended 1
            streamExecutionEnvironment.getCheckpointConfig().setMaxConcurrentCheckpoints(properties.getMaxConcurrentCheckpoints());

            // enable externalized checkpoints which are retained after job cancellation, RETAIN_ON_CANCELLATION is recommended
            streamExecutionEnvironment.getCheckpointConfig().enableExternalizedCheckpoints(properties.getCheckpointCleanup());

        }
    }
}
