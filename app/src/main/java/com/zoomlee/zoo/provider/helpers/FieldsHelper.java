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

import java.util.Arrays;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FIELDS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FIELDS_HISTORY;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FIELDS_ID;
import static com.zoomlee.zoo.provider.helpers.DocumentsTypes2FieldTypesHelper.DocumentsTypes2FieldTypesContract;
import static com.zoomlee.zoo.provider.helpers.FieldsTypesProviderHelper.FieldsTypesContract;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class FieldsHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + FieldsContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    FieldsContract.DOCUMENT_ID + TYPE_INTEGER + COMMA_SEP +
                    FieldsContract.FIELD_TYPE_ID + TYPE_INTEGER + COMMA_SEP +
                    FieldsContract.NAME + TYPE_TEXT + COMMA_SEP +
                    FieldsContract.VALUE + TYPE_TEXT + COMMA_SEP +
                    FieldsContract.NOTIFY_ON + TYPE_INTEGER + COMMA_SEP +
                    FieldsContract.CREATE_TIME + TYPE_INTEGER + COMMA_SEP +
                    FieldsContract.VIEWED + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "fields", ROUTE_FIELDS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "fields/*", ROUTE_FIELDS_ID);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "fields_history", ROUTE_FIELDS_HISTORY);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_FIELDS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_FIELDS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return FieldsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return FieldsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return FieldsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return FieldsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return FieldsContract.TABLE_NAME;
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
            case ROUTE_FIELDS_ID:
                String id = uri.getLastPathSegment();
                where = FieldsContract.TABLE_NAME + "." + BaseColumns._ID + "=" + id;
            case ROUTE_FIELDS:
                if (where == null) where = selection;
                if (projection == null) projection = FieldsContract.ALL_COLUMNS_PROJECTION;

                StringBuilder queryString = new StringBuilder("SELECT ")
                        .append(DBUtil.formatProjection(projection)).append(" ")
                        .append(FieldsContract.FROM_SQL);
                if (where != null)
                    queryString.append(" WHERE ").append(where);
                if (sortOrder != null)
                    queryString.append(" ORDER BY ").append(sortOrder).append(";");
                else
                    queryString.append(";");

                Cursor c = db.rawQuery(queryString.toString(), selectionArgs);

                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                assert context != null;
                c.setNotificationUri(context.getContentResolver(), uri);
                return c;
            case ROUTE_FIELDS_HISTORY:
                if (projection == null) projection = FieldsHistoryContract.ALL_COLUMNS_PROJECTION;

                String fieldTypes = null;
                if (selectionArgs != null && selectionArgs.length > 0) {
                    if (selectionArgs.length > 1) {
                        String[] fieldTypesArgs = Arrays.copyOfRange(selectionArgs, 1, selectionArgs.length);
                        fieldTypes = DBUtil.formatArgsAsSet(fieldTypesArgs);
                    }

                    selectionArgs = new String[]{selectionArgs[0]};
                }

                queryString =
                        new StringBuilder("SELECT ").append(DBUtil.formatProjection(projection)).append(" ")
                                .append(FieldsHistoryContract.FROM_SQL);
                if (fieldTypes != null)
                    queryString.append(FieldsHistoryContract.WHERE_SQL).append(fieldTypes);
                else
                    queryString.append(FieldsHistoryContract.WHERE_FALSE);
                queryString.append(FieldsHistoryContract.GROUP_BY_SQL);

                c = db.rawQuery(queryString.toString(), selectionArgs);

                assert context != null;
                c.setNotificationUri(context.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Columns supported by "field" records.
     */
    public static class FieldsContract implements DataColumns {

        /**
         * Path component for "fields"-type resources..
         */
        private static final String PATH = "fields";
        /**
         * MIME type for lists of fields.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.fields";
        /**
         * MIME type for individual field.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.field";

        /**
         * Fully qualified URI for "field" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "field" resources.
         */
        public static final String TABLE_NAME = "fields";

        /**
         * Document's id
         */
        public static final String DOCUMENT_ID = "document_id";
        /**
         * Field type's id
         */
        public static final String FIELD_TYPE_ID = "field_type_id";
        /**
         * Field's name
         */
        public static final String NAME = "name";
        /**
         * Field's value
         */
        public static final String VALUE = "value";

        public static final String NOTIFY_ON = "notify_on";
        public static final String VIEWED = "viewed";
        public static final String CREATE_TIME = "create_time";
        public static final String WEIGHT = DocumentsTypes2FieldTypesContract.WEIGHT;
        public static final String TYPE = FieldsTypesContract.TYPE;
        public static final String SUGGEST = FieldsTypesContract.SUGGEST;
        public static final String REMINDER = FieldsTypesContract.REMINDER;

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                TABLE_NAME + "." + _ID, TABLE_NAME + "." + REMOTE_ID, TABLE_NAME + "." + UPDATE_TIME,
                TABLE_NAME + "." + STATUS, TABLE_NAME + "." + DOCUMENT_ID, TABLE_NAME + "." + FIELD_TYPE_ID,
                TABLE_NAME + "." + NAME, TABLE_NAME + "." + VALUE, NOTIFY_ON, VIEWED, WEIGHT, TYPE,
                TABLE_NAME + "." + CREATE_TIME, SUGGEST, REMINDER
        };

        private static final String FROM_SQL =
                " FROM fields " +
                        "INNER JOIN documents " +
                        "ON fields.document_id = documents._id" +
                        " LEFT OUTER JOIN documents_types2fields_types " +
                        "ON fields.field_type_id = documents_types2fields_types.fields_type_id " +
                        "AND documents.document_type_id = documents_types2fields_types.document_id " +
                        " LEFT OUTER JOIN field_types ON field_types.remote_id = fields.field_type_id ";
    }

    /**
     * require 2 parameters "local person id"  and "field's type ids" (as set like: '[1,2,43]')
     */
    public static class FieldsHistoryContract {

        /**
         * Path component for "fields"-type resources..
         */
        private static final String PATH = "fields_history";

        /**
         * Fully qualified URI for "fields_history" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();


        public static final String FIELD_TYPE_ID = FieldsContract.TABLE_NAME
                + "." + FieldsContract.FIELD_TYPE_ID;
        public static final String FIELD_VALUE = FieldsContract.TABLE_NAME
                + "." + FieldsContract.VALUE;

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                FIELD_TYPE_ID, FIELD_VALUE
        };

        private static final String FROM_SQL =
                " FROM documents INNER JOIN fields ON" +
                        " fields.document_id = documents._id AND documents.person_id = ?";
        private static final String WHERE_SQL = " WHERE fields.field_type_id in ";
        private static final String GROUP_BY_SQL = " GROUP BY fields.field_type_id;";
        private static final String WHERE_FALSE = " WHERE 0 ";
    }
}
