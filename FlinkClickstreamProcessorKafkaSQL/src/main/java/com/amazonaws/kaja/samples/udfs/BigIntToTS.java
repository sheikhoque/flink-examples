package com.amazonaws.kaja.samples.udfs;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.ScalarFunction;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BigIntToTS extends ScalarFunction {
    public static final long UNIX_EPOCH_START_MS = 0L;
    public static final long MAX_TIMESTAMP = 253_402_243_200_000L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public Timestamp eval(final Long ms){
        final long finalMs = (ms == null || ms < UNIX_EPOCH_START_MS || ms > MAX_TIMESTAMP)? UNIX_EPOCH_START_MS:ms;
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(finalMs), ZoneId.systemDefault());
        return Timestamp.valueOf(localDateTime.format(FORMATTER));
    }
}
