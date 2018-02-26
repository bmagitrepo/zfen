package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.Color;
import com.zoomlee.zoo.provider.helpers.ColorsProviderHelper.ColorsContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class ColorsDaoHelper extends DaoHelper<Color> {

    protected ColorsDaoHelper() {
        super(LastSyncTimeKeys.COLORS, ColorsContract.CONTENT_URI);
    }

    @Override
    public List<Color> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Color> readItems(Context context, Cursor cursor, OnItemLoadedListener<Color> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Color>();
        List<Color> items = new ArrayList<Color>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(ColorsContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(ColorsContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(ColorsContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(ColorsContract.UPDATE_TIME);
        int nameIndex = cursor.getColumnIndex(ColorsContract.NAME);
        int hexIndex = cursor.getColumnIndex(ColorsContract.HEX);


        while (cursor.moveToNext()) {
            Color item = new Color();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            item.setHex(cursor.getString(hexIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getColors();
    }

    @Override
    protected CommonResponse<List<Color>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getColors(getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(Color item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(ColorsContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(ColorsContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(ColorsContract.STATUS, item.getStatus());
        contentValues.put(ColorsContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(ColorsContract.NAME, item.getName());
        contentValues.put(ColorsContract.HEX, item.getHex());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<Color> items) {
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.ColorsChanged());
        return result;
    }
}
