package com.att.dtv.kda.sources;

import java.io.IOException;
import java.util.Arrays;

import com.att.dtv.kda.model.app.TimeProcessable;
import com.att.dtv.kda.serde.FromJsonDeserializer;
import org.apache.flink.api.common.io.DelimitedInputFormat;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;

public class GenericFileInputFormat<OUT extends TimeProcessable> extends DelimitedInputFormat<OUT> {
    private static final long serialVersionUID = -3707783238357873246L;
    private static final byte CARRIAGE_RETURN = 13;
    private static final byte NEW_LINE = 10;
    private FromJsonDeserializer<OUT> deserializer;
    private Path filePath;

    public GenericFileInputFormat(Path filePath, FromJsonDeserializer<OUT> deserializer) {
        super(filePath, (Configuration)null);
        this.deserializer = deserializer;
        this.filePath = filePath;
    }

    public OUT readRecord(OUT reusable, byte[] bytes, int offset, int numBytes) throws IOException {
        if (this.getDelimiter() != null && this.getDelimiter().length == 1 && this.getDelimiter()[0] == NEW_LINE && offset + numBytes >= 1 && bytes[offset + numBytes - 1] == CARRIAGE_RETURN) {
            --numBytes;
        }

        return deserializer.deserialize(Arrays.copyOfRange(bytes, offset, offset + numBytes), filePath.getName());
    }

    public boolean supportsMultiPaths() {
        return true;
    }
}

