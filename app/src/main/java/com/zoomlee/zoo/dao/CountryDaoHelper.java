package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.provider.helpers.CountriesProviderHelper.CountriesContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class CountryDaoHelper extends DaoHelper<Country> {

    protected CountryDaoHelper() {
        super(LastSyncTimeKeys.COUNTRIES, CountriesContract.CONTENT_URI);
    }

    @Override
    public List<Country> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Country> readItems(Context context, Cursor cursor, OnItemLoadedListener<Country> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Country>();
        List<Country> items = new ArrayList<Country>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(CountriesContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(CountriesContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(CountriesContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(CountriesContract.UPDATE_TIME);
        int nameIndex = cursor.getColumnIndex(CountriesContract.NAME);
        int codeIndex = cursor.getColumnIndex(CountriesContract.CODE);
        int prioritizeIndex = cursor.getColumnIndex(CountriesContract.PRIORITIZE);
        int flagIndex = cursor.getColumnIndex(CountriesContract.FLAG);


        while (cursor.moveToNext()) {
            Country item = new Country();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            item.setCode(cursor.getString(codeIndex));
            item.setPrioritize(cursor.getInt(prioritizeIndex));
            item.setFlag(cursor.getString(flagIndex));

            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public boolean requireUpdate(Changes changes) {
        return getLastSyncTime() < changes.getCountries();
    }

    @Override
    protected CommonResponse<List<Country>> callApi(Context context, Object api) {
        return ((StaticDataApi) api).getCountries(getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(Country item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(CountriesContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(CountriesContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(CountriesContract.STATUS, item.getStatus());
        contentValues.put(CountriesContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(CountriesContract.NAME, item.getName());
        contentValues.put(CountriesContract.CODE, item.getCode());
        contentValues.put(CountriesContract.PRIORITIZE, item.getPrioritize());
        contentValues.put(CountriesContract.FLAG, item.getFlag());
        return contentValues;
    }

    @Override
    public int saveRemoteChanges(Context context, List<Country> items) {
        int result = super.saveRemoteChanges(context, items);
        if (result > 0)
            EventBus.getDefault().post(new Events.CountriesChanged());
        return result;
    }
}
