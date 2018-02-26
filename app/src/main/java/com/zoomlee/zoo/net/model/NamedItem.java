package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public abstract class NamedItem extends BaseItem {

    @SerializedName("name")
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NamedItem{" +
                "name='" + name + '\'' +
                ", remoteId='" + remoteId + '\'' +
                ", status=" + status +
                ", updateTime=" + updateTime +
                '}';
    }
}
