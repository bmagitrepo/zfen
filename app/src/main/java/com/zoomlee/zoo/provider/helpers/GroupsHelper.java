package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class GroupsHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + GroupsContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    GroupsContract.NAME + TYPE_TEXT + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "documents_types_groups", ROUTE_DOCUMENTS_TYPES_GROUPS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "documents_types_groups/*", ROUTE_DOCUMENTS_TYPES_GROUPS_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_DOCUMENTS_TYPES_GROUPS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_DOCUMENTS_TYPES_GROUPS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return GroupsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return GroupsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return GroupsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return GroupsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return GroupsContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }


    /**
     * Columns supported by "Document types groups" records.
     */
    public static class GroupsContract implements DataColumns {

        /**
         * Path component for "documents_types_group"-type resources..
         */
        private static final String PATH = "documents_types_groups";
        /**
         * MIME type for lists of documents_types_groups.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.documents_types_groups";
        /**
         * MIME type for individual documents_types_group.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.documents_types_group";

        /**
         * Fully qualified URI for "documents_types_group" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "documents_types_group" resources.
         */
        public static final String TABLE_NAME = "documents_types_groups";

        /**
         * group's name
         */
        public static final String NAME = "name";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, NAME
        };
    }

}
