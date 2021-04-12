package com.amazonaws.kaja.samples;

import com.amazonaws.kaja.samples.configs.TopicConfig;
import org.apache.commons.lang3.StringUtils;

public class ClickStreamQueries {
    public static final String ONE_MINUTE_INTERVAL = "interval '1' minute";

    public static String userSessionCount(String srcTable, String destTable) {
        return " insert into "+destTable+" \n" +
                " select count(*) as userSessionCount \n" +
                " ,sum((case when event_type = 'order_checkout' then 1 else 0 end)) as userSessionCountWithOrderCheckout \n" +
                " , percentageSession(count(*), sum((case when event_type = 'order_checkout' then 1 else 0 end))) \n" +
                " , tstobigint(tumble_start(event_timestamp,"+ONE_MINUTE_INTERVAL+")) \n" +
                " , tstobigint(tumble_end(event_timestamp,"+ONE_MINUTE_INTERVAL+")) \n" +
                " from "+srcTable+ "\n" +
                " group by tumble(event_timestamp,"+ONE_MINUTE_INTERVAL+")";
    }

    public static String userSessionDetails(String srcTable, String destTable) {
        return " insert into "+destTable+" \n" +
                " select userid, count(*) as eventCount, LISTAGG(product_type) as deptList \n" +
                " , tstobigint(tumble_start(event_timestamp,"+ONE_MINUTE_INTERVAL+")) \n" +
                " , tstobigint(tumble_end(event_timestamp,"+ONE_MINUTE_INTERVAL+")) \n" +
                " from "+srcTable+"\n" +
                " group by tumble(event_timestamp,"+ONE_MINUTE_INTERVAL+"), userid";
    }

    public static String departmentCount(String srcTable, String destTable) {
        return " insert into "+destTable+" \n" +
                " select product_type as departmentName, count(*) as departmentCount \n" +
                " , tstobigint(tumble_start(event_timestamp,"+ONE_MINUTE_INTERVAL+")) \n" +
                " , tstobigint(tumble_end(event_timestamp,"+ONE_MINUTE_INTERVAL+")) \n" +
                " from "+srcTable+"\n" +
                " group by tumble(event_timestamp,"+ONE_MINUTE_INTERVAL+ "),product_type";
    }

}
