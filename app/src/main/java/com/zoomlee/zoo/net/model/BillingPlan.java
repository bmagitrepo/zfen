package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import com.zoomlee.zoo.utils.BillingUtils.BillingType;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 5/21/15
 */
public class BillingPlan implements Parcelable {
    @SerializedName("plan")
    private int planType;
    private int active;
    @SerializedName("plan_valid_to")
    private String validTo;


    public int getPlanType() {
        return planType;
    }

    public void setPlanType(int planType) {
        this.planType = planType;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public boolean isPro() {
        return planType == BillingType.ZOOMLEE_PRO_MONTH || planType == BillingType.ZOOMLEE_PRO_YEAR;
    }

    public boolean isFamily() {
        return planType == BillingType.ZOOMLEE_FAMILY_MONTH || planType == BillingType.ZOOMLEE_FAMILY_YEAR || planType == BillingType.ZOOMLEE_FAMILY_TRIAL;
    }

    public boolean isActive(){
        return active == 1;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.planType);
        dest.writeInt(this.active);
        dest.writeString(this.validTo);
    }

    public BillingPlan() {
    }

    private BillingPlan(Parcel in) {
        this.planType = in.readInt();
        this.active = in.readInt();
        this.validTo = in.readString();
    }

    public static final Parcelable.Creator<BillingPlan> CREATOR = new Parcelable.Creator<BillingPlan>() {
        public BillingPlan createFromParcel(Parcel source) {
            return new BillingPlan(source);
        }

        public BillingPlan[] newArray(int size) {
            return new BillingPlan[size];
        }
    };

    @Override
    public String toString() {
        return "BillingPlan{" +
                "planType=" + planType +
                ", active=" + active +
                ", validTo='" + validTo + '\'' +
                '}';
    }
}
