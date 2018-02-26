package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.provider.helpers.FieldsHelper.FieldsContract;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.NotificationsUtil;
import com.zoomlee.zoo.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class FieldsDaoHelper extends DaoHelper<Field> {

    protected FieldsDaoHelper() {
        super("", FieldsContract.CONTENT_URI);
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return false;
    }

    @Override
    public List<Field> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Field> readItems(Context context, Cursor cursor, OnItemLoadedListener<Field> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Field>();
        List<Field> items = new ArrayList<Field>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(FieldsContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(FieldsContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(FieldsContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(FieldsContract.UPDATE_TIME);
        int nameIndex = cursor.getColumnIndex(FieldsContract.NAME);
        int fieldTypeIdIndex = cursor.getColumnIndex(FieldsContract.FIELD_TYPE_ID);
        int valueIndex = cursor.getColumnIndex(FieldsContract.VALUE);
        int documentIdIndex = cursor.getColumnIndex(FieldsContract.DOCUMENT_ID);
        int notifyOnIndex = cursor.getColumnIndex(FieldsContract.NOTIFY_ON);
        int weightIndex = cursor.getColumnIndex(FieldsContract.WEIGHT);
        int typeIndex = cursor.getColumnIndex(FieldsContract.TYPE);
        int createTimeIndex = cursor.getColumnIndex(FieldsContract.CREATE_TIME);
        int suggestIndex = cursor.getColumnIndex(FieldsContract.SUGGEST);
        int reminderIndex = cursor.getColumnIndex(FieldsContract.REMINDER);

        while (cursor.moveToNext()) {
            Field item = new Field();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            item.setFieldTypeId(cursor.getInt(fieldTypeIdIndex));
            item.setValue(cursor.getString(valueIndex));
            item.setLocalDocumentId(cursor.getInt(documentIdIndex));
            item.setNotifyOn(cursor.getString(notifyOnIndex));
            item.setWeight(cursor.getInt(weightIndex));
            item.setType(cursor.getInt(typeIndex));
            item.setCreateTime(cursor.getInt(createTimeIndex));
            item.setSuggest(cursor.getInt(suggestIndex));
            item.setReminder(cursor.getInt(reminderIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    protected CommonResponse<List<Field>> callApi(Context context, Object api) {
        //stub
        return null;
    }

    @Override
    protected ContentValues convertEntity(Field item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(FieldsContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(FieldsContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(FieldsContract.STATUS, item.getStatus());
        contentValues.put(FieldsContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(FieldsContract.NAME, item.getName());
        contentValues.put(FieldsContract.FIELD_TYPE_ID, item.getFieldTypeId());
        contentValues.put(FieldsContract.VALUE, item.getValue());
        contentValues.put(FieldsContract.DOCUMENT_ID, item.getLocalDocumentId());
        contentValues.put(FieldsContract.NOTIFY_ON, item.getNotifyOn());
        contentValues.put(FieldsContract.CREATE_TIME, item.getCreateTime());
        return contentValues;
    }

    protected void saveItems(Context context, List<Field> fieldsList, int localDocId) {
        deleteAllItems(context, localDocId);

        long currentTimestamp = TimeUtil.getServerCurrentTimestamp();
        for (Field field : fieldsList)
            field.setLocalDocumentId(localDocId);

        super.saveItems(context, fieldsList);

        List<Field> fieldsSaved = getAllItems(context, FieldsContract.TABLE_NAME + "." + FieldsContract.DOCUMENT_ID + "=" + localDocId, null, null);
        for (Field field : fieldsSaved)
            if (field.getLongNotifyOn() > currentTimestamp)
                NotificationsUtil.addReminder(context, field);

    }

    protected void deleteAllItems(Context context, int localDocId) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = FieldsContract.TABLE_NAME + "." + FieldsContract.NOTIFY_ON + " IS NOT NULL" +
                " AND " + FieldsContract.TABLE_NAME + "." + FieldsContract.DOCUMENT_ID + "=?";
        List<Field> fieldsToRemove = getAllItems(context, selection, DBUtil.getArgsArray(localDocId), null);
        DeveloperUtil.michaelLog(fieldsToRemove);
        for (Field field : fieldsToRemove)
            NotificationsUtil.removeReminder(context, field);

        contentResolver.delete(FieldsContract.CONTENT_URI, FieldsContract.DOCUMENT_ID + "=?",
                DBUtil.getArgsArray(localDocId));
    }

    @Override
    public Field getItemByLocalId(Context context, int localId, boolean showDeleted) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = FieldsContract.TABLE_NAME + "." + FieldsContract._ID + "=" + localId;
        selection += showDeleted ? "" : " AND " + FieldsContract.TABLE_NAME + "." + FieldsContract.STATUS + "=1";
        Cursor cursor = contentResolver.query(FieldsContract.CONTENT_URI,
                null, selection, null, null);

        List<Field> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

}
