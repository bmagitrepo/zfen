package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.zoomlee.zoo.provider.ZoomleeProvider;
import com.zoomlee.zoo.utils.DBUtil;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_TAX;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_TAX_ID;
import static com.zoomlee.zoo.provider.helpers.CountriesProviderHelper.CountriesContract;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 30.04.15.
 */
public class TaxProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TaxContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    TaxContract.USER_ID + TYPE_INTEGER + COMMA_SEP +
                    TaxContract.COUNTRY_ID + TYPE_INTEGER + COMMA_SEP +
                    TaxContract.ARRIVAL + TYPE_INTEGER + COMMA_SEP +
                    TaxContract.DEPARTURE + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "tax", ROUTE_TAX);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "tax/*", ROUTE_TAX_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_TAX;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_TAX_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return TaxContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return TaxContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return TaxContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return TaxContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return TaxContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    @Override
    public Cursor query(Context context, SQLiteDatabase db, Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        int uriMatch = match(uri);
        String where = null;
        switch (uriMatch) {
            case ROUTE_TAX_ID:
                String id = uri.getLastPathSegment();
                where = TaxContract.TABLE_NAME + "." + BaseColumns._ID + "=" + id;
            case ROUTE_TAX:
                if (where == null) where = selection;
                if (projection == null) projection = TaxContract.ALL_COLUMNS_PROJECTION;

                StringBuilder queryString =
                        new StringBuilder("SELECT ").append(DBUtil.formatProjection(projection)).append(" ")
                                .append(TaxContract.FROM_SQL);
                if (where != null)
                    queryString.append(" WHERE ").append(where);
                if (sortOrder != null)
                    queryString.append(sortOrder).append(";");
                else
                    queryString.append(";");

                Cursor c = db.rawQuery(queryString.toString(), selectionArgs);

                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                assert context != null;
                c.setNotificationUri(context.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Columns supported by "tax" records.
     */
    public static class TaxContract implements DataColumns {

        /**
         * Path component for "tax"-type resources..
         */
        private static final String PATH = "tax";
        /**
         * MIME type for lists of tax.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.tax";
        /**
         * MIME type for individual tax.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.tax_item";

        /**
         * Fully qualified URI for "tax" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "tax" resources.
         */
        public static final String TABLE_NAME = "tax";

        public static final String USER_ID = "user_id";
        public static final String COUNTRY_ID = "country_id";
        public static final String ARRIVAL = "arrival";
        public static final String DEPARTURE = "departure";

        public static final String COUNTRY_NAME = CountriesContract.TABLE_NAME + "." + CountriesContract.NAME;
        public static final String COUNTRY_CODE = CountriesContract.TABLE_NAME + "." + CountriesContract.CODE;
        public static final String COUNTRY_FLAG = CountriesContract.TABLE_NAME + "." + CountriesContract.FLAG;

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                TABLE_NAME + "." + _ID, TABLE_NAME + "." + STATUS, TABLE_NAME + "." + REMOTE_ID,
                TABLE_NAME + "." + UPDATE_TIME, TABLE_NAME + "." + USER_ID, TABLE_NAME + "." + COUNTRY_ID,
                TABLE_NAME + "." + ARRIVAL, TABLE_NAME + "." + DEPARTURE, COUNTRY_NAME, COUNTRY_CODE, COUNTRY_FLAG
        };

        private static final String FROM_SQL =
                        " FROM tax INNER JOIN countries " +
                        " ON tax.country_id = countries.remote_id";
    }
}