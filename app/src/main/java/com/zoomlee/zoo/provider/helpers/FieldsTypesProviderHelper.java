package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class FieldsTypesProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + FieldsTypesContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    FieldsTypesContract.NAME + TYPE_TEXT + COMMA_SEP +
                    FieldsTypesContract.TYPE + TYPE_TEXT + COMMA_SEP +
                    FieldsTypesContract.SUGGEST + TYPE_INTEGER + COMMA_SEP +
                    FieldsTypesContract.REMINDER + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "field_types", ROUTE_FIELD_TYPES);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "field_types/*", ROUTE_FIELD_TYPES_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_FIELD_TYPES;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_FIELD_TYPES_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return FieldsTypesContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return FieldsTypesContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return FieldsTypesContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return FieldsTypesContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return FieldsTypesContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }


    /**
     * Columns supported by "field type" records.
     */
    public static class FieldsTypesContract implements DataColumns {

        /**
         * Path component for "field_type"-type resources..
         */
        private static final String PATH = "field_types";
        /**
         * MIME type for lists of field_types.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.field_types";
        /**
         * MIME type for individual field_type.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.field_type";

        /**
         * Fully qualified URI for "field_type" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "field_type" resources.
         */
        public static final String TABLE_NAME = "field_types";

        /**
         * Field type's name
         */
        public static final String NAME = "name";

        /**
         * Field type
         */
        public static final String TYPE = "type";
        public static final String SUGGEST = "suggest";
        public static final String REMINDER = "reminder";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, NAME, TYPE, SUGGEST, REMINDER
        };
    }
}
