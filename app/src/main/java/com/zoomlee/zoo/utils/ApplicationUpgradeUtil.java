package com.zoomlee.zoo.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.io.File;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.04.15.
 */
public class ApplicationUpgradeUtil {

    private static final String APP_CODE = "app_version_code";

    private static final int VERSION_CODE_2_0_0 = 2;
    private static final int VERSION_CODE_2_0_1 = 4;
    private static final int VERSION_CODE_2_1 = 7;

    private Context context;

    public static ApplicationUpgradeUtil getInstance(Context context) {
        return new ApplicationUpgradeUtil(context);
    }

    private ApplicationUpgradeUtil(Context context) {
        this.context = context;
    }

    public void upgradeApp() {
        try {
            int prevVersion = SharedPreferenceUtils.getUtils().getIntSetting(APP_CODE);
            int newVersionCode = getCurrentAppVersionCode();

            if (prevVersion != 0 && prevVersion == newVersionCode) return; // no upgrade required

            if (prevVersion == 0) {
                // clear or upgrade from version 1.0
                clearFirstVersionAppData();
            } else {
                if (prevVersion < VERSION_CODE_2_0_1) {
                    new File("/sdcard/internal.db").delete();
                }
                if (prevVersion < VERSION_CODE_2_1) {
                    SharedPreferenceUtils.getUtils().setIntSetting(LastSyncTimeKeys.STATIC_DATA, 0);
                    SharedPreferenceUtils.getUtils().setIntSetting(LastSyncTimeKeys.COUNTRIES, 0);
                }
            }
            SharedPreferenceUtils.getUtils().setIntSetting(APP_CODE, newVersionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getCurrentAppVersionCode() {
        int newVersionCode = 0;
        try {
            newVersionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return newVersionCode;
    }

    private void clearFirstVersionAppData() {
        clearCacheDir();
        clearFilesDir();
        clearSharedPreferencesDir();
    }

    private void clearCacheDir() {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                FileUtil.deleteDir(dir);
            }
        } catch (Exception e) {
        }
    }

    private void clearFilesDir() {
        try {
            File dir = context.getFilesDir();
            if (dir != null && dir.isDirectory()) {
                FileUtil.deleteDir(dir);
            }
        } catch (Exception e) {
        }
    }

    private void clearSharedPreferencesDir() {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        File sharedPrefsDir = new File(appDir, "shared_prefs");
        if(sharedPrefsDir.exists()){
            String[] children = appDir.list();
            for(String s : children){
                if(!s.equals(SharedPreferenceUtils.PREF_NAME)){
                    new File(appDir, s).delete();
                }
            }
        }
    }
}