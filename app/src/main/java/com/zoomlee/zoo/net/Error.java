package com.zoomlee.zoo.net;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/13/15
 */

public class Error {
    public static final int NO_CONNECTION_CODE = 0x139;
    private int code = 0;
    private String reason = null;

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Error { code:" + code + ", reason:" + reason + "}";
    }
}