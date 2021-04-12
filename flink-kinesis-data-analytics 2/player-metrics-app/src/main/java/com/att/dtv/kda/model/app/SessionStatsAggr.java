package com.att.dtv.kda.model.app;

import com.att.dtv.kda.process.Interval;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

public class SessionStatsAggr implements Serializable, TimeProcessable {


    private int sequenceStart;
    private int sequenceEnd;

    private String partitionId;

    public void setPartitionId(String id) {
        this.partitionId = id;
    }

    public String getPartitionId() {
        return this.partitionId;
    }

    private BitrateStats bitrateStats;
    private FramerateStats framerateStats;

    private Long videoStartupTime;

    private Interval interval;

    private Heartbeat metaHeartbeat;

    private Long playDuration;
    private Long rebuferringDuration;
    private Double rebuferringRatio;
    private Long ciRebuferringDuration;
    private Double ciRebuferringRatio;

    private Long playDurationTotal;

    private Long endOfSessionTS;

    private Byte endedPlay;

    private Byte endBeforeVideoStart;

    private Long videoRestartTime;
    private Integer videoRestartCount;
    private Long videoRestartTimeTotal;

    private Byte videoPlayFailures;

    private List<ClientError> errorCounts;

    private Byte videoStartFailure;
    private Integer videoStartFailureCode;

    private String cdn;

    private String pod;

    private Byte expired;

    private Byte attempt;

    private boolean isReport = false;

    private Integer fatalErrorCount;

    private Integer nonFatalErrorCount;

    private String countryISOCode;

    private String subdivisionISOCode;

    private String city;

    private String postalCode;

    private Integer asn;

    private String aso;

    private String isp;

    private int interrupts;

    private String hostName;

    private String kdaAppVersion;

    private Integer dmaCode;

    private String dma;

    private Timestamp processingTimestamp;

    public SessionStatsAggr() {
    }

    public SessionStatsAggr(Interval interval) {
        this.interval = interval;
    }

    public SessionStatsAggr(Interval interval, int sequenceStart, int sequenceEnd) {
        this(interval);
        this.sequenceStart = sequenceStart;
        this.sequenceEnd = sequenceEnd;
    }

    public SessionStatsAggr(Interval interval, Heartbeat metaHeartbeat, int sequenceStart, int sequenceEnd) {
        this(interval, sequenceStart, sequenceEnd);
        this.metaHeartbeat = metaHeartbeat;
    }

    public SessionStatsAggr(SessionStatsAggr lastInterval) {
        if (lastInterval != null) {
            setMetaHeartbeat(lastInterval.metaHeartbeat);
            setSequenceStart(lastInterval.sequenceStart);
            setSequenceEnd(lastInterval.sequenceEnd);
            setBitrateStats(lastInterval.bitrateStats);
            setFramerateStats(lastInterval.framerateStats);
            setVideoStartupTime(lastInterval.videoStartupTime);
            setInterval(lastInterval.interval);
            setPlayDuration(lastInterval.playDuration);
            setRebuferringDuration(lastInterval.rebuferringDuration);
            setRebuferringRatio(lastInterval.rebuferringRatio);
            setCiRebuferringDuration(lastInterval.ciRebuferringDuration);
            setCiRebuferringRatio(lastInterval.ciRebuferringRatio);
            setEndOfSessionTS(lastInterval.endOfSessionTS);
            setEndedPlay(lastInterval.endedPlay);
            setEndBeforeVideoStart(lastInterval.endBeforeVideoStart);
            setVideoRestartTime(lastInterval.videoRestartTime);
            setVideoRestartCount(lastInterval.videoRestartCount);
            setVideoPlayFailures(lastInterval.videoPlayFailures);
            setErrorCounts(lastInterval.errorCounts);
            setVideoStartFailure(lastInterval.videoStartFailure);
            setVideoStartFailureCode(lastInterval.videoStartFailureCode);
            setCdn(lastInterval.cdn);
            setPod(lastInterval.pod);
            setExpired(lastInterval.expired);
            setAttempt(lastInterval.attempt);
            setInterrupts(lastInterval.interrupts);
        }
    }

    public Integer getVideoRestartCount() {
        return videoRestartCount;
    }

    public void setVideoRestartCount(Integer videoRestartCount) {
        this.videoRestartCount = videoRestartCount;
    }

    public String getKdaAppVersion() {
        return kdaAppVersion;
    }

    public void setKdaAppVersion(String kdaAppVersion) {
        this.kdaAppVersion = kdaAppVersion;
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

    public BitrateStats getBitrateStats() {
        return bitrateStats;
    }

    public void setBitrateStats(BitrateStats bitrateStats) {
        this.bitrateStats = bitrateStats;
    }

    public FramerateStats getFramerateStats() {
        return framerateStats;
    }

    public void setFramerateStats(FramerateStats framerateStats) {
        this.framerateStats = framerateStats;
    }

    public Long getVideoStartupTime() {
        return videoStartupTime;
    }

    public void setVideoStartupTime(Long videoStartupTime) {
        this.videoStartupTime = videoStartupTime;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public int getSequenceStart() {
        return sequenceStart;
    }

    public void setSequenceStart(int sequenceStart) {
        this.sequenceStart = sequenceStart;
    }

    public int getSequenceEnd() {
        return sequenceEnd;
    }

    public void setSequenceEnd(int sequenceEnd) {
        this.sequenceEnd = sequenceEnd;
    }

    public Heartbeat getMetaHeartbeat() {
        return metaHeartbeat;
    }

    public void setMetaHeartbeat(Heartbeat metaHeartbeat) {
        this.metaHeartbeat = metaHeartbeat;
        if(this.metaHeartbeat!=null)
            this.metaHeartbeat.setEvents(null);
    }

    public Long getPlayDuration() {
        return playDuration;
    }

    public void setPlayDuration(Long playDuration) {
        this.playDuration = playDuration;
    }

    public Long getRebuferringDuration() {
        return rebuferringDuration;
    }

    public void setRebuferringDuration(Long rebuferringDuration) {
        this.rebuferringDuration = rebuferringDuration;
    }

    public Double getRebuferringRatio() {
        return rebuferringRatio;
    }

    public void setRebuferringRatio(Double rebuferringRatio) {
        this.rebuferringRatio = rebuferringRatio;
    }

    public Long getCiRebuferringDuration() {
        return ciRebuferringDuration;
    }

    public void setCiRebuferringDuration(Long ciRebuferringDuration) {
        this.ciRebuferringDuration = ciRebuferringDuration;
    }

    public Double getCiRebuferringRatio() {
        return ciRebuferringRatio;
    }

    public void setCiRebuferringRatio(Double ciRebuferringRatio) {
        this.ciRebuferringRatio = ciRebuferringRatio;
    }

    public Long getEndOfSessionTS() {
        return endOfSessionTS;
    }

    public void setEndOfSessionTS(Long endOfSessionTS) {
        this.endOfSessionTS = endOfSessionTS;
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

    public Byte getVideoPlayFailures() {
        return videoPlayFailures;
    }

    public void setVideoPlayFailures(Byte videoPlayFailures) {
        this.videoPlayFailures = videoPlayFailures;
    }

    public List<ClientError> getErrorCounts() {
        return errorCounts;
    }

    public void setErrorCounts(List<ClientError> errorCounts) {
        this.errorCounts = errorCounts;
    }

    public Byte getVideoStartFailure() {
        return videoStartFailure;
    }

    public void setVideoStartFailure(Byte videoStartFailure) {
        this.videoStartFailure = videoStartFailure;
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

    public boolean isReport() {
        return isReport;
    }

    public boolean getIsReport() {
        return isReport;
    }

    public void setIsReport(boolean isReport) {
        this.isReport = isReport;
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

    public Integer getVideoStartFailureCode() {
        return videoStartFailureCode;
    }

    public void setVideoStartFailureCode(Integer videoStartFailureCode) {
        this.videoStartFailureCode = videoStartFailureCode;
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

    public int getInterrupts() {
        return interrupts;
    }

    public void setInterrupts(int interrupts) {
        this.interrupts = interrupts;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public Long getPlayDurationTotal() {
        return playDurationTotal;
    }

    public void setPlayDurationTotal(Long playDurationTotal) {
        this.playDurationTotal = playDurationTotal;
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

    public void setReport(boolean report) {
        isReport = report;
    }

    public Timestamp getProcessingTimestamp() {
        return processingTimestamp;
    }

    public void setProcessingTimestamp(Timestamp processingTimestamp) {
        this.processingTimestamp = processingTimestamp;
    }

    @Override
    public String toString() {
        return "SessionStatsAggr{" +
                "sequenceStart=" + sequenceStart +
                ", sequenceEnd=" + sequenceEnd +
                ", bitrateStats=" + bitrateStats +
                ", framerateStats=" + framerateStats +
                ", videoStartupTime=" + videoStartupTime +
                ", interval=" + interval +
                ", metaHeartbeat=" + metaHeartbeat +
                ", playDuration=" + playDuration +
                ", rebuferringDuration=" + rebuferringDuration +
                ", rebuferringRatio=" + rebuferringRatio +
                ", ciRebuferringDuration=" + ciRebuferringDuration +
                ", ciRebuferringRatio=" + ciRebuferringRatio +
                ", endOfSessionTS=" + endOfSessionTS +
                ", endedPlay=" + endedPlay +
                ", endBeforeVideoStart=" + endBeforeVideoStart +
                ", videoRestartTime=" + videoRestartTime +
                ", videoPlayFailures=" + videoPlayFailures +
                ", errorCounts=" + errorCounts +
                ", videoStartFailure=" + videoStartFailure +
                ", videoStartFailureCode=" + videoStartFailureCode +
                ", cdn='" + cdn + '\'' +
                ", pod='" + pod + '\'' +
                ", expired=" + expired +
                ", attempt=" + attempt +
                ", isReport=" + isReport +
                ", fatalErrorCount=" + fatalErrorCount +
                ", nonFatalErrorCount=" + nonFatalErrorCount +
                ", countryISOCode='" + countryISOCode + '\'' +
                ", subdivisionISOCode='" + subdivisionISOCode + '\'' +
                ", city='" + city + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", interrupts='" + interrupts + '\'' +
                '}';
    }

    @Override
    public void setEventTime(Timestamp eventTimestamp) {
        //do nothing
    }
}
