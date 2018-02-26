package com.zoomlee.zoo;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.net.model.Tax;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.provider.helpers.TaxProviderHelper.TaxContract;
import com.zoomlee.zoo.ui.activity.CreateEditTaxActivity;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.GeoCodeUtil;
import com.zoomlee.zoo.utils.NotificationsUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 5/6/15
 */
public class LocationService extends Service implements LocationListener {

    public static void stopLocationTracking(Context context) {
        Intent intent = new Intent(context, LocationService.class);
        context.stopService(intent);
    }

    public static void startLocationTracking(final Context context) {
        LocationThread.getInstance().locationHandler.post(new Runnable() {
            @Override
            public void run() {
                GeoCodeUtil.init(context);
            }
        });

        Intent intent = new Intent(context, LocationService.class);
        context.startService(intent);
    }

    private DaoHelper<Tax> taxDaoHelper;

    public LocationService(){
        super();
        taxDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tax.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        manager.requestLocationUpdates(0, 5000, criteria, this, LocationThread.getInstance().getLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        manager.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!BillingUtils.isPro(SharedPreferenceUtils.getUtils().getUserSettings())) return;

        String countryCode = GeoCodeUtil.getCountryISOCode(location.getLatitude(), location.getLongitude());
        Country country = GeoCodeUtil.getCountryByISOCode(countryCode);
        if (country == null)
            return;

        int prevCountryId = SharedPreferenceUtils.getUtils().getIntSetting(SharedPreferenceUtils.CURRENT_COUNTRY_KEY);
        if (country.getRemoteId() == prevCountryId)
            return;

        int homeCountryId = SharedPreferenceUtils.getUtils().getIntSetting(SharedPreferenceUtils.UsersKeys.COUNTRY_ID);
        if (homeCountryId == country.getRemoteId())
            closeTrip(country);
        else
            newTrip(country);
        SharedPreferenceUtils.getUtils().setIntSetting(SharedPreferenceUtils.CURRENT_COUNTRY_KEY, country.getRemoteId());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void closeTrip(Country newCountry) {
        String selection = TaxContract.DEPARTURE + "=-1 AND "
                + TaxContract.TABLE_NAME + "." + TaxContract.STATUS + "=" + Tax.STATUS_NORMAL;
        List<Tax> taxList = taxDaoHelper.getAllItems(this, selection, null, null);
        long departure = System.currentTimeMillis() / 1000L;
        for (Tax tax: taxList) {
            if (tax.getCountryId() != newCountry.getRemoteId()) {
                tax.setDeparture(departure);
                taxDaoHelper.saveLocalChanges(this, tax);
                showNotification(tax, false);
            }
        }
    }

    private void newTrip(Country country) {
        closeTrip(country);

        User user = SharedPreferenceUtils.getUtils().getUserSettings();

        Tax tax = new Tax();
        tax.setUserId(user.getRemoteId());
        tax.setCountryId(country.getRemoteId());
        tax.setCountryName(country.getName());
        tax.setArrival(System.currentTimeMillis() / 1000L);
        taxDaoHelper.saveLocalChanges(this, tax);
        showNotification(tax, true);
    }

    static class LocationThread extends HandlerThread {

        private static volatile LocationThread instance;

        private Handler locationHandler;

        public static LocationThread getInstance() {
            if (instance == null) {
                synchronized (LocationThread.class) {
                    if (instance == null) {
                        instance = new LocationThread();
                    }
                }
            }
            return instance;
        }

        private LocationThread() {
            super("LocationThread");
            start();
            waitUntilReady();
        }

        private void waitUntilReady() {
            locationHandler = new Handler(getLooper());
        }
    }

    private void showNotification(Tax tax, boolean created) {
        Intent activityIntent = CreateEditTaxActivity.getIntentForStart(this, true, tax);
        activityIntent.setAction("open_tax_" + System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);

        int stringId = created ? R.string.you_have_arrived_to : R.string.you_have_left;
        String title = getString(stringId, tax.getCountryName());

        NotificationsUtil.showNotification(this, pendingIntent, title, null);
    }
}