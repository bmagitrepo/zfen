package com.zoomlee.zoo.ui.fragments;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.api.FQPlacesApi;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.net.model.FQResponse;
import com.zoomlee.zoo.net.model.Place;
import com.zoomlee.zoo.ui.view.PlaceInfoView;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit.RestAdapter;
import retrofit.RetrofitError;


public class BeSafeFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private static GoogleMap mMap;
    private HashMap<Marker, Place> mMarkersHashMap = new HashMap<>();
    private final static int TYPE_POLICIES = 1;
    private final static int TYPE_HOSPITALS = 2;
    private final static int TYPE_CONSULATES = 3;
    private final static float ZOOM_LEVEL = 10;

    private int curPlaceType = -1;
    private final String todayVersion;

    private MapView mapView;
    private PlaceInfoView placeInfoView;
    private ToggleButton policeTrigger;
    private ToggleButton hospitalTrigger;
    private ToggleButton consulateTrigger;
    private List<Place> polices;
    private List<Place> hospitals;
    private List<Place> consulates;
    private Marker oldMarker;
    private Location mLastLocation;
    private String countryName = null;
    private FQPlacesApi fqApi = new RestAdapter.Builder()
            .setEndpoint(FQPlacesApi.API_URL)
            .build()
            .create(FQPlacesApi.class);

    public static BeSafeFragment newInstance() {
        BeSafeFragment fragment = new BeSafeFragment();
        return fragment;
    }

    public BeSafeFragment() {
        Calendar today = Calendar.getInstance();
        todayVersion = String.format("%04d%02d%02d", today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_be_safe, container, false);
        mapView = (MapView) mView.findViewById(R.id.mapview);
        placeInfoView = (PlaceInfoView) mView.findViewById(R.id.placeInfoView);
        mapView.onCreate(savedInstanceState);
        policeTrigger = (ToggleButton) mView.findViewById(R.id.policeTb);
        hospitalTrigger = (ToggleButton) mView.findViewById(R.id.hospitalTb);
        consulateTrigger = (ToggleButton) mView.findViewById(R.id.consulateTb);

        if (Util.checkPlayServices(getActivity()))
            setupMap();
        else {
            DeveloperUtil.michaelLog("UNSUPPORT SERVICEs");
            return mView;
        }

        policeTrigger.setOnCheckedChangeListener(this);
        hospitalTrigger.setOnCheckedChangeListener(this);
        consulateTrigger.setOnCheckedChangeListener(this);

        policeTrigger.setChecked(true);
        return mView;
    }


    private void setupMap() {
        MapsInitializer.initialize(this.getActivity());
        mMap = mapView.getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                boolean loadPlaces = mLastLocation == null;
                mLastLocation = location;
                if (loadPlaces) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), ZOOM_LEVEL);
                    mMap.animateCamera(cameraUpdate);
                    showPlaces(curPlaceType);
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
                if (oldMarker != null)
                    oldMarker.setIcon(getMarkerIcon());

                marker.setIcon(getMarkerIconActive());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(marker.getPosition());
                mMap.animateCamera(cameraUpdate);
                showItemInfo(marker);
                oldMarker = marker;
                return true;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (oldMarker != null)
                    oldMarker.setIcon(getMarkerIcon());
                hideItemInfo();
            }
        });

        if (mLastLocation != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), ZOOM_LEVEL);
            mMap.moveCamera(cameraUpdate);
            mLastLocation = null;
        }
    }


    private void showItemInfo(com.google.android.gms.maps.model.Marker marker) {
        marker.showInfoWindow();
        Place place = mMarkersHashMap.get(marker);
        placeInfoView.setVisibility(View.VISIBLE);
        placeInfoView.setPlace(place);
    }

    private void hideItemInfo() {
        oldMarker = null;
        placeInfoView.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        GAUtil.getUtil().timeSpent(GAEvents.ACTION_BESAFE_SEGMENT);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) return;
        switch (buttonView.getId()) {
            case R.id.policeTb:
                hospitalTrigger.setChecked(false);
                consulateTrigger.setChecked(false);
                showPlaces(TYPE_POLICIES);
                break;
            case R.id.hospitalTb:
                policeTrigger.setChecked(false);
                consulateTrigger.setChecked(false);
                showPlaces(TYPE_HOSPITALS);
                break;
            case R.id.consulateTb:
                policeTrigger.setChecked(false);
                hospitalTrigger.setChecked(false);
                showPlaces(TYPE_CONSULATES);
                break;
        }
    }

    private void showPlaces(int placeType) {
        mMap.clear();
        hideItemInfo();
        curPlaceType = placeType;
        switch (placeType) {
            case TYPE_POLICIES:
                if (polices != null)
                    showPlaces(polices);
                else
                    new LoadPlacesAsyncTask(TYPE_POLICIES).execute();
                break;
            case TYPE_HOSPITALS:
                if (hospitals != null)
                    showPlaces(hospitals);
                else
                    new LoadPlacesAsyncTask(TYPE_HOSPITALS).execute();
                break;
            case TYPE_CONSULATES:
                if (consulates != null)
                    showPlaces(consulates);
                else
                    new LoadPlacesAsyncTask(TYPE_CONSULATES).execute();
                break;
        }
    }

    private void showPlaces(List<Place> places) {
        if (places == null) return;

        mMarkersHashMap.clear();
        mMap.clear();
        for (Place place : places) {
            MarkerOptions markerOption = new MarkerOptions().position(place.getLocation());
            markerOption.icon(getMarkerIcon());
            Marker currentMarker = mMap.addMarker(markerOption);
            mMarkersHashMap.put(currentMarker, place);
        }

    }

    private BitmapDescriptor getMarkerIconActive() {
        switch (curPlaceType) {
            case TYPE_POLICIES:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_police_active);
            case TYPE_HOSPITALS:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital_active);
            case TYPE_CONSULATES:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_consulate_active);
        }
        return null;
    }

    private BitmapDescriptor getMarkerIcon() {
        switch (curPlaceType) {
            case TYPE_POLICIES:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_police);
            case TYPE_HOSPITALS:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital);
            case TYPE_CONSULATES:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_consulate);
        }
        return null;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        if(mMap == null)
            return;

        int countryId = SharedPreferenceUtils.getUtils().getUserSettings().getCountryId();
        DaoHelper<Country> countryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Country.class);
        Country country = countryDaoHelper.getItemByRemoteId(getActivity(), countryId);
        countryName = country == null ? null : country.getName();

        consulates = null;
        polices = null;
        hospitals = null;
        showPlaces(curPlaceType);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private class LoadPlacesAsyncTask extends AsyncTask<Void, Void, List<Place>> {
        private final int placeType;

        private LoadPlacesAsyncTask(int placeType) {
            this.placeType = placeType;
        }

        @Override
        protected List<Place> doInBackground(Void... params) {
            if (mLastLocation == null) {
                DeveloperUtil.michaelLog("Can't load places, placeType - " + placeType + ", reason: UNKNOWN USER POSITION");
                Log.w("LoadPlaces", "Can't load places, placeType - " + placeType + ", reason: UNKNOWN USER POSITION");
                return null;
            }
            FQResponse fqResponse = null;
            String coordinates = String.format(Locale.US, "%f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude());

            try {
                switch (placeType) {
                    case TYPE_POLICIES:
                        fqResponse = fqApi.getPolices(todayVersion, coordinates);
                        polices = fqResponse.getPlaces();
                        return polices;
                    case TYPE_HOSPITALS:
                        fqResponse = fqApi.getHospitals(todayVersion, coordinates);
                        hospitals = fqResponse.getPlaces();
                        return hospitals;
                    case TYPE_CONSULATES:
                        if (countryName == null)
                            fqResponse = fqApi.getConsulates(todayVersion, coordinates);
                        else
                            fqResponse = fqApi.getConsulates(todayVersion, coordinates, countryName);
                        consulates = fqResponse.getPlaces();
                        return consulates;
                }
            } catch (RetrofitError re) {
                Log.w("LoadPlaces", "Can't load places, placeType - " + placeType + ", reason: " + re.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Place> places) {
            showPlaces(places);
        }
    }

    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        public MarkerInfoWindowAdapter() {
        }

        @Override
        public View getInfoWindow(Marker marker) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.marker_infowindow, null);

            Place place = mMarkersHashMap.get(marker);

            TextView markerLabel = (TextView) v.findViewById(R.id.marker_label);

            markerLabel.setText(place.getName());

            return v;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
