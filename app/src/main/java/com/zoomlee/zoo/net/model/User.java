package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
public class User extends Person {

    public static final String USER_PIC_NAME = "custom_me.png";
    public static final String USER_144PIC_NAME = "custom_me_144x144.png";

    @SerializedName("email")
    private String email;
    @SerializedName("phone")
    private String phone;
    @SerializedName("country_id")
    private int countryId = -1;
    @SerializedName("private_key")
    private String privateKey;
    @SerializedName("create_time")
    private int createTime;
    @SerializedName("plans")
    private List<BillingPlan> plans;
    @SerializedName("invites_count")
    private int invitesCount;

    @Override
    public String getName() {
        return "Me";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public List<BillingPlan> getPlans() {
        return plans;
    }

    public void setPlans(List<BillingPlan> plans) {
        this.plans = plans;
    }

    public int getInvitesCount() {
        return invitesCount;
    }

    public void setInvitesCount(int invitesCount) {
        this.invitesCount = invitesCount;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", countryId=" + countryId +
                ", privateKey='" + privateKey + '\'' +
                ", createTime=" + createTime +
                ", plans=" + plans +
                ", invitesCount=" + invitesCount +
                '}';
    }
}
