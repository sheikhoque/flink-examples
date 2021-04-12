package com.att.dtv.kda.process;

import com.att.dtv.kda.model.app.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class HeartbeatTimelineProcessor {

    private static final Long DEFAULT_BUFFER_LENGTH = 150_000L;
    private static final Long MAX_SEEK_BUF_MILLIS = 10_000L; //10 seconds
    private static Logger LOG = LoggerFactory.getLogger(HeartbeatTimelineProcessor.class);

    private static List<PlayerState> PLAYABLE_STATES = Arrays.stream(new PlayerState[]{PlayerState.play, PlayerState.ad_break_start, PlayerState.ad_break_end, PlayerState.ad_start, PlayerState.ad_end})
                                                              .collect(Collectors.toList());

    public static void updateTimelineAllKeys(Heartbeat hb, SessionTimeline timeline) {
        for (EventField eventField : EventField.values()) {
            updateTimeline(hb, timeline, eventField);
        }
        updateSequences(timeline, hb);
    }

    public static void updateTimeline(Heartbeat hb, SessionTimeline timeline, EventField eventField) {
        long hbTS = hb.tsMillis();
        if (eventField.hasLevel(EventField.ChangeLevel.EVENT) && hb.getEvents() != null && !hb.getEvents().isEmpty()) {
            for (HeartbeatEvent event : hb.getEvents()) {
                // check end of session event:
                if (eventField == EventField.END_OF_SESSION && "CwsSessionEndEvent".equals(event.getType())) {
                    long eventTS = (long) (event.getEventTimeMillisec() + hb.getSessionStartTime());
                    timeline.addChange(ChangeEvent.of(eventField, eventTS, eventTS));
                }
                long eventTS = (long) (event.getEventTimeMillisec() + hb.getSessionStartTime());
                if (eventField == EventField.CLIENT_ERROR && StringUtils.isNotEmpty(event.getErrCode())) {
                    ClientError clientError = new ClientError(event.getErrCode(), event.isFatal());
                    timeline.addChange(ChangeEvent.of(eventField, clientError, eventTS));
                    continue;
                }
                if (event.getNewValue() == null)
                    continue;
                Object newValue = heartbeatValue(event.getNewValue(), eventField);
                if (newValue != null) {
                    timeline.addChange(ChangeEvent.of(eventField, newValue, eventTS));
                }
            }
        }

        if (eventField.hasLevel(EventField.ChangeLevel.HEARTBEAT)) {
            Object newValue = heartbeatValue(hb, eventField);
            if (newValue != null) {
                timeline.addChange(ChangeEvent.of(eventField, newValue, hbTS));
            }
        }

    }

    /**
     * This method calculates all the metrics. At the arrival of a heartbeat, we store all relevant data points(br, fps etc.)
     * in session time line. In this method, at arrival of a heartbeat, these metrics are aggregated for all the data points
     * for the particular session
     * @param sessionStatsMap a place holder to collect the metrics
     * @param sessionTimeline a state where we store all the metrics value keyed by session (vid, sid)
     * @param heartbeat       heartbeat arrived at point of time
     * @param intervalLength  the length of interval we are considering to store in elastic search
     */
    /**
     *  a Heartbeat can overlap in subsequent intervals, we need to find out the affected intervals.
     *  in Heartbeat, sst+st defines heartbeat packaging time, i.e end of heartbeat.
     *  However we need to know the start time of heartbeat. Strictly speaking, start of heartbeat is the
     *  end of the previous heartbeat.
     * @param currentHeartbeat the heartbeat arrived at this point of time
     * @param intervalLength  length of the interval we will considering
     * @param hbSequenceTL    the session time line of heartbeats that we store
     * @return   affected intervals
     */
    private static SessionStatsAggr[] findAffectedIntervals(Heartbeat currentHeartbeat, int intervalLength, NavigableMap<Long, Object> hbSequenceTL) {
        List<SessionStatsAggr> affectedIntervals = new ArrayList<>();
        // we know hbEndTime = sst+st, but hbStartTime is hbEndTime of previous heartbeat. we look for that in our Session Timeline
        long hbStartTime = findHeartbeatStartTime(hbSequenceTL, currentHeartbeat);
        long hbEndTime = currentHeartbeat.tsMillis();

        //if the time difference between current heartbeat and last hearbeat is more than SESSION_ESXPIRED,
        // we should consider this as stale data and ignore...
        if(!isHeartbeatTimeDiffValid(hbStartTime, hbEndTime)) {
            LOG.warn("IgnoredSession: time difference between current and previous HB is bigger than Session Expire Timeout, HB streamID {}", currentHeartbeat.getStreamID());
            return null;
        }

        Interval currentInterval = Interval.fromTS(hbEndTime, intervalLength);
        affectedIntervals.add(new SessionStatsAggr(currentInterval, currentHeartbeat, currentHeartbeat.getSequence(), currentHeartbeat.getSequence()));

        // if hb start time is within same interval, then there are no more affected intervals and we can leave from here
        //this case also covers if current hearbeat is outof order by the startime (which is previous hb end time) belongs to same interval
        if(currentInterval.contains(hbStartTime)){
            return affectedIntervals.toArray(new SessionStatsAggr[affectedIntervals.size()]);
        }

        // cases below will consider more than one interval as because hb start time and end time does not fall in same interval and overlaps to multiple interval
        //add all intervals between heartbeat End time and Start time
        while(hbStartTime < currentInterval.getStartTS()){
            currentInterval = currentInterval.prev();
            affectedIntervals.add( new SessionStatsAggr(currentInterval, currentHeartbeat, currentHeartbeat.getSequence(), currentHeartbeat.getSequence()));
        }
        return affectedIntervals.toArray(new SessionStatsAggr[affectedIntervals.size()]);
    }

    private static boolean isHeartbeatTimeDiffValid(long hbStartTime, long hbEndTime) {
        return (hbEndTime - hbStartTime) < HeartbeatProcessFunction.SESSION_EXPIRE_TIMEOUT;
    }

    /**
     * In session timeline, we place hbseq (heartbeat sequence) data point for each heartbeat, where key is hbEndTime(sst+st)
     * and value is hbseq. each heartbeat has its hbEndTime(sst+st), however to know the start time of the heartbeat
     * we strictly consider the hbEndTime of previous heartbeat. So, what we do is we check for the key (i.e. hbEndTime of
     * previous heartbeat) in session timeline. if we dont find any previous heartbeat(i.e. null), that means it is the first
     * heartbeat of the session and we consider sst (sessionStartTime) is the hbStartTime. If hb exist, then we consider the
     * key value as the hbStartTime of current heart beat.
     * Note that, this covers the out of order hb as well, as our Session Timeline is a Navigation Map, if a out_of_order
     * heartbeat arrives, it will place it at proper place (based on its key - hbEndtime) in the session timeline
     *
     * @param hbSequenceTL session time line of heartbeat sequence
     * @param currentHeartbeat heartbeat at point of time
     * @return
     */
    private static long findHeartbeatStartTime(NavigableMap<Long, Object> hbSequenceTL, Heartbeat currentHeartbeat) {
        long hbStartTime=(long)currentHeartbeat.getSessionStartTime();
        if(hbSequenceTL==null){
            LOG.error("HBSeqNoTimelineError: Expecting hbsequence in session time line for heartbeat {} ", currentHeartbeat);
        }else{
            Entry<Long, Object> previousHBSeqEntry = hbSequenceTL.lowerEntry(currentHeartbeat.tsMillis());
            while(previousHBSeqEntry!=null){
                int prevSeq = (int)previousHBSeqEntry.getValue();
                // ideally below case should not happen, navigable map should have placed
                // the heartbeat at rightpoint based on its key (hearbeat packaged time - sst+st)
                // still should we keep this? just want to verify client side is publishing
                // heartbeat to kinesis in ordered sequence similar to order of sst+st
                if(currentHeartbeat.getSequence() < prevSeq){
                    LOG.error("PrevSeq Error: Expecting hb sequence to be stored in order in Session Timeline, prevSeq:{}, current_hb {}", prevSeq, currentHeartbeat);
                    previousHBSeqEntry = hbSequenceTL.lowerEntry(previousHBSeqEntry.getKey());
                }else{
                    hbStartTime = previousHBSeqEntry.getKey();
                    break;
                }
            }
        }
        return hbStartTime;
    }

    /**
     * This method calculates all the metrics. At the arrival of a heartbeat, we store all relevant data points(br, fps etc.)
     * in session time line. In this method, at arrival of a heartbeat, these metrics are aggregated for all the data points
     * for the particular session
     * @param sessionStatsMap a place holder to collect the metrics
     * @param sessionTimeline a state where we store all the metrics value keyed by session (vid, sid)
     * @param heartbeat       heartbeat arrived at point of time
     * @param intervalLength  the length of interval we are considering to store in elastic search
     */
    public static void updateMetrics(TreeMap<Long, SessionStatsAggr> sessionStatsMap,
                                     SessionTimeline sessionTimeline,
                                     Heartbeat heartbeat,
                                     int intervalLength) {
        SessionStatsAggr[] affectedStatsIntervals;
        // a hb can overlap in two subsequent intervals. We need to find the affected intervals
       affectedStatsIntervals = findAffectedIntervals(heartbeat, intervalLength, sessionTimeline.getChangesTimelines().get(EventField.HEARTBEAT_SEQUENCE));
       if(affectedStatsIntervals==null)
           return;
       for (SessionStatsAggr statsInterval : affectedStatsIntervals) {
            sessionStatsMap.put(statsInterval.getInterval().getStartTS(), statsInterval);
            // end of session:
            NavigableMap<Long, Object> endOfSessionTimeline = null;
            Long eosTs = null;
            if (sessionTimeline != null && sessionTimeline.getChangesTimelines() != null
                    && (endOfSessionTimeline = sessionTimeline.getChangesTimelines().get(EventField.END_OF_SESSION)) != null
                    && endOfSessionTimeline.lastEntry() != null) {

                eosTs = endOfSessionTimeline.lastEntry().getKey();

                if (eosTs >= statsInterval.getInterval().getStartTS() && eosTs < statsInterval.getInterval().getEndTS()) {
                    statsInterval.setEndOfSessionTS(eosTs);
                }
            }
        }

        Tuple2<Boolean, Long> fatalErrorPair = checkFatalErros(sessionTimeline.getChangesTimelines().get(EventField.CLIENT_ERROR));
        boolean isGracefulEOS = isGracefulEndOfSession(sessionTimeline);
        boolean hasVideoPlayState = isSessionPlayingVideo(sessionTimeline);
        boolean sessionExpired = false; // updateMetrics is called from processElement where we dont look for session expired.

        SessionStatsAggr[] longIntervals = Arrays.stream(affectedStatsIntervals).filter(x -> x.getInterval().getLength() > 1).toArray(SessionStatsAggr[]::new);
        SessionStatsAggr[] shortIntervals = Arrays.stream(affectedStatsIntervals).filter(x -> x.getInterval().getLength() == 1).toArray(SessionStatsAggr[]::new);

        updateAttempt(sessionStatsMap, heartbeat);

        //Video Restart Time (VRT):
        updateVRT(sessionTimeline, affectedStatsIntervals);

        // Errors:
        updateErrors(sessionTimeline, affectedStatsIntervals);

        // update play duration, rebuffering, ci-rebuffering
        updatePlayRebuffering(sessionTimeline, heartbeat, affectedStatsIntervals);

        if(longIntervals != null && longIntervals.length > 0)
            updateTotalPlayDuration(longIntervals, sessionTimeline.getSessionMetadata().getSessionPlayDurationsLongInterval());

        if(shortIntervals != null && shortIntervals.length > 0)
            updateTotalPlayDuration(shortIntervals, sessionTimeline.getSessionMetadata().getSessionPlayDurationsShortInterval());

        updateVST(sessionTimeline, heartbeat, affectedStatsIntervals);

        updateBitrate(affectedStatsIntervals, sessionTimeline);

        updateAverageFrameRate(affectedStatsIntervals, sessionTimeline);

        updateEOSDependentMetrics(sessionTimeline, affectedStatsIntervals, fatalErrorPair.f0,isGracefulEOS, sessionExpired, hasVideoPlayState);

    }

    public static void updateTotalPlayDuration(SessionStatsAggr[] affectedIntervals, Map<String,Long> sessionPlayDurations) {
        Map<String, SessionStatsAggr> sessionStatsMap = new HashMap<>();
        for (SessionStatsAggr statsInterval : affectedIntervals) {
            String key = statsInterval.getInterval().getStartTS() + "-" + statsInterval.getInterval().getEndTS();
            sessionStatsMap.put(key, statsInterval);
        }

        Set<String> durationKeys = sessionPlayDurations.keySet();
        if (!durationKeys.isEmpty()) {
            Long totalDuration = 0l;
            for (String key : durationKeys) {
                totalDuration = totalDuration + sessionPlayDurations.get(key);
                SessionStatsAggr statsInterval = sessionStatsMap.get(key);
                if (statsInterval != null) {
                    statsInterval.setPlayDurationTotal(totalDuration);
                }
            }
        }
    }


    public static Tuple2<Boolean, Long> checkFatalErros(NavigableMap<Long, Object> errorTimeLine) {
        if (errorTimeLine == null) return Tuple2.of(false, -1L);
        ClientError clientError = null;
        for (Entry<Long, Object> entry : errorTimeLine.entrySet()) {
            clientError = (ClientError) entry.getValue();
            if (clientError.isFatal()) {
                return Tuple2.of(true, entry.getKey());
            }
        }
        return Tuple2.of(false, -1L);
    }

    /**
     * Computes the average bitrate for given intervals in weighted manner.
     *
     * @param affectedStatsIntervals - affected intervals that needs to be computed
     * @param timeline               - time line of all the events
     */
    public static void updateBitrate(SessionStatsAggr[] affectedStatsIntervals, SessionTimeline timeline) {
        NavigableMap<Long, Object> brTL = timeline.getChangesTimelines().get(EventField.BITRATE);
        if (brTL == null) return;
        for (SessionStatsAggr interval : affectedStatsIntervals) {
            Entry<Long, Object> prevChange = brTL.lowerEntry(interval.getInterval().getStartTS());
            Long prevTS = prevChange == null ? 0L : prevChange.getKey();
            Integer prevVal = prevChange == null ? 0 : (Integer) prevChange.getValue();
            long totalDuration = 0, brSum = 0;
            Entry<Long, Object> brEntry;
            while (prevTS < interval.getInterval().getEndTS()) {
                brEntry = brTL.higherEntry(prevTS);
                long brTS = brEntry == null ? interval.getInterval().getEndTS(): (Long) brEntry.getKey();
                Integer brVal = brEntry == null ? 0 : (Integer) brEntry.getValue();
                if (prevVal > 0) {
                    long duration = subtractNonPlayerStatesDuration(Math.max(prevTS, interval.getInterval().getStartTS()),
                            Math.min(brTS, interval.getInterval().getEndTS()), timeline.getChangesTimelines().get(EventField.PLAYER_STATE));
                    brSum += prevVal * duration;
                    totalDuration += duration;

                }
                prevTS = brTS;
                prevVal = brVal;
            }
            interval.setBitrateStats(totalDuration == 0 ? null :
                    new BitrateStats(brSum / totalDuration, interval.getInterval().getStartTS(), interval.getInterval().getEndTS()));
        }

    }

    private static long subtractNonPlayerStatesDuration(long startOfDuration, long endOfDuration, NavigableMap<Long, Object> playStatesTimeline) {
        long duration = endOfDuration - startOfDuration;
        if (playStatesTimeline == null)// is it really possible to have bitrate without playerstate?
            return duration;
        Entry<Long, Object> playStateEntry = playStatesTimeline.lowerEntry(startOfDuration);
        if (playStateEntry == null) {
            playStateEntry = playStatesTimeline.higherEntry(startOfDuration);
            if (playStateEntry == null) {
                LOG.debug("No PlayerState in timeline but BitRate is reported in same timeframe");
                return duration;
            }
        }
        long subtractDurationStart = 0;
        while (playStateEntry != null && playStateEntry.getKey() < endOfDuration) {
            PlayerState playerState = (PlayerState) playStateEntry.getValue();
            if (playerState != null && !PLAYABLE_STATES.stream().anyMatch(referenceState -> referenceState.equals(playerState))) {
                //found player state != play &&
                //  if subtractDurationStart ==0 then mark it as start, now in this case, if the state prevails from previous interval then
                //  for this interval, we should be considering only startOfDuration which will have impact for the playerstate on this interval
                // if subtractDurationStart !=0 then it is already marked so no need to do anything until
                // we found again playerstate = play or end of the duration considered here for the bitrate.
                if (subtractDurationStart == 0) {
                    subtractDurationStart = playStateEntry.getKey() < startOfDuration ? startOfDuration : playStateEntry.getKey();
                }
            } else {
                //found playerstate = play and
                // if found subtractDurationStart!=0 that means any of the previous traversal of the loop  marked
                // non playered state, now we need to subtract the duration of this non player state to player state
                // from the bitrate total duration.
                if (subtractDurationStart != 0) {
                    long subtractDurationEnd = playStateEntry.getKey();
                    duration = duration - (subtractDurationEnd - subtractDurationStart);
                    subtractDurationStart = 0;
                }
            }
            playStateEntry = playStatesTimeline.higherEntry(playStateEntry.getKey());
        }
        // playerstate!=play  continued till the end of our "endOfDuration" then
        // we subtract that interval
        if (subtractDurationStart != 0) {
            duration = duration - (endOfDuration - subtractDurationStart);
        }
        return duration;
    }

    public static void updateAttempt(TreeMap<Long, SessionStatsAggr> sessionStatsMap, Heartbeat heartbeat) {
        for (Entry<Long, SessionStatsAggr> entry : sessionStatsMap.entrySet()) {
            SessionStatsAggr sessionStatsAggr = entry.getValue();
            // TODO Fix this bug, intervals should not be inclusive from both sides!!!
            if (sessionStatsAggr.getInterval().getStartTS() <= heartbeat.getSessionStartTime()
                    && heartbeat.getSessionStartTime() < sessionStatsAggr.getInterval().getEndTS()) {
                sessionStatsAggr.setAttempt((byte) 1);
            }
        }
    }

    /**
     * compute the average frame rate for the affected intervals, note that afps is reported at HeartBeat level
     * the calculation logic is a weighted function like - (w1x1 +w2x2)/(w1+w2) where
     * w1 is the amount of time value x1 (afps) has been prevailed.
     *
     * @param affectedStatsIntervals
     * @param sessionTimeline
     */

    public static void updateAverageFrameRate(SessionStatsAggr[] affectedStatsIntervals, SessionTimeline sessionTimeline) {
        NavigableMap<Long, Object> avgFrameRateTimeLine = sessionTimeline.getChangesTimelines().get(EventField.AVG_FRAME_RATE);
        if (avgFrameRateTimeLine == null) return;
        for (SessionStatsAggr interval : affectedStatsIntervals) {
            Entry<Long, Object> prevChange = avgFrameRateTimeLine.lowerEntry(interval.getInterval().getStartTS());
            Long prevTS = prevChange == null ? 0L : prevChange.getKey();
            Integer prevVal = prevChange == null ? 0 : (Integer) prevChange.getValue();
            long totalDuration = 0, avgFrameRateSum = 0;
            Entry<Long, Object> avgFrameRateEntry;
            while (prevTS < interval.getInterval().getEndTS()) {
                avgFrameRateEntry = avgFrameRateTimeLine.higherEntry(prevTS);
                long avgFrameRateTimestamp = avgFrameRateEntry == null ? interval.getInterval().getEndTS() : (Long) avgFrameRateEntry.getKey();
                Integer avgFrameRateValue = avgFrameRateEntry == null ? 0 : (Integer) avgFrameRateEntry.getValue();
                if (prevVal > 0) {
                    long duration = subtractNonPlayerStatesDuration(Math.max(prevTS, interval.getInterval().getStartTS()),
                            Math.min(avgFrameRateTimestamp, interval.getInterval().getEndTS()), sessionTimeline.getChangesTimelines().get(EventField.PLAYER_STATE));
                    avgFrameRateSum += prevVal * duration;
                    totalDuration += duration;
                }
                prevTS = avgFrameRateTimestamp;
                prevVal = avgFrameRateValue;
            }
            interval.setFramerateStats(totalDuration == 0 ? null :
                    new FramerateStats(avgFrameRateSum / totalDuration, interval.getInterval().getStartTS(), interval.getInterval().getEndTS()));

        }
    }

    /**
     *  if playStarted at this current Interval
     *       calculate the difference between hb's sessionStartTime to video play state time
     * @param sessionTimeline
     * @param heartbeat
     * @param affectedStatsIntervals
     */
    public static void updateVST(SessionTimeline sessionTimeline,
                                 Heartbeat heartbeat,
                                 SessionStatsAggr[] affectedStatsIntervals) {
        Long playStartedAt = null, vst = null;
        playStartedAt = findInitialPlayerState(sessionTimeline.getChangesTimelines().get(EventField.PLAYER_STATE), PlayerState.play);

        if(playStartedAt==null)
            return;
        for (SessionStatsAggr statsInterval : affectedStatsIntervals) {
            if (playStartedAt >= statsInterval.getInterval().getStartTS() && playStartedAt < statsInterval.getInterval().getEndTS()) {
                vst = computeVST(sessionTimeline,heartbeat.getStreamType(),heartbeat.getPlatformType(),heartbeat,playStartedAt);
                if(vst < 0L){
                    LOG.error("VSTError: negative vst[videoStartedAt-SST] - {}, for playStartedAt {}, sst {}, sid {}, streamID {}", vst, playStartedAt, heartbeat.getSessionStartTime(), heartbeat.getSessionId(), heartbeat.getStreamID());
                    return ;
                }
                statsInterval.setVideoStartupTime(vst);
                break;
            }
        }
    }

    private static Long getVSTForVOD(SessionTimeline sessionTimeline, String platformType, Long vst, Long playStartedAt){
        if(platformType.toLowerCase().contains("roku")){
            Long preRollAdEndAt = findPlayerStateBeforeFirstPlayState(sessionTimeline.getChangesTimelines().get(EventField.PLAYER_STATE), PlayerState.ad_end, playStartedAt);
            vst = (preRollAdEndAt != null && playStartedAt >= preRollAdEndAt) ? (playStartedAt - preRollAdEndAt) : vst;
            return vst;
        }else if(platformType.toLowerCase().contains("osprey")){
            Long preRollAdEndAt = findPlayerStateBeforeFirstPlayState(sessionTimeline.getChangesTimelines().get(EventField.PLAYER_STATE), PlayerState.ad_end, playStartedAt);
            Long preRollAdStartAt = findInitialPlayerState(sessionTimeline.getChangesTimelines().get(EventField.PLAYER_STATE), PlayerState.ad_start);
            vst = (preRollAdStartAt != null && preRollAdEndAt != null && playStartedAt >= preRollAdEndAt && preRollAdEndAt > preRollAdStartAt) ? (vst - (preRollAdEndAt - preRollAdStartAt)) : vst;
            return vst;
        }else{
            return vst;
        }
    }

    private static Long computeVST(SessionTimeline sessionTimeline, String streamType, String platformType, Heartbeat heartbeat, Long playStartedAt){
        Long vst = playStartedAt - (long) heartbeat.getSessionStartTime();
        switch(streamType){
            case "VOD":
                vst = getVSTForVOD(sessionTimeline,platformType,vst,playStartedAt);
                break;
            default:
                break;
        }
        return vst;
    }

    /**
     * looking for the latest "playerState" before specified timevalue
     * playerstatetimeline can have multiple play state (play, pause, play eg.)
     * to calculate the vst, we need to find the latest adEnd first player state before first play.
     * @param playerStateTimeline
     * @param playerStateType
     * @param playStartTime
     * @return
     */
    private static Long findPlayerStateBeforeFirstPlayState(NavigableMap<Long, Object> playerStateTimeline, PlayerState playerStateType, Long playStartTime) {
        if(playerStateTimeline==null)
            return null;
        if(playStartTime == null)
            return null;
        Entry<Long, Object> currentEntry = playerStateTimeline.lowerEntry(playStartTime);
        while(currentEntry!=null){
            if(currentEntry.getValue() == playerStateType){
                return currentEntry.getKey();
            }
            currentEntry = playerStateTimeline.lowerEntry(currentEntry.getKey());
        }
        return null;
    }

    /**
     * looking for the first player state of specified 'playerStateType'.
     * playerstatetimeline can have multiple play state (play, pause, play eg.)
     * to calculate the vst, we need to find the first player state specified 'playerStateType'.
     * @param playerStateTimeline
     * @param playerStateType
     * @return
     */
    private static Long findInitialPlayerState(NavigableMap<Long, Object> playerStateTimeline, PlayerState playerStateType) {
        if(playerStateTimeline==null)
            return null;
        Entry<Long, Object> currentEntry = playerStateTimeline.firstEntry();
        while(currentEntry!=null){
            if(currentEntry.getValue() == playerStateType){
                return currentEntry.getKey();
            }
            currentEntry = playerStateTimeline.higherEntry(currentEntry.getKey());
        }
        return null;
    }

    public static boolean isSessionPlayingVideo(SessionTimeline sessionTimeline){
        NavigableMap<Long, Object> playerStateTimeline=null;
        if(sessionTimeline==null || (playerStateTimeline=sessionTimeline.getChangesTimelines().get(EventField.PLAYER_STATE))==null){
            return false;
        }
        return playerStateTimeline.containsValue(PlayerState.play);
    }




    public static boolean isGracefulEndOfSession(SessionTimeline sessionTimeline) {
        return sessionTimeline != null &&
                sessionTimeline.getChangesTimelines() != null &&
                sessionTimeline.getChangesTimelines().get(EventField.END_OF_SESSION) != null &&
                !sessionTimeline.getChangesTimelines().get(EventField.END_OF_SESSION).isEmpty();
    }

    /*
     this method calculates interrupts for entire session (SSD) only.
     */
    public static void updateBufferInterrupts(SessionTimeline timeline, SessionStatsAggr[] affectedIntervals){
        NavigableMap<Long, Object> playerTimeline = timeline.getChangesTimelines().get(EventField.PLAYER_STATE);
        if (playerTimeline == null) return;
        int bufferInterrupts = 0;
        Entry<Long, Object> currentEntry = playerTimeline.firstEntry();
        while(currentEntry!=null){
            PlayerState playerState = (PlayerState)currentEntry.getValue();
            if(PlayerState.buffer_start.equals(playerState)){
                bufferInterrupts++;
            }
            currentEntry = playerTimeline.higherEntry(currentEntry.getKey());
        }
        for(SessionStatsAggr sessionStatsAggr : affectedIntervals){
            sessionStatsAggr.setInterrupts(bufferInterrupts);
        }
    }


    /**
     *  calculates play duration and rebuffering duration
     *    rebuffering duration is sum of connection induced and seek induced
     *    separately connection induced rebuffering is also logged
     * @param timeline
     * @param heartbeat
     * @param affectedIntervals
     */
    public static void updatePlayRebuffering(SessionTimeline timeline,
                                             Heartbeat heartbeat,
                                             SessionStatsAggr[] affectedIntervals) {
        NavigableMap<Long, Object> playerTimeline = timeline.getChangesTimelines().get(EventField.PLAYER_STATE);
        if (playerTimeline == null) return;
        Long rebufferingDuration = 0L;
        Long ciRebufferingDuration = 0L;
        Long playDuration = 0L;
        Long effectiveEnd;
        Long playStartedAt = findInitialPlayerState(timeline.getChangesTimelines().get(EventField.PLAYER_STATE), PlayerState.play);

        for (SessionStatsAggr statsInterval : affectedIntervals) {
            Long eventTimestamp = statsInterval.getInterval().getEndTS();
            long intervalStartTs = statsInterval.getInterval().getStartTS();
            // We need to check if the timeline progressed father then current HB, since HB can come out of order
            effectiveEnd = Math.min(Math.max(heartbeat.tsMillis(), playerTimeline.lastKey()), eventTimestamp);
            while (eventTimestamp != null && eventTimestamp > intervalStartTs) {
                Entry<Long, Object> playerEvent = playerTimeline.lowerEntry(eventTimestamp);
                //process previous state
                if (playerEvent == null)
                    break;
                long playerEventTS = playerEvent.getKey();
                long effectiveStart = Math.max(playerEventTS, intervalStartTs);
                long addedDuration = (effectiveEnd - effectiveStart);
                PlayerState currentState = (PlayerState) playerEvent.getValue();
                if (PlayerState.play.equals(currentState)) {
                    playDuration += addedDuration;
                }else{
                        if (PlayerState.buffer_start.equals(currentState) || PlayerState.buffer_complete.equals(currentState)
                            || PlayerState.seek_complete.equals(currentState)
                                || PlayerState.ad_break_end.equals(currentState) || PlayerState.ad_end.equals(currentState)){
                            if ((playStartedAt != null) && (playerEventTS > playStartedAt.longValue())) {
                                boolean hasBufferStart = PlayerState.buffer_start.equals(currentState)? true: findBufferStart(playerTimeline, playerEvent);

                                if(hasBufferStart) {
                                    if (!seekInducedBuffering(playerTimeline, playerEventTS, currentState) && !adInducedBuffering(playerTimeline, playerEventTS, currentState)) {
                                        ciRebufferingDuration += addedDuration;
                                    }
                                    rebufferingDuration += addedDuration;
                                }
                            }
                        }


                }
                eventTimestamp = effectiveEnd = effectiveStart;
            }
            String intervalKey = statsInterval.getInterval().getStartTS() + "-" + statsInterval.getInterval().getEndTS();
            if(statsInterval.getInterval().getLength() > 1)
                timeline.getSessionMetadata().addLongIntervalPlayDuration(intervalKey,playDuration);
            else
                timeline.getSessionMetadata().addShortIntervalPlayDuration(intervalKey,playDuration);
            statsInterval.setPlayDuration(playDuration);
            statsInterval.setRebuferringDuration(rebufferingDuration);
            statsInterval.setCiRebuferringDuration(ciRebufferingDuration);
            Double rebufferingRatio = 0.0d;
            if (rebufferingDuration > 0)
                rebufferingRatio = ((double) rebufferingDuration) / (playDuration + rebufferingDuration);
            Double ciRebufferingRatio = 0.0d;
            if (ciRebufferingDuration > 0)
                ciRebufferingRatio = ((double) ciRebufferingDuration) / (playDuration + rebufferingDuration);
            statsInterval.setRebuferringRatio(rebufferingRatio);
            statsInterval.setCiRebuferringRatio(ciRebufferingRatio);
            playDuration = 0L;
            rebufferingDuration = 0L;
            ciRebufferingDuration = 0L;
        }
    }

    /**
     *
     * @param playerTimeline
     * @param currentEvent
     * @return
     * check if there is a buffer start state between current state and previous terminate state such as play, pause.
     * TODO: should we consider advertise break as terminate state as well?
     */
    private static boolean findBufferStart(NavigableMap<Long, Object> playerTimeline, Entry<Long, Object> currentEvent) {
        if(currentEvent==null)
            return false;
        Entry<Long, Object> prevEvent = playerTimeline.lowerEntry(currentEvent.getKey());
        while(prevEvent!=null){
            PlayerState playerState = (PlayerState)prevEvent.getValue();
            if(PlayerState.buffer_start.equals(playerState)){
                return true;
            }
            if(PLAYABLE_STATES.stream().anyMatch(referenceState -> referenceState.equals(playerState))
                    || PlayerState.buffer_complete.equals(playerState)){
                break;
            }
            prevEvent = playerTimeline.lowerEntry(prevEvent.getKey());
        }

        PlayerState currentState = (PlayerState) currentEvent.getValue();
        return  PlayerState.buffer_complete.equals(currentState)?true: false;
    }

    /**
     *
     * @param playerTimeline
     * @param currentEventTS
     * @param currentState
     * @return
     * if current entry is buffer_start or buffer_complete and the prev entry has seek state then it is seek induced buffering
     * if current entry is seek then it is always seek induced..
     */
    private static boolean seekInducedBuffering(NavigableMap<Long, Object> playerTimeline, Long currentEventTS, PlayerState currentState) {
        if(!PlayerState.buffer_start.equals(currentState) && !PlayerState.buffer_complete.equals(currentState)
                && !PlayerState.ad_break_end.equals(currentState) && !PlayerState.ad_end.equals(currentState))
            return true;
        if(PlayerState.buffer_start.equals(currentState) || PlayerState.buffer_complete.equals(currentState)) {
            /*Entry<Long, Object> prevEvent = playerTimeline.lowerEntry(currentEventTS);
            if (prevEvent != null
                    && (PlayerState.seek_complete.equals(prevEvent.getValue()) || PlayerState.seek_start.equals(prevEvent.getValue()))
                    && ((currentEventTS - prevEvent.getKey()) < MAX_SEEK_BUF_MILLIS))
                return true;*/
            Entry<Long, Object> prevEvent = playerTimeline.lowerEntry(currentEventTS);
            while(prevEvent!=null){
                PlayerState playerState = (PlayerState)prevEvent.getValue();
                if(PlayerState.seek_start.equals(playerState) ||
                        PlayerState.seek_complete.equals(playerState)){
                    return true;
                }
                if(PLAYABLE_STATES.stream().anyMatch(referenceState -> referenceState.equals(playerState))){
                    break;
                }
                prevEvent = playerTimeline.lowerEntry(prevEvent.getKey());
            }
        }
        return false;
    }

    /**
     *
     * @param playerTimeline
     * @param currentEventTS
     * @param currentState
     * @return
     * if current entry is buffer_start or buffer_complete and the prev entry has ad state then it is ad induced buffering
     * if current entry is ad then it is always ad induced..
     */
    private static boolean adInducedBuffering(NavigableMap<Long, Object> playerTimeline, Long currentEventTS, PlayerState currentState) {
        if(!PlayerState.buffer_start.equals(currentState) && !PlayerState.buffer_complete.equals(currentState)
                && !PlayerState.seek_start.equals(currentState) && !PlayerState.seek_complete.equals(currentState))
            return true;
        if(PlayerState.buffer_start.equals(currentState) || PlayerState.buffer_complete.equals(currentState)) {
            /*Entry<Long, Object> prevEvent = playerTimeline.lowerEntry(currentEventTS);
            if (prevEvent != null
                    && (PlayerState.ad_end.equals(prevEvent.getValue()) || PlayerState.ad_break_end.equals(prevEvent.getValue()))
                    && ((currentEventTS - prevEvent.getKey()) < MAX_SEEK_BUF_MILLIS))
                return true;*/
            Entry<Long, Object> prevEvent = playerTimeline.lowerEntry(currentEventTS);
            while(prevEvent!=null){
                PlayerState playerState = (PlayerState)prevEvent.getValue();
                if(PlayerState.ad_break_end.equals(playerState) ||
                        PlayerState.ad_end.equals(playerState)){
                    return true;
                }
                if(PLAYABLE_STATES.stream().anyMatch(referenceState -> referenceState.equals(playerState))){
                    break;
                }
                prevEvent = playerTimeline.lowerEntry(prevEvent.getKey());
            }
        }
        return false;
    }

    public static void updateErrors(SessionTimeline sessionTimeline,
                                    SessionStatsAggr[] affectedIntervals) {

        NavigableMap<Long, Object> errorsTL = sessionTimeline.getChangesTimelines().get(EventField.CLIENT_ERROR);
        if (errorsTL == null) return;

        for (int i = affectedIntervals.length - 1; i >= 0; i--) { //backward direction
            SessionStatsAggr statsInterval = affectedIntervals[i];
            Long ts = statsInterval.getInterval().getEndTS();
            Map<ClientError, Integer> counts = new HashMap<>();
            while (ts != null && ts >= statsInterval.getInterval().getStartTS()) {
                Entry<Long, Object> entry = errorsTL.floorEntry(ts);
                if (entry == null || (ts = entry.getKey()) < statsInterval.getInterval().getStartTS()) break;
                //increment specific counter for an error
                counts.compute((ClientError) entry.getValue(), (k, v) -> v == null ? 1 : v + 1);
                ts = errorsTL.lowerKey(ts);
            }
            if (!counts.isEmpty()) {
                Tuple3<Integer, Integer, List<ClientError>> errorInfoTriple = flattenErrorInfo(counts);
                statsInterval.setErrorCounts(errorInfoTriple.f2);
                statsInterval.setFatalErrorCount(errorInfoTriple.f0);
                statsInterval.setNonFatalErrorCount(errorInfoTriple.f1);
            }

        }
    }

    private static Tuple3<Integer, Integer, List<ClientError>> flattenErrorInfo(Map<ClientError, Integer> counts) {
        List<ClientError> clientErrors = new ArrayList<>();
        int nonFatal = 0, fatal = 0;
        for (Entry<ClientError, Integer> entry : counts.entrySet()) {
            ClientError clientError = entry.getKey();
            clientError.setCount(entry.getValue());
            clientErrors.add(clientError);
            if (clientError.isFatal()) {
                fatal += entry.getValue();
            } else if (!clientError.isFatal()) {
                nonFatal += entry.getValue();
            }
        }
        return Tuple3.of(fatal, nonFatal, clientErrors);
    }

    /**
     * Looks for video restart time.
     * Restart time considered as a time video is buffering after seek.
     *
     * @param sessionTimeline
     * @param affectedIntervals
     */
    public static void updateVRT(SessionTimeline sessionTimeline, SessionStatsAggr[] affectedIntervals) {

        NavigableMap<Long, Object> playerStateTL = sessionTimeline.getChangesTimelines().get(EventField.PLAYER_STATE);
        if (playerStateTL == null) return;

        for (int i = affectedIntervals.length - 1; i >= 0; i--) { //backward direction
            SessionStatsAggr statsInterval = affectedIntervals[i];
            Long ts = statsInterval.getInterval().getEndTS();
            int count = 0;
            long totalTime = 0;
            HashSet<String> vrtBoundaries = new HashSet<>();
            while (ts != null && ts >= statsInterval.getInterval().getStartTS()) {
                Entry<Long, Object> entry = playerStateTL.floorEntry(ts);
                if (entry == null) break;
                PlayerState plSt = (PlayerState) entry.getValue();
                if (plSt == PlayerState.play) {
                    Entry<Long, Object> prevEntry;
                    Long playTS = entry.getKey();
                    Long tempTS = playTS;
                    boolean bufferingFound = false;
                    while ((prevEntry = playerStateTL.lowerEntry(tempTS)) != null) {
                        plSt = (PlayerState) prevEntry.getValue();
                        if (plSt == PlayerState.buffer_complete || plSt == PlayerState.buffer_start) {
                            tempTS = prevEntry.getKey();
                            bufferingFound = true;
                            continue;
                        } else if (bufferingFound && (plSt == PlayerState.seek_complete || plSt == PlayerState.seek_start)) {
                            String key = playTS+""+tempTS;
                            if(vrtBoundaries.add(key)) {
                                count++;
                                totalTime += (playTS - tempTS);
                            }
                        }
                        break;
                    }
                }
                ts = playerStateTL.lowerKey(ts);
            }
            if (count > 0) {
                statsInterval.setVideoRestartTime(totalTime / count);
                statsInterval.setVideoRestartCount(count);
                statsInterval.setVideoRestartTimeTotal(totalTime);
            }
        }
    }

    private static void updateSequences(SessionTimeline timeline, Heartbeat heartbeat) {
        timeline.addChange(ChangeEvent.of(EventField.HEARTBEAT_SEQUENCE, heartbeat.getSequence(), heartbeat.tsMillis()));
        if(heartbeat.getEvents()==null)
            return;
        for (HeartbeatEvent event : heartbeat.getEvents()) {
            //TODO we have to store list of sequences as value since we can have multiple events at same ts) - think...
            timeline.addChange(ChangeEvent.of(EventField.EVENT_SEQUENCE, event.getSequence(),
                    (long) (heartbeat.getSessionStartTime() + event.getEventTimeMillisec())));
        }
    }

    private static Object heartbeatValue(Heartbeat heartbeat, EventField field) {
        switch (field) {
            case PLAYER_STATE:
                Integer playerState = heartbeat.getPlayerState();
                return playerState == null ? null : PlayerState.fromVal(playerState);
            case AVG_FRAME_RATE:
                return heartbeat.getFramesPerSecond();
            case BUFFER_LENGTH:
                return heartbeat.getBufferLength();
            case BITRATE:
                return heartbeat.getBitrate();
            default:
                return null;
        }
    }


    /**
     *  The method is invoked to check for metrics EOP,EBVS, VSF && VPF
     *  for all four following check is true
     *  if it is end of session either gracefully or expired
     *          this four metrics should be checked based couple of more check fatalError & Video Play state
     * @param sessionTimeline
     * @param affectedStatsIntervals
     * @param hasFatalError
     * @param isGracefulEOS
     * @param sessionExpired
     * @param hasVideoPlayState
     */
    public static void updateEOSDependentMetrics(SessionTimeline sessionTimeline, SessionStatsAggr[] affectedStatsIntervals, boolean hasFatalError, boolean isGracefulEOS, boolean sessionExpired, boolean hasVideoPlayState) {

        if(!isGracefulEOS && !sessionExpired){
            return;
        }

        NavigableMap<Long, Object> endOfSessionTimeline = sessionTimeline.getChangesTimelines().get(EventField.END_OF_SESSION);
        //if it is session expired then we wont have any eosTs, in that case, the current interval should be marked
        Long eosTs = (!sessionExpired && endOfSessionTimeline!=null && endOfSessionTimeline.lastKey()!=null)?endOfSessionTimeline.lastKey():null;

        for (SessionStatsAggr statsInterval : affectedStatsIntervals) {
            if(eosTs==null || (eosTs != null && eosTs >= statsInterval.getInterval().getStartTS()
                    && eosTs < statsInterval.getInterval().getEndTS())){
                if(hasFatalError){
                    if(hasVideoPlayState){
                        // if there is a fatal error and video play state , eligible for VPF
                        statsInterval.setVideoPlayFailures((byte)1);
                        break;
                    }else if(!hasVideoPlayState){
                        // if there is fatal error but no video play state eligible for VSF
                       statsInterval.setVideoStartFailure((byte)1);
                        break;
                    }
                }else if(!hasFatalError){
                    if(hasVideoPlayState){
                        //If there is no fatal error and there was a video play state eligible for ended play.
                        statsInterval.setEndedPlay((byte)1);
                        break;
                    }else if(!hasVideoPlayState){
                        //if there is no fatal error and no video play state then eligible for Ended Before Video Start
                        statsInterval.setEndBeforeVideoStart((byte)1);
                        break;
                    }
                }
            }
        }
    }
}
