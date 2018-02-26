package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.FormField;
import com.zoomlee.zoo.provider.helpers.FormFieldsProviderHelper.FormFieldsContract;
import com.zoomlee.zoo.utils.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 26.05.15.
 */
class FormFieldsDaoHelper extends DaoHelper<FormField> {

    protected FormFieldsDaoHelper() {
        super("", FormFieldsContract.CONTENT_URI);
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return false;
    }

    @Override
    public List<FormField> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<FormField> readItems(Context context, Cursor cursor, OnItemLoadedListener<FormField> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<FormField>();
        List<FormField> items = new ArrayList<FormField>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(FormFieldsContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(FormFieldsContract.REMOTE_ID);
        int fieldTypeIdIndex = cursor.getColumnIndex(FormFieldsContract.FIELD_TYPE_ID);
        int valueIndex = cursor.getColumnIndex(FormFieldsContract.VALUE);
        int formIdIndex = cursor.getColumnIndex(FormFieldsContract.FORM_ID);

        while (cursor.moveToNext()) {
            FormField item = new FormField();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setFieldTypeId(cursor.getInt(fieldTypeIdIndex));
            item.setValue(cursor.getString(valueIndex));
            item.setLocalFormId(cursor.getInt(formIdIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    protected CommonResponse<List<FormField>> callApi(Context context, Object api) {
        //stub
        return null;
    }

    @Override
    protected ContentValues convertEntity(FormField item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(FormFieldsContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(FormFieldsContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(FormFieldsContract.FIELD_TYPE_ID, item.getFieldTypeId());
        contentValues.put(FormFieldsContract.VALUE, item.getValue());
        contentValues.put(FormFieldsContract.FORM_ID, item.getLocalFormId());
        return contentValues;
    }

    @Override
    public List<FormField> getAllItems(Context context) {
        return getAllItems(context, null, null, null);
    }

    protected void saveItems(Context context, List<FormField> formFieldsList, int localFormId) {
        deleteAllItems(context, localFormId);

        for (FormField formField : formFieldsList)
            formField.setLocalFormId(localFormId);

        super.saveItems(context, formFieldsList);
    }

    protected void deleteAllItems(Context context, int localFormId) {
        ContentResolver contentResolver = context.getContentResolver();

        contentResolver.delete(FormFieldsContract.CONTENT_URI, FormFieldsContract.FORM_ID + "=?",
                DBUtil.getArgsArray(localFormId));
    }

    @Override
    public FormField getItemByLocalId(Context context, int localId, boolean showDeleted) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = FormFieldsContract._ID + "=" + localId;
        Cursor cursor = contentResolver.query(FormFieldsContract.CONTENT_URI,
                null, selection, null, null);

        List<FormField> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }
}
