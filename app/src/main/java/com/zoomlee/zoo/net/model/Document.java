package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class Document extends NamedItem implements Parcelable {

    @SerializedName("person_id")
    private int remotePersonId = -1;
    private transient int localPersonId = -1;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("category_id")
    private int categoryId;
    @SerializedName("color_id")
    private int colorId;
    @SerializedName("type")
    private int typeId;
    @SerializedName("notes")
    private String notes;
    @SerializedName("create_time")
    private int createTime;
    @SerializedName("fields")
    private List<Field> fieldsList = new ArrayList<>();
    @SerializedName("files")
    private List<File> filesList = new ArrayList<>();
    @SerializedName("tags")
    private List<Tag> tagsList = new ArrayList<>();
    private transient String categoryName;
    private transient int categoryWeight;
    private transient String colorName;
    private transient String colorHEX;
    private transient String typeName;
    private transient int groupId;
    private transient String groupName;
    private transient List<Field> visibleFields = new ArrayList<>();

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public List<File> getFilesList() {
        return filesList;
    }

    public void setFilesList(List<File> filesList) {
        this.filesList = filesList;
    }

    public List<Field> getFieldsList() {
        return fieldsList;
    }

    public void setFieldsList(List<Field> fieldsList) {
        this.fieldsList = fieldsList;
        this.visibleFields = new ArrayList<>();
    }

    public List<Tag> getTagsList() {
        return tagsList;
    }

    public void setTagsList(List<Tag> tagsList) {
        this.tagsList = tagsList;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryWeight() {
        return categoryWeight;
    }

    public void setCategoryWeight(int categoryWeight) {
        this.categoryWeight = categoryWeight;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorHEX() {
        return colorHEX;
    }

    public void setColorHEX(String colorHEX) {
        this.colorHEX = colorHEX;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean haveTag(int localTagId){
        for(Tag tag : tagsList)
            if(tag.getId() == localTagId)
                return true;
        return false;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id+ '\'' +
                ",remoteId='" + remoteId + '\'' +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", localPersonId=" + localPersonId +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", colorId=" + colorId +
                ", typeId=" + typeId +
                ", notes='" + notes + '\'' +
                ", createTime=" + createTime +
                ", fieldsList=" + fieldsList +
                ", filesList=" + filesList +
                ", tagsList=" + tagsList +
                ", categoryName='" + categoryName + '\'' +
                ", categoryWeight=" + categoryWeight +
                ", colorName='" + colorName + '\'' +
                ", colorHEX='" + colorHEX + '\'' +
                ", typeName='" + typeName + '\'' +
                ", groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        if (remoteId != -1 && document.remoteId != -1)
            return remoteId == document.remoteId;

        return id == document.id;
    }

    @Override
    public int hashCode() {
        return 31 * remoteId;
    }

    public int getRemotePersonId() {
        return remotePersonId;
    }

    public void setRemotePersonId(int remotePersonId) {
        this.remotePersonId = remotePersonId;
    }

    public int getLocalPersonId() {
        return localPersonId;
    }

    public void setLocalPersonId(int localPersonId) {
        this.localPersonId = localPersonId;
    }

    public List<Field> getVisibleFields() {
        if (this.visibleFields.size() == 0) {
            List<Field> visibleFields = new ArrayList<>();
            for (Field field: fieldsList) {
                if (!TextUtils.isEmpty(field.getValue()))
                    visibleFields.add(field);
            }
            this.visibleFields = visibleFields;
        }
        return this.visibleFields;
    }

    public boolean isWorkPermit(){
        return typeId == 51;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.remotePersonId);
        dest.writeInt(this.localPersonId);
        dest.writeInt(this.userId);
        dest.writeInt(this.categoryId);
        dest.writeInt(this.colorId);
        dest.writeInt(this.typeId);
        dest.writeString(this.notes);
        dest.writeInt(this.createTime);
        dest.writeTypedList(fieldsList);
        dest.writeTypedList(filesList);
        dest.writeTypedList(tagsList);
        dest.writeString(this.categoryName);
        dest.writeInt(this.categoryWeight);
        dest.writeString(this.colorName);
        dest.writeString(this.colorHEX);
        dest.writeString(this.typeName);
        dest.writeInt(this.groupId);
        dest.writeString(this.groupName);
        dest.writeString(this.name);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.status);
        dest.writeInt(this.updateTime);
    }

    public Document() {
    }

    private Document(Parcel in) {
        this.remotePersonId = in.readInt();
        this.localPersonId = in.readInt();
        this.userId = in.readInt();
        this.categoryId = in.readInt();
        this.colorId = in.readInt();
        this.typeId = in.readInt();
        this.notes = in.readString();
        this.createTime = in.readInt();
        in.readTypedList(fieldsList, Field.CREATOR);
        in.readTypedList(filesList, File.CREATOR);
        in.readTypedList(tagsList, Tag.CREATOR);
        this.categoryName = in.readString();
        this.categoryWeight = in.readInt();
        this.colorName = in.readString();
        this.colorHEX = in.readString();
        this.typeName = in.readString();
        this.groupId = in.readInt();
        this.groupName = in.readString();
        this.name = in.readString();
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.status = in.readInt();
        this.updateTime = in.readInt();
    }

    public static final Parcelable.Creator<Document> CREATOR = new Parcelable.Creator<Document>() {
        public Document createFromParcel(Parcel source) {
            return new Document(source);
        }

        public Document[] newArray(int size) {
            return new Document[size];
        }
    };
}
