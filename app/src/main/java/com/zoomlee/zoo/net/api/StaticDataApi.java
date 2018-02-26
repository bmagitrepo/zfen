package com.zoomlee.zoo.net.api;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.model.CategoriesDocumentsType;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.Color;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.net.model.DocumentsType2Field;
import com.zoomlee.zoo.net.model.FieldsType;
import com.zoomlee.zoo.net.model.FilesType;
import com.zoomlee.zoo.net.model.Group;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
public interface StaticDataApi {

    @GET("/changes/")
    CommonResponse<Changes> getChanges(@Query("time") int time);

    @GET("/categories/")
    CommonResponse<List<Category>> getCategories(@Query("time") int time);

    @GET("/categories-documents-types/")
    CommonResponse<List<CategoriesDocumentsType>> getCategoriesDocumentsTypes(@Query("time") int time);

    @GET("/colors/")
    CommonResponse<List<Color>> getColors(@Query("time") int time);

    @GET("/countries/")
    CommonResponse<List<Country>> getCountries(@Query("time") int time);

    @GET("/documents-types/")
    CommonResponse<List<DocumentsType>> getDOcumentsTypes(@Query("time") int time);

    @GET("/documents-types-fields/")
    CommonResponse<List<DocumentsType2Field>> getDocumentsTypes2Fields(@Query("time") int time);

    @GET("/documents-types-groups/")
    CommonResponse<List<Group>> getDocumentsTypes2Groups(@Query("time") int time);

    @GET("/fields/")
    CommonResponse<List<FieldsType>> getFieldsTypes(@Query("time") int time);

    @GET("/files-types/")
    CommonResponse<List<FilesType>> getFilesTypes(@Query("time") int time);
}