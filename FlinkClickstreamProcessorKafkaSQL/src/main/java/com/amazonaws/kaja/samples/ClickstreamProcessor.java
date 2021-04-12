package com.amazonaws.kaja.samples;

import com.amazonaws.kaja.samples.configs.ElasticsearchConfig;
import com.amazonaws.kaja.samples.configs.TopicConfig;
import com.amazonaws.kaja.samples.udfs.BigIntToTS;
import com.amazonaws.kaja.samples.udfs.PercentageUserSession;
import com.amazonaws.kaja.samples.udfs.TimestampToBigInt;
import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.ExecutionCheckpointingOptions;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;


public class ClickstreamProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ClickstreamProcessor.class);
    private static final List<String> MANDATORY_PARAMETERS = Arrays.asList("BootstrapServers", "ZookeeperConnect", "ElasticsearchEndpoint");

    public static void main(String[] args) throws Exception {

        //Setting up the ExecutionEnvironment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings environmentSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        final StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment.create(env, environmentSettings);
        registerUdfs(streamTableEnvironment);

        Map<String, Properties> applicationProperties;

        if (env instanceof LocalStreamEnvironment) {
            applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties(Objects.requireNonNull(ClickstreamProcessor.class.getClassLoader().getResource("KDAApplicationProperties.json")).getPath());
        } else {
            applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();
        }

        if (applicationProperties == null) {
            LOG.error("Unable to load application properties from the Kinesis Analytics Runtime. Exiting.");
            return;
        }

        Properties flinkProperties = applicationProperties.get("FlinkApplicationProperties");

        if (flinkProperties == null) {
            LOG.error("Unable to load FlinkApplicationProperties properties from the Kinesis Analytics Runtime. Exiting.");
            return;
        }

        if (!flinkProperties.keySet().containsAll(MANDATORY_PARAMETERS)) {
            LOG.error("Missing mandatory parameters. Expected '{}' but found '{}'. Exiting.",
                    String.join(", ", MANDATORY_PARAMETERS),
                    flinkProperties.keySet());

            return;
        }

        setupEnvironment(env, streamTableEnvironment);
        setupSqlTables(streamTableEnvironment, flinkProperties);

        final StatementSet sqlSet = streamTableEnvironment.createStatementSet();
        sqlSet.addInsertSql(ClickStreamQueries.userSessionCount("clickevent", "UserSessionCount"));
        sqlSet.addInsertSql(ClickStreamQueries.userSessionDetails("clickevent", "UserSessionDetails"));
        sqlSet.addInsertSql(ClickStreamQueries.departmentCount("clickevent", "DepartmentCount"));

        LOG.info("Starting execution..");
        sqlSet.execute();

    }

    private static void setupEnvironment(StreamExecutionEnvironment env, StreamTableEnvironment streamTableEnvironment) {
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        if (env instanceof LocalStreamEnvironment) {
            env.setParallelism(1);
        }
        streamTableEnvironment.getConfig().getConfiguration().set(ExecutionCheckpointingOptions.CHECKPOINTING_MODE, CheckpointingMode.EXACTLY_ONCE);
        streamTableEnvironment.getConfig().getConfiguration().set(ExecutionCheckpointingOptions.CHECKPOINTING_INTERVAL, Duration.ofSeconds(60 * 3));
        streamTableEnvironment.getConfig().getConfiguration().set(ExecutionCheckpointingOptions.ENABLE_UNALIGNED, true);
        streamTableEnvironment.getConfig().setIdleStateRetentionTime(Time.hours(12), Time.hours(24));
    }


    private static void setupSqlTables(StreamTableEnvironment streamTableEnvironment, Properties flinkProperties) {
        streamTableEnvironment.executeSql(SqlTables.clickevent(new TopicConfig(flinkProperties.getProperty("Topic", "ExampleTopic"), flinkProperties.getProperty("GroupId", "flink-clickstream-sql-processor"), flinkProperties.getProperty("BootstrapServers"), flinkProperties.getProperty("SchemaUrl"))));
        streamTableEnvironment
                .executeSql(
                        SqlTables.userSessionCount("UserSessionCount",
                                new ElasticsearchConfig(
                                        flinkProperties.getProperty("ElasticsearchEndpoint")
                                        , "user_session_counts",
                                        "user_session_counts")));
        streamTableEnvironment
                .executeSql(
                        SqlTables.userSessionDetails("UserSessionDetails",
                                new ElasticsearchConfig(
                                        flinkProperties.getProperty("ElasticsearchEndpoint")
                                        , "user_session_details"
                                        ,"user_session_details")));
        streamTableEnvironment
                .executeSql(
                        SqlTables.DepartmentCount("DepartmentCount",
                                new ElasticsearchConfig(
                                        flinkProperties.getProperty("ElasticsearchEndpoint")
                                        , "departments_count"
                                        ,"departments_count")));
        streamTableEnvironment
                .executeSql(
                        SqlTables.userSessionCount("UserSessionCountTopic",
                                new TopicConfig(flinkProperties.getProperty("userSessionsAggregatesWithOrderCheckout_Topic")
                                        ,flinkProperties.getProperty("GroupId", "flink-clickstream-sql-processor")
                                        , flinkProperties.getProperty("BootstrapServers"), flinkProperties.getProperty("SchemaUrl")
                                )
                        ));
        streamTableEnvironment
                .executeSql(
                        SqlTables.userSessionDetails("UserSessionDetailsTopic",
                                new TopicConfig(flinkProperties.getProperty("clickEventsUserIdAggResult_Topic")
                                        ,flinkProperties.getProperty("GroupId", "flink-clickstream-sql-processor")
                                        , flinkProperties.getProperty("BootstrapServers"), flinkProperties.getProperty("SchemaUrl")
                                )
                        )
                );
        streamTableEnvironment.executeSql(
                SqlTables.DepartmentCount("DepartmentCountTopic",
                        new TopicConfig(flinkProperties.getProperty("DepartmentsAgg_Topic")
                                ,flinkProperties.getProperty("GroupId", "flink-clickstream-sql-processor")
                                , flinkProperties.getProperty("BootstrapServers"), flinkProperties.getProperty("SchemaUrl")
                        )
                )
        );

    }

    private static void registerUdfs(final StreamTableEnvironment env) {
        env.createFunction("percentageSession", PercentageUserSession.class);
        env.createFunction("biginttots", BigIntToTS.class);
        env.createFunction("tstobigint", TimestampToBigInt.class);
    }
}
