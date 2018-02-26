package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.provider.helpers.BaseProviderHelper;
import com.zoomlee.zoo.provider.helpers.CategoriesProviderHelper.CategoriesContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class CategoryDaoHelper extends DaoHelper<Category> {

    private static final String CUSTOM_CATEGORY_ORDER_KEY = "custom_category_order";

    protected CategoryDaoHelper() {
        super(LastSyncTimeKeys.CATEGORY, CategoriesContract.CONTENT_URI);
    }

    @Override
    public List<Category> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Category> readItems(Context context, Cursor cursor, OnItemLoadedListener<Category> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Category>();
        List<Category> items = new ArrayList<Category>(cursor.getCount());

        String[] projection = CategoriesContract.ALL_COLUMNS_PROJECTION;

        int idIndex = Util.findIndex(projection, CategoriesContract.TABLE_NAME + "." + CategoriesContract._ID);
        int remoteIdIndex = Util.findIndex(projection, CategoriesContract.TABLE_NAME + "." + CategoriesContract.REMOTE_ID);
        int statusIndex = Util.findIndex(projection, CategoriesContract.TABLE_NAME + "." + CategoriesContract.STATUS);
        int updateTimeIndex = Util.findIndex(projection, CategoriesContract.TABLE_NAME + "." + CategoriesContract.UPDATE_TIME);
        int nameIndex = Util.findIndex(projection, CategoriesContract.TABLE_NAME + "." + CategoriesContract.NAME);
        int weightIndex = Util.findIndex(projection, CategoriesContract.TABLE_NAME + "." + CategoriesContract.WEIGHT);
        int docTypeRemoteIdIndex = Util.findIndex(projection, CategoriesContract.DOCUMENT_TYPE_REMOTE_ID);
        int docTypeNameIndex = Util.findIndex(projection, CategoriesContract.DOCUMENT_TYPE_NAME);


        Category item = null;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(idIndex);
            if (item == null || item.getId() != id) {
                item = new Category();
                item.setId(cursor.getInt(idIndex));
                item.setRemoteId(cursor.getInt(remoteIdIndex));
                item.setStatus(cursor.getInt(statusIndex));
                item.setUpdateTime(cursor.getInt(updateTimeIndex));
                item.setName(cursor.getString(nameIndex));
                item.setWeight(cursor.getInt(weightIndex));
                items.add(item);
            }
            int docTypeRemoteId = cursor.getInt(docTypeRemoteIdIndex);
            String docTypeName = cursor.getString(docTypeNameIndex);
            DocumentsType documentsType = new DocumentsType(docTypeRemoteId, docTypeName);
            item.addDocumentsType(documentsType);


            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getCategories();
    }

    @Override
    protected CommonResponse<List<Category>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getCategories(getLastSyncTime());
    }

    @Override
    public List<Category> getAllItems(Context context) {
        String selection = CategoriesContract.TABLE_NAME + "." + CategoriesContract.STATUS + "=1";
        return getAllItems(context, selection, null, null);
    }

    @Override
    public Category getItemByLocalId(Context context, int localId, boolean showDeleted) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = CategoriesContract.TABLE_NAME + "." + CategoriesContract._ID + "=" +localId;
        selection = selection + (showDeleted ? "" : " AND " +  CategoriesContract.TABLE_NAME + "." + CategoriesContract.STATUS + "=1");
        Cursor cursor = contentResolver.query(CategoriesContract.CONTENT_URI,
                null, selection, null, null);

        List<Category> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    @Override
    public Category getItemByRemoteId(Context context, int remoteId) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(getURI(), null,
                CategoriesContract.TABLE_NAME + "." + BaseProviderHelper.DataColumns.REMOTE_ID + "=?",
                DBUtil.getArgsArray(remoteId), null);

        List<Category> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    @Override
    protected ContentValues convertEntity(Category item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(CategoriesContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(CategoriesContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(CategoriesContract.STATUS, item.getStatus());
        contentValues.put(CategoriesContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(CategoriesContract.NAME, item.getName());
        if (!item.isAvoidWeightSaving())
            contentValues.put(CategoriesContract.WEIGHT, item.getWeight());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<Category> items) {
        if (Util.isEmpty(items)) return 0;

        boolean customOrder = SharedPreferenceUtils.getUtils().getBooleanSettings(CUSTOM_CATEGORY_ORDER_KEY);
        if (customOrder) {
            for (Category category: items)
                category.setAvoidWeightSaving(true);
        }
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.CategoriesChanged());
        return result;
    }

    @Override
    public Uri saveRemoteChanges(Context context, Category item) {
        boolean customOrder = SharedPreferenceUtils.getUtils().getBooleanSettings(CUSTOM_CATEGORY_ORDER_KEY);
        if (customOrder) item.setAvoidWeightSaving(true);
        return super.saveRemoteChanges(context, item);
    }

    /**
     * create or update item in db. </br>
     * Calling this method set CUSTOM_CATEGORY_ORDER_KEY to true.
     * After that, call categories saved
     * by calling {@linkplain com.zoomlee.zoo.dao.CategoryDaoHelper#saveRemoteChanges(android.content.Context, com.zoomlee.zoo.net.model.Category)}
     * or {@linkplain com.zoomlee.zoo.dao.CategoryDaoHelper#saveRemoteChanges(android.content.Context, java.util.List)}
     * will avoid save/update 'weight' field. </br>
     * This field can be updated only by calling {@linkplain com.zoomlee.zoo.dao.CategoryDaoHelper#saveLocalChanges(android.content.Context, com.zoomlee.zoo.net.model.Category)}
     * </br>
     * </br>
     * After relogin to another user, all shared preferences will be reset and on first static data synchronization, user will have default category order.
     *
     *
     * @param context
     * @param item
     * @return Uri of saved element
     */
    @Override
    public Uri saveLocalChanges(Context context, Category item) {
        SharedPreferenceUtils.getUtils().setBooleanSettings(CUSTOM_CATEGORY_ORDER_KEY, true);
        item.setAvoidWeightSaving(false);
        return saveItem(context, item);
    }
}