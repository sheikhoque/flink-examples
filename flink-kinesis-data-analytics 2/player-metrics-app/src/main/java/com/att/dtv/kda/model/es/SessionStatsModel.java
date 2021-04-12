package com.att.dtv.kda.model.es;

import com.att.dtv.kda.model.app.ClientError;
import com.att.dtv.kda.model.app.TimeProcessable;
import com.att.dtv.kda.util.IPAddressUtil;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * App POJO class
 */
public class SessionStatsModel implements Serializable, TimeProcessable {

    // use this to avoid any serialization deserialization used within Flink
    public static final long serialVersionUID = 41L;

    private String partitionId;

    public void setPartitionId(String id) {
        this.partitionId = id;
    }

    public String getPartitionId() {
        return this.partitionId;
    }

    /* ID of the document in form: <SID>@<VID>@<TS>
     * where:
     *     - SID: session ID
     *     - VID: viewer ID
     *     - TS:  a time stamp (epoch) of interval start
     * */
    @SerializedName("id")
    private String _id;

    /* Session ID */
    @SerializedName("sid")
    private long sessionId;

    /* The least sequence number of a heartbeat having impact on a given interval */
    @SerializedName("seq_st")
    private int sequence_start;

    /* The greatest sequence number of a heartbeat having impact on a given interval */
    @SerializedName("seq_end")
    private int sequence_end;

    /* Client ID */
    @SerializedName("cid")
    private String clientId;

    /* Viewer ID */
    @SerializedName("vid")
    private String viewerId;

    /* Session start time stamp (epoch) */
    @SerializedName("sst")
    private long sessionStartTime;

    /* Time stamp of the beginning of an interval */
    @SerializedName("timestamp")
    private Timestamp timestamp;

    /* Asset name (channel, programming, VOD record, etc */
    @SerializedName("an")
    private String assetName;

    /* Connection type */
    @SerializedName("ct")
    private String connectionType;

    /* Average bitrate in given interval */
    @SerializedName("br")
    private double avgBitrate;

    /* Total bits of content in given interval */
    @SerializedName("bits")
    private long bits;

    public long getBits() {
        return bits;
    }

    public void setBits(long bits) {
        this.bits = bits;
    }

    /* Average buffer length in given interval */
    @SerializedName("bl")
    private BufferLengthStats avgBufferLength;

    /* Platform type (Android, iOS, ChromeOS, etc */
    @SerializedName("pt")
    private String platformType;

    /* Live programming indicator */
    @SerializedName("lv")
    private boolean live;

    /* Video startup time (epoch) */
    @SerializedName("vst")
    private Long videoStartupTime;

    /* Duration (milliseconds) a video is in 'play' state */
    @SerializedName("pd")
    private Long playDuration;

    /* Duration (milliseconds) a video is in 'play' state for entire session */
    @SerializedName("pd_total")
    private Long playDurationTotal;

    /* Duration (milliseconds) a video is buffering */
    @SerializedName("rbd")
    private Long rebufferingDuration;

    /* Ratio between duration of rebuffering to duration of playing */
    @SerializedName("rbr")
    private Double rebufferingRatio;

    /* Duration (milliseconds) of connection induced video buffering */
    @SerializedName("crd")
    private Long ciRebufferingDuration;

    /* Ratio between duration of connection induced rebuffering to duration of playing + non-connection induced rebuffering */
    @SerializedName("cirr")
    private Double ciRebufferingRatio;

    /* Average frames per seconds in given interval */
    @SerializedName("afps")
    private Double averageFrameRate;

    /* End of session time stamp (epoch) */
    @SerializedName("eos")
    private Long endOfSession;

    /* Ended play. For easier calculation in ES, the value is 1 if presented (so sum(...) can work in ES) */
    @SerializedName("eop")
    private Byte endedPlay;

    /* Ended session before video started. For easier calculation in ES, the value is 1 if presented (so sum(...) can work in ES) */
    @SerializedName("ebvs")
    private Byte endBeforeVideoStart;

    /* Video restart time */
    @SerializedName("vrt")
    private Long videoRestartTime;

    /* Total video restart time */
    @SerializedName("vrt_total")
    private Long videoRestartTimeTotal;

    /* Video restart count */
    @SerializedName("vrt_count")
    private Integer videoRestartCount;

    /* Map of error types to number of times it appeared in given interval */
    @SerializedName("cl_err_detail")
    private List<ClientError> clientErrorCounts;

    /* Video starting failures. Critical failures that prevented play from starting */
    @SerializedName("vsf")
    private Byte videoStartFailure;

    /*
    Video player failures. Critical failures that interrupted play (play after error did not exceed last reported
    buffer length. If no buffer length reported a default 150 seconds is used
      */
    @SerializedName("vpf")
    private Byte videoPlayFailure;

    /*
     * Provides CDN information extracted from heartbeat url, based in a mapping document with matching regex.
     */
    @SerializedName("cdn")
    private String cdn;

    /*
     * Provides POD name information extracted from heartbeat url, based in a mapping with matching regex.
     */
    @SerializedName("pod")
    private String pod;

    /*
     * Provides an indication that session has expired and closed without waiting for end of session event.
     */
    @SerializedName("expired")
    private Byte expired;

    /*
     * Provides an indication that session has expired and closed without waiting for end of session event.
     */
    @SerializedName("attempt")
    private Byte attempt;

    /* Time stamp (epoch) of last update of the document */
    @SerializedName("lastupdatets")
    private Timestamp processingTimestamp;

    @SerializedName("cl_fatal_err_cnt")
    private Integer fatalErrorCount;

    @SerializedName("cl_nonfatal_err_cnt")
    private Integer nonFatalErrorCount;

    @SerializedName("ip")
    private String ipAddress;

    @SerializedName("long")
    private String longitude;

    @SerializedName("lat")
    private String latitude;

    @SerializedName("strmt")
    private String streamType;

    @SerializedName("os")
    private String deviceOS;

    @SerializedName("osVer")
    private String deviceOSVersion;

    @SerializedName("dn")
    private String deviceName;

    @SerializedName("dt")
    private String deviceType;

    @SerializedName("dm")
    private String deviceModel;

    @SerializedName("dmfg")
    private String deviceManufacturer;

    @SerializedName("dl")
    private String deviceLocale;

    @SerializedName("streamID")
    private String streamID;

    @SerializedName("did")
    private String deviceID;

    @SerializedName("did_mm3")
    private Integer deviceIDMurMur3;

    @SerializedName("isDai")
    private boolean isDai;

    @SerializedName("ptv")
    private String ptv;

    @SerializedName("daiType")
    private String daiType;

    @SerializedName("Country")
    private String countryISOCode;

    @SerializedName("State")
    private String subdivisionISOCode;

    @SerializedName("City")
    private String city;

    @SerializedName("Zip")
    private String postalCode;

    @SerializedName("ASN")
    private Integer asn;

    @SerializedName("ASO")
    private String aso;

    @SerializedName("isRestartEligible")
    private boolean isRestartEligible;

    @SerializedName(value = "appSessionID", alternate = {"appSid"})
    private String appSessionID;

    @SerializedName(value = "deviceAdID", alternate = {"daid"})
    private String deviceAdID;

    @SerializedName(value = "launchType", alternate = {"ll"})
    private String launchType;

    @SerializedName("customerType")
    private String customerType;

    @SerializedName("playerSettings")
    private String playerSettings;

    @SerializedName("bitrateCapping")
    private boolean bitrateCapping;

    @SerializedName("liveAuthzCache")
    private boolean liveAuthzCache;

    @SerializedName("targetGroup")
    private String targetGroup;

    @SerializedName("serialNumber")
    private String serialNumber;

    @SerializedName("cl")
    private long contentLength;

    @SerializedName("rterr")
    private String runtimeError;

    @SerializedName("hn")
    private String hostName;

    @SerializedName("kdaVer")
    private String kdaAppVersion;

    @SerializedName("isp")
    private String isp;

    @SerializedName("dma_code")
    private Integer dmaCode;

    @SerializedName("dma")
    private String dma;

    private int intervalLength;

    private long intervalStartTS;

    public SessionStatsModel() {

    }

    public SessionStatsModel(String _id, long sessionId, int sequence_start, int sequence_end, String clientId, String viewerId, long sessionStartTime, Timestamp timestamp,
                             String assetName, String connectionType, double avgBitrate, long bits, BufferLengthStats avgBufferLength, String platformType,
                             boolean live, Long videoStartupTime, Long playDuration, Long rebufferingDuration, Double rebufferingRatio, Long ciRebufferingDuration,
                             Double ciRebufferingRatio, Double averageFrameRate, Long endOfSession, Byte endedPlay, Byte endBeforeVideoStart, Long videoRestartTime,
                             List<ClientError> clientErrorCounts, Byte videoStartFailure, Byte videoPlayFailure, String cdn, String pod, Byte expired, Byte attempt,
                             Timestamp processingTimestamp, Integer fatalErrorCount, Integer nonFatalErrorCount, String ipAddress, String longitude, String latitude,
                             String streamType, String deviceOS, String countryISOCode, String subdivisionISOCode, String city, String postalCode,
                             String deviceID, String streamID, boolean isDai, String ptv, String daiType, Integer asn, String aso,
                             String deviceOSVersion, String deviceType, String deviceName, String deviceManufacturer, String deviceLocale, String deviceModel) {

        this._id = _id;
        this.sessionId = sessionId;
        this.sequence_start = sequence_start;
        this.sequence_end = sequence_end;
        this.clientId = clientId;
        this.viewerId = viewerId;
        this.sessionStartTime = sessionStartTime;
        this.timestamp = timestamp;
        this.assetName = assetName;
        this.connectionType = connectionType;
        this.avgBitrate = avgBitrate;
        this.bits = bits;
        this.avgBufferLength = avgBufferLength;
        this.platformType = platformType;
        this.live = live;
        this.videoStartupTime = videoStartupTime;
        this.playDuration = playDuration;
        this.rebufferingDuration = rebufferingDuration;
        this.rebufferingRatio = rebufferingRatio;
        this.ciRebufferingDuration = ciRebufferingDuration;
        this.ciRebufferingRatio = ciRebufferingRatio;
        this.averageFrameRate = averageFrameRate;
        this.endOfSession = endOfSession;
        this.endedPlay = endedPlay;
        this.endBeforeVideoStart = endBeforeVideoStart;
        this.videoRestartTime = videoRestartTime;
        this.clientErrorCounts = clientErrorCounts;
        this.videoStartFailure = videoStartFailure;
        this.videoPlayFailure = videoPlayFailure;
        this.cdn = cdn;
        this.pod = pod;
        this.expired = expired;
        this.attempt = attempt;
        this.processingTimestamp = processingTimestamp;
        this.fatalErrorCount = fatalErrorCount;
        this.nonFatalErrorCount = nonFatalErrorCount;
        this.ipAddress = StringUtils.isNotBlank(ipAddress) &&  IPAddressUtil.isIPv4LiteralAddress(ipAddress)?ipAddress: StringUtils.EMPTY;
        this.longitude = longitude;
        this.latitude = latitude;
        this.streamType = streamType;
        this.streamID = streamID;
        this.ptv = ptv;
        this.deviceID =deviceID;
        this.deviceOS = deviceOS;
        this.daiType = daiType;
        this.isDai = isDai;
        this.deviceOS = deviceOS;
        this.countryISOCode = countryISOCode;
        this.subdivisionISOCode = subdivisionISOCode;
        this.city = city;
        this.postalCode = postalCode;
        this.asn = asn;
        this.aso = aso;
        this.deviceOSVersion=deviceOSVersion;
        this.deviceLocale=deviceLocale;
        this.deviceType=deviceType;
        this.deviceManufacturer=deviceManufacturer;
        this.deviceModel = deviceModel;
        this.deviceName=deviceName;
    }

    public String getAso() {
        return aso;
    }

    public void setAso(String aso) {
        this.aso = aso;
    }

    public Integer getAsn() {
        return asn;
    }

    public void setAsn(Integer asn) {
        this.asn = asn;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public int getSequence_start() {
        return sequence_start;
    }

    public void setSequence_start(int sequence_start) {
        this.sequence_start = sequence_start;
    }

    public int getSequence_end() {
        return sequence_end;
    }

    public void setSequence_end(int sequence_end) {
        this.sequence_end = sequence_end;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public long getSessionStartTime() {
        return sessionStartTime;
    }

    public void setSessionStartTime(long sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public double getAvgBitrate() {
        return avgBitrate;
    }

    public void setAvgBitrate(double avgBitrate) {
        checkValidFloatingPoint(avgBitrate);
        this.avgBitrate = avgBitrate;
    }

    public BufferLengthStats getAvgBufferLength() {
        return avgBufferLength;
    }

    public void setAvgBufferLength(BufferLengthStats avgBufferLength) {
        this.avgBufferLength = avgBufferLength;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public Long getVideoStartupTime() {
        return videoStartupTime;
    }

    public void setVideoStartupTime(Long videoStartupTime) {
        this.videoStartupTime = videoStartupTime;
    }

    public Timestamp getProcessingTimestamp() {
        return processingTimestamp;
    }

    public void setProcessingTimestamp(Timestamp processingTimestamp) {
        this.processingTimestamp = processingTimestamp;
    }

    public Long getPlayDuration() {
        return playDuration;
    }

    public void setPlayDuration(Long playDuration) {
        this.playDuration = playDuration;
    }

    public Long getRebufferingDuration() {
        return rebufferingDuration;
    }

    public void setRebufferingDuration(Long rebufferingDuration) {
        this.rebufferingDuration = rebufferingDuration;
    }

    public Double getRebufferingRatio() {
        return rebufferingRatio;
    }

    public void setRebufferingRatio(Double rebufferingRatio) {
        this.checkValidFloatingPoint(rebufferingRatio);
        this.rebufferingRatio = rebufferingRatio;
    }

    public Double getAverageFrameRate() {
        return averageFrameRate;
    }

    public void setAverageFrameRate(Double averageFrameRate) {
        this.checkValidFloatingPoint(averageFrameRate);
        this.averageFrameRate = averageFrameRate;
    }

    public Long getEndOfSession() {
        return endOfSession;
    }

    public void setEndOfSession(Long endOfSession) {
        this.endOfSession = endOfSession;
    }

    public Long getCiRebufferingDuration() {
        return ciRebufferingDuration;
    }

    public void setCiRebufferingDuration(Long ciRebufferingDuration) {
        this.ciRebufferingDuration = ciRebufferingDuration;
    }

    public Double getCiRebufferingRatio() {
        return ciRebufferingRatio;
    }

    public void setCiRebufferingRatio(Double ciRebufferingRatio) {
        this.checkValidFloatingPoint(ciRebufferingRatio);
        this.ciRebufferingRatio = ciRebufferingRatio;
    }

    public Byte getEndedPlay() {
        return endedPlay;
    }

    public void setEndedPlay(Byte endedPlay) {
        this.endedPlay = endedPlay;
    }

    public Byte getEndBeforeVideoStart() {
        return endBeforeVideoStart;
    }

    public void setEndBeforeVideoStart(Byte endBeforeVideoStart) {
        this.endBeforeVideoStart = endBeforeVideoStart;
    }

    public Long getVideoRestartTime() {
        return videoRestartTime;
    }

    public void setVideoRestartTime(Long videoRestartTime) {
        this.videoRestartTime = videoRestartTime;
    }

    public List<ClientError> getClientErrorCounts() {
        return clientErrorCounts;
    }

    public void setClientErrorCounts(List<ClientError> clientErrorCounts) {
        this.clientErrorCounts = clientErrorCounts;
    }

    public Byte getVideoStartFailure() {
        return videoStartFailure;
    }

    public void setVideoStartFailure(Byte videoStartFailure) {
        this.videoStartFailure = videoStartFailure;
    }

    public Byte getVideoPlayFailure() {
        return videoPlayFailure;
    }

    public void setVideoPlayFailure(Byte videoPlayFailure) {
        this.videoPlayFailure = videoPlayFailure;
    }

    public String getCdn() {
        return cdn;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public Byte getExpired() {
        return expired;
    }

    public void setExpired(Byte expired) {
        this.expired = expired;
    }

    public Byte getAttempt() {
        return attempt;
    }

    public void setAttempt(Byte attempt) {
        this.attempt = attempt;
    }

    public Integer getFatalErrorCount() {
        return fatalErrorCount;
    }

    public void setFatalErrorCount(Integer fatalErrorCount) {
        this.fatalErrorCount = fatalErrorCount;
    }

    public Integer getNonFatalErrorCount() {
        return nonFatalErrorCount;
    }

    public void setNonFatalErrorCount(Integer nonFatalErrorCount) {
        this.nonFatalErrorCount = nonFatalErrorCount;
    }

    public void setIpAddress(String ipAddress) {
        if( StringUtils.isNotBlank(ipAddress) && IPAddressUtil.isIPv4LiteralAddress(ipAddress)){
            this.ipAddress = ipAddress;
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getCountryISOCode() {
        return countryISOCode;
    }

    public void setCountryISOCode(String countryISOCode) {
        this.countryISOCode = countryISOCode;
    }

    public String getSubdivisionISOCode() {
        return subdivisionISOCode;
    }

    public void setSubdivisionISOCode(String subdivisionISOCode) {
        this.subdivisionISOCode = subdivisionISOCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreamID() {
        return streamID;
    }

    public void setStreamID(String streamID) {
        this.streamID = streamID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public boolean isDai() {
        return isDai;
    }

    public void setIsDai(boolean isDai) {
        this.isDai = isDai;
    }

    public String getPtv() {
        return ptv;
    }

    public void setPtv(String ptv) {
        this.ptv = ptv;
    }

    public String getDaiType() {
        return daiType;
    }

    public void setDaiType(String daiType) {
        this.daiType = daiType;
    }

    public boolean isRestartEligible() {
        return isRestartEligible;
    }

    public void setRestartEligible(boolean restartEligible) {
        isRestartEligible = restartEligible;
    }

    public String getAppSessionID() {
        return appSessionID;
    }

    public void setAppSessionID(String appSessionID) {
        this.appSessionID = appSessionID;
    }

    public String getDeviceAdID() {
        return deviceAdID;
    }

    public void setDeviceAdID(String deviceAdID) {
        this.deviceAdID = deviceAdID;
    }

    public String getLaunchType() {
        return launchType;
    }

    public void setLaunchType(String launchType) {
        this.launchType = launchType;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getPlayerSettings() {
        return playerSettings;
    }

    public void setPlayerSettings(String playerSettings) {
        this.playerSettings = playerSettings;
    }

    public boolean isBitrateCapping() {
        return bitrateCapping;
    }

    public void setBitrateCapping(boolean bitrateCapping) {
        this.bitrateCapping = bitrateCapping;
    }

    public boolean isLiveAuthzCache() {
        return liveAuthzCache;
    }

    public void setLiveAuthzCache(boolean liveAuthzCache) {
        this.liveAuthzCache = liveAuthzCache;
    }

    public String getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(String targetGroup) {
        this.targetGroup = targetGroup;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getDeviceOSVersion() {
        return deviceOSVersion;
    }

    public void setDeviceOSVersion(String deviceOSVersion) {
        this.deviceOSVersion = deviceOSVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public String getDeviceLocale() {
        return deviceLocale;
    }

    public void setDeviceLocale(String deviceLocale) {
        this.deviceLocale = deviceLocale;
    }

    public String getRuntimeError() {
        return runtimeError;
    }

    public void setRuntimeError(String runtimeError) {
        this.runtimeError = runtimeError;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getKdaAppVersion() {
        return kdaAppVersion;
    }

    public void setKdaAppVersion(String kdaAppVersion) {
        this.kdaAppVersion = kdaAppVersion;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public Integer getDeviceIDMurMur3() {
        return deviceIDMurMur3;
    }

    public void setDeviceIDMurMur3(Integer deviceIDMurMur3) {
        this.deviceIDMurMur3 = deviceIDMurMur3;
    }

    public Long getPlayDurationTotal() {
        return playDurationTotal;
    }

    public void setPlayDurationTotal(Long playDurationTotal) {
        this.playDurationTotal = playDurationTotal;
    }

    public int getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(int intervalLength) {
        this.intervalLength = intervalLength;
    }

    public long getIntervalStartTS() {
        return intervalStartTS;
    }

    public void setIntervalStartTS(long intervalStartTS) {
        this.intervalStartTS = intervalStartTS;
    }

    public Integer getDmaCode() {
        return dmaCode;
    }

    public void setDmaCode(Integer dmaCode) {
        this.dmaCode = dmaCode;
    }

    public String getDma() {
        return dma;
    }

    public void setDma(String dma) {
        this.dma = dma;
    }

    public Long getVideoRestartTimeTotal() {
        return videoRestartTimeTotal;
    }

    public void setVideoRestartTimeTotal(Long videoRestartTimeTotal) {
        this.videoRestartTimeTotal = videoRestartTimeTotal;
    }

    public Integer getVideoRestartCount() {
        return videoRestartCount;
    }

    public void setVideoRestartCount(Integer videoRestartCount) {
        this.videoRestartCount = videoRestartCount;
    }

    @Override
    public String toString() {
        return "SessionStatsModel{" +
                "_id='" + _id + '\'' +
                ", sessionId=" + sessionId +
                ", sequence_start=" + sequence_start +
                ", sequence_end=" + sequence_end +
                ", clientId='" + clientId + '\'' +
                ", viewerId='" + viewerId + '\'' +
                ", sessionStartTime=" + sessionStartTime +
                ", timestamp=" + timestamp +
                ", assetName='" + assetName + '\'' +
                ", connectionType='" + connectionType + '\'' +
                ", avgBitrate=" + avgBitrate +
                ", bits=" + bits +
                ", avgBufferLength=" + avgBufferLength +
                ", platformType='" + platformType + '\'' +
                ", live=" + live +
                ", videoStartupTime=" + videoStartupTime +
                ", playDuration=" + playDuration +
                ", rebufferingDuration=" + rebufferingDuration +
                ", rebufferingRatio=" + rebufferingRatio +
                ", ciRebufferingDuration=" + ciRebufferingDuration +
                ", ciRebufferingRatio=" + ciRebufferingRatio +
                ", averageFrameRate=" + averageFrameRate +
                ", endOfSession=" + endOfSession +
                ", endedPlay=" + endedPlay +
                ", endBeforeVideoStart=" + endBeforeVideoStart +
                ", videoRestartTime=" + videoRestartTime +
                ", clientErrorCounts=" + clientErrorCounts +
                ", videoStartFailure=" + videoStartFailure +
                ", videoPlayFailure=" + videoPlayFailure +
                ", cdn='" + cdn + '\'' +
                ", pod='" + pod + '\'' +
                ", expired=" + expired +
                ", attempt=" + attempt +
                ", processingTimestamp=" + processingTimestamp +
                ", fatalErrorCount=" + fatalErrorCount +
                ", nonFatalErrorCount=" + nonFatalErrorCount +
                ", ipAddress='" + ipAddress + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", streamType='" + streamType + '\'' +
                ", deviceOS='" + deviceOS + '\'' +
                ", deviceOSVersion='" + deviceOSVersion + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceManufacturer='" + deviceManufacturer + '\'' +
                ", deviceLocale='" + deviceLocale + '\'' +
                ", streamID='" + streamID + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", deviceIDMurMur3='" + deviceIDMurMur3 + '\'' +
                ", isDai=" + isDai +
                ", ptv='" + ptv + '\'' +
                ", daiType='" + daiType + '\'' +
                ", countryISOCode='" + countryISOCode + '\'' +
                ", subdivisionISOCode='" + subdivisionISOCode + '\'' +
                ", city='" + city + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", asn=" + asn +
                ", aso='" + aso + '\'' +
                ", isRestartEligible=" + isRestartEligible +
                ", appSessionID='" + appSessionID + '\'' +
                ", deviceAdID='" + deviceAdID + '\'' +
                ", launchType='" + launchType + '\'' +
                ", customerType='" + customerType + '\'' +
                ", playerSettings='" + playerSettings + '\'' +
                ", bitrateCapping=" + bitrateCapping +
                ", liveAuthzCache=" + liveAuthzCache +
                ", targetGroup='" + targetGroup + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", contentLength=" + contentLength +
                ", runtimeError='" + runtimeError + '\'' +
                ", hostName='" + hostName + '\'' +
                ", kdaAppVersion='" + kdaAppVersion + '\'' +
                ", isp='" + isp + '\'' +
                '}';
    }

    void checkValidFloatingPoint(Double value) throws IllegalArgumentException {
        if (value == null) return;

        if (Double.isNaN(value) || Double.isInfinite(value))
            throw new IllegalArgumentException("Value should not be Infinite or NaN");
    }

    @Override
    public void setEventTime(Timestamp processingTimestamp) {
        // nothing to do here..
    }
}