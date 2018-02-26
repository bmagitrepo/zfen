package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.provider.helpers.DocumentsHelper.CategoriesDocAlertsContract;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.TimeUtil;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zoomlee.zoo.provider.helpers.DocumentsHelper.DocumentsContract;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.02.15.
 */
public class CategoriesDocAlertsDaoHelper {

    private Context context;

    public CategoriesDocAlertsDaoHelper(Context context) {
        this.context = context;
    }

    /**
     * get CategoriesDocAlerts person by localId
     * (for user set personId = {@linkplain Person#ME_ID}, for all persons use {@linkplain Person#ALL_ID})
     * @param personId
     * @return
     */
    public List<CategoriesDocAlerts> getCategoriesDocAlerts(int personId) {
        Cursor cursor = getCursor(personId);
        if (cursor == null) return new ArrayList<>();

        List<CategoriesDocAlerts> result = new ArrayList<>();
        try {
            String[] projection = CategoriesDocAlertsContract.ALL_COLUMNS_PROJECTION;
            int categoryIdIndex = Util.findIndex(projection, CategoriesDocAlertsContract.CATEGORY_REMOTE_ID);
            int categoryNameIndex = Util.findIndex(projection, CategoriesDocAlertsContract.CATEGORY_NAME);
            int categoryWeightIndex = Util.findIndex(projection, CategoriesDocAlertsContract.CATEGORY_WEIGHT);
            int alertsCountIndex = Util.findIndex(projection, CategoriesDocAlertsContract.ALERTS_COUNT);
            int docTypeNameIndex = Util.findIndex(projection, CategoriesDocAlertsContract.DOCUMENT_TYPE_NAME);

            CategoriesDocAlerts categoriesDocAlerts = null;
            while (cursor.moveToNext()) {
                int categoryId = cursor.getInt(categoryIdIndex);
                if (categoriesDocAlerts == null ||
                        categoriesDocAlerts.getCategoryRemoteId() != categoryId) {
                    categoriesDocAlerts = new CategoriesDocAlerts();
                    categoriesDocAlerts.setCategoryRemoteId(categoryId);
                    categoriesDocAlerts.setCategoryName(cursor.getString(categoryNameIndex));
                    categoriesDocAlerts.setCategoryWeight(cursor.getInt(categoryWeightIndex));
                    result.add(categoriesDocAlerts);
                }
                categoriesDocAlerts.addAlertsCount(cursor.getInt(alertsCountIndex));
                categoriesDocAlerts.addDocTypeName(cursor.getString(docTypeNameIndex));
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    private Cursor getCursor(int personId) {
        int timestamp = (int) TimeUtil.getServerEndDayTimestamp();
        ContentResolver contentResolver = context.getContentResolver();

        String selection = DocumentsContract.TABLE_NAME + "." + DocumentsContract.STATUS + "=" + Document.STATUS_NORMAL;
        String[] args;
        if (personId == Person.ALL_ID) {
            args = DBUtil.getArgsArray(timestamp);
        } else {
            selection += " AND " + CategoriesDocAlertsContract.PERSON_ID + " = ?";
            args = DBUtil.getArgsArray(timestamp, personId);
        }

        Cursor cursor = contentResolver.query(CategoriesDocAlertsContract.URI, null, selection,
                args, CategoriesDocAlertsContract.CATEGORY_WEIGHT + " DESC, " +
                        DocumentsContract.TABLE_NAME + "." + DocumentsContract.UPDATE_TIME + " DESC");

        return cursor;
    }

    public static class CategoriesDocAlerts {
        private int categoryRemoteId;
        private String categoryName;
        private int categoryWeight;
        private Set<String> docsTypesNames = new HashSet<String>();
        private int alertsCount;

        public CategoriesDocAlerts(){}

        public CategoriesDocAlerts(CategoriesDocAlerts categoriesDocAlerts) {
            this.categoryRemoteId = categoriesDocAlerts.categoryRemoteId;
            this.categoryName = categoriesDocAlerts.categoryName;
            this.categoryWeight = categoriesDocAlerts.categoryWeight;
            this.docsTypesNames = categoriesDocAlerts.docsTypesNames;
            this.alertsCount = categoriesDocAlerts.alertsCount;
        }

        public CategoriesDocAlerts(Category category) {
            this.categoryRemoteId = category.getRemoteId();
            this.categoryName = category.getName();
            this.categoryWeight = category.getWeight();
        }

        public int getCategoryRemoteId() {
            return categoryRemoteId;
        }

        public void setCategoryRemoteId(int categoryRemoteId) {
            this.categoryRemoteId = categoryRemoteId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Set<String> getDocsTypesNames() {
            return docsTypesNames;
        }

        public void addDocTypeName(String docType) {
                docsTypesNames.add(docType);
        }

        public int getAlertsCount() {
            return alertsCount;
        }

        public void addAlertsCount(int alertsCount) {
            this.alertsCount += alertsCount;
        }

        public int getCategoryWeight() {
            return categoryWeight;
        }

        public void setCategoryWeight(int categoryWeight) {
            this.categoryWeight = categoryWeight;
        }

        public Category getCategory() {
            Category category = new Category();
            category.setRemoteId(categoryRemoteId);
            category.setName(categoryName);
            category.setWeight(categoryWeight);
            return category;
        }

        @Override
        public String toString() {
            return "CategoriesDocAlerts{" +
                    "categoryRemoteId=" + categoryRemoteId +
                    ", categoryName='" + categoryName + '\'' +
                    ", categoryWeight=" + categoryWeight +
                    ", docsTypesNames=" + docsTypesNames +
                    ", alertsCount=" + alertsCount +
                    '}';
        }
    }
}
