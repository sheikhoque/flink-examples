package com.att.dtv.kda.model.app;

import java.sql.Timestamp;

public interface TimeProcessable {
    void setEventTime(Timestamp eventTimestamp);
    void setPartitionId(String id);
}
