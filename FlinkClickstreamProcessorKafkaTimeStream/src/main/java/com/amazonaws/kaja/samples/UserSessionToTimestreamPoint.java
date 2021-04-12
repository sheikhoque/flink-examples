package com.amazonaws.kaja.samples;

import com.amazonaws.kaja.samples.sink.timestream.TimestreamPoint;
import com.amazonaws.services.timestreamwrite.model.MeasureValueType;
import com.amazonaws.services.timestreamwrite.model.TimeUnit;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

import java.time.format.DateTimeFormatter;

public class UserSessionToTimestreamPoint extends RichMapFunction<UserIdSessionEvent, TimestreamPoint> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public TimestreamPoint map(UserIdSessionEvent userIdSessionEvent) throws Exception {
        TimestreamPoint dataPoint = new TimestreamPoint();
        dataPoint.setMeasureName("event_count");
        dataPoint.setMeasureValue(String.valueOf(userIdSessionEvent.getEventCount()));
        dataPoint.setMeasureValueType(MeasureValueType.BIGINT);
        dataPoint.setTime(userIdSessionEvent.getWindowEndTime());
        dataPoint.setTimeUnit(TimeUnit.MILLISECONDS.toString());
    /*    dataPoint.addDimension("userId",String.valueOf(userIdSessionEvent.getUserId()));
        dataPoint.addDimension("windowStartTime",
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(userIdSessionEvent.getWindowBeginTime())
                        , ZoneId.systemDefault()).format(FORMATTER));
        dataPoint.addDimension("windowEndTime",
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(userIdSessionEvent.getWindowEndTime())
                        , ZoneId.systemDefault()).format(FORMATTER));*/

        return dataPoint;
    }
}
