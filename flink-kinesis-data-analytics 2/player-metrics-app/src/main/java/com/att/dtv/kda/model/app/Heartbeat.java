package com.att.dtv.kda.model.app;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * App POJO class
 */
public class Heartbeat implements Cloneable, Serializable,TimeProcessable {

    // use this to avoid any serialization deserialization used within Flink
    public static final long serialVersionUID = 40L;

    @SerializedName("sid")
    private long sessionId;

    private String partitionId;

    public void setPartitionId(String id) {
        this.partitionId = id;
    }

    public String getPartitionId() {
        return this.partitionId;
    }

    @SerializedName(value = "hbseq")
    private int sequence;

    @SerializedName("cid")
    private String clientId;

    @SerializedName("vid")
    private String viewerId;

    @SerializedName("ppid")
    private String partnerProfileId;

    @SerializedName("sst")
    private double sessionStartTime;

    @SerializedName("st")
    private double startTime;

    @SerializedName("timestamp")
    private Timestamp timestamp;

    @SerializedName("an")
    private String assetName;

    @SerializedName("url")
    private String url;

    @SerializedName("ct")
    private String connectionType;

    @SerializedName("br")
    private Integer bitrate;

    @SerializedName("bl")
    private Long bufferLength;

    @SerializedName("pt")
    private String platformType;

    @SerializedName("lv")
    private boolean live;

    @SerializedName("ps")
    private Integer playerState;

    @SerializedName("afps")
    private Integer framesPerSecond;

    @SerializedName("ip")
    private String ipAddress;

    @SerializedName("long")
    private double longitude;

    @SerializedName("lat")
    private double latitude;

    @SerializedName(value = "sevs", alternate = {"sves", "evs"})
    private List<HeartbeatEvent> events;

    @SerializedName("strmt")
    private String streamType;

    @SerializedName("os")
    private String deviceOS;

    @SerializedName("streamID")
    private String streamID;

    @SerializedName("did")
    private String deviceID;

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

    @SerializedName("isDai")
    private boolean isDai;

    @SerializedName("ptv")
    private String ptv;

    @SerializedName("daiType")
    private String daiType;

    @SerializedName("isRestartEligible")
    private boolean isRestartEligible;

    @SerializedName(value = "appSessionID", alternate = {"appSid"})
    private String appSessionID;

    @SerializedName(value = "deviceAdID", alternate = {"daid"})
    private String deviceAdID;

    @SerializedName(value = "launchType", alternate = {"lt"})
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

    @SerializedName("isAdPlaying")
    private boolean isAdPlaying;

    @SerializedName("pht")
    private long playerHead;

    @SerializedName("startType")
    private String startType;

    private Timestamp eventTimestamp;



    /**
     * helper method to initialize processing timestamp while deserializing input json
     *
     * @param eventTimestamp
     * @return
     */
    public void setEventTime(Timestamp eventTimestamp) {
        this.setEventTimestamp(eventTimestamp);
    }

    public Heartbeat clone() throws CloneNotSupportedException {
        return (Heartbeat) super.clone();
    }

    public Heartbeat() {
    }

    public Heartbeat(long sessionId, int sequence, String clientId, String viewerId, String partnerProfileId, double sessionStartTime,
                     double startTime, Timestamp timestamp, String assetName, String url, String connectionType, Integer bitrate,
                     Long bufferLength, String platformType, String platformTypeVersion, boolean live, Integer playerState,
                     List<HeartbeatEvent> events, Timestamp eventTimestamp,
                     Integer framesPerSecond, String ipAddress, double longitude, double latitude, String streamType,
                     String deviceOS, String streamID, String deviceID, boolean isDai, String ptv, String daiType, long pht, String startType) {
        super();
        this.sessionId = sessionId;
        this.sequence = sequence;
        this.clientId = clientId;
        this.viewerId = viewerId;
        this.partnerProfileId = partnerProfileId;
        this.sessionStartTime = sessionStartTime;
        this.startTime = startTime;
        this.timestamp = timestamp;
        this.assetName = assetName;
        this.url = url;
        this.connectionType = connectionType;
        this.bitrate = bitrate;
        this.bufferLength = bufferLength;
        this.platformType = platformType;
        this.live = live;
        this.playerState = playerState;
        this.events = events;
        this.eventTimestamp = eventTimestamp;
        this.framesPerSecond = framesPerSecond;
        this.ipAddress = ipAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streamType = streamType;
        this.streamID = streamID;
        this.deviceOS = deviceOS;
        this.deviceID = deviceID;
        this.isDai = isDai;
        this.ptv = ptv;
        this.daiType = daiType;
        this.playerHead = pht;
        this.startType = startType;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
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

    public String getPartnerProfileId() {
        return partnerProfileId;
    }

    public void setPartnerProfileId(String partnerProfileId) {
        this.partnerProfileId = partnerProfileId;
    }

    public double getSessionStartTime() {
        return sessionStartTime;
    }

    public void setSessionStartTime(double sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public Long getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(Long bufferLength) {
        this.bufferLength = bufferLength;
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

    public boolean isDai() {
        return isDai;
    }

    public void setIsDai(boolean isDai) {
        this.isDai = isDai;
    }

    public Integer getPlayerState() {
        return playerState;
    }

    public void setPlayerState(Integer playerState) {
        this.playerState = playerState;
    }

    public List<HeartbeatEvent> getEvents() {
        return events;
    }

    public void setEvents(List<HeartbeatEvent> events) {
        this.events = events;
    }

    public Timestamp getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Timestamp eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public Integer getFramesPerSecond() {
        return framesPerSecond;
    }

    public void setFramesPerSecond(Integer framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
    }

    public Long tsMillis() {
        return (long) (sessionStartTime + startTime);
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
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

    public void setIsRestartEligible(boolean restartEligible) {
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

    public boolean isAdPlaying() {
        return isAdPlaying;
    }

    public void setAdPlaying(boolean adPlaying) {
        isAdPlaying = adPlaying;
    }

    public void setIsAdPlaying(boolean adPlaying) {
        isAdPlaying = adPlaying;
    }

    public long getPlayerHead() {
        return playerHead;
    }

    public void setPlayerHead(long playerHead) {
        this.playerHead = playerHead;
    }

    public String getStartType() {
        return startType;
    }

    public void setStartType(String startType) {
        this.startType = startType;
    }

    public double getEventTime() {
        return this.eventTimestamp.getTime();
    }

    @Override
    public String toString() {
        return "Heartbeat{" +
                "sessionId=" + sessionId +
                ", sequence=" + sequence +
                ", clientId='" + clientId + '\'' +
                ", viewerId='" + viewerId + '\'' +
                ", partnerProfileId='" + partnerProfileId + '\'' +
                ", sessionStartTime=" + sessionStartTime +
                ", startTime=" + startTime +
                ", timestamp=" + timestamp +
                ", assetName='" + assetName + '\'' +
                ", url='" + url + '\'' +
                ", connectionType='" + connectionType + '\'' +
                ", bitrate=" + bitrate +
                ", bufferLength=" + bufferLength +
                ", platformType='" + platformType + '\'' +
                ", live=" + live +
                ", playerState=" + playerState +
                ", framesPerSecond=" + framesPerSecond +
                ", ipAddress='" + ipAddress + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", events=" + events +
                ", streamType='" + streamType + '\'' +
                ", deviceOS='" + deviceOS + '\'' +
                ", streamID='" + streamID + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", isDai='" + isDai + '\'' +
                ", ptv='" + ptv + '\'' +
                ", daiType='" + daiType + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                ", playerHead=" + playerHead +
                ", startType=" + startType +
                '}';
    }
}