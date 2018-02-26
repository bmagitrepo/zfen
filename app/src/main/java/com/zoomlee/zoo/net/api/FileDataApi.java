package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.File;

import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Streaming;
import retrofit.mime.TypedFile;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
public interface FileDataApi {

    @Multipart
    @POST("/files/")
    CommonResponse<File> postFile(@Header("zoomle_key") String privateKey, @Part("document_id") int documentId,
                                  @Part("type_id") int typeId, @Part("file") TypedFile typedFile);

    @DELETE("/files/")
    CommonResponse<Object> deleteFile(@Header("zoomle_key") String privateKey, @Query("id") int id);


    @GET("/{file_path}")
    @Streaming
    Response getAttachedFile(@Path("file_path") String filePath, @Header("zoomle_key") String privateKey);
}
