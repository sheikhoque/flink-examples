package com.att.dtv.kda.process;

import com.att.dtv.kda.model.app.*;
import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class HeartbeatProcessFunction extends KeyedProcessFunction<Tuple3<String, Long, String>, Heartbeat, SessionStatsAggr> {
    private static final long serialVersionUID = -6367581548520475656L;

    public static final int SESSION_EXPIRE_TIMEOUT = 4 * 60 * 1000; //4m

    private static Logger LOG = LoggerFactory.getLogger(HeartbeatProcessFunction.class);

    private transient ValueState<SessionStatsAggr> latestStatsStateShortInterval;
    private transient ValueState<SessionStatsAggr> latestStatsStateLongerInterval;

    private transient ValueState<SessionTimeline> sessionTimelineState;

    private final ValueStateDescriptor<SessionStatsAggr> sessionAggStateDescShortInterval;
    private final ValueStateDescriptor<SessionStatsAggr> sessionAggStateDescLongerInterval;

    private final ValueStateDescriptor<SessionTimeline> sessionTimelineStateDesc;

    private final List<Integer> intervalLengths;

    VideoPlayerStatsProperties videoPlayerStatsProperties;

    final IntCounter newSessions = new IntCounter();
    final IntCounter expiredSessions = new IntCounter();

    public HeartbeatProcessFunction(VideoPlayerStatsProperties videoPlayerStatsProperties) {
        this.intervalLengths = videoPlayerStatsProperties.getIntervalLengths();
        this.videoPlayerStatsProperties = videoPlayerStatsProperties;
        sessionAggStateDescShortInterval = new ValueStateDescriptor<>("short_interval_m_session-stats",
                SessionStatsAggr.class);
        sessionAggStateDescLongerInterval = new ValueStateDescriptor<>("long_interval_m_session-stats",
                SessionStatsAggr.class);
        sessionTimelineStateDesc = new ValueStateDescriptor<>(intervalLengths.get(0) + "m_timeline",
                SessionTimeline.class);
    }

    public void open(Configuration cfg) throws Exception {
        latestStatsStateShortInterval = getRuntimeContext().getState(sessionAggStateDescShortInterval);
        latestStatsStateLongerInterval = getRuntimeContext().getState(sessionAggStateDescLongerInterval);
        sessionTimelineState = getRuntimeContext().getState(sessionTimelineStateDesc);
        this.getRuntimeContext().addAccumulator("new-sessions", newSessions);
        this.getRuntimeContext().addAccumulator("expired-sessions", expiredSessions);
    }


    private long counter = 0;

    @Override
    public void processElement(Heartbeat hb, Context ctx, Collector<SessionStatsAggr> out) throws Exception{

    // it checks for a) null heartbeat, b) long running session && c_time travellers
        if(!validSession(hb)){
            return ;
        }

        SessionStatsAggr latestStatsShortInterval = null;
        SessionStatsAggr latestStatsLongerInterval = null;
        SessionTimeline sessionTimeline = null;
        Tuple3<String, Long, String> key = ctx.getCurrentKey();
        try {
            latestStatsShortInterval = latestStatsStateShortInterval.value();
            latestStatsLongerInterval = latestStatsStateLongerInterval.value();

            LOG.debug("Found latest latestStatsShortInterval: {}", latestStatsShortInterval);
            LOG.debug("Found latest latestStatsLongerInterval: {}", latestStatsLongerInterval);

            sessionTimeline = sessionTimelineState.value();
            LOG.debug("Found latest sessionTimeline: {}", sessionTimeline);
            if (sessionTimeline == null) {
                if (hb.getSequence() > 0) {
                    LOG.debug("Skipping records. There is no state for session: {} hb_sequence: {}", hb.getSessionId(), hb.getSequence());
                    return;
                }
                sessionTimeline = new SessionTimeline(hb.getSessionId());
                this.newSessions.add(1);
            }
        } catch (IOException e) {
            LOG.warn("Failed to get state for {}", hb, e);
            sessionTimeline = new SessionTimeline(hb.getSessionId());
        }

        try {
            List<HeartbeatEvent> events;
            counter++;

            LOG.debug("Processing HB seq: {} with SID: {}", hb.getSequence(), hb.getSessionId());

            // check with client team if sorting can be removed (for performance).
            events = hb.getEvents();
            if (events != null) {
                events.sort(Comparator.comparing(HeartbeatEvent::getSequence));
            }

            // -------------------------------
            // update timeline state
            HeartbeatTimelineProcessor.updateTimelineAllKeys(hb, sessionTimeline);
            ValueState<SessionStatsAggr> currentLatestState = null;
            for (int intervalLength : intervalLengths) {
                currentLatestState = intervalLength == 1 ? latestStatsStateShortInterval : latestStatsStateLongerInterval;
                TreeMap<Long, SessionStatsAggr> sessionStatsMap = new TreeMap<>();
                // add timeline changes to session stats
                HeartbeatTimelineProcessor.updateMetrics(sessionStatsMap, sessionTimeline, hb, intervalLength);
                if (sessionStatsMap.isEmpty()) {
                    return;
                }
//                SessionMetadataUtils.updateMetadata(hb, sessionStatsMap, intervalLength);
                for (SessionStatsAggr sessionStats : sessionStatsMap.values()) {
                    sessionStats.setMetaHeartbeat(hb);
//                    LOG.debug("Sending intervals for " +
//                            (sessionStats.getMetaHeartbeat() == null ? "null" : sessionStats.getMetaHeartbeat().getSessionId()) +
//                            " with start seq: " + sessionStats.getSequenceStart() +
//                            " and end seq: " + sessionStats.getSequenceEnd()+" and interval: "+sessionStats.getInterval().getLength());
                    out.collect(sessionStats);
                }


                // update latestState
                //we should update latestState only if this is the first heartbeat of the app or the current heartbeat is not out of order
                // i.e. current heartbeat tsMillis() should be greater than previous stored hb.
                // if current hb is out of order, we don't update the latest state
                try {
                    Heartbeat prevStatesHB = currentLatestState != null && currentLatestState.value() != null ? currentLatestState.value().getMetaHeartbeat() : null;
                    if (!sessionStatsMap.isEmpty() && (prevStatesHB == null || prevStatesHB.tsMillis() < hb.tsMillis())) {
                        currentLatestState.update(sessionStatsMap.lastEntry().getValue());
                    }

                } catch (IOException e) {
                    LOG.error("Couldn't update session agg state for key: {} Exception: {}", key.toString(), e);
                    throw new IllegalStateException("Couldn't update session agg state for key: " + key.toString(), e);
                }
            }


            LOG.debug("Processed heartbeats: {}", counter);

            // update timeline state
            try {
                if (!sessionTimeline.isEmpty()) {
                    long ts = hb.getEventTimestamp().getTime();

//                  un-schedule previous timer
                    if (sessionTimeline.getLastModified() != 0 && roundToInterval(ts) != roundToInterval(sessionTimeline.getLastModified())) {
                        ctx.timerService().deleteEventTimeTimer(roundToInterval(sessionTimeline.getLastModified() + SESSION_EXPIRE_TIMEOUT));
                        LOG.debug("Un-schedule timer for {}. Current timestamp is {}, LastModified is {} + was scheduled on {}",
                                sessionTimeline.getSessionId(), ts, sessionTimeline.getLastModified(), roundToInterval(sessionTimeline.getLastModified() + SESSION_EXPIRE_TIMEOUT));
                    }
                    sessionTimeline.setLastModified(ts);
                    sessionTimelineState.update(sessionTimeline);
                }
            } catch (IOException e) {
                LOG.error("Couldn't update session timeline state for key: {} Exception: {}", key.toString(), e);
                throw new IOException("Couldn't update session timeline state for key: " + key.toString(), e);
            }

            // let's round it to a whole minute to aggregate
            ctx.timerService().registerEventTimeTimer(roundToInterval(sessionTimeline.getLastModified() + SESSION_EXPIRE_TIMEOUT));
        } catch (Exception e) {
            LOG.error("Fatal error during processing of: {}", hb.toString(), e);
            LOG.error("Stack trace: {}", e.getStackTrace());
        }

    }

    /**
     * validates the session,  checks 1) if hb is null 2) long running session 3)time traveller from past if it is configured
     * @param hb
     * @return
     */
    private boolean validSession(Heartbeat hb) {
        if(hb==null)
            return false;
        if(forceKillSession(hb)){
            LOG.debug(" LongRunningSessionWarn: This Stream/sid {}/{} is running for {} millisecond, configured threshold is for {} millis.",
                    hb.getStreamID(), hb.getSessionId(), hb.getStartTime(), videoPlayerStatsProperties.getSessionLifetime());
            return false;
        }

        if(videoPlayerStatsProperties.getTimeTravellersTolerancePeriod()>0 && (hb.tsMillis() < (System.currentTimeMillis()-videoPlayerStatsProperties.getTimeTravellersTolerancePeriod()))){
            return false;
        }

        return true;

    }


    /**
     * This onTimer is for expiration functionality.
     * Once onTimer is called, we need to delete all the states we store so far, and submit new aggregates indicating end of session by expiration.
     */
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx,
                        Collector<SessionStatsAggr> out) throws IOException {
        SessionTimeline sessionTimeline = sessionTimelineState.value();

        if (sessionTimeline == null) {
            LOG.debug("Session {} is already closed gracefully or expired. Just skipping the timer. Timestamp: {}", ctx.getCurrentKey(), timestamp);
            return;
        }

        if (timestamp < roundToInterval(sessionTimeline.getLastModified() + SESSION_EXPIRE_TIMEOUT)) {
            LOG.debug("Skipping current onTimer for {}, as it is not current. Current timestamp is {} and is less than scheduled {}",
                    sessionTimeline.getSessionId(), timestamp, roundToInterval(sessionTimeline.getLastModified() + SESSION_EXPIRE_TIMEOUT));
            return;
        }

        LOG.debug("Expiring the session key: {}, Current timestamp: {}, scheduled timestamp {}", ctx.getCurrentKey(), timestamp,
                roundToInterval(sessionTimeline.getLastModified() + SESSION_EXPIRE_TIMEOUT));

        try {
            boolean isGracefulEndOfSession = HeartbeatTimelineProcessor.isGracefulEndOfSession(sessionTimeline);

            LOG.debug("Session {} ended gracefully: {}", sessionTimeline.getSessionId(), isGracefulEndOfSession);

            Tuple2<Boolean, Long> hasFatalError = HeartbeatTimelineProcessor.checkFatalErros(sessionTimeline.getChangesTimelines().get(EventField.CLIENT_ERROR));
            boolean hasVideoPlayState = HeartbeatTimelineProcessor.isSessionPlayingVideo(sessionTimeline);
            SessionStatsAggr currentInterval = null;
            if (!isGracefulEndOfSession) {
                for (int interval : intervalLengths) {
                    currentInterval = interval == 1 ? latestStatsStateShortInterval.value() : latestStatsStateLongerInterval.value();
                    currentInterval.setExpired(forceKillSession(currentInterval.getMetaHeartbeat())?(byte)2:(byte) 1);
                    currentInterval.setEndOfSessionTS(timestamp);
                    HeartbeatTimelineProcessor.updateBufferInterrupts(sessionTimeline, new SessionStatsAggr[]{currentInterval});
                    HeartbeatTimelineProcessor.updateEOSDependentMetrics(sessionTimeline, new SessionStatsAggr[]{currentInterval}, hasFatalError.f0, !isGracefulEndOfSession, isGracefulEndOfSession, hasVideoPlayState);
                    out.collect(currentInterval);
                }
            }
            SessionStatsAggr sessionReport = generateSessionReport(sessionTimeline, currentInterval == null ? latestStatsStateShortInterval.value() : currentInterval, hasFatalError, isGracefulEndOfSession, hasVideoPlayState, timestamp);
            if (sessionReport != null)
                out.collect(sessionReport);

            clearAllStates();

            this.expiredSessions.add(1);
        } catch (Exception e) {
            LOG.error("Error in onTimer at HeartbeatProcessFunction ", e);
            LOG.error("Stack trace: {}", e.getStackTrace());
        }

    }

    private boolean forceKillSession(Heartbeat hb) {
       return (long)hb.getStartTime() > videoPlayerStatsProperties.getSessionLifetime();
    }

    /*
     * Generates Sessinoo Report (to be used as SSD report).
     */
    private SessionStatsAggr generateSessionReport(SessionTimeline timeline, SessionStatsAggr lastInterval, Tuple2<Boolean, Long> hasFatalError, boolean isGracefulEndOfSession, boolean hasVideoPlayState, long timerTimeStamp) {
        SessionStatsAggr report = new SessionStatsAggr();
        report.setIsReport(true);
        Heartbeat heartbeat = lastInterval.getMetaHeartbeat();
        if (heartbeat == null) {
            LOG.error("SSD Report failure - metaheartbeat is null for last SessionStats agg {)", lastInterval.toString());
            return null;
        }
        report.setMetaHeartbeat(heartbeat);
        Interval rh = new Interval();
        rh.setStartTS((long) heartbeat.getSessionStartTime());
        rh.setEndTS(heartbeat.tsMillis());
        report.setInterval(rh);

        TreeMap<Long, SessionStatsAggr> sessionStatsMap = new TreeMap<>();
        sessionStatsMap.put(0L, report);
        SessionStatsAggr[] statsInterval = new SessionStatsAggr[]{report};


        // end of session:
        NavigableMap<Long, Object> endOfSessionTimeline = null;
        Long eosTs = null;
        if (isGracefulEndOfSession
                && (endOfSessionTimeline = timeline.getChangesTimelines().get(EventField.END_OF_SESSION)) != null
                && endOfSessionTimeline.lastEntry() != null) {
            eosTs = endOfSessionTimeline.lastEntry().getKey();
            report.setEndOfSessionTS(eosTs);
        } else {
            report.setExpired(forceKillSession(heartbeat)?(byte)2:(byte) 1);
            report.setEndOfSessionTS(timerTimeStamp);
        }

//        HeartbeatTimelineProcessor.updateAttempt(sessionStatsMap, timeline, heartbeat, resolutionMinutes, firstIntervalStats, lastIntervalStats);

        HeartbeatTimelineProcessor.updateBufferInterrupts(timeline, statsInterval);

        HeartbeatTimelineProcessor.updateEOSDependentMetrics(timeline, statsInterval, hasFatalError.f0, !isGracefulEndOfSession, isGracefulEndOfSession, hasVideoPlayState);

        //Video Restart Time (VRT):
        HeartbeatTimelineProcessor.updateVRT(timeline, statsInterval);

        // Errors:
        HeartbeatTimelineProcessor.updateErrors(timeline, statsInterval);

        // update play duration, rebuffering, ci-rebuffering
        HeartbeatTimelineProcessor.updatePlayRebuffering(timeline, heartbeat, statsInterval);

        HeartbeatTimelineProcessor.updateVST(timeline, heartbeat, statsInterval);

        HeartbeatTimelineProcessor.updateBitrate(statsInterval, timeline);

        HeartbeatTimelineProcessor.updateAverageFrameRate(statsInterval, timeline);

        return report;
    }

    /*
     * Clearing states
     */
    private void clearAllStates() {
        latestStatsStateShortInterval.clear();
        latestStatsStateLongerInterval.clear();
        sessionTimelineState.clear();
    }

    private long roundToInterval(long x) {
        long r = intervalLengths.size() != 0 ? intervalLengths.get(0) * 60 * 1000 : 1 * 60 * 1000; //ms in Interval
        return (x / r) * r + r;
    }

}
