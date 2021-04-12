package com.att.dtv.kda.process;

import com.att.dtv.kda.model.app.Heartbeat;
import com.att.dtv.kda.model.app.SessionStatsAggr;

import java.util.TreeMap;

public class SessionMetadataUtils {

    public static void updateMetadata(Heartbeat hb, TreeMap<Long, SessionStatsAggr> sessionStatsMap, int intervalLength) {
        if (sessionStatsMap == null) return;

        SessionStatsAggr sessionStats = sessionStatsMap.get(Interval.fromTS((long) (hb.getSessionStartTime() + hb.getStartTime()), intervalLength).getStartTS());
        if (sessionStats != null && hb != null) {
            sessionStats.setMetaHeartbeat(hb);
        }
    }
}
