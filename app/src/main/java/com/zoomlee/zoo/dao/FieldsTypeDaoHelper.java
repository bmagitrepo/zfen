package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.FieldsType;
import com.zoomlee.zoo.provider.helpers.FieldsTypesProviderHelper.FieldsTypesContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class FieldsTypeDaoHelper extends DaoHelper<FieldsType> {

    protected FieldsTypeDaoHelper() {
        super(LastSyncTimeKeys.FIELDS_TYPES, FieldsTypesContract.CONTENT_URI);
    }

    @Override
    public List<FieldsType> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<FieldsType> readItems(Context context, Cursor cursor, OnItemLoadedListener<FieldsType> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<FieldsType>();
        List<FieldsType> items = new ArrayList<FieldsType>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(FieldsTypesContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(FieldsTypesContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(FieldsTypesContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(FieldsTypesContract.UPDATE_TIME);
        int nameIndex = cursor.getColumnIndex(FieldsTypesContract.NAME);
        int typeIndex = cursor.getColumnIndex(FieldsTypesContract.TYPE);
        int suggestIndex = cursor.getColumnIndex(FieldsTypesContract.SUGGEST);
        int reminderIndex = cursor.getColumnIndex(FieldsTypesContract.REMINDER);

        while (cursor.moveToNext()) {
            FieldsType item = new FieldsType();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            item.setType(cursor.getInt(typeIndex));
            item.setSuggest(cursor.getInt(suggestIndex));
            item.setReminder(cursor.getInt(reminderIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getFieldsTypes();
    }

    @Override
    protected CommonResponse<List<FieldsType>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getFieldsTypes(getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(FieldsType item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(FieldsTypesContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(FieldsTypesContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(FieldsTypesContract.STATUS, item.getStatus());
        contentValues.put(FieldsTypesContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(FieldsTypesContract.NAME, item.getName());
        contentValues.put(FieldsTypesContract.TYPE, item.getType());
        contentValues.put(FieldsTypesContract.SUGGEST, item.getSuggest());
        contentValues.put(FieldsTypesContract.REMINDER, item.getReminder());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<FieldsType> items) {
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.FieldsTypesChanged());
        return result;
    }
}
