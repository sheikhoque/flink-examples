package com.att.dtv.kda.factory;

import com.att.dtv.kda.model.app.ApplicationProperties;
import com.att.dtv.kda.model.app.TimeProcessable;
import com.att.dtv.kda.model.app.VideoPlayerStatsProperties;
import com.att.dtv.kda.serde.FromJsonDeserializer;
import com.att.dtv.kda.sources.FlinkSQSConsumer;
import com.att.dtv.kda.sources.GenericFileInputFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.io.FilePathFilter;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import software.amazon.kinesis.connectors.flink.FlinkKinesisConsumer;
import software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants;

import java.net.URI;
import java.util.Properties;

public class SourceFactory {

  public static <T extends TimeProcessable> DataStreamSource<T> getInputDataStream(StreamExecutionEnvironment streamExecutionEnvironment, ApplicationProperties props, Class<T> type) {
        return getStream(streamExecutionEnvironment, props.getSourceEndpoint(), props.getRegion(), props.getStreamStartingPosition(), type, FileProcessingMode.PROCESS_ONCE, -1, props.getInitialTimestamp(), props.getKinesisShardAssigner(),
                props.getKdsEFOConsumerARN(), props.getKdsEFORegType(), props.getKplKinesisEndpoint());
    }

    public static <T extends TimeProcessable> BroadcastStream<T> getBroadcastStream(StreamExecutionEnvironment streamExecutionEnvironment, VideoPlayerStatsProperties props, Class<T> type) {
        // ugly workaround for unit testing
        DataStream<T> dataStream = getStream(streamExecutionEnvironment, props.getAppControlEndpoint(), props.getRegion(), props.getStreamStartingPosition(), type,
                props.getControlEndpointRescanInterval() == -1 ? FileProcessingMode.PROCESS_ONCE : FileProcessingMode.PROCESS_CONTINUOUSLY, props.getControlEndpointRescanInterval(), props.getInitialTimestamp(),
                StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, props.getKplKinesisEndpoint());

        BroadcastStream<T> broadcastStream;
        MapStateDescriptor<String, T> broadcastState = new MapStateDescriptor<>(
                "broadcastState",
                BasicTypeInfo.STRING_TYPE_INFO,
                dataStream.getType());

        broadcastStream = dataStream.broadcast(broadcastState);

        return broadcastStream;
    }

    public static <T extends TimeProcessable> DataStreamSource<T> getStream(StreamExecutionEnvironment streamExecutionEnvironment, URI endpointURI, String region, String streamStartPosition,
                                                                            Class<T> type, FileProcessingMode fileProcessingMode, long interval, String initialTimestamp,
                                                                            String kinesisShardAssigner, String kdsEFOConsumerArn, String kdsEFORegType, String kinesisEndpoint) {
        DataStreamSource<T> dataStreamSource;
        FromJsonDeserializer<T> fromJsonDeserializer = new FromJsonDeserializer<>(type);
        SupportedProtocol supportedProtocol = new SupportedProtocol(endpointURI);

        switch (supportedProtocol.getProtocolType()) {
            case KINESIS:
                Properties consumerConfig = new Properties();
                consumerConfig.put(ConsumerConfigConstants.AWS_REGION, region);
                consumerConfig.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, streamStartPosition);
                consumerConfig.put(ConsumerConfigConstants.SHARD_GETRECORDS_INTERVAL_MILLIS, "1000");
                consumerConfig.put(ConsumerConfigConstants.SHARD_GETRECORDS_MAX, "600");
                consumerConfig.put(ConsumerConfigConstants.SHARD_USE_ADAPTIVE_READS, "true");
                consumerConfig.put(ConsumerConfigConstants.SHARD_GETRECORDS_RETRIES, "8");
                consumerConfig.put(ConsumerConfigConstants.SHARD_IDLE_INTERVAL_MILLIS,"200");
                consumerConfig.put(ConsumerConfigConstants.SUBSCRIBE_TO_SHARD_RETRIES, "100");
                consumerConfig.put(ConsumerConfigConstants.AWS_ENDPOINT, kinesisEndpoint);
                consumerConfig.put("aws.clientconfig.maxErrorRetry", 10);
                if(StringUtils.isNotEmpty(kdsEFOConsumerArn)){
                    String consumerArnKey = new StringBuilder().append(ConsumerConfigConstants.EFO_CONSUMER_ARN_PREFIX).append(".").append(supportedProtocol.getEndpoint()).toString();
                    consumerConfig.put(ConsumerConfigConstants.RECORD_PUBLISHER_TYPE, "EFO");
                    consumerConfig.put(consumerArnKey, kdsEFOConsumerArn);
                    consumerConfig.put(ConsumerConfigConstants.EFO_REGISTRATION_TYPE, kdsEFORegType);
                }

                if(ConsumerConfigConstants.InitialPosition.valueOf(streamStartPosition) == ConsumerConfigConstants.InitialPosition.AT_TIMESTAMP){
                    if (StringUtils.isBlank(initialTimestamp)) {
                        throw new IllegalArgumentException("Please set value for initial timestamp ('"
                                + ConsumerConfigConstants.STREAM_INITIAL_TIMESTAMP + "') when using AT_TIMESTAMP initial position.");
                    }
                    consumerConfig.put(ConsumerConfigConstants.STREAM_INITIAL_TIMESTAMP, initialTimestamp);
                }

                FlinkKinesisConsumer<T> flinkKinesisConsumer = new FlinkKinesisConsumer<>(
                        supportedProtocol.getEndpoint(),  new CustomKinesisDeserializationSchema<>(fromJsonDeserializer), consumerConfig);

                if(StringUtils.isNotEmpty(kinesisShardAssigner) && VideoPlayerStatsProperties.CUSTOM_KINESIS_SHARD_ASSIGNER.equalsIgnoreCase(kinesisShardAssigner)){
                    flinkKinesisConsumer.setShardAssigner(new RoundRobinKinesisShardAssigner());
                }
                dataStreamSource = streamExecutionEnvironment.addSource(flinkKinesisConsumer);
                break;
            case HDFS:
            case LOCALFS:
            case S3:
                String path = supportedProtocol.getEndpoint();
                GenericFileInputFormat inputFormat = new GenericFileInputFormat(new Path(path), fromJsonDeserializer);
                inputFormat.setNestedFileEnumeration(true);
                inputFormat.setFilesFilter(FilePathFilter.createDefaultFilter());
                dataStreamSource = streamExecutionEnvironment.readFile(inputFormat, path, fileProcessingMode, interval, TypeInformation.of(type));
                break;
            case SQS:
                dataStreamSource = streamExecutionEnvironment.addSource(new FlinkSQSConsumer<>(supportedProtocol.getEndpoint(), fromJsonDeserializer, 20));
                break;
            default:
                throw new UnsupportedOperationException("Protocol for endpoint: " + endpointURI + " is not implemented yet.");
        }

        return dataStreamSource;
    }
}
