package com.att.dtv.kda.converters;

import com.att.dtv.kda.model.app.ControlCommand;
import com.att.dtv.kda.serde.FromJsonDeserializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class GsonTest {
    @Test
    void test() throws IOException {
        String json = "{\"code\":1, \"arg\":\"s3://some/path\"}";
        FromJsonDeserializer fromJsonDeserializer = new FromJsonDeserializer(ControlCommand.class);
        ControlCommand command = (ControlCommand) fromJsonDeserializer.deserialize(json.getBytes());
    }
}
