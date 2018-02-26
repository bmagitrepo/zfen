package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class FileTypesProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + FilesTypesContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    FilesTypesContract.NAME + TYPE_TEXT + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "file_types", ROUTE_FILE_TYPES);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "file_types/*", ROUTE_FILE_TYPES_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_FILE_TYPES;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_FILE_TYPES_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return FilesTypesContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return FilesTypesContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return FilesTypesContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return FilesTypesContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return FilesTypesContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Columns supported by "file type" records.
     */
    public static class FilesTypesContract implements DataColumns {

        /**
         * Path component for "file_type"-type resources..
         */
        private static final String PATH = "file_types";
        /**
         * MIME type for lists of file_types.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.file_types";
        /**
         * MIME type for individual file_type.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.file_type";

        /**
         * Fully qualified URI for "file_type" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "file_type" resources.
         */
        public static final String TABLE_NAME = "file_types";

        /**
         * File type's name
         */
        public static final String NAME = "name";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, NAME
        };
    }
}
