package com.att.dtv.kda.model.app;

import org.apache.flink.api.java.tuple.Tuple2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class SessionTimeline implements Serializable {
    public static final long serialVersionUID = 51L;

    private Map<EventField, NavigableMap<Long, Object>> changesTimelines;
    private long lastModified;
    private Tuple2<String, Long> key;

    private Long sessionId;

    private SessionMetadata sessionMetadata;

    public SessionTimeline() {
        this.changesTimelines = new HashMap<>();
        this.sessionMetadata = new SessionMetadata();
    }

    public SessionTimeline(long sessionId) {
        this();
        this.sessionId = sessionId;
        this.sessionMetadata = new SessionMetadata();
    }

    public void setChangesTimelines(Map<EventField, NavigableMap<Long, Object>> changesTimelines) {
        this.changesTimelines = changesTimelines;
    }

    public Map<EventField, NavigableMap<Long, Object>> getChangesTimelines() {
        return changesTimelines;
    }

    public boolean addChange(ChangeEvent<?> changeEvent) {
        NavigableMap<Long, Object> eventTimeline = changesTimelines.computeIfAbsent(changeEvent.getEventField(),
                e -> new TreeMap<>());
        //latest change should be different and happen before current change (in case we're re-processing same heartbeat)
        Object currentVal = eventTimeline.get(changeEvent.getTs());
        if (currentVal != null && currentVal.equals(changeEvent.getEventValue())) {
            return false;
        }
        eventTimeline.put(changeEvent.getTs(), changeEvent.getEventValue());
        return true;
    }

    public Object getLatestValue(EventField eventField) {
        if (isEmpty())
            return null;
        NavigableMap<Long, Object> changesTimeline = this.changesTimelines.getOrDefault(eventField, new TreeMap<>());
        Map.Entry<Long, Object> latest = changesTimeline.lastEntry();
        if (latest == null)
            return null;
        else
            return latest.getValue();
    }

    public boolean contains(ChangeEvent changeEvent) {
        return contains(changeEvent, 0L);
    }

    public boolean contains(ChangeEvent changeEvent, Long afterTs) {
        if (isEmpty())
            return false;
        NavigableMap<Long, Object> changesTimeline = this.changesTimelines.getOrDefault(changeEvent.getEventField(), new TreeMap<>());
        return changesTimeline.tailMap(afterTs).containsValue(changeEvent);
    }

    public boolean isEmpty() {
        return this.changesTimelines == null || this.changesTimelines.isEmpty();
    }

    public NavigableMap<Long, Object> changesPostTs(EventField eventField, Long afterTs) {
        if (isEmpty())
            return new TreeMap<>();
        NavigableMap<Long, Object> changesTimeline = this.changesTimelines.getOrDefault(eventField, new TreeMap<>());
        return changesTimeline.tailMap(afterTs, true);
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public Tuple2<String, Long> getKey() {
        return key;
    }

    public void setKey(Tuple2<String, Long> key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "SessionTimeline [changesTimelines=" + changesTimelines + ", lastModified=" + lastModified + ", key=" + key + "]";
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public SessionMetadata getSessionMetadata() {
        return sessionMetadata;
    }

    public void setSessionMetadata(SessionMetadata sessionMetadata) {
        this.sessionMetadata = sessionMetadata;
    }
}
