package com.att.dtv.kda.videoapp;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.att.dtv.kda.factory.KdsEsConfig;
import com.att.dtv.kda.factory.SupportedProtocol;
import com.att.dtv.kda.model.app.ApplicationProperties;
import com.att.dtv.kda.model.es.SessionStatsModel;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.DataSetUtils;
import org.junit.jupiter.api.*;
import testutils.TestFileInputFormat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VideoMetricsESSinkAppTest {

    private final static GsonBuilder builder = new GsonBuilder();
    private static DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final static Gson gson = builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Timestamp.class, new JsonDeserializer() {
        @Override
        public Timestamp deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Timestamp.valueOf(LocalDateTime.parse(jsonElement.getAsString(), sdf));
        }
    }).create();
    static Map<String, ApplicationProperties> appPropGroups = new HashMap<>();
    static Map<String, SupportedProtocol> testResultEndpoints = new HashMap<>();
    final static ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();


    @BeforeAll
    static void init() throws Exception {
        env.setParallelism(1);
        Map<String, Properties> propGroups = KinesisAnalyticsRuntime.getApplicationProperties(VideoMetricsESSinkAppTest.class.getClassLoader().getResource("conf/app_properties.json").getPath());
        for (String group: propGroups.keySet()) {
            ApplicationProperties props = new KdsEsConfig(propGroups.get(group));
            SupportedProtocol protocol = new SupportedProtocol(props.getIntervalStatsEndpoint());
            appPropGroups.putIfAbsent(group, props);
            testResultEndpoints.putIfAbsent(group, protocol);
        }
    }


    @Test @DisplayName("localw")
    void localRW(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoMetricsESSinkApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        List<String> out = result.map((x) -> x.getStreamID()).collect();
        assertEquals("c86e5531fcc33a6def9bf710061ad1be4891318d4feef2115698770a329e6198", out.get(0));
    }


    private DataSet<SessionStatsModel> getResultDataSet(String groupId) {
        final String outputDir = testResultEndpoints.get(groupId).getEndpoint().substring(5);
        DataSet<SessionStatsModel> result = env.readFile(new TestFileInputFormat(), outputDir)
                .map((line) -> "{" + line.split("\\d,\\{|\\},\\d")[1] + "}")
                .map((json) -> gson.fromJson(json, SessionStatsModel.class));
        result = DataSetUtils.zipWithIndex(result)
                .map((t) -> {
                    SessionStatsModel ssm = t.f1;
                    Timestamp tmsp = ssm.getProcessingTimestamp();
                    tmsp.setTime(tmsp.getTime() + t.f0);
                    ssm.setProcessingTimestamp(tmsp);
                    return ssm;
                });

        return result;
    }

    @BeforeEach
    public void cleanPreviousTestResults(TestInfo info) throws IOException {
        String TestName = info.getDisplayName();
        if (!testResultEndpoints.get(TestName).getEndpoint().equals("dummy")) {
            String outputDir = testResultEndpoints.get(TestName).getEndpoint().substring(5);
            File dir = new File(outputDir);
            if (dir.exists())
                FileUtils.cleanDirectory(dir);
        }
    }

}
