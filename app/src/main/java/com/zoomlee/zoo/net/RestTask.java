package com.zoomlee.zoo.net;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 26.01.15.
 */
public class RestTask {

    private int id = -1;
    private int localItemId;
    private int type;

    public RestTask(){}

    public RestTask(int type){
        this.type = type;
    }

    public RestTask(int localItemId, int type) {
        this.localItemId = localItemId;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocalItemId() {
        return localItemId;
    }

    public void setLocalItemId(int localItemId) {
        this.localItemId = localItemId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestTask restTask = (RestTask) o;

        if (localItemId != restTask.localItemId) return false;
        if (type != restTask.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = localItemId;
        result = 31 * result + type;
        return result;
    }

    public static final class Types {
        public static final int USER_PUT = 1;

        public static final int PERSON_DELETE = 2;
        public static final int DOCUMENTS_DELETE = 3;
        public static final int TAG_DELETE = 4;
        public static final int FILES_DELETE = 5;
        public static final int TAX_DELETE = 6;

        public static final int PERSON_UPLOAD = 7;
        public static final int DOCUMENTS_UPLOAD = 8;
        public static final int FILES_POST = 9;
        public static final int TAG_PUT = 10;
        public static final int TAX_UPLOAD = 11;
        public static final int FORM_POST = 12;

        public static final int USER_GET = 13;
        public static final int USER_GET_ICON = 14;
        public static final int USER_DATA_GET = 15;

        public static final int PERSON_GET_ICON = 16;
        public static final int FILES_GET = 17;
        public static final int STATIC_DATA_GET = 18;
    }
}