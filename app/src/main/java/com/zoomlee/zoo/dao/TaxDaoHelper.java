package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.TaxDataApi;
import com.zoomlee.zoo.net.model.Tax;
import com.zoomlee.zoo.provider.helpers.TaxProviderHelper.TaxContract;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;
import com.zoomlee.zoo.utils.TimeUtil;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;


/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 30.04.15.
 */
public class TaxDaoHelper extends UserDataDaoHelper<Tax> {

    public TaxDaoHelper() {
        super(LastSyncTimeKeys.TAX, TaxContract.CONTENT_URI, RestTask.Types.TAX_UPLOAD, RestTask.Types.TAX_DELETE);
    }

    @Override
    public List<Tax> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Tax> readItems(Context context, Cursor cursor, OnItemLoadedListener<Tax> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<>();
        List<Tax> items = new ArrayList<>(cursor.getCount());

        String[] projection = TaxContract.ALL_COLUMNS_PROJECTION;

        int idIndex = Util.findIndex(projection, TaxContract.TABLE_NAME + "." + TaxContract._ID);
        int remoteIdIndex = Util.findIndex(projection, TaxContract.TABLE_NAME + "." + TaxContract.REMOTE_ID);
        int statusIndex = Util.findIndex(projection, TaxContract.TABLE_NAME + "." + TaxContract.STATUS);
        int updateTimeIndex = Util.findIndex(projection, TaxContract.TABLE_NAME + "." + TaxContract.UPDATE_TIME);
        int userIdIndex = Util.findIndex(projection, TaxContract.TABLE_NAME + "." + TaxContract.USER_ID);
        int countryIdIndex = Util.findIndex(projection, TaxContract.TABLE_NAME + "." + TaxContract.COUNTRY_ID);
        int arrivalIndex = Util.findIndex(projection, TaxContract.TABLE_NAME + "." + TaxContract.ARRIVAL);
        int departureIndex = Util.findIndex(projection, TaxContract.TABLE_NAME + "." + TaxContract.DEPARTURE);
        int countryNameIndex = Util.findIndex(projection, TaxContract.COUNTRY_NAME);
        int countryCodeIndex = Util.findIndex(projection, TaxContract.COUNTRY_CODE);
        int countryFlagIndex = Util.findIndex(projection, TaxContract.COUNTRY_FLAG);

        while (cursor.moveToNext()) {
            Tax item = new Tax();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setUserId(cursor.getInt(userIdIndex));
            item.setCountryId(cursor.getInt(countryIdIndex));
            item.setCountryName(cursor.getString(countryNameIndex));
            item.setCountryCode(cursor.getString(countryCodeIndex));
            item.setCountryFlag(cursor.getString(countryFlagIndex));
            item.setArrival(cursor.getInt(arrivalIndex));
            item.setDeparture(cursor.getInt(departureIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public List<Tax> getAllItems(Context context) {
        String selection = TaxContract.TABLE_NAME + "." + TaxContract.STATUS + "=" + Tax.STATUS_NORMAL;
        List<Tax> taxList = getAllItems(context, selection, null, null);
        List<Tax> result = new ArrayList<>(taxList.size());
        for (Tax tax : taxList) {
            splitTax(result, tax, Long.MIN_VALUE, Long.MAX_VALUE);
        }
        return result;
    }

    @Override
    public Tax getItemByLocalId(Context context, int localId, boolean showDeleted) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = TaxContract.TABLE_NAME + "." + TaxContract._ID + "=" + localId;
        selection = selection + (showDeleted ? "" : " AND " + TaxContract.TABLE_NAME + "." + TaxContract.STATUS + "=" + Tax.STATUS_NORMAL);
        Cursor cursor = contentResolver.query(TaxContract.CONTENT_URI,
                null, selection, null, null);

        List<Tax> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    @Override
    public Tax getItemByRemoteId(Context context, int remoteId) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(getURI(), null, TaxContract.TABLE_NAME + "." + TaxContract.REMOTE_ID + "=?",
                new String[]{Integer.toString(remoteId)}, null);

        List<Tax> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    /**
     * Return tax that intersect interval [fromDate, toDate]. Result will be split by years. <br/>
     * To get split values, that included in interval, use getDisplayArrival() and getDisplayDeparture() method of Tax.
     *
     * @param context
     * @param fromDate begin of interval (UTC timestamp in seconds)
     * @param toDate   end of interval (UTC timestamp in seconds)
     * @return
     */
    public List<Tax> getTaxInInterval(Context context, long fromDate, long toDate) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = TaxContract.TABLE_NAME + "." + TaxContract.STATUS + "=" + Tax.STATUS_NORMAL
                + " AND " + TaxContract.TABLE_NAME + "." + TaxContract.ARRIVAL + "<=" + toDate + " AND ("
                + TaxContract.TABLE_NAME + "." + TaxContract.DEPARTURE + "<=0 AND "
                + TaxContract.TABLE_NAME + "." + TaxContract.ARRIVAL + "<=" + System.currentTimeMillis() / 1000 + " OR "
                + TaxContract.TABLE_NAME + "." + TaxContract.DEPARTURE + ">=" + fromDate + ")";
        Cursor cursor = contentResolver.query(TaxContract.CONTENT_URI, null, selection, null, null);
        List<Tax> taxList = readItems(context, cursor);
        List<Tax> result = new ArrayList<>(taxList.size());
        for (Tax tax : taxList) {
            splitTax(result, tax, fromDate, toDate);
        }

        return result;
    }

    private void splitTax(List<Tax> taxList, Tax tax, long fromDate, long toDate) {
        long displayArrival = Math.max(tax.getArrival(), fromDate);
        long displayDeparture = Math.min(tax.getDeparturewWithAuto(), toDate);
        if (displayDeparture <= 0) displayDeparture = toDate;
        tax.setDisplayArrival(displayArrival);
        tax.setDisplayDeparture(displayDeparture);

        while (tax.getDisplayDeparture() > TimeUtil.getYearsEnd(tax.getDisplayArrival())) {
            Tax newTax = new Tax(tax);
            newTax.setDisplayArrival(TimeUtil.getNextYearsBegin(tax.getDisplayArrival()));
            tax.setDisplayDeparture(TimeUtil.getYearsEnd(tax.getDisplayArrival()));
            taxList.add(tax);
            tax = newTax;
        }

        taxList.add(tax);
    }

    @Override
    protected CommonResponse<List<Tax>> callApi(Context context, Object api) {
        return buildTaxDataApi().getTax(SharedPreferenceUtils.getUtils().getPrivateKey(), getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(Tax item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(TaxContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(TaxContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(TaxContract.STATUS, item.getStatus());
        contentValues.put(TaxContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(TaxContract.USER_ID, item.getUserId());
        contentValues.put(TaxContract.COUNTRY_ID, item.getCountryId());
        contentValues.put(TaxContract.ARRIVAL, item.getArrival());
        contentValues.put(TaxContract.DEPARTURE, item.getDeparture());
        return contentValues;
    }

    @Override
    protected void postEntityChanged(Tax tax) {
        EventBus.getDefault().post(new Events.TaxChanged(Events.TaxChanged.UPDATED, tax));
    }

    @Override
    protected void postEntityDeleted(Tax tax) {
        EventBus.getDefault().post(new Events.TaxChanged(Events.TaxChanged.DELETED, tax));
    }

    private TaxDataApi buildTaxDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(TaxDataApi.class);
    }
}
