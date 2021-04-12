package com.att.dtv.kda.model.app;

import java.util.Objects;

public class ChangeEvent<T> {
    public static final long serialVersionUID = 52L;
    private EventField eventField;
    private T eventValue;
    private Long ts;


    public ChangeEvent(EventField eventField, T eventValue, Long ts) {
        this.eventField = eventField;
        this.eventValue = eventValue;
        this.ts = ts;
    }

    public ChangeEvent(EventField eventField, T eventValue) {
        this.eventField = eventField;
        this.eventValue = eventValue;
    }

    public EventField getEventField() {
        return eventField;
    }

    public void setEventField(EventField eventField) {
        this.eventField = eventField;
    }

    public T getEventValue() {
        return eventValue;
    }

    public void setEventValue(T eventValue) {
        this.eventValue = eventValue;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public ChangeEvent<T> toKey() {
        return new ChangeEvent<T>(this.eventField, this.eventValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeEvent<?> that = (ChangeEvent<?>) o;
        return eventField == that.eventField &&
                Objects.equals(eventValue, that.eventValue) &&
                Objects.equals(ts, that.ts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventField, eventValue, ts);
    }

    @Override
    public String toString() {
        return "ChangeEvent{" +
                "eventKey=" + eventField +
                ", eventValue=" + eventValue +
                '}';
    }

    public static <T> ChangeEvent of(EventField eventField, T eventValue) {
        return new ChangeEvent<T>(eventField, eventValue);
    }

    public static <T> ChangeEvent of(EventField eventField, T eventValue, Long ts) {
        return new ChangeEvent<T>(eventField, eventValue, ts);
    }


}
