package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.DocumentDataApi;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.provider.helpers.BaseProviderHelper;
import com.zoomlee.zoo.provider.helpers.DocumentsHelper.DocumentsContract;
import com.zoomlee.zoo.provider.helpers.DocumentsHelper.FullDocumentsContract;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;

import static com.zoomlee.zoo.provider.helpers.FieldsHelper.FieldsContract;
import static com.zoomlee.zoo.provider.helpers.FilesProviderHelper.FilesContract;
import static com.zoomlee.zoo.provider.helpers.TagsProviderHelper.TagsContract;
import static com.zoomlee.zoo.utils.Events.DocumentChanged;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class DocumentsDaoHelper extends UserDataDaoHelper<Document> {

    protected DocumentsDaoHelper() {
        super(LastSyncTimeKeys.DOCUMENTS, DocumentsContract.CONTENT_URI,
                RestTask.Types.DOCUMENTS_UPLOAD, RestTask.Types.DOCUMENTS_DELETE);
    }

    /**
     * save document and reinsert all fields
     *
     * @param context
     * @param document
     * @return
     */
    @Override
    public synchronized Uri saveRemoteChanges(Context context, Document document) {
        if (document.getStatus() != Document.STATUS_DELETED) {
            PersonsDaoHelper personsDaoHelper = (PersonsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
            if (document.getRemotePersonId() != -1) {
                Person person = personsDaoHelper.getItemByRemoteId(context, document.getRemotePersonId());
                if (person == null) return null;
                document.setLocalPersonId(person.getId());
            }
        }

        return super.saveRemoteChanges(context, document);
    }

    @Override
    public List<Document> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Document> readItems(Context context, Cursor cursor, OnItemLoadedListener<Document> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Document>();
        List<Document> items = new ArrayList<Document>(cursor.getCount());

        String[] projection = FullDocumentsContract.ALL_COLUMNS_FULL_PROJECTION;

        int idIndex = Util.findIndex(projection, FullDocumentsContract._ID);
        int remoteIdIndex = Util.findIndex(projection, FullDocumentsContract.REMOTE_ID);
        int statusIndex = Util.findIndex(projection, FullDocumentsContract.STATUS);
        int updateTimeIndex = Util.findIndex(projection, FullDocumentsContract.UPDATE_TIME);
        int nameIndex = Util.findIndex(projection, FullDocumentsContract.NAME);
        int categoryIdIndex = Util.findIndex(projection, FullDocumentsContract.CATEGORY_ID);
        int colorIdIndex = Util.findIndex(projection, FullDocumentsContract.COLOR_ID);
        int createTimeIndex = Util.findIndex(projection, FullDocumentsContract.CREATE_TIME);
        int documentTypeIdIndex = Util.findIndex(projection, FullDocumentsContract.DOCUMENT_TYPE_ID);
        int notesIndex = Util.findIndex(projection, FullDocumentsContract.NOTES);
        int personIdIndex = Util.findIndex(projection, FullDocumentsContract.PERSON_ID);
        int userIdIndex = Util.findIndex(projection, FullDocumentsContract.USER_ID);

        int categoryNameIndex = Util.findIndex(projection, FullDocumentsContract.CATEGORY_NAME);
        int categoryWeightIndex = Util.findIndex(projection, FullDocumentsContract.CATEGORY_WEIGHT);
        int colorNameIndex = Util.findIndex(projection, FullDocumentsContract.COLOR_NAME);
        int colorHEXIndex = Util.findIndex(projection, FullDocumentsContract.COLOR_HEX);
        int typeNameIndex = Util.findIndex(projection, FullDocumentsContract.TYPE_NAME);
        int groupIdIndex = Util.findIndex(projection, FullDocumentsContract.TYPE_GROUP_ID);
        int groupNameIndex = Util.findIndex(projection, FullDocumentsContract.GROUP_NAME);

        DaoHelper<Field> fieldDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Field.class);
        DaoHelper<File> fileDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(File.class);
        DaoHelper<Tag> tagDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);

        while (cursor.moveToNext()) {
            Document item = new Document();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            item.setCategoryId(cursor.getInt(categoryIdIndex));
            item.setColorId(cursor.getInt(colorIdIndex));
            item.setCreateTime(cursor.getInt(createTimeIndex));
            item.setTypeId(cursor.getInt(documentTypeIdIndex));
            item.setNotes(cursor.getString(notesIndex));
            item.setLocalPersonId(cursor.getInt(personIdIndex));
            item.setUserId(cursor.getInt(userIdIndex));

            item.setCategoryName(cursor.getString(categoryNameIndex));
            item.setCategoryWeight(cursor.getInt(categoryWeightIndex));
            item.setColorName(cursor.getString(colorNameIndex));
            item.setColorHEX(cursor.getString(colorHEXIndex));
            item.setTypeName(cursor.getString(typeNameIndex));
            item.setGroupId(cursor.getInt(groupIdIndex));
            item.setGroupName(cursor.getString(groupNameIndex));

            item.setFieldsList(fieldDaoHelper.getAllItems(context,
                    FieldsContract.TABLE_NAME + "." + FieldsContract.DOCUMENT_ID + "=?",
                    DBUtil.getArgsArray(item.getId()),
                    FieldsContract.WEIGHT + " DESC, " + FieldsContract.TABLE_NAME + "." + FieldsContract.CREATE_TIME + " ASC"));
            item.setFilesList(fileDaoHelper.getAllItems(context,
                    FilesContract.TABLE_NAME + "." + FilesContract.DOCUMENT_ID + "=? AND " + FilesContract.TABLE_NAME + "." + FilesContract.STATUS + "=1",
                    DBUtil.getArgsArray(item.getId()),
                    FilesContract.CREATE_TIME + " DESC"));
            item.setTagsList(tagDaoHelper.getAllItems(context,
                    TagsContract.DOCUMENT_ID + "=? AND " + TagsContract.TABLE_NAME + "." + TagsContract.STATUS + "=1",
                    DBUtil.getArgsArray(item.getId()),
                    TagsContract.TABLE_NAME + "." + TagsContract.UPDATE_TIME + " DESC"));

            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public List<Document> getAllItems(Context context) {
        String selection = FullDocumentsContract.STATUS + "=1";
        return getAllItems(context, selection, null, null);
    }

    @Override
    public Document getItemByLocalId(Context context, int localId, boolean showDeleted) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = FullDocumentsContract._ID + "=" + localId;
        selection = selection + (showDeleted ? "" : " AND " + FullDocumentsContract.STATUS + "=1");
        Cursor cursor = contentResolver.query(FullDocumentsContract.FULL_DATA_URI,
                null, selection, null, null);

        List<Document> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    @Override
    public Document getItemByRemoteId(Context context, int remoteId) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(FullDocumentsContract.FULL_DATA_URI, null,
                FullDocumentsContract.TABLE_NAME + "." + BaseProviderHelper.DataColumns.REMOTE_ID + "=?",
                DBUtil.getArgsArray(remoteId), null);

        List<Document> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    @Override
    protected int saveItems(Context context, List<Document> items) {
        for (Document document : items) {
            saveItem(context, document);
        }

        return items.size();
    }

    @Override
    protected void saveRelatedLocalData(Context context, Document document) {
        FieldsDaoHelper fieldDaoHelper = (FieldsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Field.class);
        fieldDaoHelper.saveItems(context, document.getFieldsList(), document.getId());
        FilesDaoHelper fileDaoHelper = (FilesDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(File.class);
        fileDaoHelper.saveLocalChanges(context, document.getFilesList(), document.getId());
        TagsDaoHelper tagDaoHelper = (TagsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
        tagDaoHelper.updateBindToDocument(context, document.getTagsList(), document.getId());
    }

    @Override
    protected void saveRelatedRemoteData(Context context, Document document) {
        FieldsDaoHelper fieldDaoHelper = (FieldsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Field.class);
        fieldDaoHelper.saveItems(context, document.getFieldsList(), document.getId());
        FilesDaoHelper fileDaoHelper = (FilesDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(File.class);
        fileDaoHelper.saveRemoteChanges(context, document.getFilesList(), document.getId());
        TagsDaoHelper tagDaoHelper = (TagsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
        tagDaoHelper.updateBindToDocument(context, document.getTagsList(), document.getId());
    }

    @Override
    protected void deleteRelatedData(Context context, Document document) {
        FieldsDaoHelper fieldDaoHelper = (FieldsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Field.class);
        fieldDaoHelper.deleteAllItems(context, document.getId());
        FilesDaoHelper fileDaoHelper = (FilesDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(File.class);
        fileDaoHelper.deleteAllDocsItems(context, document);
        TagsDaoHelper tagDaoHelper = (TagsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
        tagDaoHelper.deleteAllItems(context, document.getId());
    }

    @Override
    protected void postEntityChanged(Document document) {
        EventBus.getDefault().post(new DocumentChanged(DocumentChanged.UPDATED, document));
    }

    @Override
    protected void postEntityDeleted(Document document) {
        EventBus.getDefault().post(new DocumentChanged(DocumentChanged.DELETED, document));
    }

    @Override
    protected CommonResponse<List<Document>> callApi(Context context, Object api) {
        return buildDocumentDataApi().getDocuments(SharedPreferenceUtils.getUtils().getPrivateKey(), getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(Document item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(DocumentsContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(DocumentsContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(DocumentsContract.STATUS, item.getStatus());
        contentValues.put(DocumentsContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(DocumentsContract.NAME, item.getName());
        contentValues.put(DocumentsContract.CATEGORY_ID, item.getCategoryId());
        contentValues.put(DocumentsContract.COLOR_ID, item.getColorId());
        contentValues.put(DocumentsContract.CREATE_TIME, item.getCreateTime());
        contentValues.put(DocumentsContract.DOCUMENT_TYPE_ID, item.getTypeId());
        contentValues.put(DocumentsContract.NOTES, item.getNotes());
        contentValues.put(DocumentsContract.PERSON_ID, item.getLocalPersonId());
        contentValues.put(DocumentsContract.USER_ID, item.getUserId());
        return contentValues;
    }

    protected void deleteAllItems(Context context, int personLocalId) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = FullDocumentsContract.PERSON_ID + "=?";
        String[] args = DBUtil.getArgsArray(personLocalId);
        Cursor cursor = contentResolver.query(FullDocumentsContract.FULL_DATA_URI, null, selection, args, null);
        List<Document> docs = readItems(context, cursor);
        for (Document doc : docs) {
            FieldsDaoHelper fieldDaoHelper = (FieldsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Field.class);
            fieldDaoHelper.deleteAllItems(context, doc.getId());
            FilesDaoHelper fileDaoHelper = (FilesDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(File.class);
            fileDaoHelper.deleteAllDocsItems(context, doc);
            TagsDaoHelper tagDaoHelper = (TagsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
            tagDaoHelper.deleteAllItems(context, doc.getId());
        }
        contentResolver.delete(FullDocumentsContract.CONTENT_URI, selection, args);

        for (Document doc : docs) {
            EventBus.getDefault().post(new DocumentChanged(DocumentChanged.DELETED, doc));
        }
    }

    private DocumentDataApi buildDocumentDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(DocumentDataApi.class);
    }
}
