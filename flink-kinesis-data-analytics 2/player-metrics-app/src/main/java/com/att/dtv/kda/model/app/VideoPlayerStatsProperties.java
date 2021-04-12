package com.att.dtv.kda.model.app;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


public class VideoPlayerStatsProperties extends ApplicationProperties {

    public static final String CUSTOM_KINESIS_SHARD_ASSIGNER="RoundRobin";

    private final int sessionTimeout;
    private final URI appControlEndpoint;
    private final List<Integer> intervalLengths;
    private final URI mapFileLocation;
    private final URI dmaFileLocation;
    private final String geoIpCityDBLocation;
    private final String geoIpISPDBLocation;
    private final int heartbeatValidityOffset;
    private final long sessionLifetime;
    private final long timeTravellersTolerancePeriod;

    public VideoPlayerStatsProperties(Properties appProperties) throws URISyntaxException {
        super(appProperties);
        
        this.mapFileLocation = new URI(checkNotBlank(appProperties,"cdn.map.location", ""));

        this.dmaFileLocation = new URI(checkNotBlank(appProperties,"dma.map.location", ""));

        this.geoIpCityDBLocation = checkNotBlank(appProperties,"maxMindCityEndpoint", "");

        this.geoIpISPDBLocation = checkNotBlank(appProperties,"maxMindISPEndpoint", "");

        this.intervalLengths = Arrays.stream(checkNotBlank(appProperties,"intervalLength", "").split(",")).map(x-> Integer.parseInt(x)).collect(Collectors.toList());

        this.appControlEndpoint = new URI(checkNotBlank(appProperties,"appControlEndpoint", ""));

        this.sessionTimeout = getProperty(appProperties, "app.session.timeout", 4*60*1000);

        int intValue;
        String value = getProperty(appProperties,"heartbeatValidityOffset", "365");
        try {
            intValue = Integer.parseInt(value.trim());
        }catch(Exception e){
            e.printStackTrace();
            intValue = 365;
        }
        this.heartbeatValidityOffset = intValue;

        this.sessionLifetime = getProperty(appProperties, "session.lifetime.hours",504)*60*60*1000;

        this.timeTravellersTolerancePeriod = getProperty(appProperties, "timetraveller.tolerance.hours",0)*60*60*1000;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public URI getSessionStatsEndpoint() {
        return sessionStatsEndpoint;
    }

    public URI getAppControlEndpoint() {
        return appControlEndpoint;
    }

    @Override
    public List<Integer> getIntervalLengths() {
        return intervalLengths;
    }

    public URI getMapFileLocation() {
        return mapFileLocation;
    }

    public URI getDmaFileLocation() {
        return dmaFileLocation;
    }

    public String getGeoIpCityDBLocation() {
        return geoIpCityDBLocation;
    }

    public String getGeoIpISPDBLocation() {
        return geoIpISPDBLocation;
    }

    public long getSessionLifetime() {
        return sessionLifetime;
    }

    public long getTimeTravellersTolerancePeriod() {
        return timeTravellersTolerancePeriod;
    }

    @Override
    public String toString() {
        return "VideoPlayerStatsProperties{" +
                "sourceEndpoint=" + sourceEndpoint +
                ", intervalStatsEndpoint=" + intervalStatsEndpoint +
                ", sessionStatsEndpoint=" + sessionStatsEndpoint +
                ", appControlEndpoint=" + appControlEndpoint +
                ", controlEndpointRescanInterval=" + controlEndpointRescanInterval +
                ", intervalStatsIndexName='" + intervalStatsIndexName + '\'' +
                ", intervalLengths=" + intervalLengths +
                ", region='" + region + '\'' +
                ", streamStartingPosition='" + streamStartingPosition + '\'' +
                ", mapFileLocation=" + mapFileLocation +
                ", dmaFileLocation=" + dmaFileLocation +
                ", geoIpCityDBLocation='" + geoIpCityDBLocation + '\'' +
                ", geoIpISPDBLocation='" + geoIpISPDBLocation + '\'' +
                ", version='" + version + '\'' +
                ", checkpointInterval=" + checkpointInterval +
                ", minPauseBetweenCheckpoints=" + minPauseBetweenCheckpoints +
                ", checkpointTimeout=" + checkpointTimeout +
                ", maxConcurrentCheckpoints=" + maxConcurrentCheckpoints +
                ", checkpointMode=" + checkpointMode +
                ", checkpointCleanup=" + checkpointCleanup +
                ", longerIntervalIndexPrefix='" + longerIntervalIndexPrefix + '\'' +
                ", shorterIntervalIndexPrefix='" + shorterIntervalIndexPrefix + '\'' +
                ", initialTimestamp='" + initialTimestamp + '\'' +
                ", elasticSearchUserName='" + elasticSearchUserName + '\'' +
                ", elasticSearchPassword='" + elasticSearchPassword + '\'' +
                ", heartbeatValidityOffset=" + heartbeatValidityOffset +
                ", elasticsearchMaxFlushSizeMB=" + elasticsearchMaxFlushSizeMB +
                ", elasticsearchBackoffRetryNumber=" + elasticsearchBackoffRetryNumber +
                ", elasticsearchBackoffDelayInMillis=" + elasticsearchBackoffDelayInMillis +
                ", elasticsearchFlushMaxActions=" + elasticsearchFlushMaxActions +
                ", autoWatermarkInterval=" + autoWatermarkInterval +
                ", sessionTimeout=" + sessionTimeout +
                ", sessionLifetime=" + sessionLifetime +
                ", timeTravellersTolerancePeriod=" + timeTravellersTolerancePeriod +
                ", kinesisShardAssigner='" + kinesisShardAssigner + '\'' +
                ", kdsEFOConsumerName='" + kdsEFOConsumerName + '\'' +
                ", kdsEFOConsumerARN='" + kdsEFOConsumerARN + '\'' +
                ", kdsEFORegType='" + kdsEFORegType + '\'' +
                ", esIndexPeriod='" + esIndexPeriod + '\'' +
                ", kplKinesisEndpoint='" + kplKinesisEndpoint + '\'' +
                ", kplMaxConnections='" + kplMaxConnections + '\'' +
                ", kplLogLevel='" + kplLogLevel + '\'' +
                ", kplMetricsGranularity='" + kplMetricsGranularity + '\'' +
                ", kplAggregationMaxSize='" + kplAggregationMaxSize + '\'' +
                ", kplCollectionMaxCount='" + kplCollectionMaxCount + '\'' +
                ", kplCollectionMaxSize='" + kplCollectionMaxSize + '\'' +
                ", kplConnectTimeout='" + kplConnectTimeout + '\'' +
                ", kplRateLimit='" + kplRateLimit + '\'' +
                ", kplRecordMaxBufferedTime='" + kplRecordMaxBufferedTime + '\'' +
                ", kplRecordTtl='" + kplRecordTtl + '\'' +
                ", kplRequestTimeout='" + kplRequestTimeout + '\'' +
                ", kplThreadPoolSize='" + kplThreadPoolSize + '\'' +
                ", kplAggregationEnabled='" + kplAggregationEnabled + '\'' +
                ", kplQueueLimit=" + kplQueueLimit +
                '}';
    }
}
