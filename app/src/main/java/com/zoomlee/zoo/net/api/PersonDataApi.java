package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.Person;

import java.util.List;

import retrofit.http.DELETE;
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
public interface PersonDataApi {

    @GET("/persons/")
    CommonResponse<List<Person>> getPersons(@Header("zoomle_key") String privateKey, @Query("time") int time);

    @Multipart
    @POST("/persons/")
    CommonResponse<Person> postPerson(@Header("zoomle_key") String privateKey,
                                      @Part("name") String name, @Part("image") TypedFile typedFile);
    
    @Multipart
    @PUT("/persons/")
    CommonResponse<Person> putPerson(@Header("zoomle_key") String privateKey, @Query("id") int id,
                                     @Query("name") String name, @Part("image") TypedFile typedFile);

    @PUT("/persons/")
    CommonResponse<Person> putPerson(@Header("zoomle_key") String privateKey, @Query("id") int id,
                                     @Query("name") String name);

    @DELETE("/persons/")
    CommonResponse<Object> deletePerson(@Header("zoomle_key") String privateKey, @Query("id") int id);
}
