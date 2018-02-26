/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zoomlee.zoo.syncservice;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {
    private static final long ONE_MINUTE = 60L; // one minute in seconds

    private static Context context;

    public static void init(Context applicationContext) {
        SyncUtils.context = applicationContext;
    }

    public static void triggerSync() {
        triggerSync(false);
    }

    private static void triggerSync(boolean addPeriodicTasks) {
        if (isNetworkActive(context)) {
            DeveloperUtil.michaelLog("SyncUtils.triggerSync start service");
            Intent intent = new Intent(context, SyncService.class);
            intent.putExtra(SyncService.PERIODIC_SYNC, addPeriodicTasks);
            context.startService(intent);
        } else {
            DeveloperUtil.michaelLog("SyncUtils.triggerSync no internet");
            SharedPreferenceUtils.getUtils().setBooleanSettings(SharedPreferenceUtils.REQUIRE_SYNC, true);
        }
    }

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private static final Runnable beeper = new Runnable() {
        public void run() {
            triggerSync(true);
        }
    };

    private static ScheduledFuture beeperHandle;
    private static int counter = 0;

    /**
     * call from UI thread
     */
    public static void startPeriodicRefresh() {
        if (beeperHandle == null)
            beeperHandle = scheduler.scheduleAtFixedRate(beeper, ONE_MINUTE, ONE_MINUTE, TimeUnit.SECONDS);
        counter++;
    }

    /**
     * call from UI thread
     */
    public static void cancelPeriodicRefresh() {
        counter--;
        if (beeperHandle != null && counter == 0) {
            beeperHandle.cancel(true);
            beeperHandle = null;
        }
    }

    public static boolean isNetworkActive(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable())
            return false;
        return true;
    }
}
