package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.zoomlee.zoo.provider.ZoomleeProvider;
import com.zoomlee.zoo.provider.helpers.CategoriesProviderHelper.CategoriesContract;
import com.zoomlee.zoo.provider.helpers.ColorsProviderHelper.ColorsContract;
import com.zoomlee.zoo.provider.helpers.DocumentsTypesHelper.DocumentsTypesContract;
import com.zoomlee.zoo.provider.helpers.FieldsHelper.FieldsContract;
import com.zoomlee.zoo.provider.helpers.GroupsHelper.GroupsContract;
import com.zoomlee.zoo.utils.DBUtil;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_ALERTS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_CATEGORY_DOC_ALERTS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_DOCUMENTS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_DOCUMENTS_ID;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FULL_DOCUMENTS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_FULL_DOCUMENTS_ID;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_TAG_DOCUMENTS;
import static com.zoomlee.zoo.provider.helpers.Tags2DocumentsProviderHelper.Tags2DocumentsContract;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class DocumentsHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + DocumentsContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    DocumentsContract.PERSON_ID + TYPE_INTEGER + COMMA_SEP +
                    DocumentsContract.USER_ID + TYPE_INTEGER + COMMA_SEP +
                    DocumentsContract.CATEGORY_ID + TYPE_INTEGER + COMMA_SEP +
                    DocumentsContract.COLOR_ID + TYPE_INTEGER + COMMA_SEP +
                    DocumentsContract.DOCUMENT_TYPE_ID + TYPE_INTEGER + COMMA_SEP +
                    DocumentsContract.NAME + TYPE_TEXT + COMMA_SEP +
                    DocumentsContract.NOTES + TYPE_TEXT + COMMA_SEP +
                    DocumentsContract.CREATE_TIME + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "documents", ROUTE_DOCUMENTS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "documents/*", ROUTE_DOCUMENTS_ID);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "full_documents", ROUTE_FULL_DOCUMENTS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "full_documents/*", ROUTE_FULL_DOCUMENTS_ID);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "categories_doc_alerts", ROUTE_CATEGORY_DOC_ALERTS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "alerts", ROUTE_ALERTS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "tag_documents", ROUTE_TAG_DOCUMENTS);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_DOCUMENTS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_DOCUMENTS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return DocumentsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return DocumentsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return DocumentsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return DocumentsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return DocumentsContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all entries (/{table}) and individual entry by ID
     * (/{table}/{ID}).
     */
    public Cursor query(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        int uriMatch = match(uri);
        String where = null;
        String fromSql = null;
        switch (uriMatch) {
            case ROUTE_ALERTS:
                if (where == null) where = selection;
                if (projection == null) projection = AlertsContract.ALL_COLUMNS_PROJECTION;

                StringBuilder queryString =
                        new StringBuilder("SELECT ").append(DBUtil.formatProjection(projection)).append(" ")
                                .append(AlertsContract.FROM_SQL);
                if (where != null)
                    queryString.append(" AND ").append(where);
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
            case ROUTE_CATEGORY_DOC_ALERTS:
                if (where == null) where = selection;
                if (projection == null) projection = CategoriesDocAlertsContract.ALL_COLUMNS_PROJECTION;

                StringBuilder queryString3 =
                        new StringBuilder("SELECT ").append(DBUtil.formatProjection(projection)).append(" ")
                                .append(CategoriesDocAlertsContract.FROM_SQL);
                if (where != null)
                    queryString3.append(" WHERE ").append(where);
                if (sortOrder != null)
                    queryString3.append(" ORDER BY ").append(sortOrder).append(";");
                else
                    queryString3.append(";");

                Cursor c3 = db.rawQuery(queryString3.toString(), selectionArgs);

                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                assert context != null;
                c3.setNotificationUri(context.getContentResolver(), uri);
                return c3;
            case ROUTE_FULL_DOCUMENTS_ID:
                String id = uri.getLastPathSegment();
                where = DocumentsContract.TABLE_NAME + "." + BaseColumns._ID + "=" + id;
            case ROUTE_TAG_DOCUMENTS:
                fromSql = TagDocumentsContract.FROM_SQL;
            case ROUTE_FULL_DOCUMENTS:
                if (where == null) where = selection;
                if (projection == null) projection = FullDocumentsContract.ALL_COLUMNS_FULL_PROJECTION;
                if (fromSql == null) fromSql = FullDocumentsContract.FROM_SQL;

                StringBuilder queryString2 =
                        new StringBuilder("SELECT ").append(DBUtil.formatProjection(projection)).append(" ")
                                .append(fromSql);
                if (where != null)
                    queryString2.append(" WHERE ").append(where);
                if (sortOrder != null)
                    queryString2.append(" ORDER BY ").append(sortOrder).append(";");
                else
                    queryString2.append(";");

                Cursor c2 = db.rawQuery(queryString2.toString(), selectionArgs);

                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                assert context != null;
                c2.setNotificationUri(context.getContentResolver(), uri);
                return c2;
            default:
                return super.query(context, db, uri, projection, selection, selectionArgs, sortOrder);
        }
    }/**/


    /**
     * Columns supported by "document" records.
     */
    public static class DocumentsContract implements DataColumns {

        /**
         * Path component for "documents"-type resources..
         */
        private static final String PATH = "documents";

        /**
         * MIME type for lists of documents.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.documents";
        /**
         * MIME type for individual document.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.document";

        /**
         * Fully qualified URI for "document" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "document" resources.
         */
        public static final String TABLE_NAME = "documents";

        /**
         * Local person's id
         */
        public static final String PERSON_ID = "person_id";
        /**
         * User's id
         */
        public static final String USER_ID = "user_id";
        /**
         * Category's id
         */
        public static final String CATEGORY_ID = "category_id";
        /**
         * Color's id
         */
        public static final String COLOR_ID = "color_id";
        /**
         * Document type's id
         */
        public static final String DOCUMENT_TYPE_ID = "document_type_id";
        /**
         * Document's name
         */
        public static final String NAME = "name";
        /**
         * Document's notes
         */
        public static final String NOTES = "notes";
        /**
         * Document's creation time
         */
        public static final String CREATE_TIME = "create_time";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, PERSON_ID, USER_ID, CATEGORY_ID, COLOR_ID,
                DOCUMENT_TYPE_ID, NAME, NOTES, CREATE_TIME
        };
    }

    public static class FullDocumentsContract extends DocumentsContract{

        /**
         * Path component for full "documents"-type resources..
         */
        private static final String PATH_TO_FULL = "full_documents";
        /**
         * URI for full "document" resources (merged with other tables, except fields and files).
         */
        public static final Uri FULL_DATA_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH_TO_FULL).build();

        public static final String _ID = TABLE_NAME + "." + DocumentsContract._ID;
        public static final String REMOTE_ID = TABLE_NAME + "." + DocumentsContract.REMOTE_ID;
        public static final String UPDATE_TIME = TABLE_NAME + "." + DocumentsContract.UPDATE_TIME;
        public static final String STATUS = TABLE_NAME + "." + DocumentsContract.STATUS;
        public static final String PERSON_ID = TABLE_NAME + "." + DocumentsContract.PERSON_ID;
        public static final String USER_ID = TABLE_NAME + "." + DocumentsContract.USER_ID;
        public static final String CATEGORY_ID = TABLE_NAME + "." + DocumentsContract.CATEGORY_ID;
        public static final String COLOR_ID = TABLE_NAME + "." + DocumentsContract.COLOR_ID;
        public static final String DOCUMENT_TYPE_ID = TABLE_NAME + "." + DocumentsContract.DOCUMENT_TYPE_ID;
        public static final String NAME = TABLE_NAME + "." + DocumentsContract.NAME;
        public static final String NOTES = TABLE_NAME + "." + DocumentsContract.NOTES;
        public static final String CREATE_TIME = TABLE_NAME + "." + DocumentsContract.CREATE_TIME;


        public static final String CATEGORY_NAME = CategoriesContract.TABLE_NAME
                + "." + CategoriesContract.NAME;
        public static final String CATEGORY_WEIGHT = CategoriesContract.TABLE_NAME
                + "." + CategoriesContract.WEIGHT;
        public static final String COLOR_NAME = ColorsContract.TABLE_NAME
                + "." + ColorsContract.NAME;
        public static final String COLOR_HEX = ColorsContract.TABLE_NAME
                + "." + ColorsContract.HEX;
        public static final String TYPE_NAME = DocumentsTypesContract.TABLE_NAME
                + "." + DocumentsTypesContract.NAME;
        public static final String TYPE_GROUP_ID = DocumentsTypesContract.TABLE_NAME
                + "." + DocumentsTypesContract.GROUP_TYPE;
        public static final String GROUP_NAME = GroupsContract.TABLE_NAME
                + "." + GroupsContract.NAME;

        public static final String[] ALL_COLUMNS_FULL_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, PERSON_ID, USER_ID, CATEGORY_ID, COLOR_ID,
                DOCUMENT_TYPE_ID, NAME, NOTES, CREATE_TIME, CATEGORY_NAME, CATEGORY_WEIGHT,
                COLOR_NAME, COLOR_HEX, TYPE_NAME, TYPE_GROUP_ID, GROUP_NAME
        };

        private static final String FROM_SQL =
                "FROM " + DocumentsContract.TABLE_NAME + " LEFT OUTER JOIN " +
                CategoriesContract.TABLE_NAME + " ON " +
                CATEGORY_ID + " = " + CategoriesContract.TABLE_NAME + "." + CategoriesContract.REMOTE_ID +
                " LEFT OUTER JOIN " + ColorsContract.TABLE_NAME + " ON " +
                COLOR_ID + " = " + ColorsContract.TABLE_NAME + "." + ColorsContract.REMOTE_ID +
                " LEFT OUTER JOIN " + DocumentsTypesContract.TABLE_NAME + " ON " +
                DOCUMENT_TYPE_ID + " = " + DocumentsTypesContract.TABLE_NAME + "." + DocumentsTypesContract.REMOTE_ID +
                " LEFT OUTER JOIN " + GroupsContract.TABLE_NAME + " ON " +
                TYPE_GROUP_ID + " = " + GroupsContract.TABLE_NAME + "." + GroupsContract.REMOTE_ID;
    }

    public static class TagDocumentsContract extends FullDocumentsContract{

        private static final String PATH = "tag_documents";

        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        private static final String FROM_SQL =
                "FROM " + DocumentsContract.TABLE_NAME +
                        " INNER JOIN " + Tags2DocumentsContract.TABLE_NAME + " ON " +
                            _ID + " = " + Tags2DocumentsContract.TABLE_NAME + "." + Tags2DocumentsContract.DOCUMENT_ID +
                            " AND " +Tags2DocumentsContract.TABLE_NAME + "." + Tags2DocumentsContract.TAG_ID + " = ?" +
                        " LEFT OUTER JOIN " + CategoriesContract.TABLE_NAME + " ON " +
                            CATEGORY_ID + " = " + CategoriesContract.TABLE_NAME + "." + CategoriesContract.REMOTE_ID +
                        " LEFT OUTER JOIN " + ColorsContract.TABLE_NAME + " ON " +
                            COLOR_ID + " = " + ColorsContract.TABLE_NAME + "." + ColorsContract.REMOTE_ID +
                        " LEFT OUTER JOIN " + DocumentsTypesContract.TABLE_NAME + " ON " +
                            DOCUMENT_TYPE_ID + " = " + DocumentsTypesContract.TABLE_NAME + "." + DocumentsTypesContract.REMOTE_ID +
                        " LEFT OUTER JOIN " + GroupsContract.TABLE_NAME + " ON " +
                            TYPE_GROUP_ID + " = " + GroupsContract.TABLE_NAME + "." + GroupsContract.REMOTE_ID;
    }

    public static class CategoriesDocAlertsContract {

        private static final String PATH = "categories_doc_alerts";

        public static final Uri URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();


        public static final String CATEGORY_REMOTE_ID = CategoriesContract.TABLE_NAME + "." + CategoriesContract.REMOTE_ID;
        public static final String CATEGORY_NAME = CategoriesContract.TABLE_NAME
                + "." + CategoriesContract.NAME;
        public static final String CATEGORY_WEIGHT = CategoriesContract.TABLE_NAME
                + "." + CategoriesContract.WEIGHT;
        public static final String PERSON_ID = DocumentsContract.TABLE_NAME
                + "." + DocumentsContract.PERSON_ID;
        public static final String ALERTS_COUNT = "alerts_count";
        public static final String DOCUMENT_TYPE_NAME = DocumentsTypesContract.TABLE_NAME
                + "." + DocumentsTypesContract.NAME;


        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                CATEGORY_REMOTE_ID, CATEGORY_NAME, CATEGORY_WEIGHT,
                PERSON_ID, ALERTS_COUNT, DOCUMENT_TYPE_NAME
        };

        private static final String FROM_SQL =
                "FROM categories INNER JOIN documents " +
                 "ON categories.remote_id = documents.category_id" +
                        " INNER JOIN documents_types" +
                        " ON documents.document_type_id = documents_types.remote_id"+
                 " LEFT OUTER JOIN " +
                    "(SELECT fields.document_id AS fields_doc_id, count(fields._id) AS alerts_count " +
                        "FROM fields " +
                        "WHERE fields.notify_on < ? GROUP BY fields_doc_id)" +
                 " ON documents._id = fields_doc_id";
    }

    public static class AlertsContract {

        private static final String PATH = "alerts";

        public static final Uri URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();


        public static final String CATEGORY_REMOTE_ID = DocumentsContract.TABLE_NAME + "." + DocumentsContract.CATEGORY_ID;
        public static final String DOCUMENT_NAME = DocumentsContract.TABLE_NAME
                + "." + DocumentsContract.NAME;
        public static final String PERSON_ID = DocumentsContract.TABLE_NAME
                + "." + DocumentsContract.PERSON_ID;
        public static final String DOCUMENT_ID = DocumentsContract.TABLE_NAME
                + "." + DocumentsContract._ID;
        public static final String FIELD_ID = FieldsContract.TABLE_NAME
                + "." + FieldsContract._ID;
        public static final String FIELD_NAME = FieldsContract.TABLE_NAME
                + "." + FieldsContract.NAME;
        public static final String FIELD_VALUE = FieldsContract.TABLE_NAME
                + "." + FieldsContract.VALUE;
        public static final String NOTIFY_ON = FieldsContract.NOTIFY_ON;
        public static final String VIEWED = FieldsContract.VIEWED;


        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                CATEGORY_REMOTE_ID, FIELD_ID, FIELD_NAME, FIELD_VALUE, DOCUMENT_ID,
                DOCUMENT_NAME, PERSON_ID, NOTIFY_ON, VIEWED
        };

        private static final String FROM_SQL =
                " FROM fields INNER JOIN documents ON fields.document_id = documents._id" +
                        " WHERE fields.notify_on < ?";
    }
}