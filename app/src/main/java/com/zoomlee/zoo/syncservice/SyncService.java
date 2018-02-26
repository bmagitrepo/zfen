package com.zoomlee.zoo.syncservice;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.provider.helpers.RestTasksHelper;
import com.zoomlee.zoo.syncservice.resttask_executors.TaskExecutorFactory;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 29.04.15.
 */
public class SyncService extends IntentService {
    private static final String NAME = "SyncService";

    public static final String PERIODIC_SYNC = "periodic_sync";
    private volatile boolean queueFull = false;

    public SyncService() {
        super(NAME);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        DeveloperUtil.michaelLog("SyncService.onStart queueFull " + queueFull);
        if (!queueFull) {
            super.onStart(intent, startId);
            queueFull = true;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        queueFull = false;
        int status = SyncEvents.SYNC_ERROR;
        SyncEvents.SyncServiceStatus syncServiceStatus = new SyncEvents.SyncServiceStatus(SyncEvents.SYNC_STARTED);
        EventBus.getDefault().postSticky(syncServiceStatus);
        if (SyncUtils.isNetworkActive(this)) {
            DeveloperUtil.michaelLog("SyncService.onHandleIntent performSync");
            SharedPreferenceUtils.getUtils().setBooleanSettings(SharedPreferenceUtils.REQUIRE_SYNC, false);
            status = performSync(intent);
        } else {
            DeveloperUtil.michaelLog("SyncService.onHandleIntent no internet");
            SharedPreferenceUtils.getUtils().setBooleanSettings(SharedPreferenceUtils.REQUIRE_SYNC, true);
        }
        EventBus.getDefault().removeStickyEvent(syncServiceStatus);
        EventBus.getDefault().postSticky(new SyncEvents.SyncServiceStatus(status));
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SyncService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private int performSync(Intent intent) {
        boolean periodicSync = intent.getBooleanExtra(PERIODIC_SYNC, false);
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = null;
        Set<RestTask> restTasks = null;

        try {
            cursor = contentResolver.query(RestTasksHelper.RestTasksContract.CONTENT_URI, null, null, null, RestTasksHelper.RestTasksContract.TYPE + " ASC");
            if (cursor != null) {
                restTasks = new LinkedHashSet<RestTask>(cursor.getCount());
                int idIndex = cursor.getColumnIndex(RestTasksHelper.RestTasksContract._ID);
                int internalIdIndex = cursor.getColumnIndex(RestTasksHelper.RestTasksContract.LOCAL_ID);
                int typeIndex = cursor.getColumnIndex(RestTasksHelper.RestTasksContract.TYPE);

                while (cursor.moveToNext()) {
                    RestTask restTask = new RestTask();
                    restTask.setId(cursor.getInt(idIndex));
                    restTask.setLocalItemId(cursor.getInt(internalIdIndex));
                    restTask.setType(cursor.getInt(typeIndex));
                    restTasks.add(restTask);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
            if (restTasks == null) restTasks = new LinkedHashSet<RestTask>();
        }

        if (periodicSync) {
            restTasks.add(new RestTask(RestTask.Types.USER_GET));
            restTasks.add(new RestTask(RestTask.Types.USER_DATA_GET));
            restTasks.add(new RestTask(RestTask.Types.STATIC_DATA_GET));
        }

        return proceedTasks(restTasks);
    }

    private int proceedTasks(Set<RestTask> restTasks) {
        List<RestTask> executedTasks = new ArrayList<RestTask>();
        TaskExecutorFactory.RestTaskExecutor taskExecutor;
        for (RestTask restTask : restTasks) {
            SyncEvents.RestTaskStatus restTaskStatus = new SyncEvents.RestTaskStatus(SyncEvents.SYNC_STARTED, restTask);
            EventBus.getDefault().postSticky(restTaskStatus);

            taskExecutor = TaskExecutorFactory.getExecutor(restTask.getType());
            if (taskExecutor != null && taskExecutor.execute(this, restTask)) {
                EventBus.getDefault().removeStickyEvent(restTaskStatus);
                EventBus.getDefault().post(new SyncEvents.RestTaskStatus(SyncEvents.SYNC_FINISHED, restTask));
                executedTasks.add(restTask);
            } else {
                EventBus.getDefault().removeStickyEvent(restTaskStatus);
                EventBus.getDefault().post(new SyncEvents.RestTaskStatus(SyncEvents.SYNC_ERROR, restTask));
            }
        }

        ContentResolver contentResolver = getContentResolver();
        for (RestTask restTask : executedTasks) {
            contentResolver.delete(Uri.parse(RestTasksHelper.RestTasksContract.CONTENT_URI + "/" + restTask.getId()), null, null);
        }

        if (executedTasks.size() == 0 && restTasks.size() != 0) return SyncEvents.SYNC_ERROR;
        else return SyncEvents.SYNC_FINISHED;
    }
}
