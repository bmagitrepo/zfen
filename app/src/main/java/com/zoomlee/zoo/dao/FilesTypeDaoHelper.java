package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.FilesType;
import com.zoomlee.zoo.provider.helpers.FileTypesProviderHelper.FilesTypesContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class FilesTypeDaoHelper extends DaoHelper<FilesType> {

    protected FilesTypeDaoHelper() {
        super(LastSyncTimeKeys.FILES_TYPES, FilesTypesContract.CONTENT_URI);
    }

    @Override
    public List<FilesType> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<FilesType> readItems(Context context, Cursor cursor, OnItemLoadedListener<FilesType> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<FilesType>();
        List<FilesType> items = new ArrayList<FilesType>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(FilesTypesContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(FilesTypesContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(FilesTypesContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(FilesTypesContract.UPDATE_TIME);
        int nameIndex = cursor.getColumnIndex(FilesTypesContract.NAME);


        while (cursor.moveToNext()) {
            FilesType item = new FilesType();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getFilesTypes();
    }

    @Override
    protected CommonResponse<List<FilesType>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getFilesTypes(getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(FilesType item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(FilesTypesContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(FilesTypesContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(FilesTypesContract.STATUS, item.getStatus());
        contentValues.put(FilesTypesContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(FilesTypesContract.NAME, item.getName());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<FilesType> items) {
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.FilesTypesChanged());
        return result;
    }
}
