package com.zoomlee.zoo.utils;

import android.app.Activity;
import android.content.Intent;

import com.zoomlee.zoo.net.model.BillingPlan;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.ui.activity.SubscriptionActivity;

import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 5/21/15
 */
public class BillingUtils {
    public static interface BillingType {
        int ZOOMLEE_BASIC = 0;
        int ZOOMLEE_PRO_MONTH = 1;
        int ZOOMLEE_PRO_YEAR = 2;
        int ZOOMLEE_FAMILY_MONTH = 3;
        int ZOOMLEE_FAMILY_YEAR = 4;
        int ZOOMLEE_PRO_TRIAL = 5;
        int ZOOMLEE_FAMILY_TRIAL = 6;
    }

    public static String getBillingPlanName(User user) {
        String planName = "No Plan";
        List<BillingPlan> plans = user.getPlans();
        for (BillingPlan plan : plans) {
            if (plan.isPro() && plan.isActive())
                return "Zoomlee Pro";
            if (plan.isFamily() && plan.isActive())
                planName = "Family";
        }
        return planName;
    }

    public static boolean isPro(User user) {
        List<BillingPlan> plans = user.getPlans();
        for (BillingPlan plan : plans) {
            if (plan.isPro() && plan.isActive())
                return true;
        }
        return false;
    }

    public static boolean isProTrial(User user) {
        List<BillingPlan> plans = user.getPlans();
        for (BillingPlan plan : plans) {
            if (plan.getPlanType() == BillingType.ZOOMLEE_PRO_TRIAL && plan.isActive())
                return true;
        }
        return false;
    }

    public static boolean isProUsed(User user) {
        List<BillingPlan> plans = user.getPlans();
        for (BillingPlan plan : plans) {
            if (plan.isPro() && plan.getValidTo() != null)
                return true;
        }
        return false;
    }

    public static boolean isFamily(User user) {
        List<BillingPlan> plans = user.getPlans();
        boolean isFamily = false;
        for (BillingPlan plan : plans) {
            if (plan.isPro() && plan.isActive())
                return false;
            if (plan.isFamily() && plan.isActive())
                isFamily = true;
        }
        return isFamily;
    }

    public static boolean isFamilyContent(User user) {
        List<BillingPlan> plans = user.getPlans();
        for (BillingPlan plan : plans) {
            if (plan.isActive() && (plan.isPro() || plan.isFamily()))
                return true;
        }
        return false;
    }

    public static boolean canFamilyTrial(User user) {
        List<BillingPlan> plans = user.getPlans();
        for (BillingPlan plan : plans) {
            if (plan.getPlanType() == BillingType.ZOOMLEE_FAMILY_TRIAL && !plan.isActive() && plan.getValidTo() == null)
                return true;
        }
        return false;
    }

    public static boolean need2RequestSubscribe(User user) {
        List<BillingPlan> plans = user.getPlans();
        boolean proUsed = false;
        boolean proActive = false;
        boolean familyUsed = false;
        boolean familyActive = false;
        for (BillingPlan plan : plans) {
            if (plan.isFamily() && plan.isActive()) {
                familyActive = true;
                continue;
            }
            if (plan.isPro() && plan.isActive()) {
                proActive = true;
                continue;
            }
            if (plan.isPro() && plan.getValidTo() != null) {
                proUsed = true;
                continue;
            }
            if (plan.isFamily() && plan.getValidTo() != null) {
                familyUsed = true;
            }

        }

        return !proActive && (proUsed || !familyActive && familyUsed);
    }

    public enum ActionType {

        TAXES, ADD_PERSON, RENEW, SELECT_PERSON, MANAGE_FAMILY_MEMBERS, MANAGE_SUBSCRIPTION, IMMIGRATION_FORMS,
        /**
         * Designed to make empty IntendedAction, so that no one could respond to it.
         */
        NONE
    }

    /**
     * Check whether user can start action and if not pass him to subscription activity.
     *
     * @param activity   to use
     * @param actionType to start
     * @return true if can start
     */
    public static boolean canStart(Activity activity, ActionType actionType) {
        boolean canStart = canStartDry(actionType);

        if (!canStart) {
            SubscriptionActivity.startActivity(activity, actionType);

        }

        return canStart;
    }

    /**
     * Check if user can start this action without going to subscription screen.
     *
     * @param actionType to check
     * @return true if can go
     */
    public static boolean canStartDry(ActionType actionType) {
        boolean canStart = true;
        User user = SharedPreferenceUtils.getUtils().getUserSettings();
        switch (actionType) {
            case IMMIGRATION_FORMS:
                if (!isPro(user) && !isProTrial(user)) {
                    canStart = false;
                }
                break;
            case TAXES:
                if (!isProUsed(user)) {
                    canStart = false;
                }
                break;
            case MANAGE_FAMILY_MEMBERS:
            case SELECT_PERSON:
            case ADD_PERSON:
                if (!isFamilyContent(user)) {
                    canStart = false;
                }
                break;
        }
        return canStart;
    }

    /**
     * Gets intended action before user was passed to subscription screen.
     *
     * @param resultCode to use
     * @param data       to use
     * @return intended action
     */
    public static IntendedAction intendedAction(int resultCode, Intent data) {
        if (data == null) {
            return new IntendedAction(ActionType.NONE, false);
        }
        ActionType actionType = (ActionType) data.getSerializableExtra(IntentUtils.EXTRA_ACTION_TYPE);
        return new IntendedAction(actionType, resultCode == Activity.RESULT_OK);
    }

    public static class IntendedAction {

        public final ActionType actionType;
        public final boolean success;

        public IntendedAction(ActionType actionType, boolean success) {
            this.actionType = actionType;
            this.success = success;
        }
    }
}
