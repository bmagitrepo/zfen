package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.04.15.
 */
public class Invite {

    public static final int TYPE_INVITED_BY_EMAIL = 0;
    public static final int TYPE_INVITED_BY_PHONE = 1;
    public static final int TYPE_INVITED_BY_EMAIL_EXIST = 2;
    public static final int TYPE_INVITED_BY_PHONE_EXIST = 3;

    @SerializedName("id")
    protected int remoteId = -1;
    @SerializedName("user_id")
    protected int userId = -1;
    @SerializedName("type")
    protected int type;
    @SerializedName("to")
    protected String toEmailPhone;
    @SerializedName("create_time")
    protected int createTime;

    public int getRemoteId() {
        return remoteId;
    }

    public int getUserId() {
        return userId;
    }

    public int getType() {
        return type;
    }

    public String getToEmailPhone() {
        return toEmailPhone;
    }

    public int getCreateTime() {
        return createTime;
    }
}
