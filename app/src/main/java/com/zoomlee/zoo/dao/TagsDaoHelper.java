package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.TagDataApi;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;

import static com.zoomlee.zoo.provider.helpers.Tags2DocumentsProviderHelper.Tags2DocumentsContract;
import static com.zoomlee.zoo.provider.helpers.TagsProviderHelper.TagsContract;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 07.04.15.
 */
class TagsDaoHelper extends UserDataDaoHelper<Tag> {

    protected TagsDaoHelper() {
        super("", TagsContract.CONTENT_URI, RestTask.Types.TAG_PUT, RestTask.Types.TAG_DELETE);
    }

    @Override
    public List<Tag> getAllItems(Context context) {
        String selection = TagsContract.TABLE_NAME + "." + TagsContract.STATUS + "=1";
        return getAllItems(context, selection, null, null);
    }

    @Override
    public List<Tag> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Tag> readItems(Context context, Cursor cursor, OnItemLoadedListener<Tag> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Tag>();
        List<Tag> items = new ArrayList<Tag>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(TagsContract._ID);
        int statusIndex = cursor.getColumnIndex(TagsContract.STATUS);
        int remoteIdIndex = cursor.getColumnIndex(TagsContract.REMOTE_ID);
        int updateTimeIndex = cursor.getColumnIndex(TagsContract.UPDATE_TIME);
        int userIdIndex = cursor.getColumnIndex(TagsContract.USER_ID);
        int nameIndex = cursor.getColumnIndex(TagsContract.NAME);
        int docCountIndex = cursor.getColumnIndex(TagsContract.DOCS_COUNT);

        while (cursor.moveToNext()) {
            Tag item = new Tag();
            item.setId(cursor.getInt(idIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setUserId(cursor.getInt(userIdIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            item.setDocsCount(cursor.getInt(docCountIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public Tag getItemByLocalId(Context context, int localId, boolean showDeleted) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = TagsContract.TABLE_NAME + "." + TagsContract._ID + "=" + localId;
        selection += showDeleted ? "" : " AND " + TagsContract.TABLE_NAME + "." + TagsContract.STATUS + "=1";
        Cursor cursor = contentResolver.query(TagsContract.CONTENT_URI,
                null, selection, null, null);

        List<Tag> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    @Override
    public Tag getItemByRemoteId(Context context, int remoteId) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = TagsContract.TABLE_NAME + "." + TagsContract.REMOTE_ID + "=" + remoteId;
        Cursor cursor = contentResolver.query(TagsContract.CONTENT_URI,
                null, selection, null, null);

        List<Tag> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    protected void updateBindToDocument(Context context, List<Tag> tagsList, int localDocId) {
        for (Tag tag : tagsList) {
            String id = saveItem(context, tag).getLastPathSegment();
            tag.setId(Integer.valueOf(id));

            EventBus.getDefault().post(new Events.TagChanged(Events.TagChanged.UPDATED, tag));
        }

        String[] docIdArgs = DBUtil.getArgsArray(localDocId);
        ContentResolver contentResolver = context.getContentResolver();
        String selection = TagsContract.DOCUMENT_ID + "=?";
        Cursor cursor = contentResolver.query(TagsContract.CONTENT_URI, null, selection, docIdArgs, null);
        List<Tag> deletedTags = readItems(context, cursor);
        cursor.close();
        List<Tag> addedTags = new ArrayList<>(tagsList);
        addedTags.removeAll(deletedTags);
        deletedTags.removeAll(tagsList);

        if (deletedTags.size() > 0) {
            String deleteWhere = Tags2DocumentsContract.DOCUMENT_ID + " = ? AND "
                    + Tags2DocumentsContract.TAG_ID + " in "
                    + getIdSetString(deletedTags);

            contentResolver.delete(Tags2DocumentsContract.CONTENT_URI, deleteWhere, docIdArgs);
        }

        if (addedTags.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[addedTags.size()];
            for (int i = 0; i < addedTags.size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Tags2DocumentsContract.TAG_ID, addedTags.get(i).getId());
                contentValues.put(Tags2DocumentsContract.DOCUMENT_ID, localDocId);
                contentValuesArray[i] = contentValues;
            }
            contentResolver.bulkInsert(Tags2DocumentsContract.CONTENT_URI, contentValuesArray);
        }

        deleteUnusedTags(context);
    }

    @Override
    protected void deleteRelatedData(Context context, Tag tag) {
        context.getContentResolver().delete(Tags2DocumentsContract.CONTENT_URI,
                Tags2DocumentsContract.TAG_ID + "=?", DBUtil.getArgsArray(tag.getId()));
    }

    @Override
    protected void postEntityChanged(Tag tag) {
        EventBus.getDefault().post(new Events.TagChanged(Events.TagChanged.UPDATED, tag));
    }

    @Override
    protected void postEntityDeleted(Tag tag) {
        EventBus.getDefault().post(new Events.TagChanged(Events.TagChanged.DELETED, tag));
    }

    @Override
    protected CommonResponse<List<Tag>> callApi(Context context, Object api) {
        CommonResponse<List<Tag>> commonResponse = buildTagDataApi().getTags(SharedPreferenceUtils.getUtils().getPrivateKey());
        return commonResponse;
    }

    @Override
    protected ContentValues convertEntity(Tag item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(TagsContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(TagsContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(TagsContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(TagsContract.STATUS, item.getStatus());
        contentValues.put(TagsContract.USER_ID, item.getUserId());
        contentValues.put(TagsContract.NAME, item.getName());
        return contentValues;
    }

    protected void deleteAllItems(Context context, int localDocId) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(Tags2DocumentsContract.CONTENT_URI, Tags2DocumentsContract.DOCUMENT_ID + "=?",
                DBUtil.getArgsArray(localDocId));

        deleteUnusedTags(context);
    }

    private TagDataApi buildTagDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(TagDataApi.class);
    }

    /**
     * delete all not bind to document tags. getAllItems(context) method return only bind to documents tags
     *
     * @param context
     */
    private void deleteUnusedTags(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        List<Tag> tags = getAllItems(context);

        String deleteWhere = TagsContract.STATUS + " = " + Tag.STATUS_NORMAL;
        if (tags.size() > 0)
            deleteWhere += " AND " + TagsContract._ID + " NOT IN " + getIdSetString(tags);

        contentResolver.delete(TagsContract.CONTENT_URI, deleteWhere, null);
    }

    private String getIdSetString(List<Tag> tagList) {
        String[] args = new String[tagList.size()];
        for (int i = 0; i < tagList.size(); i++)
            args[i] = String.valueOf(tagList.get(i).getId());

        return DBUtil.formatArgsAsSet(args);
    }
}