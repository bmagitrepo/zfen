package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class FieldsType extends NamedItem {

    public static final int SUGGEST = 1;
    public static final int NOT_SUGGEST = 0;

    // field types id "first name", "place of issue" etc
    public static final int FIRST_NAME_TYPE_ID = 1;
    public static final int LAST_NAME_TYPE_ID = 2;
    public static final int DATE_OF_BIRTH_TYPE_ID = 13;
    public static final int PASSPORT_NUMBER_TYPE_ID = 31;
    public static final int COUNTRY_OF_ISSUE_TYPE_ID = 35;
    public static final int CASE_NUMBER_TYPE_ID = 42;
    public static final int RECEIPT_NUMBER_TYPE_ID = 44;

    // field value's types "text", "date"
    public static final int TEXT_VALUE_TYPE = 1;
    public static final int DATE_VALUE_TYPE = 2;

    @SerializedName("type")
    private int type;
    @SerializedName("reminder")
    protected int reminder = -1;
    @SerializedName("suggest")
    protected int suggest;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
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
     * @return {@linkplain #SUGGEST} or {@linkplain #NOT_SUGGEST}
     */
    public int getSuggest() {
        return suggest;
    }

    /**
     *
     * @param suggest - {@linkplain #SUGGEST} or {@linkplain #NOT_SUGGEST}
     */
    public void setSuggest(int suggest) {
        this.suggest = suggest;
    }
}
