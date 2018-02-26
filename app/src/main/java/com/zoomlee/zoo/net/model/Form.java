package com.zoomlee.zoo.net.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.zoomlee.zoo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 26.05.15.
 */
public class Form extends BaseItem implements Parcelable {

    @SerializedName("user_id")
    protected int userId;
    @SerializedName("person_id")
    protected int remotePersonId = -1;
    private transient int localPersonId = -1;
    @SerializedName("data")
    protected List<FormField> data = new ArrayList<>();

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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRemotePersonId() {
        return remotePersonId;
    }

    public void setRemotePersonId(int remotePersonId) {
        this.remotePersonId = remotePersonId;
    }

    public List<FormField> getData() {
        return data;
    }

    public void setData(List<FormField> data) {
        this.data = data;
    }

    public int getLocalPersonId() {
        return localPersonId;
    }

    public void setLocalPersonId(int localPersonId) {
        this.localPersonId = localPersonId;
    }

    public String getName() {
        //US form hardcoded
        return "United States Arrival Card";
    }

    public int getColor(Context context) {
        //US form hardcoded
        return context.getResources().getColor(R.color.us_form_color);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeInt(this.remotePersonId);
        dest.writeInt(this.localPersonId);
        dest.writeTypedList(data);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.updateTime);
    }

    public Form() {
    }

    private Form(Parcel in) {
        this.userId = in.readInt();
        this.remotePersonId = in.readInt();
        this.localPersonId = in.readInt();
        in.readTypedList(data, FormField.CREATOR);
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.updateTime = in.readInt();
    }

    public static final Parcelable.Creator<Form> CREATOR = new Parcelable.Creator<Form>() {
        public Form createFromParcel(Parcel source) {
            return new Form(source);
        }

        public Form[] newArray(int size) {
            return new Form[size];
        }
    };
}
