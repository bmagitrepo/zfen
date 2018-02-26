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

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;
import static com.zoomlee.zoo.provider.helpers.DocumentsTypesHelper.DocumentsTypesContract;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class CategoriesProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + CategoriesContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    CategoriesContract.NAME + TYPE_TEXT + COMMA_SEP +
                    CategoriesContract.WEIGHT + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "categories", ROUTE_CATEGORIES);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "categories/*", ROUTE_CATEGORIES_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_CATEGORIES;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_CATEGORIES_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return CategoriesContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return CategoriesContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return CategoriesContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return CategoriesContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return CategoriesContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }


    public Cursor query(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        int uriMatch = match(uri);
        String where = null;
        switch (uriMatch) {
            case ROUTE_CATEGORIES_ID:
                String id = uri.getLastPathSegment();
                where = CategoriesContract.TABLE_NAME + "." + BaseColumns._ID + "=" + id;
            case ROUTE_CATEGORIES:
                if (where == null) where = selection;
                if (projection == null) projection = CategoriesContract.ALL_COLUMNS_PROJECTION;

                StringBuilder queryString =
                        new StringBuilder("SELECT ").append(DBUtil.formatProjection(projection)).append(" ")
                                .append(CategoriesContract.FROM_SQL);
                if (where != null)
                    queryString.append(" WHERE ").append(where);
                queryString.append(" ORDER BY weight DESC");
                if (sortOrder != null)
                    queryString.append(", ").append(sortOrder).append(";");
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
     * Columns supported by "categories" records.
     */
    public static class CategoriesContract implements DataColumns {

        /**
         * Path component for "category"-type resources..
         */
        private static final String PATH = "categories";
        /**
         * MIME type for lists of categories.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.categories";
        /**
         * MIME type for individual category.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.category";

        /**
         * Fully qualified URI for "category" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "category" resources.
         */
        public static final String TABLE_NAME = "categories";

        /**
         * category's name
         */
        public static final String NAME = "name";
        public static final String WEIGHT = "weight";
        public static final String DOCUMENT_TYPE_REMOTE_ID = DocumentsTypesContract.TABLE_NAME + "." + DocumentsTypesContract.REMOTE_ID;
        public static final String DOCUMENT_TYPE_NAME = DocumentsTypesContract.TABLE_NAME + "." + DocumentsTypesContract.NAME;

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                TABLE_NAME + "." + _ID, TABLE_NAME + "." + REMOTE_ID, TABLE_NAME + "." + UPDATE_TIME,
                TABLE_NAME + "." + STATUS, TABLE_NAME + "." + NAME, TABLE_NAME + "." + WEIGHT,
                DOCUMENT_TYPE_REMOTE_ID, DOCUMENT_TYPE_NAME
        };

        private static final String FROM_SQL = "From categories INNER JOIN category2documents_types " +
                "ON categories.remote_id = category2documents_types.category_id " +
                "INNER JOIN documents_types ON category2documents_types.documents_type_id = documents_types.remote_id ";
    }

}
