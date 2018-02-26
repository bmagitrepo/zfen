package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_TAGS2DOCUMENTS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_TAGS2DOCUMENTS_ID;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 06.04.15.
 */
public class Tags2DocumentsProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + Tags2DocumentsContract.TABLE_NAME + " (" +
                    DataColumns._ID + " INTEGER PRIMARY KEY," +
                    Tags2DocumentsContract.TAG_ID + TYPE_INTEGER + COMMA_SEP +
                    Tags2DocumentsContract.DOCUMENT_ID + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "tags2documents", ROUTE_TAGS2DOCUMENTS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "tags2documents/*", ROUTE_TAGS2DOCUMENTS_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_TAGS2DOCUMENTS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_TAGS2DOCUMENTS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return Tags2DocumentsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return Tags2DocumentsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return Tags2DocumentsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return Tags2DocumentsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return Tags2DocumentsContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Columns supported by "tags2documents" records.
     */
    public static class Tags2DocumentsContract implements BaseColumns {

        /**
         * Path component for "tags2document"-type resources..
         */
        private static final String PATH = "tags2documents";
        /**
         * MIME type for lists of tags2documents.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.tags2documents";
        /**
         * MIME type for individual tags2document.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.tags2document";

        /**
         * Fully qualified URI for "tags2document" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "tags2document" resources.
         */
        public static final String TABLE_NAME = "tags2documents";

        public static final String TAG_ID = "tag_id";
        public static final String DOCUMENT_ID = "document_id";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, TAG_ID, DOCUMENT_ID
        };
    }
}
