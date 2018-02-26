package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.zoomlee.zoo.utils.TimeUtil;

import java.util.Calendar;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class Field extends NamedItem implements Parcelable {

    public static final int DATE_TYPE = 2;
    public static final int TEXT_TYPE = 1;

    public static final int CUSTOM_DATE_FIELD_ID = 96;
    public static final int CUSTOM_TEXT_FIELD_ID = 95;

    public static final String CUSTOM_FIELD_NAME = "Other";

    @SerializedName("document_id")
    private int remoteDocumentId;
    @SerializedName("field_id")
    private int fieldTypeId;
    @SerializedName("value")
    private String value;
    @SerializedName("notify_on")
    private String notifyOn;
    @SerializedName("create_time")
    private int createTime;

    private transient int weight;
    private transient int type;
    private transient int localDocumentId;
    private transient int reminder = -1;
    private transient int suggest;

    public Field() {
    }

    public Field(DocumentsType2Field dt2f) {
        fieldTypeId = dt2f.getFieldTypeId();
        name = dt2f.getFieldTypeName();
        type = dt2f.getFieldTypeValue();
        weight = dt2f.getWeight();
        reminder = dt2f.getReminder();
        suggest = dt2f.getSuggest();

        setDefaultValue();
    }

    private void setDefaultValue() {
        if (type == DATE_TYPE) {
            Calendar currentCalendar = TimeUtil.getCalendarForCurrentTime();
            value = String.valueOf(currentCalendar.getTimeInMillis() / 1000L);
            if (reminder != -1) {
                Calendar remindOn = (Calendar) currentCalendar.clone();
                remindOn.add(Calendar.DAY_OF_MONTH, 0-reminder);
                notifyOn = String.valueOf(remindOn.getTimeInMillis() / 1000L);
            }
        }
    }

    public static Field createCustomField(int type) {
        Field field = new Field();
        field.name = CUSTOM_FIELD_NAME;
        field.value = "";
        switch (type) {
            case TEXT_TYPE:
                field.fieldTypeId = CUSTOM_TEXT_FIELD_ID;
                break;
            case DATE_TYPE:
                field.fieldTypeId = CUSTOM_DATE_FIELD_ID;
                break;
        }
        field.type = type;
        field.createTime = (int) (System.currentTimeMillis() / 1000);
        return field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        return this.fieldTypeId == field.fieldTypeId;
    }

    @Override
    public int hashCode() {
        return fieldTypeId * 31;
    }

    public boolean isCustom() {
        return fieldTypeId == CUSTOM_DATE_FIELD_ID || fieldTypeId == CUSTOM_TEXT_FIELD_ID;
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

    public String getFormattedValue() {
        if (type != FieldsType.DATE_VALUE_TYPE) return value;

        try {
            Calendar calendar = TimeUtil.getCalendarForServerTime(Long.parseLong(value));
            return TimeUtil.formatDateUTC(calendar.getTime());
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNotifyOn() {
        return notifyOn;
    }

    /**
     * @return server timestamp of notification
     */
    public long getLongNotifyOn() {
        if (notifyOn == null) return -1;

        try {
            return Long.parseLong(notifyOn);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void setNotifyOn(String notifyOn) {
        this.notifyOn = notifyOn;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getRemoteDocumentId() {
        return remoteDocumentId;
    }

    public void setRemoteDocumentId(int remoteDocumentId) {
        this.remoteDocumentId = remoteDocumentId;
    }

    public int getLocalDocumentId() {
        return localDocumentId;
    }

    public void setLocalDocumentId(int localDocumentId) {
        this.localDocumentId = localDocumentId;
    }

    /**
     *
     * @return default reminder in days
     */
    public int getReminder() {
        return reminder;
    }

    /**
     * set default reminder in days
     * @param reminder
     */
    public void setReminder(int reminder) {
        this.reminder = reminder;
    }

    /**
     *
     * @return {@linkplain FieldsType#SUGGEST} or {@linkplain FieldsType#NOT_SUGGEST}
     */
    public int getSuggest() {
        return suggest;
    }

    /**
     *
     * @param suggest - {@linkplain FieldsType#SUGGEST} or {@linkplain FieldsType#NOT_SUGGEST}
     */
    public void setSuggest(int suggest) {
        this.suggest = suggest;
    }

    @Override
    public String toString() {
        return "Field{" +
                "id=" + id +
                ", name=" + name +
                ", remoteDocumentId=" + remoteDocumentId +
                ", localDocumentId=" + localDocumentId +
                ", fieldTypeId=" + fieldTypeId +
                ", value='" + value + '\'' +
                ", notifyOn='" + notifyOn + '\'' +
                ", weight=" + weight +
                ", type=" + type +
                ", createTime=" + createTime +
                ", reminder=" + reminder +
                ", suggest=" + suggest +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.remoteDocumentId);
        dest.writeInt(this.localDocumentId);
        dest.writeInt(this.fieldTypeId);
        dest.writeString(this.value);
        dest.writeString(this.notifyOn);
        dest.writeInt(this.weight);
        dest.writeInt(this.type);
        dest.writeString(this.name);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.status);
        dest.writeInt(this.updateTime);
        dest.writeInt(this.createTime);
        dest.writeInt(this.reminder);
        dest.writeInt(this.suggest);
    }

    private Field(Parcel in) {
        this.remoteDocumentId = in.readInt();
        this.localDocumentId = in.readInt();
        this.fieldTypeId = in.readInt();
        this.value = in.readString();
        this.notifyOn = in.readString();
        this.weight = in.readInt();
        this.type = in.readInt();
        this.name = in.readString();
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.status = in.readInt();
        this.updateTime = in.readInt();
        this.createTime = in.readInt();
        this.reminder = in.readInt();
        this.suggest = in.readInt();
    }

    public static final Parcelable.Creator<Field> CREATOR = new Parcelable.Creator<Field>() {
        public Field createFromParcel(Parcel source) {
            return new Field(source);
        }

        public Field[] newArray(int size) {
            return new Field[size];
        }
    };
}
