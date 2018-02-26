package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.BaseItem;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.provider.helpers.BaseProviderHelper.DataColumns;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
public abstract class DaoHelper<Entity extends BaseItem> {

    private final String LAST_SYNC_TIME_KEY;
    protected final Uri CONTENT_URI;

    protected DaoHelper(String lastSyncTimeKey, Uri contentUri) {
        this.LAST_SYNC_TIME_KEY = lastSyncTimeKey;
        this.CONTENT_URI = contentUri;
    }

    /**
     * Save entities to content provider.
     * It will update item by {@linkplain DataColumns#_ID}
     * or {@linkplain DataColumns#REMOTE_ID} (if it's not -1)
     *
     * @param context
     * @param items
     * @return the number of newly created rows
     */
    protected int saveItems(Context context, List<Entity> items) {
        if (Util.isEmpty(items)) return 0;

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues[] contentValuesArray = new ContentValues[items.size()];
        for (int i = 0; i < items.size(); i++) {
            contentValuesArray[i] = convertEntity(items.get(i));
        }
        return contentResolver.bulkInsert(getURI(), contentValuesArray);
    }


    protected Uri saveItem(Context context, Entity entity) {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.insert(getURI(), convertEntity(entity));
    }

    /**
     * @param changes
     * @return <i>true</i> if data loaded
     */
    public boolean downloadItems(Context context, Object api, Changes changes) {
        boolean success = true;
        if (!requireUpdate(changes)) return success;

        int newLastItemUpdate = getLastSyncTime();
        try {
            CommonResponse<List<Entity>> commonResponse = callApi(context, api);
            if (commonResponse.getError().getCode() == 200) {
                List<Entity> items = commonResponse.getBody();
                saveRemoteChanges(context, items);
                for (Entity item: items)
                    if (item.getUpdateTime() > newLastItemUpdate) {
                        newLastItemUpdate = item.getUpdateTime() + 1;
                    }
                setLastSyncTime(newLastItemUpdate);
            } else {
                success = false;
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    /**
     * Get all not deleted items
     *
     * @param context
     * @return
     */
    public List<Entity> getAllItems(Context context) {
        String selection = DataColumns.STATUS + "=1";
        return getAllItems(context, selection, null, null);
    }

    public List<Entity> getAllItems(Context context, String selection, String[] selectionArgs, String sortOrder) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(getURI(), null, selection, selectionArgs, sortOrder);

        List<Entity> result = readItems(context, cursor);
        cursor.close();
        return result;
    }

    public Entity getItemByLocalId(Context context, int localId) {
        return getItemByLocalId(context, localId, false);
    }

    public Entity getItemByLocalId(Context context, int localId, boolean showDeleted) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = DataColumns._ID + "=" + localId;
        selection = selection + (showDeleted ? "" : " AND " + DataColumns.STATUS + "=1");
        Cursor cursor = contentResolver.query(getURI(), null, selection, null, null);

        List<Entity> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    public Entity getItemByRemoteId(Context context, int remoteId) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(getURI(), null, DataColumns.REMOTE_ID + "=?", DBUtil.getArgsArray(remoteId), null);

        List<Entity> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    final protected void setLastSyncTime(int timestamp) {
        SharedPreferenceUtils.getUtils()
                .setIntSetting(LAST_SYNC_TIME_KEY, timestamp);
    }

    final protected int getLastSyncTime() {
        return SharedPreferenceUtils.getUtils().getIntSetting(LAST_SYNC_TIME_KEY);
    }

    final protected Uri getURI() {
        return CONTENT_URI;
    }

    public void deleteByLocalId(Context context, int localId) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(Uri.parse(getURI() + "/" + localId), null, null);
    }

    public Uri saveLocalChanges(Context context, Entity item) {
        throw new UnsupportedOperationException();
    }

    public void deleteItem(Context context, Entity item) {
        throw new UnsupportedOperationException();
    }

    public Uri saveRemoteChanges(Context context, Entity item) {
        return saveItem(context, item);
    }

    public int saveRemoteChanges(Context context, List<Entity> items) {
        return saveItems(context, items);
    }

    public static interface OnItemLoadedListener<Entity extends BaseItem> {
        void onItemLoaded(Entity item);
    }

    public abstract boolean requireUpdate(Changes changes);
    public abstract List<Entity> readItems(Context context, Cursor cursor);
    public abstract List<Entity> readItems(Context context, Cursor cursor, OnItemLoadedListener<Entity> onItemLoadedListener);

    protected abstract CommonResponse<List<Entity>> callApi(Context context, Object api);
    protected abstract ContentValues convertEntity(Entity entity);
}