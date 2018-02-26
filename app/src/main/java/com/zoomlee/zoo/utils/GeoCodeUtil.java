package com.zoomlee.zoo.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.provider.helpers.CountriesProviderHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 4/29/15
 */
public class GeoCodeUtil {
    private static JsonObject[] countries;
    private static Context ctx;

    public static void init(Context context) {
        ctx = context.getApplicationContext();
        AssetManager assetManager = ctx.getAssets();
        try {
            InputStream is = assetManager.open("ReverseGeocodeCountry.json");
            InputStreamReader reader = new InputStreamReader(is);
            Gson gson = new Gson();
            countries = gson.fromJson(reader, JsonObject[].class);
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

    public static Country getCountryByISOCode(String isoCode) {
        if (isoCode == null) return null;
        DaoHelper<Country> countryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Country.class);
        List<Country> list = countryDaoHelper.getAllItems(ctx, "upper(" + CountriesProviderHelper.CountriesContract.CODE + ")=?", new String[]{isoCode.toUpperCase()}, null);
        if (list == null || list.size() == 0)
            return null;
        else
            return list.get(0);
    }


    public static String getCountryISOCode(double lat, double lng) {
        JsonArray polygon;
        for (JsonObject country : countries) {
            PolygonType type = getPolygonType(country);

            if (type == PolygonType.POLYGON) {
                polygon = country.getAsJsonObject("geometry").getAsJsonArray("coordinates").get(0).getAsJsonArray();
                if (pointInPolygon(lat, lng, polygon))
                    return getISOCountryCode(country);

            } else if (type == PolygonType.MULTI_POLYGON) {
                for (int j = 0; j < country.getAsJsonObject("geometry").getAsJsonArray("coordinates").size(); j++) {
                    polygon = country.getAsJsonObject("geometry").getAsJsonArray("coordinates").get(j).getAsJsonArray().get(0).getAsJsonArray();
                    if (pointInPolygon(lat, lng, polygon))
                        return getISOCountryCode(country);
                }
            }

        }
        return null;
    }

    private static boolean pointInPolygon(double lat, double lng, JsonArray polygon) {
        boolean isFound = false;
        int polygonSize = polygon.size();
        int i = 0;
        int j = polygonSize - 1;
        for (; i < polygonSize; j = i++) {
            if (
                    ((getLatitude(getItem(polygon, i)) > lat) != (getLatitude(getItem(polygon, j)) > lat)) &&
                            (lng <
                                    (getLongtitude(getItem(polygon, j)) - getLongtitude(getItem(polygon, i))) * (lat - getLatitude(getItem(polygon, j))) / (getLatitude(getItem(polygon, j)) - getLatitude(getItem(polygon, i))) + getLongtitude(getItem(polygon, i))
                            )
                    ) {
                isFound = !isFound;
            }
        }
        return isFound;
    }

    private static JsonArray getItem(JsonArray polygon, int position) {
        return polygon.get(position).getAsJsonArray();
    }

    private static double getLatitude(JsonArray polygonItem) {
        return polygonItem.get(1).getAsDouble();
    }

    private static double getLongtitude(JsonArray polygonItem) {
        return polygonItem.get(0).getAsDouble();
    }

    private static String getISOCountryCode(JsonObject country) {
        return country.get("iso_code").getAsString();
    }

    enum PolygonType {POLYGON, MULTI_POLYGON}

    private static PolygonType getPolygonType(JsonObject country) {
        if (TextUtils.equals(country.getAsJsonObject("geometry").get("type").getAsString(), "Polygon"))
            return PolygonType.POLYGON;
        else if (TextUtils.equals(country.getAsJsonObject("geometry").get("type").getAsString(), "MultiPolygon"))
            return PolygonType.MULTI_POLYGON;

        return null;
    }
}

