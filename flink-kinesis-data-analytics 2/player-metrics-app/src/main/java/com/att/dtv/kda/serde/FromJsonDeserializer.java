package com.att.dtv.kda.serde;


import com.att.dtv.kda.model.app.TimeProcessable;
import com.google.gson.*;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FromJsonDeserializer<T extends TimeProcessable> extends AbstractDeserializationSchema<T> {

    private final Class<T> type;
    private static Logger LOG = LoggerFactory.getLogger(FromJsonDeserializer.class);
    private final static GsonBuilder builder = new GsonBuilder();
    private static DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final static Gson gson = builder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").registerTypeAdapter(Timestamp.class, new JsonDeserializer() {
        @Override
        public Timestamp deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Timestamp.valueOf(LocalDateTime.parse(jsonElement.getAsString(), sdf));
        }
    }).create();

    private String charsetName;
    private Integer heartbeatValidityOffset;

    public FromJsonDeserializer(Class<T> type) {
        this(type, "UTF-8");
    }

    public FromJsonDeserializer(Class<T> type, String charsetName) {
        super(type);
        this.charsetName = charsetName;
        this.type = type;
    }

    @Override
    public T deserialize(byte[] message) throws IOException {
        return convert(new String(message, charsetName), System.currentTimeMillis(), "fakeId");
    }

    public T deserialize(byte[] message, String partitionId) throws IOException {
        return convert(new String(message, charsetName), System.currentTimeMillis(), partitionId);
    }

    public T deserialize(byte[] message, long timestamp, String partitionId) throws IOException {
        return convert(new String(message, charsetName), timestamp, partitionId);
    }

    private T convert(String message, long timestamp, String partitionId) throws IOException {
        try {
            T obj = gson.fromJson(message, this.type);
            obj.setEventTime(new Timestamp(timestamp));
            obj.setPartitionId(partitionId);
            return obj;            
        } catch (Exception e) {
            LOG.error("Bad sub json data format: {}", message, e);
        }

        return null;
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeInformation.of(type);
    }
}
