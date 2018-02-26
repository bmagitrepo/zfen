package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public abstract class BaseItem{

    public static final int STATUS_DELETED = 0;
    public static final int STATUS_NORMAL = 1;

    protected transient int id = -1;
    @SerializedName("id")
    protected int remoteId = -1;
    @SerializedName("status")
    protected int status = STATUS_NORMAL;
    @SerializedName("update_time")
    protected int updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "BaseItem{" +
                "id=" + id +
                ", remoteId=" + remoteId +
                ", status=" + status +
                ", updateTime=" + updateTime +
                '}';
    }
}
