package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class DocumentsType extends NamedItem implements Parcelable {
    public static final DocumentsType OTHER_DOC_TYPE;
    public static final DocumentsType PASSPORT_DOC_TYPE;

    static {
        PASSPORT_DOC_TYPE = new DocumentsType();
        PASSPORT_DOC_TYPE.setRemoteId(32);
        PASSPORT_DOC_TYPE.setName("Passport");
        OTHER_DOC_TYPE = new DocumentsType();
        OTHER_DOC_TYPE.setRemoteId(52);
        OTHER_DOC_TYPE.setName("Other");
    }

    @SerializedName("group_id")
    private int groupId;

    public DocumentsType() {
    }

    public DocumentsType(int remoteId, String name) {
        this.remoteId = remoteId;
        this.name = name;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.groupId);
        dest.writeString(this.name);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.status);
        dest.writeInt(this.updateTime);
    }

    private DocumentsType(Parcel in) {
        this.groupId = in.readInt();
        this.name = in.readString();
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.status = in.readInt();
        this.updateTime = in.readInt();
    }

    public static final Parcelable.Creator<DocumentsType> CREATOR = new Parcelable.Creator<DocumentsType>() {
        public DocumentsType createFromParcel(Parcel source) {
            return new DocumentsType(source);
        }

        public DocumentsType[] newArray(int size) {
            return new DocumentsType[size];
        }
    };

}
