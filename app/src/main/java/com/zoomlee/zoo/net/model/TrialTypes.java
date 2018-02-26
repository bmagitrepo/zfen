package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;
import com.zoomlee.zoo.utils.BillingUtils;

/**
 * Author vbevans94.
 */
public class TrialTypes {

    @SerializedName("" + BillingUtils.BillingType.ZOOMLEE_PRO_TRIAL)
    private volatile int proTrialMonths = 0;

    @SerializedName("" + BillingUtils.BillingType.ZOOMLEE_FAMILY_TRIAL)
    private volatile int familyTrialMonths = 0;

    private TrialTypes() {
    }

    public int getFamilyTrialMonths() {
        return familyTrialMonths;
    }

    public int getProTrialMonths() {
        return proTrialMonths;
    }
}
