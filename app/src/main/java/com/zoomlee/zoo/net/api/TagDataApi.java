package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.Tag;

import java.util.List;

import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.PUT;
import retrofit.http.Query;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 09.04.15.
 */
public interface TagDataApi {

    @GET("/tags/")
    CommonResponse<List<Tag>> getTags(@Header("zoomle_key") String privateKey);

    @PUT("/tags/")
    CommonResponse<Tag> putTag(@Header("zoomle_key") String privateKey, @Query("id") int id,
                                     @Query("name") String name);

    @DELETE("/tags/")
    CommonResponse<Object> deleteTag(@Header("zoomle_key") String privateKey, @Query("id") int id);
}
