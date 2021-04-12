package com.att.dtv.kda.model.app;

import com.att.dtv.kda.util.IPAddressUtil;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * App POJO class
 */
public class SessionReportModel implements Serializable {

    // use this to avoid any serialization deserialization used within Flink
    private static final long serialVersionUID = 7443247800947451717L;


    /* Session ID */
    @SerializedName("sid")
    private long sessionId;

    /* Viewer ID */
    @SerializedName("viewerId")
    private String viewerId;

    /* Asset name (channel, programming, VOD record, etc */
    @SerializedName("asset")
    private String assetName;

    /* Asset name (channel, programming, VOD record, etc */
    @SerializedName("platform")
    private String platformType;

    /* Country - IP based geo location */
    @SerializedName("country")
    private String country;

    /* State - IP based geo location */
    @SerializedName("state")
    private String state;

    /* City - IP based geo location */
    @SerializedName("city")
    private String city;

    @SerializedName("zip")
    private String postalCode;

    /* ASN - service provider number, based no IP */
    @SerializedName("asn")
    private Integer asn;

    /* Start time of the session (sst) */
    @SerializedName("start_time")
    private Timestamp startTime;

    /* Startup time  (vst) */
    @SerializedName("startup_time")
    private long startupTime;

    /* Playing time - amount of time player was in play state */
    @SerializedName("playing_time")
    private Long playingTime;

    /* Total Playing time - amount of time player was in play state for entire session */
    @SerializedName("playing_time_total")
    private Long playingTimeTotal;

    /* Buffering time - amount of time player was in buffering state */
    @SerializedName("buffering_time")
    private long bufferingTime;

    /* Average bitrate */
    @SerializedName("average_bitrate")
    private Double averageBitrate;

    @SerializedName("avg_fps")
    private Double averageFrameRate;

    /* Startup error - when error occurred  and no playback was registered*/
    @SerializedName("startup_error")
    private String startupError;

    /* IP address */
    @SerializedName("ip_address")
    private String ip;

    /* cdn */
    @SerializedName("cdn")
    private String cdn;

    /* Streaming URL */
    @SerializedName("streaming_url")
    private String url;

    /* Error list - comma separated list of error codes */
    @SerializedName("error_list")
    private String errorList;

    @SerializedName("minor_error_list")
    private String minorErrorList;

    @SerializedName("vpf_error_list")
    private String vpfErrroList;

    /* Connection Induced Rebuffering Time (ms) */
    @SerializedName("connection_induced_rebuffering_time")
    private long cirt;

    /* Video playback failure indicator: value is '1' if video started successfully, but ended with an error. Otherwise '0' */
    @SerializedName("vpf")
    private Byte vpf;

    /* Video playback failure errors */
    @SerializedName("ended_status")
    private Byte endedStatus;

    /* Video playback failure errors */
    @SerializedName("end_time")
    private Timestamp endTime;

    @SerializedName("strmt")
    private String streamType;

    @SerializedName("device_os")
    private String deviceOS;

    @SerializedName(value = "device_os_version")
    private String deviceOSVersion;

    @SerializedName(value = "device_model")
    private String deviceModel;

    @SerializedName(value = "device_manufacturer")
    private String deviceManufacturer;

    @SerializedName("device_local")
    private String deviceLocale;

    @SerializedName(value = "stream_id")
    private String streamID;

    @SerializedName(value = "device_id")
    private String deviceID;

    @SerializedName("platform_version")
    private String ptv;

    @SerializedName("isDai")
    private boolean isDai;

    @SerializedName(value = "dai_type")
    private String daiType;

    @SerializedName("aso")
    private String aso;

    @SerializedName("device_hardware_type")
    private String deviceHardwareType;

    @SerializedName("connection_type")
    private String connectionType;

    @SerializedName("device_ad_id")
    private String deviceAdId;

    @SerializedName("serial_number")
    private String serialNumber;

    @SerializedName("app_session_id")
    private String appSessionId;

    @SerializedName("restart_eligible")
    private boolean restartEligible;

    @SerializedName("geolocation")
    private String geolocation;

    @SerializedName("vsf")
    private Byte vsf;

    @SerializedName("ebvs")
    private Byte ebvs;

    @SerializedName("video_restart_time")
    private long videoRestartTime;

    @SerializedName("video_restart_time_total")
    private Long videoRestartTimeTotal;

    @SerializedName("video_restart_count")
    private int videoRestartCount;

    @SerializedName(value = "launch_type")
    private String launchType;

    @SerializedName("customer_type")
    private String customerType;

    @SerializedName("player_settings")
    private String playerSettings;

    @SerializedName("bitrate_capping")
    private boolean bitrateCapping;

    @SerializedName("live_authz_cache")
    private boolean liveAuthzCache;

    @SerializedName("target_group")
    private String targetGroup;

    @SerializedName("profile_id")
    private String partnerProfileId;

    @SerializedName("start_type")
    private String startType;

    @SerializedName("percentage_complete")
    private int percentageComplete;

    @SerializedName("interrupts")
    private int interrupts;

    @SerializedName("browser")
    private String browser;


    @SerializedName("isp")
    private String isp;

    @SerializedName("dma_code")
    private Integer dmaCode;

    @SerializedName("dma")
    private String dma;


    public SessionReportModel() {
    }

    public SessionReportModel(long sessionId, String viewerId, String assetName, String platformType, String country, String state, String city, String postalCode,
                              Integer asn, Timestamp startTime, Long startupTime, Long playingTime, long bufferingTime, Double averageBitrate,
                              Double averageFrameRate, String startupError, String ip, String cdn, String url, String errorList, String minorErrorList,
                              long cirt, Byte vpf, Byte endedStatus, Timestamp endTime, String streamType, String deviceOS, String deviceOSVersion,
                              String deviceModel, String deviceManufacturer, String deviceLocale, String streamID, String deviceID, String ptv, String daiType,
                              String aso, String deviceHardwareType, String connectionType, String deviceAdId, String serialNumber, String appSessionId,
                              boolean restartEligible, String geolocation, Byte vsf, Byte ebvs, long videoRestartTime, String launchType,
                              String customerType, String playerSettings, boolean bitrateCapping, boolean liveAuthzCache, String targetGroup,
                              String partnerProfileId, String startType, int percentageComplete, int interrupts, String browser, String isp, String vpfErrroList) {
        this.sessionId = sessionId;
        this.viewerId = viewerId;
        this.assetName = assetName;
        this.platformType = platformType;
        this.country = country;
        this.state = state;
        this.city = city;
        this.postalCode = postalCode;
        this.asn = asn;
        this.startTime = startTime;
        this.startupTime = startupTime;
        this.playingTime = playingTime;
        this.bufferingTime = bufferingTime;
        this.averageBitrate = averageBitrate;
        this.averageFrameRate = averageFrameRate;
        this.startupError = startupError;
        this.ip = ip;
        this.cdn = cdn;
        this.url = url;
        this.errorList = errorList;
        this.minorErrorList = minorErrorList;
        this.cirt = cirt;
        this.vpf = vpf;
        this.endedStatus = endedStatus;
        this.endTime = endTime;
        this.streamType = streamType;
        this.deviceOS = deviceOS;
        this.deviceOSVersion = deviceOSVersion;
        this.deviceModel = deviceModel;
        this.deviceManufacturer = deviceManufacturer;
        this.deviceLocale = deviceLocale;
        this.streamID = streamID;
        this.deviceID = deviceID;
        this.ptv = ptv;
        this.daiType = daiType;
        this.aso = aso;
        this.deviceHardwareType = deviceHardwareType;
        this.connectionType = connectionType;
        this.deviceAdId = deviceAdId;
        this.serialNumber = serialNumber;
        this.appSessionId = appSessionId;
        this.restartEligible = restartEligible;
        this.geolocation = geolocation;
        this.vsf = vsf;
        this.ebvs = ebvs;
        this.videoRestartTime = videoRestartTime;
        this.launchType = launchType;
        this.customerType = customerType;
        this.playerSettings = playerSettings;
        this.bitrateCapping = bitrateCapping;
        this.liveAuthzCache = liveAuthzCache;
        this.targetGroup = targetGroup;
        this.partnerProfileId = partnerProfileId;
        this.startType = startType;
        this.interrupts = interrupts;
        this.percentageComplete = percentageComplete;
        this.browser = browser;
        this.isp = isp;
        this.vpfErrroList = vpfErrroList;
    }

    public int getVideoRestartCount() {
        return videoRestartCount;
    }

    public void setVideoRestartCount(int videoRestartCount) {
        this.videoRestartCount = videoRestartCount;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getAsn() {
        return asn;
    }

    public void setAsn(Integer asn) {
        this.asn = asn;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(long startupTime) {
        this.startupTime = startupTime;
    }

    public Long getPlayingTime() {
        return playingTime;
    }

    public void setPlayingTime(Long playingTime) {
        this.playingTime = playingTime;
    }

    public long getBufferingTime() {
        return bufferingTime;
    }

    public void setBufferingTime(long bufferingTime) {
        this.bufferingTime = bufferingTime;
    }

    public double getAverageBitrate() {
        return averageBitrate;
    }

    public void setAverageBitrate(double averageBitrate) {
        this.averageBitrate = averageBitrate;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        if (StringUtils.isNotBlank(ip) && IPAddressUtil.isIPv4LiteralAddress(ip)) {
            this.ip = ip;
        }
    }

    public String getCdn() {
        return cdn;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getErrorList() {
        return errorList;
    }

    public void setErrorList(String errorList) {
        this.errorList = errorList;
    }

    public String getMinorErrorList() {
        return minorErrorList;
    }

    public void setMinorErrorList(String minorErrorList) {
        this.minorErrorList = minorErrorList;
    }

    public long getCirt() {
        return cirt;
    }

    public void setCirt(long cirt) {
        this.cirt = cirt;
    }

    public Byte getVpf() {
        return vpf;
    }

    public void setVpf(Byte vpf) {
        this.vpf = vpf;
    }

    public Byte getEndedStatus() {
        return endedStatus;
    }

    public void setEndedStatus(Byte endedStatus) {
        this.endedStatus = endedStatus;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
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

    public String getDeviceOSVersion() {
        return deviceOSVersion;
    }

    public void setDeviceOSVersion(String deviceOSVersion) {
        this.deviceOSVersion = deviceOSVersion;
    }

    public String getDeviceLocale() {
        return deviceLocale;
    }

    public void setDeviceLocale(String deviceLocale) {
        this.deviceLocale = deviceLocale;
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

    public String getAso() {
        return aso;
    }

    public void setAso(String aso) {
        this.aso = aso;
    }

    public String getDeviceHardwareType() {
        return deviceHardwareType;
    }

    public void setDeviceHardwareType(String deviceHardwareType) {
        this.deviceHardwareType = deviceHardwareType;
    }


    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getDeviceAdId() {
        return deviceAdId;
    }

    public void setDeviceAdId(String deviceAdId) {
        this.deviceAdId = deviceAdId;
    }


    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getAppSessionId() {
        return appSessionId;
    }

    public void setAppSessionId(String appSessionId) {
        this.appSessionId = appSessionId;
    }

    public boolean isRestartEligible() {
        return restartEligible;
    }

    public void setRestartEligible(boolean restartEligible) {
        this.restartEligible = restartEligible;
    }

    public String getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
    }

    public Byte getVsf() {
        return vsf;
    }

    public void setVsf(Byte vsf) {
        this.vsf = vsf;
    }

    public Byte getEbvs() {
        return ebvs;
    }

    public void setEbvs(Byte ebvs) {
        this.ebvs = ebvs;
    }

    public long getVideoRestartTime() {
        return videoRestartTime;
    }

    public void setVideoRestartTime(long videoRestartTime) {
        this.videoRestartTime = videoRestartTime;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
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

    public String getPartnerProfileId() {
        return partnerProfileId;
    }

    public void setPartnerProfileId(String partnerProfileId) {
        this.partnerProfileId = partnerProfileId;
    }

    public String getStartType() {
        return startType;
    }

    public void setStartType(String startType) {
        this.startType = startType;
    }

    public int getPercentageComplete() {
        return percentageComplete;
    }

    public void setPercentageComplete(int percentageComplete) {
        this.percentageComplete = percentageComplete;
    }

    public int getInterrupts() {
        return interrupts;
    }

    public void setInterrupts(int interrupts) {
        this.interrupts = interrupts;
    }

    public String getVpfErrroList() {
        return vpfErrroList;
    }

    public void setVpfErrroList(String vpfErrroList) {
        this.vpfErrroList = vpfErrroList;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public Long getPlayingTimeTotal() {
        return playingTimeTotal;
    }

    public void setPlayingTimeTotal(Long playingTimeTotal) {
        this.playingTimeTotal = playingTimeTotal;
    }

    public boolean isDai() {
        return isDai;
    }

    public void setIsDai(boolean dai) {
        isDai = dai;
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

    public void setAverageBitrate(Double averageBitrate) {
        this.averageBitrate = averageBitrate;
    }

    public Double getAverageFrameRate() {
        return averageFrameRate;
    }

    public void setAverageFrameRate(Double averageFrameRate) {
        this.averageFrameRate = averageFrameRate;
    }

    public String getStartupError() {
        return startupError;
    }

    public void setStartupError(String startupError) {
        this.startupError = startupError;
    }

    @Override
    public String toString() {
        String s = "SessionReportModel{" +
                ", sessionId=" + sessionId +
                ", viewerId='" + viewerId + '\'' +
                ", assetName='" + assetName + '\'' +
                ", platformType='" + platformType + '\'' +
                ", country='" + country + '\'' +
                ", state='" + state + '\'' +
                ", city='" + city + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", asn='" + asn + '\'' +
                ", startTime=" + startTime +
                ", startupTime=" + startupTime +
                ", playingTime=" + playingTime +
                ", bufferingTime=" + bufferingTime +
                ", averageBitrate=" + averageBitrate +
                ", startupError='" + startupError + '\'' +
                ", ip='" + ip + '\'' +
                ", cdn='" + cdn + '\'' +
                ", url='" + url + '\'' +
                ", errorList='" + errorList + '\'' +
                ", cirt=" + cirt +
                ", vpf=" + vpf +
                ", endedStatus=" + endedStatus +
                ", endTime=" + endTime +
                ", streamType='" + streamType + '\'' +
                ", deviceOS='" + deviceOS + '\'' +
                ", deviceOSVersion='" + deviceOSVersion + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceManufacturer='" + deviceManufacturer + '\'' +
                ", deviceLocale='" + deviceLocale + '\'' +
                ", streamID='" + streamID + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", ptv='" + ptv + '\'' +
                ", daiType='" + daiType + '\'' +
                ", aso='" + aso + '\'' +
                ", deviceHardwareType='" + deviceHardwareType + '\'' +
                ", connectionType='" + connectionType + '\'' +
                ", deviceAdId='" + deviceAdId + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", appSessionId='" + appSessionId + '\'' +
                ", restartEligible=" + restartEligible +
                ", geolocation='" + geolocation + '\'' +
                ", vsf=" + vsf +
                ", ebvs=" + ebvs +
                ", videoRestartTime=" + videoRestartTime +
                ", launchType='" + launchType + '\'' +
                ", customerType='" + customerType + '\'' +
                ", playerSettings='" + playerSettings + '\'' +
                ", bitrateCapping=" + bitrateCapping +
                ", liveAuthzCache=" + liveAuthzCache +
                ", targetGroup='" + targetGroup + '\'' +
                ", partnerProfileId='" + partnerProfileId + '\'' +
                ", startType='" + startType + '\'' +
                ", interrupts='" + interrupts + '\'' +
                ", percentage_complete='" + percentageComplete + '\'' +
                ", isp='" + isp + '\'' +
                ", browser='" + browser + '\'' +
                ", vpfErrorList='" + vpfErrroList + '\'' +
                '}';
        return s;
    }
}