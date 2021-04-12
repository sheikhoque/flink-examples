package com.att.dtv.kda.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;


public class Interval implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Interval.class);

    public static final int HOUR = 60;
    public static final int DAY = 60 * 24;
    public static final int DEFAULT_RESOLUTION = HOUR;


    private long startTS;
    private long endTS;

    private int length;

    /**
     * Just in case there is custom resolution is needed.
     */
    public Interval() {
    }

    /**
     * Copy constructor. Create new copy of resolution.
     *
     * @param orig - original object to copy from
     */
    public Interval(Interval orig) {
        this.length = orig.length;
        this.startTS = orig.startTS;
        this.endTS = orig.endTS;
    }

    public Interval(int lengthInMinutes) {
        this.length = lengthInMinutes;

        if (lengthInMinutes <= 0) {
            LOG.warn("Resolution ({} minutes) cannot be <= 0. Using default resolution ({} minutes)", lengthInMinutes, 1);
            this.length = 1;
        } else if (lengthInMinutes > DAY) {
            LOG.warn("Resolution cannot be greater than 1 day. Using default resolution of one day ({} minutes)", DAY);
            this.length = DAY;
        } else {
            this.length = getIntervalLength(lengthInMinutes);
        }
    }

    /**
     * Calculates effective interval length as following:
     * if interval length is under an hour, round it to whole fractions of hour.
     * e.g.
     * if interval length is  7 minutes, round it down to  6 minutes;
     * if interval length is 25 minutes, round it down to 20 minutes;
     * if interval length is 35 minutes, round it down to 30 minutes
     * if interval length is over an hour but under 1 day, round it to whole fractions of day.
     * e.g.
     * if interval length is  5 hours, round it down to  4 hours;
     * if interval length is  7 hours, round it down to  6 hours;
     * if interval length is 23 hours, round it down to 12 hours
     *
     * @param initialLength - requested interval length.
     * @return effective interval length for convenient presentation.
     */
    public static int getIntervalLength(int initialLength) {
        int i = 0, result = DEFAULT_RESOLUTION;

        final int timeScale = initialLength < HOUR ? HOUR : DAY;

        for (i = 1; i < timeScale && !(timeScale % i == 0 && timeScale / i <= initialLength); i++) {
        }
        result = timeScale / i;
        return result;
    }

    public static Interval fromTS(long ts, int lengthInMinutes) {
        final Interval interval = new Interval(lengthInMinutes);
        return interval.withTS(ts);
    }

    /**
     * Apply current timestamp to set <code>startTS</code> and <code>endTS</code> for given time
     *
     * @param ts - timestamp to define start and end of interval length
     * @return interval length with start and end timestamps defined.
     */
    public Interval withTS(long ts) {
        final long rounder = length * 1000 * 60;
        assert (rounder != 0);
        startTS = ts / rounder * rounder;
        endTS = startTS + rounder;
        return this;
    }

    /**
     * @return next interval length with updated start and end timestamps
     */
    public Interval next() {
        final Interval nextInterval = new Interval(this);
        nextInterval.startTS = this.endTS;
        nextInterval.endTS = this.endTS + length * 60 * 1000; // convert minutes to milliseconds

        return nextInterval;
    }

    /**
     * @return previous interval length with updated start and end timestamps
     */
    public Interval prev() {
        final Interval prevInterval = new Interval(this);
        prevInterval.endTS = this.startTS;
        prevInterval.startTS = this.startTS - length * 60 * 1000; // convert minutes to milliseconds

        return prevInterval;
    }

    public boolean contains(long ts){
        return ts >= this.getStartTS() && ts <= this.getEndTS();
    }

    public long getStartTS() {
        return startTS;
    }

    public void setStartTS(long startTS) {
        this.startTS = startTS;
    }

    public long getEndTS() {
        return endTS;
    }

    public void setEndTS(long endTS) {
        this.endTS = endTS;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean equals(Interval other) {

        return other != null &&
                (this == other ||
                        (
                                this.length == other.length
                                        && this.startTS == other.startTS
                                        && this.endTS == other.endTS
                        )
                );
    }
}
