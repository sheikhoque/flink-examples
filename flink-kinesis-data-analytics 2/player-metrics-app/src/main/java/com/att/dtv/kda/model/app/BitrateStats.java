package com.att.dtv.kda.model.app;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Bitrate POJO class
 */
public class BitrateStats implements Serializable {

    // use this to avoid any serialization deserialization used within Flink
    public static final long serialVersionUID = 50L;


    @SerializedName("value")
    private double value;

    @SerializedName("start")
    private long start;

    @SerializedName("end")
    private long end;

    private long weight;

    public BitrateStats() {

    }

    public BitrateStats(double value, long start, long end) {
        this(value, start, end, end - start);
    }

    public BitrateStats(double value, long start, long end, long weight) {
        super();
        this.value = value;
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value))

            this.value = value;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "BitrateStats [value=" + value + ", start=" + start + ", end=" + end + ", weight=" + weight + "]";
    }

    public void update(double value, long endTS) {
        if (this.end >= endTS) { // already including
            return;
        }
        long updateWeight = endTS - this.end;

        long newTotalWeight = this.weight + updateWeight;

        this.value = newTotalWeight == 0 ? 0.0d : (this.value * this.weight + value * updateWeight) / newTotalWeight;
        this.end = endTS;
        this.weight = newTotalWeight;
    }

    public void merge(BitrateStats other) {
        if (other == null) return;

        if (this.value == 0 && other.value != 0) {
            this.value = other.value;
            this.start = other.end;
            this.end = other.end;
            this.weight = 0;
            return;
        }

        long updatedOtherWeight;

        if (other.value == 0) {
            updatedOtherWeight = 0;
        } else if (other.start == 0 && other.weight == 0) {
            updatedOtherWeight = other.end - this.end;
        } else {
            updatedOtherWeight = other.weight;
        }

        long startTime = other.start == 0 ? this.end : other.start;

        double newWeight = this.weight + updatedOtherWeight;
        this.end = Math.max(this.end, other.end);
        this.start = Math.min(this.start, startTime);

        this.value = newWeight > 0 ? (this.value * this.weight + other.value * updatedOtherWeight) / newWeight : 0.0d;
    }
}
