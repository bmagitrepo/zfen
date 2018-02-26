package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class DocumentsTypesHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + DocumentsTypesContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    DocumentsTypesContract.NAME + TYPE_TEXT + COMMA_SEP +
                    DocumentsTypesContract.GROUP_TYPE + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "documents_types", ROUTE_DOCUMENTS_TYPES);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "documents_types/*", ROUTE_DOCUMENTS_TYPES_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_DOCUMENTS_TYPES;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_DOCUMENTS_TYPES_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return DocumentsTypesContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return DocumentsTypesContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return DocumentsTypesContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return DocumentsTypesContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return DocumentsTypesContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }


    /**
     * Columns supported by "document type" records.
     */
    public static class DocumentsTypesContract implements DataColumns {

        /**
         * Path component for "documents_type"-type resources..
         */
        private static final String PATH = "documents_types";
        /**
         * MIME type for lists of documents_types.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.documents_types";
        /**
         * MIME type for individual documents_type.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.documents_type";

        /**
         * Fully qualified URI for "documents_type" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "documents_type" resources.
         */
        public static final String TABLE_NAME = "documents_types";

        /**
         * documents type name
         */
        public static final String NAME = "name";

        /**
         * Documents group id
         */
        public static final String GROUP_TYPE = "group_id";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, NAME, GROUP_TYPE
        };
    }
}
