package com.att.dtv.kda.model.app;

import java.io.Serializable;
import java.util.Objects;

public class ClientError implements Serializable {
    public static final long serialVersionUID = 49L;

    private String errorCode;
    private boolean isFatal;
    private Integer count;

    public ClientError(String errorCode, boolean isFatal) {
        this.errorCode = errorCode;
        this.isFatal = isFatal;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isFatal() {
        return isFatal;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientError that = (ClientError) o;
        return errorCode == that.errorCode &&
                isFatal == that.isFatal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, isFatal);
    }

    @Override
    public String toString() {
        return "ClientError{" +
                "errorCode='" + errorCode + '\'' +
                ", isFatal=" + isFatal +
                ", count=" + count +
                '}';
    }
}
