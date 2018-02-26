package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.User;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
public interface UserDataApi {

    @GET("/users/")
    void getUser(@Header("zoomle_key") String privateKey, @Query("id") int id, Callback<CommonResponse<User>> callback);


    @GET("/users/")
    CommonResponse<User> getUser(@Header("zoomle_key") String privateKey, @Query("id") int id);

    @Multipart
    @PUT("/users/")
    CommonResponse<User> updateUser(@Header("zoomle_key") String privateKey, @Query("id") int remoteId,
                                    @Query("name") String name, @Query("country_id") Integer country_id,
                                    @Part("image") TypedFile typedFile);

    @PUT("/users/")
    CommonResponse<User> updateUser(@Header("zoomle_key") String privateKey, @Query("id") int remoteId,
                                    @Query("name") String name, @Query("country_id") Integer country_id);

    @FormUrlEncoded
    @POST("/users/")
    void updatePhoneEmail(@Header("zoomle_key") String privateKey, @Field("id") int userId, @Field("email") String email, @Field("phone") String phone, Callback<CommonResponse<Object>> callback);

    @FormUrlEncoded
    @POST("/users/")
    void sendPassCode(@Header("zoomle_key") String privateKey, @Field("id") int userId, @Field("code") String passCode, Callback<CommonResponse<User>> callback);

}
