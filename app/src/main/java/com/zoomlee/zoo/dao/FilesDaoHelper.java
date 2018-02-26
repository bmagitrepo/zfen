package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.model.BaseItem;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.provider.helpers.FilesProviderHelper.FilesContract;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.FileLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class FilesDaoHelper extends DaoHelper<File> {

    protected FilesDaoHelper() {
        super("", FilesContract.CONTENT_URI);
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        //stub
        return false;
    }

    @Override
    public List<File> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<File> readItems(Context context, Cursor cursor, OnItemLoadedListener<File> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<File>();
        List<File> items = new ArrayList<File>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(FilesContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(FilesContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(FilesContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(FilesContract.UPDATE_TIME);
        int typeIdIndex = cursor.getColumnIndex(FilesContract.TYPE_ID);
        int userIdIndex = cursor.getColumnIndex(FilesContract.USER_ID);
        int documentIdIndex = cursor.getColumnIndex(FilesContract.DOCUMENT_ID);
        int remotePathIndex = cursor.getColumnIndex(FilesContract.REMOTE_PATH);
        int localPathIndex = cursor.getColumnIndex(FilesContract.LOCAL_PATH);
        int createTimeIndex = cursor.getColumnIndex(FilesContract.CREATE_TIME);


        while (cursor.moveToNext()) {
            File item = new File();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setTypeId(cursor.getInt(typeIdIndex));
            item.setUserId(cursor.getInt(userIdIndex));
            item.setLocalDocumentId(cursor.getInt(documentIdIndex));
            item.setRemotePath(cursor.getString(remotePathIndex));
            item.setLocalPath(cursor.getString(localPathIndex));
            item.setCreateTime(cursor.getInt(createTimeIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    protected CommonResponse<List<File>> callApi(Context context, Object api) {
        //stub
        return null;
    }

    @Override
    protected ContentValues convertEntity(File item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(FilesContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(FilesContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(FilesContract.STATUS, item.getStatus());
        contentValues.put(FilesContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(FilesContract.TYPE_ID, item.getTypeId());
        contentValues.put(FilesContract.USER_ID, item.getUserId());
        contentValues.put(FilesContract.DOCUMENT_ID, item.getLocalDocumentId());
        contentValues.put(FilesContract.REMOTE_PATH, item.getRemotePath());
        if (!TextUtils.isEmpty(item.getLocalPath()))
            contentValues.put(FilesContract.LOCAL_PATH, item.getLocalPath());
        contentValues.put(FilesContract.CREATE_TIME, item.getCreateTime());
        return contentValues;
    }

    protected void saveLocalChanges(Context context, List<File> filesList, int localDocId) {
        List<File> newFiles = new ArrayList<>();
        List<File> createdFiles = new ArrayList<>();
        for (File file: filesList) {
            if (file.getId() == -1) {
                newFiles.add(file);
            } else {
                createdFiles.add(file);
            }
        }

        ContentResolver contentResolver = context.getContentResolver();
        String selection = FilesContract.DOCUMENT_ID + "=?";
        Cursor cursor = contentResolver.query(FilesContract.CONTENT_URI, null, selection, DBUtil.getArgsArray(localDocId), null);
        List<File> deletedFiles = readItems(context, cursor);
        cursor.close();
        deletedFiles.removeAll(createdFiles);

        for (File file: newFiles) {
            file.setLocalDocumentId(localDocId);
            String id = saveItem(context, file).getLastPathSegment();
            RestTask restTask = new RestTask(Integer.valueOf(id), RestTask.Types.FILES_POST);
            RestTaskPoster.postTask(context, restTask, false);
        }

        for (File file: deletedFiles) {
            if (file.getStatus() == File.STATUS_DELETED) continue;
            ContentValues contentValues = new ContentValues();
            contentValues.put(FilesContract.STATUS, BaseItem.STATUS_DELETED);
            Uri itemUri = Uri.parse(FilesContract.CONTENT_URI + "/" + file.getId());
            contentResolver.update(itemUri, contentValues, null, null);
            RestTask restTask = new RestTask(file.getId(), RestTask.Types.FILES_DELETE);
            RestTaskPoster.postTask(context, restTask, false);
        }
    }

    protected void saveRemoteChanges(Context context, List<File> filesList, int localDocId) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = FilesContract.DOCUMENT_ID + "=?" + " AND " + FilesContract.REMOTE_ID + "!=-1";
        Cursor cursor = contentResolver.query(FilesContract.CONTENT_URI, null, selection, DBUtil.getArgsArray(localDocId), null);
        List<File> localFiles = readItems(context, cursor);
        cursor.close();
        List<File> deletedFiles = new ArrayList<>(localFiles);
        List<File> newFiles = new ArrayList<>(filesList);
        deletedFiles.removeAll(filesList);
        newFiles.removeAll(localFiles);

        for (File file: newFiles) {
            file.setLocalDocumentId(localDocId);
        }

        deleteAllItems(context, deletedFiles);
        saveItems(context, newFiles);
    }

    @Override
    protected int saveItems(Context context, List<File> items) {
        for (File file: items) {
            saveItem(context, file);
        }

        return items.size();
    }

    @Override
    protected Uri saveItem(Context context, File file) {
        Uri uri = super.saveItem(context, file);
        int id = Integer.valueOf(uri.getLastPathSegment());
        if (file.getLocalPath() == null) {
            FileLoader.loadFromServer(context, id, false);
        }

        return uri;
    }

    private void deleteAllItems(Context context, List<File> files) {
        ContentResolver contentResolver = context.getContentResolver();
        for (File file: files) {
            Uri itemUri = Uri.parse(FilesContract.CONTENT_URI + "/" + file.getId());
            contentResolver.delete(itemUri, null, null);
            if (file.getLocalPath() != null)
                new java.io.File(file.getLocalPath()).delete();
        }

    }

    protected void deleteAllDocsItems(Context context, Document savedDocument) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(FilesContract.CONTENT_URI, FilesContract.DOCUMENT_ID + "=?",
                DBUtil.getArgsArray(savedDocument.getId()));

        for (File file: savedDocument.getFilesList()) {
            if (file.getLocalPath() != null)
                new java.io.File(file.getLocalPath()).delete();
        }
    }
}
