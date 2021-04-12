package com.att.dtv.kda.converters;

import com.att.dtv.kda.model.app.Heartbeat;
import com.att.dtv.kda.model.es.SessionStatsModel;
import com.att.dtv.kda.model.app.SessionStatsAggr;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.elasticsearch.cluster.routing.Murmur3HashFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class SessionStatsToESModelConverter {
    private static Logger LOG = LoggerFactory.getLogger(SessionStatsToESModelConverter.class);
    private static final String INDEX_PERIOD_HOURLY="hourly";
    private static final String INDEX_PERIOD_DAILY="daily";
    private static DateTimeFormatter sdfdaily = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static DateTimeFormatter sdfhourly = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH");


    private static final ThreadLocal<DecimalFormat> decimalFormat =
            ThreadLocal.withInitial(() -> new DecimalFormat("#####.##"));

    public static SessionStatsModel convert(SessionStatsAggr sessionStats) {
        if (sessionStats == null) return null;

        SessionStatsModel statsModel = new SessionStatsModel();
        statsModel.set_id(buildId(sessionStats));
        statsModel.setIntervalLength(sessionStats.getInterval()!=null? sessionStats.getInterval().getLength():1);
        statsModel.setIntervalStartTS(sessionStats.getInterval()!=null?sessionStats.getInterval().getStartTS():Long.MIN_VALUE);
        populateHeartbeatAttributes(statsModel, sessionStats.getMetaHeartbeat());
        populateMetricsAttributes(statsModel, sessionStats);

        return statsModel;
    }

    private static void populateMetricsAttributes(SessionStatsModel statsModel, SessionStatsAggr sessionStats) {
        statsModel.setSequence_start(sessionStats.getSequenceStart());
        statsModel.setSequence_end(sessionStats.getSequenceEnd());
        statsModel.setTimestamp(new Timestamp(sessionStats.getInterval().getStartTS()));
        statsModel.setPlayDuration(sessionStats.getPlayDuration());
        statsModel.setRebufferingDuration(sessionStats.getRebuferringDuration());
        statsModel.setRebufferingRatio(sessionStats.getRebuferringRatio());
        statsModel.setCiRebufferingDuration(sessionStats.getCiRebuferringDuration());
        statsModel.setCiRebufferingRatio(sessionStats.getCiRebuferringRatio());
        statsModel.setCountryISOCode(sessionStats.getCountryISOCode());
        statsModel.setSubdivisionISOCode(sessionStats.getSubdivisionISOCode());
        statsModel.setCity(sessionStats.getCity());
        statsModel.setPostalCode(sessionStats.getPostalCode());
        statsModel.setAsn(sessionStats.getAsn());
        statsModel.setAso(sessionStats.getAso());
        if (sessionStats.getFramerateStats() != null) {
            statsModel.setAverageFrameRate(sessionStats.getFramerateStats().getValue());
        }
        statsModel.setEndOfSession(sessionStats.getEndOfSessionTS());
        //set statistics
        if (sessionStats.getBitrateStats() != null) {
            statsModel.setAvgBitrate(sessionStats.getBitrateStats().getValue());
            statsModel.setBits((long)sessionStats.getBitrateStats().getValue()*(sessionStats.getPlayDuration()==null?0:sessionStats.getPlayDuration()));
        }
        if (sessionStats.getVideoStartupTime() != null && sessionStats.getVideoStartupTime() > 0)
            statsModel.setVideoStartupTime(sessionStats.getVideoStartupTime() <= 600000 ? sessionStats.getVideoStartupTime() : -3);

        statsModel.setEndedPlay(sessionStats.getEndedPlay());
        statsModel.setEndBeforeVideoStart(sessionStats.getEndBeforeVideoStart());
        statsModel.setVideoRestartTime(sessionStats.getVideoRestartTime());
        statsModel.setVideoRestartTimeTotal(sessionStats.getVideoRestartTimeTotal() == null ? 0 : sessionStats.getVideoRestartTimeTotal());
        statsModel.setVideoRestartCount(sessionStats.getVideoRestartCount() == null ? 0 : sessionStats.getVideoRestartCount());
        statsModel.setVideoStartFailure(sessionStats.getVideoStartFailure());
        statsModel.setVideoPlayFailure(sessionStats.getVideoPlayFailures());
        statsModel.setProcessingTimestamp(new Timestamp(System.currentTimeMillis()));
        statsModel.setClientErrorCounts(sessionStats.getErrorCounts());
        statsModel.setVideoStartFailure(sessionStats.getVideoStartFailure());
        statsModel.setCdn(sessionStats.getCdn() == null ? "N/A" : sessionStats.getCdn());
        statsModel.setPod(sessionStats.getPod() == null ? "N/A" : sessionStats.getPod());
        statsModel.setExpired(sessionStats.getExpired());
        statsModel.setAttempt(sessionStats.getAttempt());
        statsModel.setFatalErrorCount(sessionStats.getFatalErrorCount());
        statsModel.setNonFatalErrorCount(sessionStats.getNonFatalErrorCount());
        if(sessionStats.getHostName() != null){
            statsModel.setHostName(sessionStats.getHostName());
        }
        statsModel.setKdaAppVersion(sessionStats.getKdaAppVersion());

        if(sessionStats.getIsp() != null){
            statsModel.setIsp(sessionStats.getIsp());
        }
        if(sessionStats.getPlayDurationTotal() != null && sessionStats.getPlayDurationTotal() > 0){
            statsModel.setPlayDurationTotal(sessionStats.getPlayDurationTotal());
        }

        statsModel.setDmaCode(sessionStats.getDmaCode() == null ? null : sessionStats.getDmaCode());
        statsModel.setDma(sessionStats.getDma() == null ? null : sessionStats.getDma());
    }

    private static void populateHeartbeatAttributes(SessionStatsModel statsModel, Heartbeat metaHeartbeat) {
        statsModel.setSessionId(metaHeartbeat.getSessionId());
        statsModel.setClientId(metaHeartbeat.getClientId());
        statsModel.setViewerId(metaHeartbeat.getViewerId());
        statsModel.setSessionStartTime((long) metaHeartbeat.getSessionStartTime());
        statsModel.setAssetName(metaHeartbeat.getAssetName());
        statsModel.setConnectionType(metaHeartbeat.getConnectionType());
        statsModel.setPlatformType(metaHeartbeat.getPlatformType());
        statsModel.setLive(metaHeartbeat.isLive());
        statsModel.setIpAddress(metaHeartbeat.getIpAddress());
        statsModel.setLongitude(decimalFormat.get().format(metaHeartbeat.getLongitude()));
        statsModel.setLatitude(decimalFormat.get().format(metaHeartbeat.getLatitude()));
        statsModel.setStreamType(metaHeartbeat.getStreamType());
        statsModel.setStreamID(metaHeartbeat.getStreamID());
        statsModel.setDeviceID(metaHeartbeat.getDeviceID());
        statsModel.setDeviceIDMurMur3(metaHeartbeat.getDeviceID() == null? null: Murmur3HashFunction.hash(metaHeartbeat.getDeviceID()));
        statsModel.setIsDai(metaHeartbeat.isDai());
        statsModel.setPtv(metaHeartbeat.getPtv());
        statsModel.setDaiType(metaHeartbeat.getDaiType());
        statsModel.setDeviceOS(metaHeartbeat.getDeviceOS());
        statsModel.setDeviceOSVersion(metaHeartbeat.getDeviceOSVersion());
        statsModel.setDeviceName(metaHeartbeat.getDeviceName());
        statsModel.setDeviceType(metaHeartbeat.getDeviceType());
        statsModel.setDeviceModel(metaHeartbeat.getDeviceModel());
        statsModel.setDeviceManufacturer(metaHeartbeat.getDeviceManufacturer());
        statsModel.setDeviceLocale(metaHeartbeat.getDeviceLocale());
        statsModel.setRestartEligible(metaHeartbeat.isRestartEligible());
        statsModel.setAppSessionID(metaHeartbeat.getAppSessionID());
        statsModel.setDeviceAdID(metaHeartbeat.getDeviceAdID());
        statsModel.setLaunchType(metaHeartbeat.getLaunchType());
        statsModel.setCustomerType(metaHeartbeat.getCustomerType());
        statsModel.setPlayerSettings(metaHeartbeat.getPlayerSettings());
        statsModel.setBitrateCapping(metaHeartbeat.isBitrateCapping());
        statsModel.setLiveAuthzCache(metaHeartbeat.isLiveAuthzCache());
        statsModel.setTargetGroup(metaHeartbeat.getTargetGroup());
        statsModel.setSerialNumber(metaHeartbeat.getSerialNumber());
        statsModel.setContentLength(metaHeartbeat.getContentLength());
        statsModel.setLiveAuthzCache(metaHeartbeat.isLiveAuthzCache());
    }


    final static GsonBuilder builder = new GsonBuilder();
    final static Gson gson = builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

    public static String convertJson(SessionStatsAggr sessionStats) {
        if (sessionStats == null) return null;
        SessionStatsModel statsModel = null;
        try {
            statsModel = convert(sessionStats);
        } catch (Exception e) {
            LOG.error("Failed to convert session stats: {}", sessionStats, e);
            LOG.error("Stack trace: {}", (Object)e.getStackTrace());
            statsModel = createDummyStatsModel(sessionStats.getMetaHeartbeat(), e);
        }
        String sessionStatsJson = gson.toJson(statsModel);
        return sessionStatsJson;
    }

    private static SessionStatsModel createDummyStatsModel(Heartbeat metaHeartbeat, Exception e) {
        SessionStatsModel sessionStatsModel = new SessionStatsModel();
        sessionStatsModel.set_id(buildId(metaHeartbeat.getSessionId(), metaHeartbeat.getViewerId(), System.currentTimeMillis()));
        populateHeartbeatAttributes(sessionStatsModel, metaHeartbeat);
        try(StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)){
            e.printStackTrace(pw);
            sessionStatsModel.setRuntimeError(sw.toString());
        } catch (IOException ex) {
            LOG.error("Error in StringWriter {}", (Object)ex.getStackTrace());
        }
        return sessionStatsModel;
    }

    private static String buildId(long sessionId, String viewerId, long intervalStartTime) {
        return sessionId+"@"+viewerId+"@"+intervalStartTime;
    }

    public static String buildIndexSuffix(long intervalStartTS, String indexPeriod){
        LocalDateTime date = LocalDateTime.ofEpochSecond(intervalStartTS/1000,0, ZoneOffset.UTC);
        return INDEX_PERIOD_HOURLY.equalsIgnoreCase(indexPeriod)?sdfhourly.format(date):sdfdaily.format(date);
    }

    public static String buildId(SessionStatsAggr sessionStats) {
        if (sessionStats == null || sessionStats.getMetaHeartbeat() == null) return null;
        return buildId(sessionStats.getMetaHeartbeat().getSessionId(), sessionStats.getMetaHeartbeat().getViewerId(), sessionStats.getInterval().getStartTS());
    }

    public static String buildSessionSuffix(SessionStatsAggr sessionStats, String indexPeriod) {
        if (sessionStats == null || sessionStats.getMetaHeartbeat() == null) return null;
        return buildIndexSuffix(sessionStats.getInterval().getStartTS(), indexPeriod);
    }
    public static String buildSessionSuffix(SessionStatsAggr sessionStats) {
        return buildSessionSuffix(sessionStats, INDEX_PERIOD_DAILY);
    }
}
