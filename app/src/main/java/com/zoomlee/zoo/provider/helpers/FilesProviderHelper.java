package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class FilesProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + FilesContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    FilesContract.DOCUMENT_ID + TYPE_INTEGER + COMMA_SEP +
                    FilesContract.USER_ID + TYPE_INTEGER + COMMA_SEP +
                    FilesContract.TYPE_ID + TYPE_INTEGER + COMMA_SEP +
                    FilesContract.REMOTE_PATH + TYPE_TEXT + COMMA_SEP +
                    FilesContract.LOCAL_PATH + TYPE_TEXT + COMMA_SEP +
                    FilesContract.CREATE_TIME + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "files", ROUTE_FILES);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "files/*", ROUTE_FILES_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_FILES;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_FILES_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return FilesContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return FilesContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return FilesContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return FilesContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return FilesContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Columns supported by "file" records.
     */
    public static class FilesContract implements DataColumns {

        /**
         * Path component for "file"-type resources..
         */
        private static final String PATH = "files";
        /**
         * MIME type for lists of files.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.files";
        /**
         * MIME type for individual file.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.file";

        /**
         * Fully qualified URI for "file" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "file" resources.
         */
        public static final String TABLE_NAME = "files";

        /**
         * File type's name
         */
        public static final String DOCUMENT_ID = "document_id";
        public static final String USER_ID = "user_id";
        public static final String TYPE_ID = "type_id";
        public static final String REMOTE_PATH = "remote_path";
        public static final String LOCAL_PATH = "local_path";
        public static final String CREATE_TIME = "create_time";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, DOCUMENT_ID, USER_ID, TYPE_ID, REMOTE_PATH,
                LOCAL_PATH, CREATE_TIME
        };
    }
}
