package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class Color extends NamedItem {

    @SerializedName("hex")
    private String hex;

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }
}
