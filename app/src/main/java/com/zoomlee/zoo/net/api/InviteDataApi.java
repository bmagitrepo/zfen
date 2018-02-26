package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.Invite;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.04.15.
 */
public interface InviteDataApi {

    @GET("/invite/")
    CommonResponse<List<Invite>> getInvites(@Header("zoomle_key") String privateKey);

    @FormUrlEncoded
    @POST("/invite/")
    CommonResponse<Object> postInvite(@Header("zoomle_key") String privateKey, @Field("phone") String phone,
                               @Field("email") String email);

    public static class ResponseCodes {

        public static class PostInvite {
            public static final int ALL_GOOD = 200;
            public static final int BAD_PRIVATE_KEY = 401;
            public static final int CANT_SEND_SMS_BAD_GATEWAY = 405;
            public static final int CANT_SEND_EMAIL_BAD_GATEWAY = 406;
            public static final int UNKNOWN_ERROR = 500;
            public static final int BAD_EMAIL = 7001;
            public static final int BAD_PHONE = 7002;
            public static final int NEED_PHONE_OR_EMAIL = 7003;
            public static final int ALLREADY_SEND_TO_THIS_EMAIL = 7004;
            public static final int ALLREADY_SEND_TO_THIS_PHONE = 7005;
            public static final int USER_WITH_THIS_EMAIL_EXIST = 7006;
            public static final int USER_WITH_THIS_PHONE_EXIST = 7007;
        }
    }
}
