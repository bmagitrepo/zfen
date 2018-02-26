package com.zoomlee.zoo.syncservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DeveloperUtil.michaelLog("NetworkChangeReceiver.onReceive");
        if (SharedPreferenceUtils.getUtils().getBooleanSettings(SharedPreferenceUtils.REQUIRE_SYNC)
                && SyncUtils.isNetworkActive(context))
            DeveloperUtil.michaelLog("NetworkChangeReceiver triggerSync");
            SyncUtils.triggerSync();
    }
}

