package com.zoomlee.zoo.utils;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.zoomlee.zoo.R;

import java.util.Map;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 3/27/15
 */
public class GAUtil {
    public static final String PROPERTY_ID = "UA-60886151-2";

    private static GAUtil utils;
    private Tracker appActivityTracker;

    public static void init(Context context) {
        utils = new GAUtil(context);
    }

    public static GAUtil getUtil() {
        return utils;
    }

    private GAUtil(Context ctx) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(ctx);
        appActivityTracker = analytics.newTracker(R.xml.global_tracker);
    }

    public Tracker getTracker() {
        return appActivityTracker;
    }

    public void screenAccess(String action) {
        screenAccess(action, null);
    }

    public void screenAccess(String screenName, String label) {
        sendEvent(GAEvents.CATEGORY_SCREEN_ACCESS, screenName + " Access", label);
    }

    public void eventMade(String action, String label) {
        sendEvent(GAEvents.CATEGORY_EVENTS_MADE, action, label);
    }

    public void timeSpent(String screenName) {
        sendScreen(screenName);
    }

    private void sendEvent(String category, String action, String label) {
        if(label !=null)
        action += " [ " + label + " ]";
        appActivityTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setValue(1)
                .build());
    }

    private void sendScreen(String path) {
        appActivityTracker.setScreenName(path);
        appActivityTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void sendData(Map<String, String> params) {
        appActivityTracker.send(params);
    }
}
