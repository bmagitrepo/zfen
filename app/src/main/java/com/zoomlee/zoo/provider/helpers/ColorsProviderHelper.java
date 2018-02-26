package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class ColorsProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + ColorsContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    ColorsContract.NAME + TYPE_TEXT + COMMA_SEP +
                    ColorsContract.HEX + TYPE_TEXT + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "colors", ROUTE_COLORS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "colors/*", ROUTE_COLORS_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_COLORS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_COLORS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return ColorsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return ColorsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return ColorsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return ColorsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return ColorsContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }


    /**
     * Columns supported by "colors" records.
     */
    public static class ColorsContract implements DataColumns {

        /**
         * Path component for "color"-type resources..
         */
        private static final String PATH = "colors";
        /**
         * MIME type for lists of colors.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.colors";
        /**
         * MIME type for individual color.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.color";

        /**
         * Fully qualified URI for "color" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "color" resources.
         */
        public static final String TABLE_NAME = "colors";

        /**
         * Color's name
         */
        public static final String NAME = "name";
        /**
         * Color's hex
         */
        public static final String HEX = "hex";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, NAME, HEX
        };
    }

}
