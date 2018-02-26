package com.zoomlee.zoo.utils;

import android.app.Activity;
import android.os.Build;

public final class ActivityUtils {

    /**
     * Finish and remove task compat.
     * @param activity to finish
     */
    public static void finishAndRemoveTask(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.finishAndRemoveTask();
        } else {
            activity.finish();
        }
    }
}
