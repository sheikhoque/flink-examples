package com.amazonaws.kaja.samples.udfs;

import org.apache.flink.table.functions.ScalarFunction;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class TimestampToBigInt extends ScalarFunction {
    public static final long UNIX_EPOCH_START_MS = 0L;
    public static final long MAX_TIMESTAMP = 253_402_243_200_000L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public Long eval(final Timestamp timestamp){
        return timestamp==null?UNIX_EPOCH_START_MS:timestamp.getTime();
    }
}
