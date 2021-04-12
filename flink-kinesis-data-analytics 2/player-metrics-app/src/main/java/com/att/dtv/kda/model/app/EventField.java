package com.att.dtv.kda.model.app;

import java.util.HashSet;
import java.util.Set;

public enum EventField {
    PLAYER_STATE(ChangeLevel.HEARTBEAT, ChangeLevel.EVENT),
    AVG_FRAME_RATE(ChangeLevel.HEARTBEAT),
    END_OF_SESSION(ChangeLevel.EVENT),
    CLIENT_ERROR(/*ChangeLevel.HEARTBEAT, */ChangeLevel.EVENT),
    BITRATE(ChangeLevel.HEARTBEAT, ChangeLevel.EVENT),
    BUFFER_LENGTH(ChangeLevel.HEARTBEAT, ChangeLevel.EVENT),
    HEARTBEAT_SEQUENCE(ChangeLevel.HEARTBEAT),
    EVENT_SEQUENCE(ChangeLevel.EVENT);

    public enum ChangeLevel {
        HEARTBEAT, EVENT;
    }

    private Set<ChangeLevel> levels;

    private EventField(final ChangeLevel... levels) {
        this.levels = new HashSet<ChangeLevel>();
        for (ChangeLevel level : levels) {
            this.levels.add(level);
        }
    }

    public boolean hasLevel(ChangeLevel level) {
        return levels.contains(level);
    }
}
