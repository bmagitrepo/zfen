package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.provider.helpers.DocumentsTypesHelper.DocumentsTypesContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class DocumentsTypeDaoHelper extends DaoHelper<DocumentsType> {

    protected DocumentsTypeDaoHelper() {
        super(LastSyncTimeKeys.DOCS_TYPE, DocumentsTypesContract.CONTENT_URI);
    }

    @Override
    public List<DocumentsType> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<DocumentsType> readItems(Context context, Cursor cursor, OnItemLoadedListener<DocumentsType> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<DocumentsType>();
        List<DocumentsType> items = new ArrayList<DocumentsType>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(DocumentsTypesContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(DocumentsTypesContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(DocumentsTypesContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(DocumentsTypesContract.UPDATE_TIME);
        int nameIndex = cursor.getColumnIndex(DocumentsTypesContract.NAME);
        int groupTypeIndex = cursor.getColumnIndex(DocumentsTypesContract.GROUP_TYPE);

        while (cursor.moveToNext()) {
            DocumentsType item = new DocumentsType();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            item.setGroupId(cursor.getInt(groupTypeIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);

        }

        return items;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getDocumentsTypes();
    }

    @Override
    protected CommonResponse<List<DocumentsType>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getDOcumentsTypes(getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(DocumentsType item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(DocumentsTypesContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(DocumentsTypesContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(DocumentsTypesContract.STATUS, item.getStatus());
        contentValues.put(DocumentsTypesContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(DocumentsTypesContract.NAME, item.getName());
        contentValues.put(DocumentsTypesContract.GROUP_TYPE, item.getGroupId());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<DocumentsType> items) {
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.DocumentsTypesChanged());
        return result;
    }
}
