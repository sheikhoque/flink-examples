package com.amazonaws.kaja.samples.customfactory.avrowithschemaregistry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

class JodaConverter {
    private static JodaConverter instance;
    private static boolean instantiated = false;

    public static JodaConverter getConverter() {
        if (instantiated) {
            return instance;
        } else {
            try {
                Class.forName("org.joda.time.DateTime", false, Thread.currentThread().getContextClassLoader());
                instance = new JodaConverter();
            } catch (ClassNotFoundException var4) {
                instance = null;
            } finally {
                instantiated = true;
            }

            return instance;
        }
    }

    public long convertDate(Object object) {
        LocalDate value = (LocalDate)object;
        return value.toDate().getTime();
    }

    public int convertTime(Object object) {
        LocalTime value = (LocalTime)object;
        return value.get(DateTimeFieldType.millisOfDay());
    }

    public long convertTimestamp(Object object) {
        DateTime value = (DateTime)object;
        return value.toDate().getTime();
    }

    private JodaConverter() {
    }
}

