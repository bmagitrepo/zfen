package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.Group;
import com.zoomlee.zoo.provider.helpers.GroupsHelper.GroupsContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class GroupsDaoHelper extends DaoHelper<Group> {

    protected GroupsDaoHelper() {
        super(LastSyncTimeKeys.GROUPS, GroupsContract.CONTENT_URI);
    }

    @Override
    public List<Group> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Group> readItems(Context context, Cursor cursor, OnItemLoadedListener<Group> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Group>();
        List<Group> items = new ArrayList<Group>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(GroupsContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(GroupsContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(GroupsContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(GroupsContract.UPDATE_TIME);
        int nameIndex = cursor.getColumnIndex(GroupsContract.NAME);


        while (cursor.moveToNext()) {
            Group item = new Group();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getDocumentsTypesGroups();
    }

    @Override
    protected CommonResponse<List<Group>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getDocumentsTypes2Groups(getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(Group item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(GroupsContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(GroupsContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(GroupsContract.STATUS, item.getStatus());
        contentValues.put(GroupsContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(GroupsContract.NAME, item.getName());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<Group> items) {
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.GroupsChanged());
        return result;
    }
}
