package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.CategoriesDocumentsType;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.provider.helpers.Category2DocumentsTypesProviderHelper.Category2DocumentsTypesContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class CategoriesDocsTypeDaoHelper extends DaoHelper<CategoriesDocumentsType> {

    protected CategoriesDocsTypeDaoHelper() {
        super(LastSyncTimeKeys.CATEGORIES_DOCS_TYPE, Category2DocumentsTypesContract.CONTENT_URI);
    }

    @Override
    public List<CategoriesDocumentsType> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<CategoriesDocumentsType> readItems(Context context, Cursor cursor, OnItemLoadedListener<CategoriesDocumentsType> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<CategoriesDocumentsType>();
        List<CategoriesDocumentsType> categories = new ArrayList<CategoriesDocumentsType>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(Category2DocumentsTypesContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(Category2DocumentsTypesContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(Category2DocumentsTypesContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(Category2DocumentsTypesContract.UPDATE_TIME);
        int categoryIdIndex = cursor.getColumnIndex(Category2DocumentsTypesContract.CATEGORY_ID);
        int documentIdIndex = cursor.getColumnIndex(Category2DocumentsTypesContract.DOCUMENTS_TYPE_ID);

        while (cursor.moveToNext()) {
            CategoriesDocumentsType item = new CategoriesDocumentsType();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setCategoryId(cursor.getInt(categoryIdIndex));
            item.setDocumentTypeId(cursor.getInt(documentIdIndex));
            categories.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return categories;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getCategoriesDocumentsTypes();
    }

    @Override
    protected CommonResponse<List<CategoriesDocumentsType>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getCategoriesDocumentsTypes(getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(CategoriesDocumentsType item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(Category2DocumentsTypesContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(Category2DocumentsTypesContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(Category2DocumentsTypesContract.STATUS, item.getStatus());
        contentValues.put(Category2DocumentsTypesContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(Category2DocumentsTypesContract.CATEGORY_ID, item.getCategoryId());
        contentValues.put(Category2DocumentsTypesContract.DOCUMENTS_TYPE_ID, item.getDocumentTypeId());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<CategoriesDocumentsType> items) {
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.CategoriesDocumentsTypesChanged());
        return result;
    }
}
