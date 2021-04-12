package com.att.dtv.kda.factory;

import com.amazonaws.services.kinesisanalytics.flink.connectors.producer.FlinkKinesisFirehoseProducer;
import com.att.dtv.kda.model.app.ApplicationProperties;
import com.att.dtv.kda.model.app.CustomKinesisProducerSerializationSchema;
import com.att.dtv.kda.model.app.VideoPlayerStatsProperties;
import com.att.dtv.kda.sinks.AmazonElasticsearchSink;
import com.att.dtv.kda.sinks.DummySink;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import software.amazon.kinesis.connectors.flink.FlinkKinesisProducer;
import software.amazon.kinesis.connectors.flink.KinesisPartitioner;
import software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants;

import java.util.Properties;
import java.util.UUID;

public class SinkFactory {
    public static SinkFunction<Tuple4<String, String, String, String>> getIntervalStatsSink(ApplicationProperties props) {

        SinkFunction<Tuple4<String, String, String, String>> dataStreamSink;
        SupportedProtocol supportedProtocol = new SupportedProtocol(props.getIntervalStatsEndpoint());

        switch (supportedProtocol.getProtocolType()) {
            case ES:
            case ES_SECURED:
                dataStreamSink = AmazonElasticsearchSink.
                        buildElasticsearchSink(supportedProtocol.getEndpoint(), props.getRegion(),
                                props.getElasticSearchUserName(), props.getElasticSearchPassword(), props.getElasticsearchMaxFlushSizeMB(), props.getElasticsearchBackoffRetryNumber(), props.getElasticsearchBackoffDelayInMillis(),
                                props.getElasticsearchFlushMaxActions());
                break;
            case LOCALFS:
            case S3:
            case HDFS:
                dataStreamSink = StreamingFileSink.forRowFormat(new Path(supportedProtocol.getEndpoint()), new SimpleStringEncoder<Tuple4<String, String, String, String>>("UTF-8")).build();
                break;
            case KINESIS:
                Properties producerConfig = new Properties();
                producerConfig.put(ConsumerConfigConstants.AWS_REGION, props.getRegion());
                producerConfig.put("MetricsGranularity", props.getKplMetricsGranularity());
                producerConfig.put("LogLevel", props.getKplLogLevel());
                producerConfig.put("AggregationEnabled", props.getKplAggregationEnabled());
                producerConfig.put("AggregationMaxSize", props.getKplAggregationMaxSize());
                producerConfig.put("CollectionMaxCount", props.getKplCollectionMaxCount());
                producerConfig.put("CollectionMaxSize", props.getKplCollectionMaxSize());
                producerConfig.put("ConnectTimeout", props.getKplConnectTimeout());
                producerConfig.put("MaxConnections", props.getKplMaxConnections());
                producerConfig.put("RateLimit", props.getKplRateLimit());
                producerConfig.put("RecordMaxBufferedTime", props.getKplRecordMaxBufferedTime());
                producerConfig.put("RecordTtl", props.getKplRecordTtl());
                producerConfig.put("RequestTimeout", props.getKplRequestTimeout());
                producerConfig.put("ThreadPoolSize", props.getKplThreadPoolSize());
                producerConfig.put(ConsumerConfigConstants.AWS_ENDPOINT, props.getKplKinesisEndpoint());

                FlinkKinesisProducer<Tuple4<String, String, String, String>> flinkKinesisProducer = new FlinkKinesisProducer<>(new CustomKinesisProducerSerializationSchema(), producerConfig);
                flinkKinesisProducer.setDefaultStream(supportedProtocol.getEndpoint());
                flinkKinesisProducer.setQueueLimit(props.getKplQueueLimit());
                flinkKinesisProducer.setCustomPartitioner(new KinesisPartitioner<Tuple4<String, String, String, String>>() {
                    @Override
                    public String getPartitionId(Tuple4<String, String, String, String> element) {
                        return element.f0.substring(0, element.f0.lastIndexOf("@"));
                    }
                });
                dataStreamSink = flinkKinesisProducer;
                break;
            case DUMMY:
                dataStreamSink = new DummySink<>();
                break;
            default:
                throw new UnsupportedOperationException("Protocol for endpoint: " + props.getIntervalStatsEndpoint() + " is not implemented yet.");
        }
        return dataStreamSink;
    }


    public static SinkFunction<String> getSessionStatsSink(ApplicationProperties props) {
        SinkFunction<String> dataStreamSink;
        SupportedProtocol supportedProtocol = new SupportedProtocol(props.getSessionStatsEndpoint());

        switch (supportedProtocol.getProtocolType()) {
            case LOCALFS:
            case S3:
            case HDFS:
                dataStreamSink = StreamingFileSink.forRowFormat(new Path(supportedProtocol.getEndpoint()), new SimpleStringEncoder<String>("UTF-8")).build();
                break;
            case KINESIS:
                Properties producerConfig = new Properties();
                producerConfig.put(ConsumerConfigConstants.AWS_REGION, props.getRegion());
                FlinkKinesisProducer<String> flinkKinesisProducer = new FlinkKinesisProducer<>(new SimpleStringSchema(), producerConfig);
                flinkKinesisProducer.setDefaultStream(supportedProtocol.getEndpoint());
                producerConfig.put("MetricsGranularity", props.getKplMetricsGranularity());
                producerConfig.put("LogLevel", props.getKplLogLevel());
                flinkKinesisProducer.setCustomPartitioner(new KinesisPartitioner<String>() {
                    @Override
                    public String getPartitionId(String element) {
                        return UUID.randomUUID().toString();
                    }
                });
                dataStreamSink = flinkKinesisProducer;
                break;
            case FIREHOSE:
                Properties configProp = new Properties();
                configProp.put(ConsumerConfigConstants.AWS_REGION, props.getRegion());
                dataStreamSink = new FlinkKinesisFirehoseProducer<>(supportedProtocol.getEndpoint(), new SimpleStringSchema(), configProp);
                break;
            case DUMMY:
                dataStreamSink = new DummySink<>();
                break;
            default:
                throw new UnsupportedOperationException("Protocol for endpoint: " + props.getSessionStatsEndpoint() + " is not implemented yet.");
        }
        return dataStreamSink;
    }

}
