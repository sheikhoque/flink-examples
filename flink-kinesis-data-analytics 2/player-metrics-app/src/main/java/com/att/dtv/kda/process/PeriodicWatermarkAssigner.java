package com.att.dtv.kda.process;

import com.att.dtv.kda.model.app.Heartbeat;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodicWatermarkAssigner extends BoundedOutOfOrdernessTimestampExtractor<Heartbeat> {
    Logger LOG = LoggerFactory.getLogger(PeriodicWatermarkAssigner.class);

    public PeriodicWatermarkAssigner(long timeout) {
        super(Time.milliseconds( timeout));
    }

    @Override
    public long extractTimestamp(Heartbeat heartbeat) {
        return heartbeat.getEventTimestamp().getTime();
    }
}
