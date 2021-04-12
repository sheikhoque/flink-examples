package com.amazonaws.kaja.samples.udfs;

import org.apache.flink.table.functions.ScalarFunction;

public class PercentageUserSession extends ScalarFunction {
    public double eval(final long totalSessionCount, final long buySessionCount){
        return ((double)buySessionCount/totalSessionCount) *100;
    }
}
