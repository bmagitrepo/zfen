package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;

import com.zoomlee.zoo.R;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class Category extends NamedItem implements Parcelable {
    public static final Category OTHER_CATEGORY;
    public static final Category TRAVEL_CATEGORY;

    static {
        TRAVEL_CATEGORY = new Category();
        TRAVEL_CATEGORY.setRemoteId(3);
        TRAVEL_CATEGORY.setName("Travel");
        OTHER_CATEGORY = new Category();
        OTHER_CATEGORY.setRemoteId(10);
        OTHER_CATEGORY.setName("Other");
    }

    @SerializedName("weight")
    private int weight;
    private transient boolean avoidWeightSaving;
    private transient List<DocumentsType> documentsTypeList = new ArrayList<>();

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    private static SparseIntArray iconResMap = new SparseIntArray();

    static {
        iconResMap.append(1, R.drawable.category_icon_personal);
        iconResMap.append(2, R.drawable.category_icon_cards);
        iconResMap.append(3, R.drawable.category_icon_travel);
        iconResMap.append(4, R.drawable.category_icon_health);
        iconResMap.append(5, R.drawable.category_icon_education);
        iconResMap.append(6, R.drawable.category_icon_work);
        iconResMap.append(7, R.drawable.category_icon_vehicle);
        iconResMap.append(8, R.drawable.category_icon_home);
        iconResMap.append(9, R.drawable.category_icon_finance);
        iconResMap.append(10, R.drawable.category_icon_other);
    }

    public static int getIconRes(int categoryRemoteId) {
        return iconResMap.get(categoryRemoteId, R.drawable.category_icon_other);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.weight);
        dest.writeString(this.name);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.status);
        dest.writeInt(this.updateTime);
    }

    public Category() {
    }

    private Category(Parcel in) {
        this.weight = in.readInt();
        this.name = in.readString();
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.status = in.readInt();
        this.updateTime = in.readInt();
    }

    public List<DocumentsType> getDocumentsTypeList() {
        return documentsTypeList;
    }

    public void addDocumentsType(DocumentsType documentsType) {
        this.documentsTypeList.add(documentsType);
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    /**
     * return true - save/update 'weight' field, false - avoid save/update of 'weight' field
     * @see com.zoomlee.zoo.dao.CategoryDaoHelper#saveLocalChanges(android.content.Context, Category)
     */
    public boolean isAvoidWeightSaving() {
        return avoidWeightSaving;
    }

    /**
     * set true to save/update 'weight' field.</br>
     * set false to prevent save/update 'weight' field
     * @param avoidWeightSaving
     * @see com.zoomlee.zoo.dao.CategoryDaoHelper#saveLocalChanges(android.content.Context, Category)
     */
    public void setAvoidWeightSaving(boolean avoidWeightSaving) {
        this.avoidWeightSaving = avoidWeightSaving;
    }
}
