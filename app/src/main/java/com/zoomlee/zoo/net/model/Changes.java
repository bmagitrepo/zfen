package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
public class Changes {

    @SerializedName("categories")
    private int categories;
    @SerializedName("categories-documents-types")
    private int categoriesDocumentsTypes;
    @SerializedName("colors")
    private int colors;
    @SerializedName("countries")
    private int countries;
    @SerializedName("documents-types")
    private int documentsTypes;
    @SerializedName("documents-types-fields")
    private int documentsTypesFields;
    @SerializedName("documents-types-groups")
    private int documentsTypesGroups;
    @SerializedName("fields")
    private int fieldsTypes;
    @SerializedName("files-types")
    private int filesTypes;

    public int getLastUpdate() {
        int lastUpdate = categories;
        lastUpdate = lastUpdate > categoriesDocumentsTypes ? lastUpdate : categoriesDocumentsTypes;
        lastUpdate = lastUpdate > colors ? lastUpdate : colors;
        lastUpdate = lastUpdate > countries ? lastUpdate : countries;
        lastUpdate = lastUpdate > documentsTypes ? lastUpdate : documentsTypes;
        lastUpdate = lastUpdate > documentsTypesFields ? lastUpdate : documentsTypesFields;
        lastUpdate = lastUpdate > documentsTypesGroups ? lastUpdate : documentsTypesGroups;
        lastUpdate = lastUpdate > fieldsTypes ? lastUpdate : fieldsTypes;
        lastUpdate = lastUpdate > filesTypes ? lastUpdate : filesTypes;

        return lastUpdate;
    }

    public int getCategories() {
        return categories;
    }

    public void setCategories(int categories) {
        this.categories = categories;
    }

    public int getCategoriesDocumentsTypes() {
        return categoriesDocumentsTypes;
    }

    public void setCategoriesDocumentsTypes(int categoriesDocumentsTypes) {
        this.categoriesDocumentsTypes = categoriesDocumentsTypes;
    }

    public int getColors() {
        return colors;
    }

    public void setColors(int colors) {
        this.colors = colors;
    }

    public int getCountries() {
        return countries;
    }

    public void setCountries(int countries) {
        this.countries = countries;
    }

    public int getDocumentsTypes() {
        return documentsTypes;
    }

    public void setDocumentsTypes(int documentsTypes) {
        this.documentsTypes = documentsTypes;
    }

    public int getDocumentsTypesFields() {
        return documentsTypesFields;
    }

    public void setDocumentsTypesFields(int documentsTypesFields) {
        this.documentsTypesFields = documentsTypesFields;
    }

    public int getDocumentsTypesGroups() {
        return documentsTypesGroups;
    }

    public void setDocumentsTypesGroups(int documentsTypesGroups) {
        this.documentsTypesGroups = documentsTypesGroups;
    }

    public int getFieldsTypes() {
        return fieldsTypes;
    }

    public void setFieldsTypes(int fieldsTypes) {
        this.fieldsTypes = fieldsTypes;
    }

    public int getFilesTypes() {
        return filesTypes;
    }

    public void setFilesTypes(int filesTypes) {
        this.filesTypes = filesTypes;
    }
}
