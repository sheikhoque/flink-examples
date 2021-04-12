package com.att.dtv.kda.sources;

import com.amazonaws.services.sqs.model.Message;
import com.att.dtv.kda.sources.config.SQSConnectionConfig;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.MultipleIdsMessageAcknowledgingSourceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FlinkSQSConsumer<T> extends MultipleIdsMessageAcknowledgingSourceBase<T, String, Long>
        implements ResultTypeQueryable<T> {

    private static Logger LOG = LoggerFactory.getLogger(FlinkSQSConsumer.class);

    private final SQSConnectionConfig sqsConnectionConfig;
    private DeserializationSchema<T> schema;
    private transient volatile boolean running;

    public FlinkSQSConsumer(SQSConnectionConfig sqsConnectionConfig, DeserializationSchema<T> schema) {
        super(String.class);
        this.sqsConnectionConfig = sqsConnectionConfig;
        this.schema = schema;
    }

    public FlinkSQSConsumer(String queueName, DeserializationSchema<T> schema, int longPolling) {
        this(new SQSConnectionConfig.SQSConnectionConfigBuilder().withQueueName(queueName).withLongPolling(longPolling).build(), schema);
    }

    public FlinkSQSConsumer(String queueName, DeserializationSchema<T> schema){
        this(queueName, schema, 0);
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return schema.getProducedType();
    }

    @Override
    public void open(Configuration config) throws Exception {
        super.open(config);
        boolean connected = this.sqsConnectionConfig.createConnection();
        if (!connected)
            throw new Exception("FlinkSQSConsumerError: Connection setup failed. " + sqsConnectionConfig);
        running = true;
    }

    @Override
    protected void acknowledgeSessionIDs(List<Long> longs) {
        //do nothing
    }

    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        while (running) {
            try {
                List<Message> messages = sqsConnectionConfig.receiveMessageRequest();
                for (final Message message : messages) {
                    ctx.collect(schema.deserialize(message.getBody().getBytes()));
                    sqsConnectionConfig.deleteMessage(message.getReceiptHandle());
                }
            } catch (Exception e) {
                LOG.error("FlinkSQSConsumerError: unable to consume message from SQS. ", e);
            }

        }
    }

    @Override
    public void cancel() {
        running = false;
    }
}
