package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class Person extends NamedItem implements Parcelable {
    public static final int ALL_ID = -2;
    public static final int ME_ID = -1;
    public static final Person ALL;

    static {
        ALL = new Person();
        ALL.setId(ALL_ID);
        ALL.setName("All");
    }

    @SerializedName("user_id")
    private int userId;
    @SerializedName("image")
    protected String imageRemotePath;
    protected transient String imageLocalPath;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getImageRemotePath() {
        return imageRemotePath;
    }

    public String getImageRemote144Path() {
        if(imageRemotePath == null)
            return null;
        int extensionIndex = imageRemotePath.lastIndexOf(".");
        StringBuilder builder = new StringBuilder(imageRemotePath);
        builder.replace(extensionIndex, imageRemotePath.length(), "_144x144.png");
        return builder.toString();
    }


    public void setImageRemotePath(String imageRemotePath) {
        this.imageRemotePath = imageRemotePath;
    }

    public String getImageLocalPath() {
        return imageLocalPath;
    }

    public String getImageLocal144Path() {
        if(imageLocalPath == null)
            return null;
        StringBuilder builder = new StringBuilder(imageLocalPath);
        builder.replace(imageLocalPath.length() - 4, imageLocalPath.length(), "_144x144.png");
        return builder.toString();
    }

    public void setImageLocalPath(String imageLocalPath) {
        this.imageLocalPath = imageLocalPath;
    }

    @Override
    public String toString() {
        return "Person{" +
                "remoteId='" + remoteId + '\'' +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", imageRemotePath='" + imageRemotePath + '\'' +
                ", imageLocalPath='" + imageLocalPath + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (remoteId != -1 && person.remoteId != -1)
            return remoteId == person.remoteId;

        return id == person.id;
    }

    @Override
    public int hashCode() {
        return 31 * remoteId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.imageRemotePath);
        dest.writeString(this.imageLocalPath);
        dest.writeString(this.name);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.status);
        dest.writeInt(this.updateTime);
    }

    public Person() {
    }

    private Person(Parcel in) {
        this.userId = in.readInt();
        this.imageRemotePath = in.readString();
        this.imageLocalPath = in.readString();
        this.name = in.readString();
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.status = in.readInt();
        this.updateTime = in.readInt();
    }

    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
}
