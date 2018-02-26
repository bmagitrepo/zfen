package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 07.04.15.
 */
public class Tag extends NamedItem implements Parcelable {

    @SerializedName("user_id")
    protected int userId;

    protected transient int docsCount;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDocsCount() {
        return docsCount;
    }

    public void setDocsCount(int docsCount) {
        this.docsCount = docsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return TextUtils.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return 31 * remoteId;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "userId=" + userId +
                "docsCount=" + docsCount +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.name);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.updateTime);
        dest.writeInt(this.docsCount);
    }

    public Tag() {
    }

    public Tag(Tag tag) {
        this.userId = tag.userId;
        this.name = tag.name;
        this.id = tag.id;
        this.remoteId = tag.remoteId;
        this.updateTime = tag.updateTime;
        this.docsCount = tag.docsCount;
    }

    private Tag(Parcel in) {
        this.userId = in.readInt();
        this.name = in.readString();
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.updateTime = in.readInt();
        this.docsCount = in.readInt();
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        public Tag createFromParcel(Parcel source) {
            return new Tag(source);
        }

        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}
