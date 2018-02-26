package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.BuildConfig;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.TrialTypes;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 5/19/15
 */
public interface BillingApi {

    String DEBUG_SUFFIX = "&debug=" + BuildConfig.IS_DEBUG;

    @FormUrlEncoded
    @PUT("/billing")
    void enableBilling(@Header("zoomle_key") String privateKey, @Field("type") int type, @Field("plan") int plan, Callback<CommonResponse<Object>> callback);

    @FormUrlEncoded
    @POST("/billing?plan=1&type=2" + DEBUG_SUFFIX)
    void enableMonthPro(@Header("zoomle_key") String privateKey, @Field("token") String token, Callback<CommonResponse<Object>> callback);

    @FormUrlEncoded
    @POST("/billing?plan=2&type=2" + DEBUG_SUFFIX)
    void enableYearPro(@Header("zoomle_key") String privateKey, @Field("token") String token, Callback<CommonResponse<Object>> callback);

    @FormUrlEncoded
    @POST("/billing?plan=3&type=2" + DEBUG_SUFFIX)
    void enableMonthFamily(@Header("zoomle_key") String privateKey, @Field("token") String token, Callback<CommonResponse<Object>> callback);

    @FormUrlEncoded
    @POST("/billing?plan=4&type=2" + DEBUG_SUFFIX)
    void enableYearFamily(@Header("zoomle_key") String privateKey, @Field("token") String token, Callback<CommonResponse<Object>> callback);

    @GET("/billing-trial-types")
    void billingTypes(@Header("zoomle_key") String privateKey, Callback<CommonResponse<TrialTypes>> callback);
}
