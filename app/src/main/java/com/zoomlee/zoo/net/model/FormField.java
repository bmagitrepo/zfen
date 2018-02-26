package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 26.05.15.
 */
public class FormField extends BaseItem implements Parcelable {

    // field types id "Family Name", "Passport issued by (country)" etc
    public static final int FAMILY_NAME_TYPE_ID = 1;
    public static final int FIRST_NAME_TYPE_ID = 2;
    public static final int BIRTH_DATE_MOUNTH1_TYPE_ID = 4;
    public static final int BIRTH_DATE_MOUNTH2_TYPE_ID = 5;
    public static final int BIRTH_DATE_DAY1_TYPE_ID = 6;
    public static final int BIRTH_DATE_DAY2_TYPE_ID = 7;
    public static final int BIRTH_DATE_YEAR1_TYPE_ID = 8;
    public static final int BIRTH_DATE_YEAR2_TYPE_ID = 9;
    public static final int PASSPORT_ISSUED_BY_TYPE_ID = 14;
    public static final int PASSPORT_NUMBER_TYPE_ID = 15;
    public static final int COUNTRY_OF_RESIDENCE_TYPE_ID = 16;

    public static final int FIRST_ARTICLE_ID = 37;
    public static final int LAST_ARTICLE_ID = 54;

    @SerializedName("field_id")
    protected int fieldTypeId;
    @SerializedName("value")
    protected String value;

    private transient int localFormId;


    /**
     * no status field
     */
    @Deprecated
    @Override
    public int getStatus() {
        return super.getStatus();
    }

    /**
     * no status field
     */
    @Deprecated
    @Override
    public void setStatus(int status) {
        super.setStatus(status);
    }

    /**
     * no updateTime field
     */
    @Deprecated
    @Override
    public int getUpdateTime() {
        return super.getUpdateTime();
    }

    /**
     * no updateTime field
     */
    @Deprecated
    @Override
    public void setUpdateTime(int updateTime) {
        super.setUpdateTime(updateTime);
    }

    public int getFieldTypeId() {
        return fieldTypeId;
    }

    public void setFieldTypeId(int fieldTypeId) {
        this.fieldTypeId = fieldTypeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get local form's id
     * @return local form's id
     */
    public int getLocalFormId() {
        return localFormId;
    }

    /**
     * set local form's id
     * @param localFormId - local form's id
     */
    public void setLocalFormId(int localFormId) {
        this.localFormId = localFormId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.fieldTypeId);
        dest.writeString(this.value);
        dest.writeInt(this.localFormId);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
    }

    public FormField() {
    }

    private FormField(Parcel in) {
        this.fieldTypeId = in.readInt();
        this.value = in.readString();
        this.localFormId = in.readInt();
        this.id = in.readInt();
        this.remoteId = in.readInt();
    }

    public static final Parcelable.Creator<FormField> CREATOR = new Parcelable.Creator<FormField>() {
        public FormField createFromParcel(Parcel source) {
            return new FormField(source);
        }

        public FormField[] newArray(int size) {
            return new FormField[size];
        }
    };
}
