package com.att.dtv.kda.converters;

import com.att.dtv.kda.model.app.ClientError;
import com.att.dtv.kda.model.app.Heartbeat;
import com.att.dtv.kda.model.app.SessionReportModel;
import com.att.dtv.kda.model.app.SessionStatsAggr;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionStatsToReportConverter {
    private static Logger LOG = LoggerFactory.getLogger(SessionStatsToReportConverter.class);
    private static final ThreadLocal<DecimalFormat> decimalFormat =
            ThreadLocal.withInitial(() -> new DecimalFormat("#####.##"));

    public static SessionReportModel convert(SessionStatsAggr sessionStats) {
        if (sessionStats == null) return null;

        SessionReportModel sReport = new SessionReportModel();

        Heartbeat hb = sessionStats.getMetaHeartbeat();
        if(hb==null) return null;

        //purging string values to make sure none of the field has garbage
        int stringMaxLength = StringUtils.isBlank(hb.getUrl())? 0:hb.getUrl().length();
        stringMaxLength = stringMaxLength>10?stringMaxLength:255;

        sReport.setSessionId(hb.getSessionId());
        sReport.setViewerId(purgeString(hb.getViewerId(), stringMaxLength));

        sReport.setAssetName(purgeString(hb.getAssetName(), stringMaxLength));
        sReport.setPlatformType(purgeString(hb.getPlatformType(), stringMaxLength));
        sReport.setStartTime(new Timestamp((long) hb.getSessionStartTime()));
        sReport.setStartupTime(sessionStats.getVideoStartupTime()==null?-1:(sessionStats.getVideoStartupTime() <= 600000 ? sessionStats.getVideoStartupTime() : -3));
        sReport.setPlayingTime(sessionStats.getPlayDuration());
        sReport.setBufferingTime(sessionStats.getRebuferringDuration()==null?0:sessionStats.getRebuferringDuration());

        if (sessionStats.getBitrateStats() != null) {
            if((sessionStats.getEndBeforeVideoStart()!= null && sessionStats.getEndBeforeVideoStart() == (byte) 1) || (sessionStats.getVideoStartFailure()!=null && sessionStats.getVideoStartFailure() == (byte) 1)){
                sReport.setAverageBitrate(0);
            }else {
                sReport.setAverageBitrate(sessionStats.getBitrateStats().getValue());
            }
        }
        sReport.setCdn(purgeString(sessionStats.getCdn(),stringMaxLength));
        sReport.setUrl(hb.getUrl());

        if (sessionStats.getErrorCounts() != null && !sessionStats.getErrorCounts().isEmpty()) {
            Map<String, Long> nonfatalErrorCodeMap = new HashMap<>();
            List<String> fatalCodes = new ArrayList<>();
            List<String> nonFatalCodes = new ArrayList<>();
            List<String> vpfErrorCodes = new ArrayList<>();
            for(ClientError clientError : sessionStats.getErrorCounts()){
                String errorCode = StringUtils.isBlank(clientError.getErrorCode())? StringUtils.EMPTY:clientError.getErrorCode().trim();
                if(clientError.isFatal()){
                    if(sessionStats.getVideoPlayFailures()!=null && sessionStats.getVideoPlayFailures()==(byte)1){
                        vpfErrorCodes.add(errorCode);
                    }else{
                        fatalCodes.add(errorCode);
                    }
                }else{
                    Long count=null;
                   if((count=nonfatalErrorCodeMap.get(errorCode))==null){
                       count = 0L;
                   }
                   nonfatalErrorCodeMap.put(errorCode, count+1);
                }
            }
            sReport.setErrorList(String.join("&", fatalCodes));
            nonfatalErrorCodeMap.entrySet().stream().forEach(entry -> {
                nonFatalCodes.add(entry.getKey()+"-"+entry.getValue());
            });
            sReport.setMinorErrorList(String.join("&", nonFatalCodes));
            sReport.setVpfErrroList(String.join("&", vpfErrorCodes));
        }

        sReport.setCirt(sessionStats.getCiRebuferringDuration()==null?0:sessionStats.getCiRebuferringDuration());
        sReport.setVpf(sessionStats.getVideoPlayFailures()==null?(byte)0:sessionStats.getVideoPlayFailures());


        if (sessionStats.getEndOfSessionTS() != null) {
            sReport.setEndTime(new Timestamp(sessionStats.getEndOfSessionTS()));
        }

        if (sessionStats.getExpired() != null && sessionStats.getExpired()==1) {
            sReport.setEndedStatus((byte) 2);
        } else if(sessionStats.getExpired()!=null && sessionStats.getExpired()==2){
            sReport.setEndedStatus((byte)3);
        }
        else {
            sReport.setEndedStatus((byte) 1);
        }

        sReport.setIp(purgeString(hb.getIpAddress(), stringMaxLength));
        sReport.setStreamType(purgeString(hb.getStreamType(), stringMaxLength));
        sReport.setDeviceOS(purgeString(hb.getDeviceOS(), stringMaxLength));
        sReport.setDeviceID(purgeString(hb.getDeviceID(), stringMaxLength));
        sReport.setStreamID(purgeString(hb.getStreamID(), stringMaxLength));
        sReport.setPtv(purgeString(hb.getPtv(), stringMaxLength));
        sReport.setDaiType(StringUtils.isBlank(hb.getDaiType())?"NA":purgeString(hb.getDaiType(), stringMaxLength));
        sReport.setIsDai(hb.isDai());
        sReport.setDeviceManufacturer(purgeString(hb.getDeviceManufacturer(), stringMaxLength));
        sReport.setDeviceModel(purgeString(hb.getDeviceModel(), stringMaxLength));
        sReport.setDeviceAdId(purgeString(hb.getDeviceAdID(), stringMaxLength));
        sReport.setAppSessionId(purgeString(hb.getAppSessionID(), stringMaxLength));
        sReport.setGeolocation(purgeString(decimalFormat.get().format(hb.getLatitude())+" "+decimalFormat.get().format(hb.getLongitude()), stringMaxLength));
        sReport.setRestartEligible(hb.isRestartEligible());
        sReport.setSerialNumber(purgeString(hb.getSerialNumber(), stringMaxLength));
        sReport.setCity(purgeString(sessionStats.getCity(), stringMaxLength));
        sReport.setState(purgeString(sessionStats.getSubdivisionISOCode(), stringMaxLength));
        sReport.setCountry(purgeString(sessionStats.getCountryISOCode(), stringMaxLength));
        sReport.setPostalCode(purgeString(sessionStats.getPostalCode(), stringMaxLength));
        sReport.setAsn(sessionStats.getAsn());
        sReport.setAso(purgeString(sessionStats.getAso(), stringMaxLength));
        sReport.setLaunchType(purgeString(hb.getLaunchType(), stringMaxLength));
        sReport.setDeviceHardwareType(purgeString(hb.getDeviceType(), stringMaxLength));
        sReport.setConnectionType(purgeString(hb.getConnectionType(), stringMaxLength));
        sReport.setCustomerType(purgeString(hb.getCustomerType(), stringMaxLength));
        sReport.setPlayerSettings(purgeString(hb.getPlayerSettings(), stringMaxLength));
        sReport.setBitrateCapping(hb.isBitrateCapping());
        sReport.setLiveAuthzCache(hb.isLiveAuthzCache());
        sReport.setTargetGroup(purgeString(hb.getTargetGroup(), stringMaxLength));
        sReport.setEbvs(sessionStats.getEndBeforeVideoStart()==null?(byte)0:sessionStats.getEndBeforeVideoStart());
        sReport.setVsf(sessionStats.getVideoStartFailure()==null?(byte)0:sessionStats.getVideoStartFailure());
        sReport.setDeviceOSVersion(purgeString(hb.getDeviceOSVersion(), stringMaxLength));
        sReport.setDeviceLocale(purgeString(hb.getDeviceLocale(), stringMaxLength));
        sReport.setPartnerProfileId(purgeString(hb.getPartnerProfileId(), stringMaxLength));
        sReport.setStartType(purgeString(hb.getStartType(), stringMaxLength));
        sReport.setPercentageComplete(hb.getContentLength()==0?-1:Math.round(hb.getPlayerHead()*100/hb.getContentLength()));
        sReport.setBrowser("non-browser");
        sReport.setIsp(sessionStats.getIsp() != null ? sessionStats.getIsp() : "NA");
        sReport.setVideoRestartTime(sessionStats.getVideoRestartTime()==null?0:sessionStats.getVideoRestartTime());
        sReport.setVideoRestartCount(sessionStats.getVideoRestartCount()==null?0:sessionStats.getVideoRestartCount());
        sReport.setVideoRestartTimeTotal(sessionStats.getVideoRestartTimeTotal() == null ? 0 : sessionStats.getVideoRestartTimeTotal());
        sReport.setInterrupts(sessionStats.getInterrupts());

        sReport.setDmaCode(sessionStats.getDmaCode() == null ? null : sessionStats.getDmaCode());
        sReport.setDma(sessionStats.getDma() == null ? null : sessionStats.getDma());

        return sReport;
    }

    private static String purgeString(String candidate, int stringMaxLength) {
        if(StringUtils.isBlank(candidate)) return candidate;
        return candidate.length()<stringMaxLength?candidate: candidate.substring(0,stringMaxLength);
    }

    final static GsonBuilder builder = new GsonBuilder();
    final static Gson gson = builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create();

    public static String convertJson(SessionStatsAggr sessionStats) {
        if (sessionStats == null) return null;
        SessionReportModel sReport = null;
        try {
            sReport = convert(sessionStats);
        } catch (Exception e) {
            LOG.error("Failed to convert session stats: {}", sessionStats, e);
            LOG.error("Stack trace: {}", (Object) e.getStackTrace());
        }
        String sessionReportJson = gson.toJson(sReport) + "\n";
        return sessionReportJson;
    }
}
