package com.att.dtv.kda.flink;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.zip.GZIPOutputStream;

import com.att.dtv.kda.model.app.Heartbeat;
import com.google.gson.*;
import org.apache.flink.api.common.io.FileOutputFormat;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Generalize it
public class GZipHeartbeatOutputFormat extends FileOutputFormat<Heartbeat> {
    private static final long serialVersionUID = -3524505776138448716L;

    private static Logger LOG = LoggerFactory.getLogger(GZipHeartbeatOutputFormat.class);

    final static GsonBuilder builder = new GsonBuilder().disableHtmlEscaping()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .registerTypeAdapter(Double.class,  new JsonSerializer<Double>() {
        @Override
        public JsonElement serialize(final Double src, final Type typeOfSrc, final JsonSerializationContext context) {
            BigDecimal value = BigDecimal.valueOf(src);

            return new JsonPrimitive(value);
        }
    });

    final static Gson gson = builder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

    String json;

    GZIPOutputStream gzipOutputStream;

    @Override
    public void configure(Configuration parameters) {
        super.configure(parameters);
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
        super.open(taskNumber, numTasks);
        gzipOutputStream = new GZIPOutputStream(this.stream);
    }

    @Override
    protected String getDirectoryFileName(int taskNumber) {
        String filename = super.getDirectoryFileName(taskNumber);
        return filename + ".gz";

    }

    @Override
    public void writeRecord(Heartbeat record) throws IOException {
        try {
            json = gson.toJson(record);
        } catch (Exception e) {
            LOG.error("Serialization error: " + json, e);
        }
        gzipOutputStream.write(json.getBytes());
        gzipOutputStream.write('\n');
    }

    @Override
    public void close() throws IOException {
        gzipOutputStream.flush();
        gzipOutputStream.finish();
        super.close();
    }
}