package com.att.dtv.kda.model.app;

import com.att.dtv.kda.process.Interval;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SessionMetadata implements Serializable {
    private Map<String,Long> sessionPlayDurationsShortInterval = new TreeMap<>(new SessionPlayTimeComparator());
    private Map<String,Long> sessionPlayDurationsLongInterval = new TreeMap<>(new SessionPlayTimeComparator());

    public SessionMetadata(){}

    public Map<String, Long> getSessionPlayDurationsShortInterval() {
        return sessionPlayDurationsShortInterval;
    }

    public Map<String, Long> getSessionPlayDurationsLongInterval() {
        return sessionPlayDurationsLongInterval;
    }

    public void addShortIntervalPlayDuration(String intervalKey, Long duration){
        this.sessionPlayDurationsShortInterval.put(intervalKey,duration);
    }

    public void addLongIntervalPlayDuration(String intervalKey, Long duration){
        this.sessionPlayDurationsLongInterval.put(intervalKey,duration);
    }

    public void setSessionPlayDurationsShortInterval(Map<String, Long> sessionPlayDurationsShortInterval) {
        this.sessionPlayDurationsShortInterval = sessionPlayDurationsShortInterval;
    }

    public void setSessionPlayDurationsLongInterval(Map<String, Long> sessionPlayDurationsLongInterval) {
        this.sessionPlayDurationsLongInterval = sessionPlayDurationsLongInterval;
    }
}
