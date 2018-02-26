package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.model.BaseItem;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.utils.DBUtil;

import java.util.List;

import static com.zoomlee.zoo.provider.helpers.BaseProviderHelper.DataColumns;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 05.05.15.
 */
abstract class UserDataDaoHelper<Entity extends BaseItem> extends DaoHelper<Entity> {

    private final int UPLOAD_TASK;
    private final int DELETE_TASK;

    protected UserDataDaoHelper(String lastSyncTimeKey, Uri contentUri, int uploadTask, int deleteTask) {
        super(lastSyncTimeKey, contentUri);

        UPLOAD_TASK = uploadTask;
        DELETE_TASK = deleteTask;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return true;
    }

    @Override
    public synchronized Uri saveLocalChanges(Context context, Entity entity) {
        if (entity.getStatus() == Entity.STATUS_DELETED) {
            deleteItem(context, entity);
            return null;
        }

        Uri result = saveItem(context, entity);
        int localEntityId = Integer.valueOf(result.getLastPathSegment());
        entity.setId(localEntityId);
        saveRelatedLocalData(context, entity);

        RestTask restTask = new RestTask(localEntityId, UPLOAD_TASK);
        RestTaskPoster.postTask(context, restTask, true);
        postEntityChanged(entity);

        return result;
    }

    @Override
    public synchronized void deleteItem(Context context, Entity entity) {
        Entity savedEntity;
        if (entity.getId() != -1) {
            savedEntity = getItemByLocalId(context, entity.getId(), true);
            if (savedEntity == null || savedEntity.getStatus() == Entity.STATUS_DELETED)
                return;
            markAsDeleted(context, savedEntity.getId());
        } else if (entity.getRemoteId() != -1) {
            savedEntity = getItemByRemoteId(context, entity.getRemoteId());
            if (savedEntity == null || savedEntity.getStatus() == Entity.STATUS_DELETED)
                return;
            ContentResolver contentResolver = context.getContentResolver();
            Uri itemUri = Uri.parse(CONTENT_URI + "/" + savedEntity.getId());
            contentResolver.delete(itemUri, null, null);
        } else
            throw new IllegalArgumentException("Entity without id!!!");

        deleteRelatedData(context, savedEntity);
        postEntityDeleted(savedEntity);
    }

    private void markAsDeleted(Context context, int id) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DataColumns.STATUS, Entity.STATUS_DELETED);
        contentResolver.update(CONTENT_URI, contentValues,
                DataColumns._ID + "=?", DBUtil.getArgsArray(id));

        RestTask restTask = new RestTask(id, DELETE_TASK);
        RestTaskPoster.postTask(context, restTask, true);
    }

    @Override
    public synchronized Uri saveRemoteChanges(Context context, Entity entity) {
        if (entity.getStatus() == Document.STATUS_DELETED) {
            deleteItem(context, entity);
            return null;
        } else {
            Uri result = saveItem(context, entity);
            int localEntityId = Integer.valueOf(result.getLastPathSegment());
            entity.setId(localEntityId);
            saveRelatedRemoteData(context, entity);
            postEntityChanged(entity);
            return result;
        }
    }

    @Override
    public int saveRemoteChanges(Context context, List<Entity> items) {
        for(Entity entity: items) {
            saveRemoteChanges(context, entity);
        }

        return items.size();
    }

    protected void saveRelatedLocalData(Context context, Entity entity){}
    protected void saveRelatedRemoteData(Context context, Entity entity){}
    protected void deleteRelatedData(Context context, Entity entity){}
    protected abstract void postEntityChanged(Entity entity);
    protected abstract void postEntityDeleted(Entity entity);
}
