package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class DocumentsType2Field extends BaseItem {

    @SerializedName("document_type_id")
    private int documentTypeId;
    @SerializedName("field_id")
    private int fieldTypeId;
    @SerializedName("weight")
    private int weight;
    private transient int fieldTypeValue;
    private transient String fieldTypeName;
    private transient int reminder;
    private transient int suggest;

    public int getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(int documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public int getFieldTypeId() {
        return fieldTypeId;
    }

    public void setFieldTypeId(int fieldTypeId) {
        this.fieldTypeId = fieldTypeId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setFieldTypeValue(int fieldTypeValue) {
        this.fieldTypeValue = fieldTypeValue;
    }

    public int getFieldTypeValue() {
        return fieldTypeValue;
    }

    public void setFieldTypeName(String fieldTypeName) {
        this.fieldTypeName = fieldTypeName;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }

    public int getReminder() {
        return reminder;
    }

    public void setReminder(int reminder) {
        this.reminder = reminder;
    }

    public int getSuggest() {
        return suggest;
    }

    public void setSuggest(int suggest) {
        this.suggest = suggest;
    }
}
