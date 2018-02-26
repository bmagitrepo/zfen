package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class Category2DocumentsTypesProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + Category2DocumentsTypesContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    Category2DocumentsTypesContract.CATEGORY_ID + TYPE_INTEGER + COMMA_SEP +
                    Category2DocumentsTypesContract.DOCUMENTS_TYPE_ID + TYPE_INTEGER + ")";


    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "category2documents_types", ROUTE_CATEGORY2DOCUMENTS_TYPES);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "category2documents_types/*", ROUTE_CATEGORY2DOCUMENTS_TYPES_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_CATEGORY2DOCUMENTS_TYPES;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_CATEGORY2DOCUMENTS_TYPES_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return Category2DocumentsTypesContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return Category2DocumentsTypesContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return Category2DocumentsTypesContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return Category2DocumentsTypesContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return Category2DocumentsTypesContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }


    /**
     * Columns supported by "category to document type" records.
     */
    public static class Category2DocumentsTypesContract implements DataColumns {

        /**
         * Path component for "category2documents_type"-type resources..
         */
        private static final String PATH = "category2documents_types";
        /**
         * MIME type for lists of category2documents_types.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.category2documents_types";
        /**
         * MIME type for individual category2documents_type.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.category2documents_type";

        /**
         * Fully qualified URI for "category2documents_type" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "category2documents_type" resources.
         */
        public static final String TABLE_NAME = "category2documents_types";

        /**
         * Category's id
         */
        public static final String CATEGORY_ID = "category_id";

        /**
         * Document type's id
         */
        public static final String DOCUMENTS_TYPE_ID = "documents_type_id";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, CATEGORY_ID, DOCUMENTS_TYPE_ID
        };
    }

}
