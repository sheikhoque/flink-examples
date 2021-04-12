package com.amazonaws.kaja.samples;

import com.amazonaws.kaja.samples.configs.ConnectorConfig;
import com.amazonaws.kaja.samples.configs.ElasticsearchConfig;
import com.amazonaws.kaja.samples.configs.TopicConfig;

import java.util.Properties;

public class SqlTables {
    public static final String ONE_MINUTE_INTERVAL = "interval '1' minute";

    private SqlTables(){}

    public static String clickevent(final TopicConfig topicConfig){
        return "CREATE TABLE clickevent (\n" +
                "  ip string \n" +
                " ,eventtimestamp bigint \n" +
                " ,event_timestamp as biginttots(eventtimestamp) \n" +
                " ,devicetype string \n" +
                " ,event_type string \n" +
                " ,product_type string \n" +
                " ,userid int \n" +
                " ,globalseq bigint \n" +
                " ,prevglobalseq bigint \n" +
                " ,watermark for event_timestamp as event_timestamp - " + ONE_MINUTE_INTERVAL+"\n" +
                " ) WITH ( \n " + topicConfig.toDDL() +
                " )";

    }

    public static String userSessionCount(String tableName, ConnectorConfig connectorConfig) {
        return "CREATE TABLE "+tableName+ " (\n" +
                "  userSessionCount bigint \n" +
                " ,userSessionCountWithOrderCheckout bigint \n" +
                " ,percentSessionswithBuy double \n" +
                " ,windowBeginTime bigint \n" +
                " ,windowEndTime bigint \n" +
                " ) WITH ( \n " + connectorConfig.toDDL() +
                " )";
    }

    public static String userSessionDetails(String tableName, ConnectorConfig connectorConfig) {
        return "CREATE TABLE "+ tableName+" (\n" +
                "  userId int \n" +
                " ,eventCount bigint \n" +
                " ,deptList string \n" +
                " ,windowBeginTime bigint \n" +
                " ,windowEndTime bigint \n" +
                " ) WITH ( \n " + connectorConfig.toDDL() +
                " )";
    }

    public static String DepartmentCount(String tableName, ConnectorConfig connectorConfig) {
        return "CREATE TABLE "+tableName+" (\n" +
                "  departmentName string \n" +
                " ,departmentCount bigint \n" +
                " ,windowBeginTime bigint \n" +
                " ,windowEndTime bigint \n" +
                " ) WITH ( \n " + connectorConfig.toDDL() +
                " )";
    }

}
