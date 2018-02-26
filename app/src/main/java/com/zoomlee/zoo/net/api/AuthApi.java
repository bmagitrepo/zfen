package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.User;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/12/15
 */
public interface AuthApi {
    //TODO: before release enable GA

    @FormUrlEncoded
    @POST("/auth/")
    Response login(@Field("email") String email, @Field("phone") String phone);

    @FormUrlEncoded
    @POST("/auth/")
    void login(@Field("email") String email, @Field("phone") String phone, Callback<CommonResponse<Object>> callback);

    @FormUrlEncoded
    @PUT("/auth/")
    void sendPasscode(@Field("email") String field, @Field("phone") String phone, @Field("code") String passCode, Callback<CommonResponse<User>> callback);

}
