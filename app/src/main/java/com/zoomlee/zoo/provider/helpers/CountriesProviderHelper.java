package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;


/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class CountriesProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + CountriesContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    CountriesContract.NAME + TYPE_TEXT + COMMA_SEP +
                    CountriesContract.PRIORITIZE + TYPE_INTEGER + COMMA_SEP +
                    CountriesContract.CODE + TYPE_TEXT + COMMA_SEP +
                    CountriesContract.FLAG + TYPE_TEXT + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "countries", ROUTE_COUNTRIES);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "countries/*", ROUTE_COUNTRIES_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_COUNTRIES;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_COUNTRIES_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return CountriesContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return CountriesContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return CountriesContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return CountriesContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return CountriesContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Columns supported by "countries" records.
     */
    public static class CountriesContract implements DataColumns {

        /**
         * Path component for "country"-type resources..
         */
        private static final String PATH = "countries";
        /**
         * MIME type for lists of countries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.countries";
        /**
         * MIME type for individual country.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.country";

        /**
         * Fully qualified URI for "country" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "country" resources.
         */
        public static final String TABLE_NAME = "countries";

        /**
         * Country's code
         */
        public static final String CODE = "code";
        /**
         * Country's name
         */
        public static final String NAME = "name";
        public static final String PRIORITIZE = "prioritize";
        public static final String FLAG = "flag";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, CODE, NAME, PRIORITIZE, FLAG
        };
    }

}
