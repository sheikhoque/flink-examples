package com.att.dtv.kda.model.app;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import software.amazon.kinesis.connectors.flink.serialization.KinesisSerializationSchema;

import java.nio.ByteBuffer;

public class CustomKinesisProducerSerializationSchema implements KinesisSerializationSchema<Tuple4<String, String, String, String>> {

    final SimpleStringSchema schema;
    public CustomKinesisProducerSerializationSchema(){
        schema = new SimpleStringSchema();
    }
    @Override
    public ByteBuffer serialize(Tuple4<String, String, String, String> element) {
        // wrap into ByteBuffer
        return ByteBuffer.wrap(schema.serialize(element.f1));
    }

    @Override
    public String getTargetStream(Tuple4<String, String, String, String> element) {
        return null;
    }
}
