package com.amazonaws.kaja.samples;

import com.amazonaws.kaja.samples.sink.timestream.TimestreamPoint;
import com.amazonaws.services.timestreamwrite.model.MeasureValueType;
import com.amazonaws.services.timestreamwrite.model.TimeUnit;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

import java.time.format.DateTimeFormatter;

public class DepartmentAggToTimestreamPoint extends RichMapFunction<DepartmentsAggEvent, TimestreamPoint> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }
    @Override
    public TimestreamPoint map(DepartmentsAggEvent departmentsAggEvent) throws Exception {
        TimestreamPoint dataPoint = new TimestreamPoint();
        dataPoint.setMeasureName("Count");
        dataPoint.setMeasureValue(String.valueOf(departmentsAggEvent.getDepartmentCount()));
        dataPoint.setMeasureValueType(MeasureValueType.BIGINT);
        dataPoint.setTime(departmentsAggEvent.getWindowEndTime());
        dataPoint.setTimeUnit(TimeUnit.MILLISECONDS.toString());
        dataPoint.addDimension("ProductName",String.valueOf(departmentsAggEvent.getDepartmentName()));
/*        dataPoint.addDimension("windowStartTime",
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(departmentsAggEvent.getWindowBeginTime())
                        , ZoneId.systemDefault()).format(FORMATTER));
        dataPoint.addDimension("windowEndTime",
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(departmentsAggEvent.getWindowEndTime())
                        , ZoneId.systemDefault()).format(FORMATTER));*/

        return dataPoint;
    }
}
