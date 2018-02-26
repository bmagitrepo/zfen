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
import static com.zoomlee.zoo.provider.helpers.FieldsTypesProviderHelper.FieldsTypesContract;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class DocumentsTypes2FieldTypesHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + DocumentsTypes2FieldTypesContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    DocumentsTypes2FieldTypesContract.DOCUMENT_TYPE_ID + TYPE_INTEGER + COMMA_SEP +
                    DocumentsTypes2FieldTypesContract.FIELD_ID + TYPE_INTEGER + COMMA_SEP +
                    DocumentsTypes2FieldTypesContract.WEIGHT + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "documents_types2fields_types", ROUTE_DOCUMENTS_TYPES2FIELD_TYPES);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "documents_types2fields_types/*", ROUTE_DOCUMENTS_TYPES2FIELD_TYPES_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_DOCUMENTS_TYPES2FIELD_TYPES;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_DOCUMENTS_TYPES2FIELD_TYPES_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return DocumentsTypes2FieldTypesContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return DocumentsTypes2FieldTypesContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return DocumentsTypes2FieldTypesContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return DocumentsTypes2FieldTypesContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return DocumentsTypes2FieldTypesContract.TABLE_NAME;
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
            case ROUTE_DOCUMENTS_TYPES2FIELD_TYPES_ID:
                String id = uri.getLastPathSegment();
                where = DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + BaseColumns._ID + "=" + id;
            case ROUTE_DOCUMENTS_TYPES2FIELD_TYPES:
                if (where == null) where = selection;
                if (projection == null) projection = DocumentsTypes2FieldTypesContract.ALL_COLUMNS_PROJECTION;

                StringBuilder queryString =
                        new StringBuilder("SELECT ").append(DBUtil.formatProjection(projection)).append(" ")
                                .append(DocumentsTypes2FieldTypesContract.FROM_SQL);
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
     * Columns supported by "Document types to field's type" records.
     */
    public static class DocumentsTypes2FieldTypesContract implements DataColumns {

        /**
         * Path component for "documents_types2fields_types"-type resources..
         */
        private static final String PATH = "documents_types2fields_types";
        /**
         * MIME type for lists of documents_types2fields_types.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.documents_types2fields_types";
        /**
         * MIME type for individual documents_types2fields_type.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.documents_types2fields_type";

        /**
         * Fully qualified URI for "documents_types2fields_type" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "documents_types2fields_type" resources.
         */
        public static final String TABLE_NAME = "documents_types2fields_types";

        /**
         * document type's id
         */
        public static final String DOCUMENT_TYPE_ID = "document_id";
        /**
         * fields type's id
         */
        public static final String FIELD_ID = "fields_type_id";
        public static final String WEIGHT = "weight";

        public static final String FIELD_TYPE_VALUE = FieldsTypesContract.TABLE_NAME + "." + FieldsTypesContract.TYPE;
        public static final String FIELD_TYPE_NAME = FieldsTypesContract.TABLE_NAME + "." + FieldsTypesContract.NAME;
        public static final String FIELD_TYPE_REMINDER = FieldsTypesContract.TABLE_NAME + "." + FieldsTypesContract.REMINDER;
        public static final String FIELD_TYPE_SUGGEST = FieldsTypesContract.TABLE_NAME + "." + FieldsTypesContract.SUGGEST;

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                TABLE_NAME + "." + _ID, TABLE_NAME + "." + REMOTE_ID, TABLE_NAME + "." + UPDATE_TIME,
                TABLE_NAME + "." + STATUS, TABLE_NAME + "." + DOCUMENT_TYPE_ID, TABLE_NAME + "." + FIELD_ID,
                TABLE_NAME + "." + WEIGHT, FIELD_TYPE_VALUE, FIELD_TYPE_NAME, FIELD_TYPE_REMINDER, FIELD_TYPE_SUGGEST
        };

        private static final String FROM_SQL = "From documents_types2fields_types INNER JOIN field_types " +
                "ON documents_types2fields_types.fields_type_id = field_types.remote_id ";
    }

}
