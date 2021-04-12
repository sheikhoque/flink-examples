package com.amazonaws.kaja.samples;

import com.amazonaws.kaja.samples.sink.timestream.TimestreamPoint;
import com.amazonaws.kaja.samples.sink.timestream.TimestreamSink;
import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.AWSConfigConstants;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samples.clickstream.avro.ClickEvent;

import java.time.Duration;
import java.util.*;


public class ClickstreamProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ClickstreamProcessor.class);
    private static final List<String> MANDATORY_PARAMETERS = Arrays.asList("BootstrapServers", "SchemaRegistryUrl");
    private static transient Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();


    public static <T> String toJson(T objToConvert){

        return gson.toJson(objToConvert);
    }

    private static <T> String tupleToString(T tupleToConvert) {
         if (tupleToConvert instanceof Tuple){
             List<String> returnVal = new ArrayList<>();
             Integer arity = ((Tuple) tupleToConvert).getArity();
             for (Integer i = 0; i < arity; i++){
                 returnVal.add(((Tuple) tupleToConvert).getField(i).toString());
             }
             return  String.join(",", returnVal);
         }
        return null;
    }
    public static DataStream<String> createKinesisSource(StreamExecutionEnvironment env, String region, String topic) throws Exception {
        //set Kinesis consumer properties
        Properties kinesisConsumerConfig = new Properties();
        //set the region the Kinesis stream is located in
        kinesisConsumerConfig.setProperty(AWSConfigConstants.AWS_REGION,
                region);
        //obtain credentials through the DefaultCredentialsProviderChain, which includes the instance metadata
        kinesisConsumerConfig.setProperty(AWSConfigConstants.AWS_CREDENTIALS_PROVIDER, "AUTO");
        String adaptiveReadSettingStr = "false";
        if(adaptiveReadSettingStr.equals("true")) {
            kinesisConsumerConfig.setProperty(ConsumerConfigConstants.SHARD_USE_ADAPTIVE_READS, "true");
        } else {
            //poll new events from the Kinesis stream once every second
            kinesisConsumerConfig.setProperty(ConsumerConfigConstants.SHARD_GETRECORDS_INTERVAL_MILLIS,
                    "1000");
            // max records to get in shot
            kinesisConsumerConfig.setProperty(ConsumerConfigConstants.SHARD_GETRECORDS_MAX,
                    "10000");
        }

        //create Kinesis source
        DataStream<String> kinesisStream = env.addSource(new FlinkKinesisConsumer<>(
                //read events from the Kinesis stream passed in as a parameter
                topic,
                //deserialize events with EventSchema
                new SimpleStringSchema(),
                //using the previously defined properties
                kinesisConsumerConfig
        )).name("KinesisSource");

        return kinesisStream;
    }

    public static void main(String[] args) throws Exception {

        //Setting up the ExecutionEnvironment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //Setting TimeCharacteristic
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Map<String, Properties> applicationProperties;

        if (env instanceof LocalStreamEnvironment) {
            applicationProperties  = KinesisAnalyticsRuntime.getApplicationProperties(Objects.requireNonNull(ClickstreamProcessor.class.getClassLoader().getResource("KDAApplicationProperties.json")).getPath());
            //Setting parallelism in code. When running on my laptop I was getting out of file handles error, so reduced parallelism, but increase it on KDA
            env.setParallelism(1);
            //Setting the Checkpoint interval. The Default for KDA App is 60,000 (or 1 min).
            // Here I'm setting it to 5 secs in the code which will override the KDA app setting
            env.enableCheckpointing(20000L);

        } else {
            applicationProperties  = KinesisAnalyticsRuntime.getApplicationProperties();
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

        if (! flinkProperties.keySet().containsAll(MANDATORY_PARAMETERS)) {
            LOG.error("Missing mandatory parameters. Expected '{}' but found '{}'. Exiting.",
                    String.join(", ", MANDATORY_PARAMETERS),
                    flinkProperties.keySet());

            return;
        }

        //Setting properties for Apache kafka (MSK)
        Properties kafkaConfig = new Properties();
        kafkaConfig.setProperty("bootstrap.servers", flinkProperties.getProperty("BootstrapServers"));
        kafkaConfig.setProperty("group.id", flinkProperties.getProperty("GroupId", "flink-clickstream-processor"));


        WatermarkStrategy watermarkStrategy = WatermarkStrategy
                .forBoundedOutOfOrderness(Duration.ofSeconds(20)).withIdleness(Duration.ofMinutes(1));
        //Setting the source for Apache kafka (MSK) and assigning Timestamps and watermarks for Event Time
        DataStream<ClickEvent> clickEvents = env
                .addSource(
                        new FlinkKafkaConsumer<>(
                                flinkProperties.getProperty("Topic", "ExampleTopic"),
                                ConfluentRegistryAvroDeserializationSchema.forSpecific(ClickEvent.class, flinkProperties.getProperty("SchemaRegistryUrl")),
                                kafkaConfig)
                                .setStartFromEarliest()
                                //.assignTimestampsAndWatermarks(new ClickEventTimestampWatermarkGenerator())
                        ).uid("source").name("source");

        String region = flinkProperties.getProperty("Region", "us-west-2");
        String database = flinkProperties.getProperty("TimestreamDB", "clickstream");
        String table = flinkProperties.getProperty("UserSessionTable", "usersessioncount");
        int batchSize = Integer.parseInt(flinkProperties.getProperty("TimestreamBatchSize", "100"));
/*
        TimestreamInitializer timestreamInitializer = new TimestreamInitializer(region);
        timestreamInitializer.createDatabase(database);
        timestreamInitializer.createTable(database, table);
*/


        DataStream<TimestreamPoint> userSessionsAggregates = clickEvents
                .keyBy(event -> event.getUserid())
                .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
                .aggregate(new UserAggregate(), new UserAggWindowFunction()).uid("useridsessionevent").name("useridsessionevent");
        /* DataStream<String> input = createKinesisSource(env, region, "ExampleInputStream");
        DataStream<TimestreamPoint> mappedInput =
                input.map(new JsonToTimestreamPayloadFn()).name("MaptoTimestreamPayload");
*/

/*
        DataStream<String> input = env
                .addSource(
                        new FlinkKafkaConsumer<>(
                                flinkProperties.getProperty("Topic", "test"),
                                new SimpleStringSchema(),
                                kafkaConfig)
                                .setStartFromEarliest()
                        //.assignTimestampsAndWatermarks(new ClickEventTimestampWatermarkGenerator())
                ).uid("source").name("source");

        DataStream<TimestreamPoint> mappedInput =
                        input.map(new JsonToTimestreamPayloadFn()).name("MaptoTimestreamPayload");
*/

        SinkFunction<TimestreamPoint> sink =new TimestreamSink(
                region,database, table,batchSize);
        userSessionsAggregates.addSink(sink).uid("timestreamsink").name("timestreamsink");

        /*

        DataStream<DepartmentsAggEvent> departmentsAgg = userSessionsAggregates
                .flatMap(new DepartmentsFlatMap())
                .keyBy(event -> event.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .reduce(new DepartmentsAggReduceFunction(), new DepartmentsAggWindowFunction())
                ;


        departmentsAgg
                .map( new DepartmentAggToTimestreamPoint())
                .addSink(new TimestreamSink(
                        flinkProperties.getProperty("Region", "us-west-2")
                        , flinkProperties.getProperty("TimestreamDB", "clickstream-flink")
                        , flinkProperties.getProperty("DepartmentAggTable", "product_type_details")
                        ,Integer.parseInt(flinkProperties.getProperty("TimestreamBatchSize", "100"))));
        DataStream<UserIdAggEvent> clickEventsUserIdAggResult =  userSessionsAggregates
                .keyBy(event -> event.getEventKey())
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .aggregate(new UserSessionAggregates(), new UserSessionWindowFunction());

        clickEventsUserIdAggResult
                .map( new UserIdToTimestreamPoint())
                .addSink(new TimestreamSink(
                        flinkProperties.getProperty("Region", "us-west-2")
                        , flinkProperties.getProperty("TimestreamDB", "clickstream-flink")
                        , flinkProperties.getProperty("UserAggTable", "user_session_count")
                        ,Integer.parseInt(flinkProperties.getProperty("TimestreamBatchSize", "100"))));
*/

        LOG.info("Starting execution..");
        env.execute();

    }
}
