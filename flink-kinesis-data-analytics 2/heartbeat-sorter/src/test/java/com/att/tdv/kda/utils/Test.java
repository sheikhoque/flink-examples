package com.att.tdv.kda.utils;


import com.att.dtv.kda.utils.HeartbeatSorter;
import org.apache.flink.core.fs.Path;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test {

    @org.junit.jupiter.api.Test @DisplayName("Parameters") @Disabled
    void testGzip(TestInfo testInfo) throws Exception {
        String testName = testInfo.getDisplayName();

        HeartbeatSorter.main(new String[] {"-g", testName});

    }

    @org.junit.jupiter.api.Test @Disabled
    void getFilePath() {
        HeartbeatSorter o = new HeartbeatSorter();
        List<Path> filePaths = o.getFilePath(LocalDateTime.now().minusDays(2), LocalDateTime.now(), 24, "s3://bucket-name/raw-data", "year=!{timestamp:YYYY}/month=!{timestamp:MM}/day=!{timestamp:dd}/");
        filePaths.forEach(f -> System.out.println(f));
    }

    @org.junit.jupiter.api.Test @Disabled
    void dateTimeFormatter() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = "2019-01-01";
        LocalDateTime dt = LocalDate.parse(date, dateTimeFormatter).atStartOfDay();
        System.out.println(dt);
        System.out.println((long)1.2d);
    }
}