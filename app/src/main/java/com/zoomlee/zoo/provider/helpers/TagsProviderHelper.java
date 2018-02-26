package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.zoomlee.zoo.db.SelectionBuilder;
import com.zoomlee.zoo.provider.ZoomleeProvider;
import com.zoomlee.zoo.utils.DBUtil;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_TAGS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_TAGS_DOC_ALERTS;
import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.ROUTE_TAGS_ID;
import static com.zoomlee.zoo.provider.helpers.DocumentsHelper.DocumentsContract;
import static com.zoomlee.zoo.provider.helpers.DocumentsTypesHelper.DocumentsTypesContract;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 06.04.15.
 */
public class TagsProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TagsContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    TagsContract.USER_ID + TYPE_INTEGER + COMMA_SEP +
                    TagsContract.NAME + TYPE_TEXT + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "tags", ROUTE_TAGS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "tags/*", ROUTE_TAGS_ID);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "tags_doc_alerts", ROUTE_TAGS_DOC_ALERTS);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_TAGS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_TAGS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return TagsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return TagsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return TagsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return TagsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return TagsContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    public Cursor query(Context context, SQLiteDatabase db, Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        int uriMatch = match(uri);
        String where = null;
        switch (uriMatch) {
            case ROUTE_TAGS_ID:
                String id = uri.getLastPathSegment();
                where = TagsContract.TABLE_NAME + "." + BaseColumns._ID + "=" + id;
            case ROUTE_TAGS:
                if (where == null) where = selection;

                StringBuilder queryString = new StringBuilder(TagsContract.FROM_SQL);
                if (where != null)
                    queryString.append(" WHERE ").append(where);
                queryString.append(TagsContract.GROUP_BY_SQL);
                if (sortOrder != null)
                    queryString.append(" ORDER BY ").append(sortOrder).append(";");
                else
                    queryString.append(";");

                Cursor c = db.rawQuery(queryString.toString(), selectionArgs);
                assert context != null;
                c.setNotificationUri(context.getContentResolver(), uri);
                return c;
            case ROUTE_TAGS_DOC_ALERTS:
                if (where == null) where = selection;
                if (projection == null) projection = TagsDocAlertsContract.ALL_COLUMNS_PROJECTION;

                queryString = new StringBuilder("SELECT ").append(DBUtil.formatProjection(projection)).append(" ")
                        .append(TagsDocAlertsContract.FROM_SQL);
                if (where != null)
                    queryString.append(" WHERE ").append(where);
                if (sortOrder != null)
                    queryString.append(" ORDER BY ").append(sortOrder).append(";");
                else
                    queryString.append(";");

                c = db.rawQuery(queryString.toString(), selectionArgs);
                assert context != null;
                c.setNotificationUri(context.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    protected long updateOrInsertByRemoteID(SQLiteDatabase db, ContentValues values) {
        long localId = -1;
        long remoteId = -1;
        if (values.containsKey(TagsContract.REMOTE_ID))
            remoteId = values.getAsLong(TagsContract.REMOTE_ID);
        if (values.containsKey(TagsContract._ID)) {
            localId = values.getAsLong(TagsContract._ID);
        } else if (remoteId != -1) {
            SelectionBuilder builder = new SelectionBuilder();
            Cursor cursor = builder.table(getTableName())
                    .where(TagsContract.REMOTE_ID + "=?", String.valueOf(remoteId))
                    .query(db, new String[]{TagsContract._ID}, null);
            try {
                if (cursor.moveToNext()) localId = cursor.getLong(0);
            } finally {
                cursor.close();
            }
        }

        if (localId == -1) {
            String name = values.getAsString(TagsContract.NAME);
            SelectionBuilder builder = new SelectionBuilder();
            String selection = TagsContract.NAME + "=?";
            if (remoteId != -1) {
                selection += " AND " + TagsContract.REMOTE_ID + " =-1";
            }
            Cursor cursor = builder.table(getTableName())
                    .where(selection, name)
                    .query(db, new String[]{TagsContract._ID}, null);
            try {
                if (cursor.moveToNext()) localId = cursor.getLong(0);
            } finally {
                cursor.close();
            }
        }

        int updateRowsCount = 0;
        if (localId != -1) {
            SelectionBuilder builder = new SelectionBuilder();
            updateRowsCount = builder.table(getTableName())
                    .where(TagsContract._ID + "=?", String.valueOf(localId))
                    .update(db, values);
        }
        if (updateRowsCount == 0)
            localId = db.insert(getTableName(), null, values);

        return localId;
    }

    /**
     * Columns supported by "tags" records.
     */
    public static class TagsContract implements DataColumns {

        /**
         * Path component for "tag"-type resources..
         */
        private static final String PATH = "tags";
        /**
         * MIME type for lists of tags.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.tags";
        /**
         * MIME type for individual tag.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.tag";

        /**
         * Fully qualified URI for "tag" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "tag" resources.
         */
        public static final String TABLE_NAME = "tags";

        public static final String USER_ID = "user_id";
        public static final String NAME = "name";
        public static final String DOCS_COUNT = "docs_count";

        public static final String DOCUMENT_ID = DocumentsContract.TABLE_NAME + "." + DocumentsContract._ID;
        public static final String PERSON_ID = DocumentsContract.TABLE_NAME + "." + DocumentsContract.PERSON_ID;

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                TABLE_NAME + "." + _ID, TABLE_NAME + "." + STATUS, TABLE_NAME + "." + REMOTE_ID,
                TABLE_NAME + "." + UPDATE_TIME, TABLE_NAME + "." + USER_ID, TABLE_NAME + "." + NAME,
                DOCS_COUNT
        };

        private static final String FROM_SQL =
                "SELECT tags._id, tags.status, tags.remote_id, tags.update_time, tags.user_id, tags.name, count(tags2documents._id) AS docs_count " +
                        " FROM tags LEFT JOIN tags2documents " +
                        "ON tags._id = tags2documents.tag_id" +
                        " LEFT JOIN documents" +
                        " ON documents._id = tags2documents.document_id ";
        private static final String GROUP_BY_SQL =
                " GROUP BY tags._id ";
    }

    public static class TagsDocAlertsContract {

        private static final String PATH = "tags_doc_alerts";

        public static final Uri URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String TAGS_ID = TagsContract.TABLE_NAME + "." + TagsContract._ID;
        public static final String TAGS_STATUS = TagsContract.TABLE_NAME + "." + TagsContract.STATUS;
        public static final String TAGS_REMOTE_ID = TagsContract.TABLE_NAME + "." + TagsContract.REMOTE_ID;
        public static final String TAGS_UPDATE_TIME = TagsContract.TABLE_NAME + "." + TagsContract.UPDATE_TIME;
        public static final String TAGS_NAME = TagsContract.TABLE_NAME + "." + TagsContract.NAME;
        public static final String TAGS_USER_ID = TagsContract.TABLE_NAME + "." + TagsContract.USER_ID;
        public static final String ALERTS_COUNT = "alerts_count";
        public static final String DOCUMENT_TYPE_NAME = DocumentsTypesContract.TABLE_NAME
                + "." + DocumentsTypesContract.NAME;

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                TAGS_ID, TAGS_STATUS, TAGS_REMOTE_ID, TAGS_UPDATE_TIME, TAGS_NAME, TAGS_USER_ID,
                ALERTS_COUNT, DOCUMENT_TYPE_NAME
        };

        private static final String FROM_SQL =
                "FROM documents LEFT JOIN tags2documents " +
                        "   ON documents._id = tags2documents.document_id" +
                        " LEFT JOIN tags" +
                        "   ON tags._id = tags2documents.tag_id " +
                        " INNER JOIN documents_types" +
                        "   ON documents.document_type_id = documents_types.remote_id" +
                        " LEFT OUTER JOIN " +
                        "  (SELECT fields.document_id AS fields_doc_id, count(fields._id) AS alerts_count " +
                        "   FROM fields " +
                        "   WHERE fields.notify_on < ? GROUP BY fields_doc_id)" +
                        " ON documents._id = fields_doc_id";
    }
}