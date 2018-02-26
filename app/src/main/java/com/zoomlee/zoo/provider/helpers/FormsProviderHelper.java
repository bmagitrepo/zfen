package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FORMS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FORMS_ID;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 27.05.15.
 */
public class FormsProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + FormsContract.TABLE_NAME + " (" +
                    DataColumns._ID + " INTEGER PRIMARY KEY," +
                    DataColumns.REMOTE_ID + TYPE_INTEGER + COMMA_SEP +
                    DataColumns.UPDATE_TIME + TYPE_INTEGER + COMMA_SEP +
                    FormsContract.USER_ID + TYPE_INTEGER + COMMA_SEP +
                    FormsContract.PERSON_ID + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "forms", ROUTE_FORMS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "forms/*", ROUTE_FORMS_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_FORMS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_FORMS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return FormsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return FormsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return FormsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return FormsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return FormsContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Columns supported by "form" records.
     */
    public static class FormsContract implements DataColumns {

        /**
         * Path component for "form"-type resources..
         */
        private static final String PATH = "forms";
        /**
         * MIME type for lists of forms.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.forms";
        /**
         * MIME type for individual form.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.form";

        /**
         * Fully qualified URI for "form" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "form" resources.
         */
        public static final String TABLE_NAME = "forms";

        /**
         * File type's name
         */
        public static final String USER_ID = "user_id";
        public static final String PERSON_ID = "person_ID";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, PERSON_ID, USER_ID
        };
    }
}
