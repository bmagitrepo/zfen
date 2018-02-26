package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class Country extends NamedItem {

    @SerializedName("code")
    private String code;
    @SerializedName("prioritize")
    private int prioritize;
    @SerializedName("flag")
    private String flag;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPrioritize() {
        return prioritize;
    }

    public void setPrioritize(int prioritize) {
        this.prioritize = prioritize;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "Country{" +
                "code='" + code + '\'' +
                ", prioritize=" + prioritize +
                ", flag=" + flag +
                "} " + super.toString();
    }
}
