package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.model.FQResponse;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 3/2/15
 */
public interface FQPlacesApi {
    String API_URL = "https://api.foursquare.com/v2";

    /***
     *
     * @param date use format like "20130815"("yyyymmdd")
     * @param coordinates use format like "50.4383923,30.5200802"("lat,lng")
     * @param categoryId
     * @return list of places
     */
    @GET("/venues/search?client_id=2B1V2ZS2HWOLMXFJGFS5NXREGSPRTXTCON0VI23Z2L4SOYW5&client_secret=NKWQTLLVC4DSDWEWZGZDOUXQYHP5I35EJE20BZJ30RGUIKXS")
    FQResponse getPlaces(@Query("v") String date/*20130815*/, @Query("ll") String coordinates /*50.4383923,30.5200802*/, @Query("categoryId") String categoryId);

    @GET("/venues/search?client_id=2B1V2ZS2HWOLMXFJGFS5NXREGSPRTXTCON0VI23Z2L4SOYW5&client_secret=NKWQTLLVC4DSDWEWZGZDOUXQYHP5I35EJE20BZJ30RGUIKXS&categoryId=4bf58dd8d48988d12e941735")
    FQResponse getPolices(@Query("v") String date/*20130815*/, @Query("ll") String coordinates /*50.4383923,30.5200802*/);

    @GET("/venues/search?client_id=2B1V2ZS2HWOLMXFJGFS5NXREGSPRTXTCON0VI23Z2L4SOYW5&client_secret=NKWQTLLVC4DSDWEWZGZDOUXQYHP5I35EJE20BZJ30RGUIKXS&categoryId=4bf58dd8d48988d196941735")
    FQResponse getHospitals(@Query("v") String date/*20130815*/, @Query("ll") String coordinates /*50.4383923,30.5200802*/);

    @GET("/venues/search?client_id=2B1V2ZS2HWOLMXFJGFS5NXREGSPRTXTCON0VI23Z2L4SOYW5&client_secret=NKWQTLLVC4DSDWEWZGZDOUXQYHP5I35EJE20BZJ30RGUIKXS&categoryId=4bf58dd8d48988d12c951735")
    FQResponse getConsulates(@Query("v") String date/*20130815*/, @Query("ll") String coordinates /*50.4383923,30.5200802*/);

    @GET("/venues/search?client_id=2B1V2ZS2HWOLMXFJGFS5NXREGSPRTXTCON0VI23Z2L4SOYW5&client_secret=NKWQTLLVC4DSDWEWZGZDOUXQYHP5I35EJE20BZJ30RGUIKXS&categoryId=4bf58dd8d48988d12c951735")
    FQResponse getConsulates(@Query("v") String date/*20130815*/, @Query("ll") String coordinates /*50.4383923,30.5200802*/, @Query("query") String query);
}
