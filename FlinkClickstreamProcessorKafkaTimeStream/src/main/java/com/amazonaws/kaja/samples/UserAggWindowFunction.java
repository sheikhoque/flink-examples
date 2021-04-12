package com.amazonaws.kaja.samples;

import com.amazonaws.kaja.samples.sink.timestream.TimestreamPoint;
import com.amazonaws.services.timestreamwrite.model.MeasureValueType;
import com.amazonaws.services.timestreamwrite.model.TimeUnit;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class UserAggWindowFunction extends ProcessWindowFunction<ClickEventAggregate, TimestreamPoint, Integer, TimeWindow> {
    @Override
    public void process(Integer integer, Context context, Iterable<ClickEventAggregate> elements, Collector<TimestreamPoint> out) throws Exception {
        ClickEventAggregate element = elements.iterator().next();

        //out.collect(new Tuple7<>(element.f0, element.f1, element.f2, element.f3, element.f4, new Timestamp(context.window().getStart()), new Timestamp(context.window().getEnd())));
        TimestreamPoint dataPoint = new TimestreamPoint();
        dataPoint.setMeasureName("EventCount");
        dataPoint.setMeasureValue(String.valueOf(element.getEventCount()));
        dataPoint.setMeasureValueType(MeasureValueType.BIGINT);
        dataPoint.setTime(context.window().getEnd());
        dataPoint.setTimeUnit(TimeUnit.SECONDS.toString());
        dataPoint.addDimension("Criteria","UserSessionCount");
        out.collect(dataPoint);

    }
}
