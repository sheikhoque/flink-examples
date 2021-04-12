package com.att.dtv.kda.model.app;

import java.io.Serializable;
import java.util.Comparator;

public class SessionPlayTimeComparator implements Comparator<String>, Serializable {

    @Override
    public int compare(String firstKey, String secondKey) {
        String [] firstParts = firstKey.split("-");
        String [] secondParts = secondKey.split("-");

        Long first = Long.parseLong(firstParts[0]);
        Long second = Long.parseLong(secondParts[0]);

        return (int)(first - second);
    }
}
