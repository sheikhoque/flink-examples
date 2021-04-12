package com.att.dtv.kda.model.app;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import software.amazon.kinesis.connectors.flink.config.ConsumerConfigConstants;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class ApplicationProperties implements Serializable {

    public static final String CUSTOM_KINESIS_SHARD_ASSIGNER="RoundRobin";

    protected final URI sourceEndpoint;
    protected final URI intervalStatsEndpoint;
    protected final long controlEndpointRescanInterval;
    protected final String intervalStatsIndexName;
    protected final List<Integer> intervalLengths;
    protected final String region;
    protected final int parallelism;
    protected final String streamStartingPosition;
    protected final String version;
    protected final int checkpointInterval;
    protected final int minPauseBetweenCheckpoints;
    protected final int checkpointTimeout;
    protected final int maxConcurrentCheckpoints;
    protected final CheckpointingMode checkpointMode;
    protected final CheckpointConfig.ExternalizedCheckpointCleanup checkpointCleanup;
    protected final String longerIntervalIndexPrefix;
    protected final String shorterIntervalIndexPrefix;
    protected final URI sessionStatsEndpoint;
    protected final String initialTimestamp;
    protected final String elasticSearchUserName;
    protected final String elasticSearchPassword;
    protected final int elasticsearchMaxFlushSizeMB;
    protected final int elasticsearchBackoffRetryNumber;
    protected final int elasticsearchBackoffDelayInMillis;
    protected final int elasticsearchFlushMaxActions;
    protected final int autoWatermarkInterval;
    protected final String kinesisShardAssigner;
    protected final String kdsEFOConsumerName;
    protected final String kdsEFOConsumerARN;
    protected final String kdsEFORegType;
    protected final String esIndexPeriod;
    protected final String kplKinesisEndpoint;
    protected final String kplMaxConnections;
    protected final String kplLogLevel;
    protected final String kplMetricsGranularity;
    protected final String kplAggregationMaxSize;
    protected final String kplCollectionMaxCount;
    protected final String kplCollectionMaxSize;
    protected final String kplConnectTimeout;
    protected final String kplRateLimit;
    protected final String kplRecordMaxBufferedTime;
    protected final String kplRecordTtl;
    protected final String kplRequestTimeout;
    protected final String kplThreadPoolSize;
    protected final String kplAggregationEnabled;
    protected final int kplQueueLimit;

    public ApplicationProperties(Properties appProperties) throws URISyntaxException {
    // use a specific region
        this.region = getProperty(appProperties,"region", "us-west-2");

    // get restart on lost offset:
        this.streamStartingPosition = getProperty(appProperties, ConsumerConfigConstants.STREAM_INITIAL_POSITION, ConsumerConfigConstants.InitialPosition.TRIM_HORIZON.name());

        this.version = getProperty(appProperties,"version", "x.x.x");
        this.parallelism = getProperty(appProperties,"parallelism", -1);
        this.checkpointInterval = getProperty(appProperties,"checkpointInterval", 60000);
        this.minPauseBetweenCheckpoints = getProperty(appProperties,"minPauseBetweenCheckpoints", 6000);
        this.checkpointTimeout = getProperty(appProperties,"checkpointTimeout", 30000);
        this.maxConcurrentCheckpoints = getProperty(appProperties,"maxConcurrentCheckpoints", 1);
        this.controlEndpointRescanInterval = getProperty(appProperties, "controlEndpointRescanInterval", -1);

        CheckpointingMode checkpointingModeTmp;
        if ("EXACTLY_ONCE".equals(getProperty(appProperties,"checkpointMode", "AT_LEAST_ONCE")))
            checkpointingModeTmp = CheckpointingMode.EXACTLY_ONCE;
        else
            checkpointingModeTmp = CheckpointingMode.AT_LEAST_ONCE;
        this.checkpointMode = checkpointingModeTmp;

        CheckpointConfig.ExternalizedCheckpointCleanup checkpointCleanupTmp;
        if ("DELETE_ON_CANCELLATION".equals(getProperty(appProperties,"checkpointCleanup", "RETAIN_ON_CANCELLATION")))
            checkpointCleanupTmp = CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION;
        else
            checkpointCleanupTmp = CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION;
        this.checkpointCleanup = checkpointCleanupTmp;

        this.sourceEndpoint = new URI(checkNotBlank(appProperties,"sourceEndpoint", ""));

        this.intervalStatsEndpoint = new URI(checkNotBlank(appProperties,"intervalStatsEndpoint", ""));

        this.intervalStatsIndexName = getProperty(appProperties,"ElasticsearchIndex", "sessions");
        this.shorterIntervalIndexPrefix = this.intervalStatsIndexName;
        this.intervalLengths = Arrays.stream(checkNotBlank(appProperties,"intervalLength", "").split(",")).map(x-> Integer.parseInt(x)).collect(Collectors.toList());
        this.longerIntervalIndexPrefix = this.intervalLengths.size()==2?"longer_interval_"+this.intervalStatsIndexName+"_"+"interval"+"_"+intervalLengths.get(1)+"min":"";

        this.sessionStatsEndpoint = new URI(getProperty(appProperties,"sessionStatsEndpoint", ""));

        this.initialTimestamp = getProperty(appProperties, "flink.stream.initpos.timestamp", "");

        this.elasticSearchUserName = getProperty(appProperties, "es.username","");
        this.elasticSearchPassword = getProperty(appProperties, "es.password","");

        this.elasticsearchBackoffRetryNumber = getProperty(appProperties, "es.flush.backoff.retrynumber", 8);
        this.elasticsearchMaxFlushSizeMB = getProperty(appProperties, "es.flush.size.mb", 1);
        this.elasticsearchBackoffDelayInMillis = getProperty(appProperties, "es.flush.backoff.delay.millis", 1000);
        this.elasticsearchFlushMaxActions = getProperty(appProperties, "es.flush.max.actions", 10000);

        this.autoWatermarkInterval = getProperty(appProperties, "flink.watermark.interval", 1000);

        this.kplLogLevel = getProperty(appProperties, "kpl.loglevel", "error");
        this.kplAggregationEnabled = getProperty(appProperties, "kpl.AggregationEnabled", "true");
        this.kplMetricsGranularity = getProperty(appProperties, "kpl.metrics.granularity", "stream");
        this.kplMaxConnections  = getProperty(appProperties, "kpl.MaxConnections", "24");
        this.kplAggregationMaxSize  = getProperty(appProperties, "kpl.AggregationMaxSize", "51200");
        this.kplCollectionMaxCount = getProperty(appProperties, "kpl.CollectionMaxCount", "500");
        this.kplCollectionMaxSize = getProperty(appProperties, "kpl.CollectionMaxSize", "5242880");
        this.kplConnectTimeout = getProperty(appProperties, "kpl.ConnectTimeout", "6000");
        this.kplRateLimit = getProperty(appProperties, "kpl.RateLimit", "100");
        this.kplRecordMaxBufferedTime = getProperty(appProperties, "kpl.RecordMaxBufferedTime", "100");
        this.kplRecordTtl = getProperty(appProperties, "kpl.RecordTtl", "30000");
        this.kplRequestTimeout = getProperty(appProperties, "kpl.RequestTimeout", "6000");
        this.kplThreadPoolSize = getProperty(appProperties, "kpl.ThreadPoolSize", "10");
        this.kplKinesisEndpoint = getProperty(appProperties, "kpl.endpoint", "kinesis.us-west-2.amazonaws.com");
        this.kplQueueLimit = getProperty(appProperties, "kpl.QueueLimit", Integer.MAX_VALUE);

        this.kinesisShardAssigner = getProperty(appProperties, "kinesis.shard.assigner",StringUtils.EMPTY);
        this.kdsEFOConsumerName = getProperty(appProperties, "kinesis.efo.consumer.name","");
        this.kdsEFOConsumerARN = getProperty(appProperties, "kinesis.efo.consumer.arn","");
        this.kdsEFORegType = getProperty(appProperties, "kinesis.efo.registration.type","NONE");

        this.esIndexPeriod = getProperty(appProperties, "es.index.period","daily");
    }

    protected static String getProperty(Properties properties, String name, String defaultValue) {
        String value = properties.getProperty(name);
        if (StringUtils.isBlank(value)) {
            value = defaultValue;
        }
        return value;
    }

    protected static int getProperty(Properties properties, String name, int defaultValue) {
        String value = properties.getProperty(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected static String checkNotBlank(Properties properties, String name, String defaultValue) throws IllegalArgumentException {
        String value = getProperty(properties, name, defaultValue);
        if (StringUtils.isBlank(value))
            throw new IllegalArgumentException(name + " should be pass using AppProperties config within create-application API call");
        return value;
    }

    public URI getSourceEndpoint() {
        return sourceEndpoint;
    }

    public URI getIntervalStatsEndpoint() {
        return intervalStatsEndpoint;
    }

    public long getControlEndpointRescanInterval() {
        return controlEndpointRescanInterval;
    }

    public String getIntervalStatsIndexName() {
        return intervalStatsIndexName;
    }

    public List<Integer> getIntervalLengths() {
        return intervalLengths;
    }

    public String getRegion() {
        return region;
    }

    public String getStreamStartingPosition() {
        return streamStartingPosition;
    }

    public String getVersion() {
        return version;
    }

    public int getCheckpointInterval() {
        return checkpointInterval;
    }

    public int getMinPauseBetweenCheckpoints() {
        return minPauseBetweenCheckpoints;
    }

    public int getCheckpointTimeout() {
        return checkpointTimeout;
    }

    public int getMaxConcurrentCheckpoints() {
        return maxConcurrentCheckpoints;
    }

    public CheckpointingMode getCheckpointMode() {
        return checkpointMode;
    }

    public CheckpointConfig.ExternalizedCheckpointCleanup getCheckpointCleanup() {
        return checkpointCleanup;
    }

    public String getLongerIntervalIndexPrefix() {
        return longerIntervalIndexPrefix;
    }

    public String getShorterIntervalIndexPrefix() {
        return shorterIntervalIndexPrefix;
    }

    public URI getSessionStatsEndpoint() {
        return sessionStatsEndpoint;
    }

    public String getInitialTimestamp() {
        return initialTimestamp;
    }

    public String getElasticSearchUserName() {
        return elasticSearchUserName;
    }

    public String getElasticSearchPassword() {
        return elasticSearchPassword;
    }

    public int getElasticsearchMaxFlushSizeMB() {
        return elasticsearchMaxFlushSizeMB;
    }

    public int getElasticsearchBackoffRetryNumber() {
        return elasticsearchBackoffRetryNumber;
    }

    public int getElasticsearchBackoffDelayInMillis() {
        return elasticsearchBackoffDelayInMillis;
    }

    public int getElasticsearchFlushMaxActions() {
        return elasticsearchFlushMaxActions;
    }

    public int getAutoWatermarkInterval() {
        return autoWatermarkInterval;
    }

    public String getKinesisShardAssigner() {
        return kinesisShardAssigner;
    }

    public String getKdsEFOConsumerName() {
        return kdsEFOConsumerName;
    }

    public String getKdsEFOConsumerARN() {
        return kdsEFOConsumerARN;
    }

    public String getKdsEFORegType() {
        return kdsEFORegType;
    }

    public String getEsIndexPeriod() {
        return esIndexPeriod;
    }

    public String getKplKinesisEndpoint() {
        return kplKinesisEndpoint;
    }

    public String getKplMaxConnections() {
        return kplMaxConnections;
    }

    public String getKplLogLevel() {
        return kplLogLevel;
    }

    public String getKplMetricsGranularity() {
        return kplMetricsGranularity;
    }

    public String getKplAggregationMaxSize() {
        return kplAggregationMaxSize;
    }

    public String getKplCollectionMaxCount() {
        return kplCollectionMaxCount;
    }

    public String getKplCollectionMaxSize() {
        return kplCollectionMaxSize;
    }

    public String getKplConnectTimeout() {
        return kplConnectTimeout;
    }

    public String getKplRateLimit() {
        return kplRateLimit;
    }

    public String getKplRecordMaxBufferedTime() {
        return kplRecordMaxBufferedTime;
    }

    public String getKplRecordTtl() {
        return kplRecordTtl;
    }

    public String getKplRequestTimeout() {
        return kplRequestTimeout;
    }

    public String getKplThreadPoolSize() {
        return kplThreadPoolSize;
    }

    public String getKplAggregationEnabled() {
        return kplAggregationEnabled;
    }

    public int getKplQueueLimit() {
        return kplQueueLimit;
    }

    public int getParallelism() {
        return parallelism;
    }
}
