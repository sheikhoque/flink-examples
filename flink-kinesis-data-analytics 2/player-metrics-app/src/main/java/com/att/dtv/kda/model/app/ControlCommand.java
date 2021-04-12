package com.att.dtv.kda.model.app;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.sql.Timestamp;

public class ControlCommand implements Serializable, TimeProcessable {

    private String partitionId;

    public void setPartitionId(String id) {
        this.partitionId = id;
    }

    public String getPartitionId() {
        return this.partitionId;
    }

    @SerializedName("code")
    private int code;

    @SerializedName("arg")
    private String argument;

    private Timestamp processingTimestamp;


    public ControlCommand(){}

    public ControlCommand(int commandCode, String commandArgument) {
        this.code = commandCode;
        this.argument = commandArgument;
    }

    @Override
    public void setEventTime(Timestamp eventTimestamp) {
        this.processingTimestamp = eventTimestamp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public double getEventTime(){
        return 0;
    }


    public Timestamp getProcessingTimestamp() {
        return processingTimestamp;
    }

    public void setProcessingTimestamp(Timestamp processingTimestamp) {
        this.processingTimestamp = processingTimestamp;
    }

    @Override
    public String toString() {
        return "ControlCommand{" +
                "commandCode=" + code +
                ", commandArgument='" + argument + '\'' +
                ", processingTimestamp=" + processingTimestamp +
                '}';
    }
}
