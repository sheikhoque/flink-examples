package com.att.dtv.kda.factory;

import com.att.dtv.kda.model.app.TimeProcessable;
import com.att.dtv.kda.serde.FromJsonDeserializer;
import software.amazon.kinesis.connectors.flink.serialization.KinesisDeserializationSchemaWrapper;

import java.io.IOException;
import java.io.Serializable;

public class CustomKinesisDeserializationSchema<T extends TimeProcessable> extends KinesisDeserializationSchemaWrapper<T> implements Serializable {
    private final FromJsonDeserializer<T> deserializationSchema;

    public CustomKinesisDeserializationSchema(FromJsonDeserializer<T> deserializationSchema) {
        super(deserializationSchema);
        this.deserializationSchema = deserializationSchema;
    }

    @Override
    public T deserialize(byte[] recordValue, String partitionKey, String seqNum, long approxArrivalTimestamp, String stream, String shardId)
            throws IOException {
        return deserializationSchema.deserialize(recordValue, approxArrivalTimestamp, shardId);
    }
}
