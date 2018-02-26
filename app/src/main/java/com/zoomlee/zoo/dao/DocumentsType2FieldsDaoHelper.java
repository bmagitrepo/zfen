package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.DocumentsType2Field;
import com.zoomlee.zoo.provider.helpers.DocumentsTypes2FieldTypesHelper.DocumentsTypes2FieldTypesContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class DocumentsType2FieldsDaoHelper extends DaoHelper<DocumentsType2Field> {

    protected DocumentsType2FieldsDaoHelper() {
        super(LastSyncTimeKeys.DOCS_TYPE2FIELDS, DocumentsTypes2FieldTypesContract.CONTENT_URI);
    }

    @Override
    public List<DocumentsType2Field> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<DocumentsType2Field> readItems(Context context, Cursor cursor, OnItemLoadedListener<DocumentsType2Field> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<DocumentsType2Field>();
        List<DocumentsType2Field> items = new ArrayList<DocumentsType2Field>(cursor.getCount());

        String[] projection = DocumentsTypes2FieldTypesContract.ALL_COLUMNS_PROJECTION;

        int idIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + DocumentsTypes2FieldTypesContract._ID);
        int remoteIdIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + DocumentsTypes2FieldTypesContract.REMOTE_ID);
        int statusIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + DocumentsTypes2FieldTypesContract.STATUS);
        int updateTimeIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + DocumentsTypes2FieldTypesContract.UPDATE_TIME);
        int documentIdIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + DocumentsTypes2FieldTypesContract.DOCUMENT_TYPE_ID);
        int fieldIdIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + DocumentsTypes2FieldTypesContract.FIELD_ID);
        int weightIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + DocumentsTypes2FieldTypesContract.WEIGHT);
        int fieldTypeValueIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.FIELD_TYPE_VALUE);
        int fieldTypeNameIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.FIELD_TYPE_NAME);
        int fieldTypeReminderIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.FIELD_TYPE_REMINDER);
        int fieldTypeSuggestIndex = Util.findIndex(projection, DocumentsTypes2FieldTypesContract.FIELD_TYPE_SUGGEST);


        while (cursor.moveToNext()) {
            DocumentsType2Field item = new DocumentsType2Field();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setDocumentTypeId(cursor.getInt(documentIdIndex));
            item.setFieldTypeId(cursor.getInt(fieldIdIndex));
            item.setWeight(cursor.getInt(weightIndex));
            item.setFieldTypeValue(cursor.getInt(fieldTypeValueIndex));
            item.setFieldTypeName(cursor.getString(fieldTypeNameIndex));
            item.setReminder(cursor.getInt(fieldTypeReminderIndex));
            item.setSuggest(cursor.getInt(fieldTypeSuggestIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getDocumentsTypesFields();
    }

    @Override
    protected CommonResponse<List<DocumentsType2Field>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getDocumentsTypes2Fields(getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(DocumentsType2Field item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(DocumentsTypes2FieldTypesContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(DocumentsTypes2FieldTypesContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(DocumentsTypes2FieldTypesContract.STATUS, item.getStatus());
        contentValues.put(DocumentsTypes2FieldTypesContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(DocumentsTypes2FieldTypesContract.DOCUMENT_TYPE_ID, item.getDocumentTypeId());
        contentValues.put(DocumentsTypes2FieldTypesContract.FIELD_ID, item.getFieldTypeId());
        contentValues.put(DocumentsTypes2FieldTypesContract.WEIGHT, item.getWeight());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<DocumentsType2Field> items) {
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.DocumentsType2FieldsChanged());
        return result;
    }
}
