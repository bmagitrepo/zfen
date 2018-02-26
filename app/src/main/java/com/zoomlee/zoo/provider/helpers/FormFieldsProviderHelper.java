package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FORM_FIELDS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FORM_FIELDS_ID;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 27.05.15.
 */
public class FormFieldsProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + FormFieldsContract.TABLE_NAME + " (" +
                    DataColumns._ID + " INTEGER PRIMARY KEY," +
                    DataColumns.REMOTE_ID + TYPE_INTEGER + COMMA_SEP +
                    FormFieldsContract.FIELD_TYPE_ID + TYPE_INTEGER + COMMA_SEP +
                    FormFieldsContract.VALUE + TYPE_TEXT + COMMA_SEP +
                    FormFieldsContract.FORM_ID + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "form_fields", ROUTE_FORM_FIELDS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "form_fields/*", ROUTE_FORM_FIELDS_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_FORM_FIELDS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_FORM_FIELDS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return FormFieldsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return FormFieldsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return FormFieldsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return FormFieldsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return FormFieldsContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Columns supported by "form_field" records.
     */
    public static class FormFieldsContract implements DataColumns {

        /**
         * Path component for "form_field_field"-type resources..
         */
        private static final String PATH = "form_fields";
        /**
         * MIME type for lists of form_fields.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.form_fields";
        /**
         * MIME type for individual form_field.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.form_field";

        /**
         * Fully qualified URI for "form_field" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "form_field" resources.
         */
        public static final String TABLE_NAME = "form_fields";

        /**
         * File type's name
         */
        public static final String FIELD_TYPE_ID = "field_type_id";
        public static final String VALUE = "value";
        public static final String FORM_ID = "form_id";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, FIELD_TYPE_ID, VALUE, FORM_ID
        };
    }
}
