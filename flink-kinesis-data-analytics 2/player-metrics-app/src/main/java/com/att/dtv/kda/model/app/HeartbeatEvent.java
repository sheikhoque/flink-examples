package com.att.dtv.kda.model.app;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HeartbeatEvent implements Serializable {

    // use this to avoid any serialization deserialization used within Flink
    public static final long serialVersionUID = 41L;

    @SerializedName(value = "seq", alternate = {"sseq"})
    private int sequence;

    @SerializedName("t")
    private String type;

    @SerializedName("bl")
    private long bufferLength;

    @SerializedName("st")
    private double eventTimeMillisec;

    @SerializedName("old")
    private Heartbeat oldValue;

    @SerializedName("new")
    private Heartbeat newValue;

    @SerializedName(value = "err", alternate = {"errorCode"})
    private String errCode;

    @SerializedName("isFatal")
    private boolean isFatal;


    public HeartbeatEvent() {

    }

    public HeartbeatEvent(int sequence, String type, long bufferLength, double eventTimeMillisec, Heartbeat oldValue,
                          Heartbeat newValue, String errCode, boolean isFatal) {
        super();
        this.sequence = sequence;
        this.type = type;
        this.bufferLength = bufferLength;
        this.eventTimeMillisec = eventTimeMillisec;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.errCode = errCode;
        this.isFatal = isFatal;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(long bufferLength) {
        this.bufferLength = bufferLength;
    }

    public double getEventTimeMillisec() {
        return eventTimeMillisec;
    }

    public void setEventTimeMillisec(double eventTimeMillisec) {
        this.eventTimeMillisec = eventTimeMillisec;
    }

    public Heartbeat getOldValue() {
        return oldValue;
    }

    public void setOldValue(Heartbeat oldValue) {
        this.oldValue = oldValue;
    }

    public Heartbeat getNewValue() {
        return newValue;
    }

    public void setNewValue(Heartbeat newValue) {
        this.newValue = newValue;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public boolean isFatal() {
        return isFatal;
    }

    public void setFatal(boolean fatal) {
        isFatal = fatal;
    }


    @Override
    public String toString() {
        return "HeartbeatEvent{" +
                "sequence=" + sequence +
                ", type='" + type + '\'' +
                ", bufferLength=" + bufferLength +
                ", eventTimeMillisec=" + eventTimeMillisec +
                ", oldValue=" + oldValue +
                ", newValue=" + newValue +
                ", errCode=" + errCode +
                ", isFatal=" + isFatal +
                '}';
    }
}
