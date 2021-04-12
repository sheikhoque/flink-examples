package com.att.dtv.kda.model.es;

import com.google.gson.annotations.SerializedName;

/**
 * BufferLengthStats POJO class
 */
public class BufferLengthStats {

    // use this to avoid any serialization deserialization used within Flink
    public static final long serialVersionUID = 51L;


    @SerializedName("value")
    private double value;

    @SerializedName("start")
    private int start;

    @SerializedName("end")
    private int end;

    public BufferLengthStats() {

    }

    public BufferLengthStats(double value, int start, int end) {
        super();
        this.value = value;
        this.start = start;
        this.end = end;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "BufferLengthStats [value=" + value + ", start=" + start + ", end=" + end + "]";
    }


}
