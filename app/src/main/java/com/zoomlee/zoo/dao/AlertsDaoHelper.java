package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.provider.helpers.DocumentsHelper.AlertsContract;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static com.zoomlee.zoo.provider.helpers.FieldsHelper.FieldsContract;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.02.15.
 */
public class AlertsDaoHelper {

    private Context context;

    public AlertsDaoHelper(Context context) {
        this.context = context;
    }

    /**
     * get CategoriesDocAlerts for user or person (for user set personId = -1)
     * @param timestamp
     * @param personId
     * @return
     */
    public List<Alert> getAlerts(int timestamp, int personId) {
        return getItems(timestamp, personId);
    }

    public void markAlertViewed(Alert alert) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse(FieldsContract.CONTENT_URI + "/" + alert.getFieldId());
        ContentValues contentValues = new ContentValues();
        contentValues.put(FieldsContract.VIEWED, 1);
        contentResolver.update(uri, contentValues, null, null);
    }

    private List<Alert> getItems(int timestamp, int personId) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = null;
        String[] args;
        if (personId != Person.ALL_ID) {
            selection = AlertsContract.PERSON_ID + " = ?";
            args = DBUtil.getArgsArray(timestamp, personId);
        } else {
            args = DBUtil.getArgsArray(timestamp);
        }

        Cursor cursor = contentResolver.query(AlertsContract.URI, null, selection,
                args,
                AlertsContract.CATEGORY_REMOTE_ID + " ASC");
        if (cursor == null) return new ArrayList<Alert>();

        List<Alert> result = new ArrayList<Alert>();
        try {
            String[] projection = AlertsContract.ALL_COLUMNS_PROJECTION;
            int categoryIdIndex = Util.findIndex(projection, AlertsContract.CATEGORY_REMOTE_ID);
            int documentIdIndex = Util.findIndex(projection, AlertsContract.DOCUMENT_ID);
            int documentNameIndex = Util.findIndex(projection, AlertsContract.DOCUMENT_NAME);
            int fieldNameIndex = Util.findIndex(projection, AlertsContract.FIELD_NAME);
            int fieldValueIndex = Util.findIndex(projection, AlertsContract.FIELD_VALUE);
            int fieldIdIndex = Util.findIndex(projection, AlertsContract.FIELD_ID);
            int notifyOnIndex = Util.findIndex(projection, AlertsContract.NOTIFY_ON);
            int viewedIndex = Util.findIndex(projection, AlertsContract.VIEWED);

            while (cursor.moveToNext()) {
                Alert alert = new Alert();
                alert.setCategoryRemoteId(cursor.getInt(categoryIdIndex));
                alert.setDocumentId(cursor.getInt(documentIdIndex));
                alert.setDocumentName(cursor.getString(documentNameIndex));
                alert.setFieldId(cursor.getInt(fieldIdIndex));
                alert.setFieldName(cursor.getString(fieldNameIndex));
                alert.setFieldValue(cursor.getString(fieldValueIndex));
                alert.setNotifyOn(cursor.getInt(notifyOnIndex));
                alert.setViewed(cursor.getInt(viewedIndex) == 1);
                result.add(alert);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static class Alert {
        private int categoryRemoteId;
        private int documentId;
        private String documentName;
        private int fieldId;
        private String fieldName;
        private String fieldValue;
        private int notifyOn;
        private boolean viewed;

        public int getCategoryRemoteId() {
            return categoryRemoteId;
        }

        public void setCategoryRemoteId(int categoryRemoteId) {
            this.categoryRemoteId = categoryRemoteId;
        }

        public int getDocumentId() {
            return documentId;
        }

        public void setDocumentId(int documentId) {
            this.documentId = documentId;
        }

        public String getDocumentName() {
            return documentName;
        }

        public void setDocumentName(String documentName) {
            this.documentName = documentName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldValue() {
            return fieldValue;
        }

        public void setFieldValue(String fieldValue) {
            this.fieldValue = fieldValue;
        }

        public int getNotifyOn() {
            return notifyOn;
        }

        public void setNotifyOn(int notifyOn) {
            this.notifyOn = notifyOn;
        }

        public boolean isViewed() {
            return viewed;
        }

        public void setViewed(boolean viewed) {
            this.viewed = viewed;
        }

        public int getFieldId() {
            return fieldId;
        }

        public void setFieldId(int fieldId) {
            this.fieldId = fieldId;
        }
    }
}
