package com.att.dtv.kda.videoapp;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.att.dtv.kda.factory.SupportedProtocol;
import com.att.dtv.kda.model.app.VideoPlayerStatsProperties;
import com.att.dtv.kda.model.es.SessionStatsModel;
import com.att.dtv.kda.testutils.GsonDateDeserializer;
import com.att.dtv.kda.testutils.TestFileInputFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.DataSetUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VideoPlayerStatsAppTest {

    final static GsonBuilder builder = new GsonBuilder();
    final static Gson gson = builder.registerTypeAdapter(Date.class, new GsonDateDeserializer()).create();
    static Map<String, VideoPlayerStatsProperties> appPropGroups = new HashMap<>();
    static Map<String, SupportedProtocol> testResultEndpoints = new HashMap<>();
    final static ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();


    @BeforeAll
    static void init() throws Exception {
        env.setParallelism(1);
        Map<String, Properties> propGroups = KinesisAnalyticsRuntime.getApplicationProperties(VideoPlayerStatsAppTest.class.getClassLoader().getResource("conf/app_properties.json").getPath());
        for (String group: propGroups.keySet()) {
            VideoPlayerStatsProperties props = new VideoPlayerStatsProperties(propGroups.get(group));
            SupportedProtocol protocol = new SupportedProtocol(props.getIntervalStatsEndpoint());
            appPropGroups.putIfAbsent(group, props);
            testResultEndpoints.putIfAbsent(group, protocol);
        }
    }


    @Test @DisplayName("playDuration")
    void playDuration(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        List<Long> out = result.map((x) -> x.getPlayDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{36000l, 60000l, 20000l}), out);
    }

    @Test @DisplayName("playDurationWithInterruption")
    void playDurationWithInterruption(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();

        VideoPlayerStatsApp.main(new String[] {"-g", testName});
    }

    @Test @DisplayName("no_br")
    void noBr(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        List<Double> noBr = result.map(x-> x.getAvgBitrate()).collect();
        assertEquals(Arrays.asList(0.0,0.0), noBr);
    }

    @Test @DisplayName("bitrate")
    void bitrate(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        List<Double> avgBitRate =result.map(x->x.getAvgBitrate()).collect();
        assertEquals(Arrays.asList(2651.0), avgBitRate);
    }
    @Test @DisplayName("framerate")
    void framerate(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        List<Double> avgFrameRate = result.map(x->x.getAverageFrameRate()).collect();
        assertEquals(Arrays.asList(31.0), avgFrameRate);
    }

    @Test @DisplayName("error")
    void errorReport(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        assertEquals(Integer.valueOf(2), result.collect().get(0).getFatalErrorCount());
        assertEquals(Integer.valueOf(1), result.collect().get(0).getNonFatalErrorCount());
    }

    @Test @DisplayName("ebvs_vsf")
    void vsfEBVSReport(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        assertEquals(null, result.collect().get(0).getEndBeforeVideoStart());
        assertEquals((byte)1,  (byte)result.collect().get(0).getVideoStartFailure());
    }

    @Test @DisplayName("vpf")
    void vpfReport(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        assertEquals((byte) 1, (byte)result.collect().get(1).getVideoPlayFailure());
    }


    @Test @DisplayName("vst")
    void vst(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        assertEquals( 1675, (long)result.collect().get(0).getVideoStartupTime());
    }

    @Test @DisplayName("vrt")
    void vrt(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getVideoRestartTimeTotal(), Order.DESCENDING).first(1);
        List<Long> vrts = result.map((x) -> x.getVideoRestartTimeTotal() == null ? 0 : x.getVideoRestartTimeTotal()).collect();
        Long vrt = vrts.get(0);
        assertEquals( 41329, (long)vrt);
    }

    @Test @DisplayName("ebvs")
    void ebvs(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        assertEquals( 1, (long)result.collect().get(0).getEndBeforeVideoStart());
    }

    @Test @DisplayName("eop")
    void eop(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        assertEquals( null, result.collect().get(0).getEndedPlay());
    }

    @Test @DisplayName("attempt")
    void attempt(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        assertEquals( 1, (int)result.collect().get(0).getAttempt());
    }

    @Test @DisplayName("perf") @Disabled
    void perf(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
    }

    @Test @DisplayName("kinesis") @Disabled
    void kinesis(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
    }

    @Test @DisplayName("cdn-pod")
    void cdn_pod(TestInfo testInfo) throws Exception{
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
    }

    @Test @DisplayName("rebuffRationMultipleSeekInBufferingState")
    void rebufferingRationCaseOne(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        List<Long> out = result.map((x) -> x.getPlayDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{16500l}), out);
        List<Long> cird = result.map((x) -> x.getCiRebufferingDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{400l}), cird);
        List<Long> rd = result.map((x) -> x.getRebufferingDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{2500l}), rd);
    }

    @Test @DisplayName("SeekInducedRebuffRationMultipleSeekInBufferingState")
    void rebufferingRationCaseTwo(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        List<Long> out = result.map((x) -> x.getPlayDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{12617l, 17417l}), out);
        List<Long> cird = result.map((x) -> x.getCiRebufferingDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{0l, 0l}), cird);
        List<Long> rd = result.map((x) -> x.getRebufferingDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{1082l, 0l}), rd);
    }

    @Test @DisplayName("NoBufferingState")
    void rebufferingRationCaseThree(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        List<Long> out = result.map((x) -> x.getPlayDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{16500l}), out);
        List<Long> cird = result.map((x) -> x.getCiRebufferingDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{0l}), cird);
        List<Long> rd = result.map((x) -> x.getRebufferingDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{0l}), rd);
    }

    @Test @DisplayName("NoBufferCompleteState")
    void rebufferingRationCaseFour(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();
        VideoPlayerStatsApp.main(new String[] {"-g", testName});
        DataSet<SessionStatsModel> result = getResultDataSet(testName);
        result = result.groupBy((x) -> x.get_id()).sortGroup((x) -> x.getProcessingTimestamp(), Order.DESCENDING).first(1);
        List<Long> out = result.map((x) -> x.getPlayDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{16500l}), out);
        List<Long> cird = result.map((x) -> x.getCiRebufferingDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{400l}), cird);
        List<Long> rd = result.map((x) -> x.getRebufferingDuration()).collect();
        assertEquals(Arrays.asList(new Long[]{2500l}), rd);
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
