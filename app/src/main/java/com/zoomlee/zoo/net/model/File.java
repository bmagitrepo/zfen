package com.zoomlee.zoo.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.zoomlee.zoo.R;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class File extends BaseItem implements Parcelable {

    @SerializedName("document_id")
    private int remoteDocumentId = -1;
    private transient int localDocumentId = -1;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("type_id")
    private int typeId;
    @SerializedName("file")
    private String remotePath;
    @SerializedName("create_time")
    private long createTime;
    private transient String localPath;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        File file = (File) o;

        if (remoteId != -1 && file.remoteId != -1)
            return remoteId == file.remoteId;

        return id == file.id;
    }

    /**
     * @return true is this file can be opened on the device
     */
    public boolean isOpenable() {
        return Type.OPENABLE.contains(getType());
    }

    /**
     * @return list of apps that can open this file, and are available to be installed
     */
    public List<App> appsThatOpen() {
        Type type = getType();
        if (type != null) {
            return Collections.singletonList(type.app);
        }
        return null;
    }

    public Type getType() {
        return Type.byPath(getPath());
    }

    /**
     * @return local path and if not set - remote
     */
    public String getPath() {
        String path = getLocalPath();
        if (path == null) {
            path = getRemotePath();
        }
        return path;
    }

    public String getName() {
        return new java.io.File(getPath()).getName();
    }

    @Override
    public int hashCode() {
        return 31 * remoteId;
    }

    @Override
    public String toString() {
        return super.toString() + "File{" +
                "remoteDocumentId=" + remoteDocumentId +
                ", localDocumentId=" + localDocumentId +
                ", userId=" + userId +
                ", typeId=" + typeId +
                ", remotePath='" + remotePath + '\'' +
                ", createTime=" + createTime +
                ", localPath='" + localPath + '\'' +
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
        dest.writeInt(this.userId);
        dest.writeInt(this.typeId);
        dest.writeString(this.remotePath);
        dest.writeLong(this.createTime);
        dest.writeString(this.localPath);
        dest.writeInt(this.id);
        dest.writeInt(this.remoteId);
        dest.writeInt(this.status);
        dest.writeInt(this.updateTime);
    }

    public File() {
    }

    private File(Parcel in) {
        this.remoteDocumentId = in.readInt();
        this.localDocumentId = in.readInt();
        this.userId = in.readInt();
        this.typeId = in.readInt();
        this.remotePath = in.readString();
        this.createTime = in.readLong();
        this.localPath = in.readString();
        this.id = in.readInt();
        this.remoteId = in.readInt();
        this.status = in.readInt();
        this.updateTime = in.readInt();
    }

    public static final Parcelable.Creator<File> CREATOR = new Parcelable.Creator<File>() {
        public File createFromParcel(Parcel source) {
            return new File(source);
        }

        public File[] newArray(int size) {
            return new File[size];
        }
    };

    public enum Type {

        DOC(".doc", "application/msword", null, FilesType.OTHER_TYPE, App.MS_OFFICE),
        DOCX(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", null, FilesType.OTHER_TYPE, App.MS_OFFICE),
        XLS(".xls", "application/vnd.ms-excel", null, FilesType.OTHER_TYPE, App.MS_OFFICE),
        XLSX(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", null, FilesType.OTHER_TYPE, App.MS_OFFICE),
        PPT(".ppt", "application/vnd.ms-powerpoint", null, FilesType.OTHER_TYPE, App.MS_OFFICE),
        PPTX(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", null, FilesType.OTHER_TYPE, App.MS_OFFICE),
        PDF(".pdf", "application/pdf", null, FilesType.OTHER_TYPE, null),
        /*RTF(".rtf", "application/rtf", null, FilesType.OTHER_TYPE),*/
        IMAGE(".png", "image/png", "image/.*", FilesType.IMAGE_TYPE, null);

        private static final Set<Type> OPENABLE = new HashSet<>();
        static {
            OPENABLE.add(IMAGE);
            OPENABLE.add(PDF);
        }

        public final String extension;
        public final String mimeType;
        public final int filesType;
        public final App app;
        private final String mimePattern;

        Type(String extension, String mimeType, String mimePattern, int filesType, App app) {
            this.extension = extension;
            this.mimeType = mimeType;
            this.filesType = filesType;
            this.app = app;
            if (mimePattern == null) {
                this.mimePattern = mimeType;
            } else {
                this.mimePattern = mimePattern;
            }
        }

        public static Type byPath(String path) {
            for (Type type : values()) {
                if (path.endsWith(type.extension)) {
                    return type;
                }
            }
            return null;
        }

        public static Type byMimeType(String mimeType) {
            for (Type type : values()) {
                if (mimeType.matches(type.mimePattern)) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum App {

        MS_OFFICE("com.microsoft.office.officehub", R.string.title_ms_office, R.drawable.office);

        private final String appId;
        private final int titleResId;
        private final int iconResId;

        App(String appId, int titleResId, int iconResId) {
            this.appId = appId;
            this.titleResId = titleResId;
            this.iconResId = iconResId;
        }

        public String getAppId() {
            return appId;
        }

        public int getIconResId() {
            return iconResId;
        }

        public int getTitleResId() {
            return titleResId;
        }
    }
}
