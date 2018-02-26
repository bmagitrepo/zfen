package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.Tax;

import java.util.List;

import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
public interface TaxDataApi {

    @GET("/tax/")
    CommonResponse<List<Tax>> getTax(@Header("zoomle_key") String privateKey, @Query("time") int time);

    @FormUrlEncoded
    @POST("/tax/")
    CommonResponse<Tax> postTax(@Header("zoomle_key") String privateKey, @Field("country_id") int countryId,
                                   @Field("arrival") long arrival, @Field("departure") Long departure);
    @FormUrlEncoded
    @PUT("/tax/")
    CommonResponse<Tax> putTax(@Header("zoomle_key") String privateKey, @Query("id") int id,
                                     @Field("country_id") int countryId, @Field("arrival") long arrival,
                                     @Field("departure") Long departure);

    @DELETE("/tax/")
    CommonResponse<Object> deleteTax(@Header("zoomle_key") String privateKey, @Query("id") int id);
}
