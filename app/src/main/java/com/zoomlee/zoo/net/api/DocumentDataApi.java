package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.Document;

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
public interface DocumentDataApi {

    @GET("/documents/")
    CommonResponse<List<Document>> getDocuments(@Header("zoomle_key") String privateKey, @Query("time") int time);

    @FormUrlEncoded
    @POST("/documents/")
    CommonResponse<Document> postDocument(@Header("zoomle_key") String privateKey, @Field("name") String name,
                                          @Field("person_id") String personId, @Field("category_id") int categoryId,
                                          @Field("color_id") int colorId, @Field("type") int type,
                                          @Field("notes") String notes, @Field("fields") String jsonFields, @Field("tags") String jsonTags);

    @PUT("/documents/")
    CommonResponse<Document> putDocument(@Header("zoomle_key") String privateKey, @Query("id") int id,
                                         @Query("name") String name,
                                         @Query("person_id") String personId, @Query("category_id") int categoryId,
                                         @Query("color_id") int colorId, @Query("type") int type,
                                         @Query("notes") String notes, @Query("fields") String jsonFields, @Query("tags") String jsonTags);

    @DELETE("/documents/")
    CommonResponse<Object> deleteDocument(@Header("zoomle_key") String privateKey, @Query("id") int id);
}
