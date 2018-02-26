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

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 28.01.15.
 */
public class RestTasksHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + RestTasksContract.TABLE_NAME + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    RestTasksContract.LOCAL_ID + TYPE_INTEGER + COMMA_SEP +
                    RestTasksContract.TYPE + TYPE_INTEGER + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "rest_tasks", ROUTE_REST_TASKS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "rest_tasks/*", ROUTE_REST_TASKS_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_REST_TASKS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_REST_TASKS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return RestTasksContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return RestTasksContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return RestTasksContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return RestTasksContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return RestTasksContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Insert a new entry into the database
     */
    public Uri insert(Context context, SQLiteDatabase db, Uri uri, ContentValues values) {
        assert db != null;
        final int match = match(uri);
        Uri result;
        long id;
        if (match == getRouteItemsCode()) {
            id = insertOrIgnore(db, values);
            result = Uri.parse(getContentUri() + "/" + id);
        } else if (match == getRouteItemsIdCode())
            throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
        else
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        if (id != -1) {
            // Send broadcast to registered ContentObservers, to refresh UI.
            assert context != null;
            context.getContentResolver().notifyChange(uri, null, false);
        }
        return result;
    }

    protected long insertOrIgnore(SQLiteDatabase db, ContentValues values) {
        long rowId = -1;

        int localId = values.getAsInteger(RestTasksContract.LOCAL_ID);
        int type = values.getAsInteger(RestTasksContract.TYPE);
        String select = RestTasksContract.LOCAL_ID + "=?" + " AND " + RestTasksContract.TYPE + "=?";

        SelectionBuilder builder = new SelectionBuilder();
        Cursor cursor = builder.table(getTableName())
                .where(select, String.valueOf(localId), String.valueOf(type))
                .query(db, new String[]{RestTasksContract._ID}, null);
        try {
            if (cursor.moveToNext()) rowId = cursor.getLong(0);
        } finally {
            cursor.close();
        }

        if (rowId == -1)
            rowId = db.insert(getTableName(), null, values);

        return rowId;
    }


    /**
     * Columns supported by "Document types groups" records.
     */
    public static class RestTasksContract implements BaseColumns {

        /**
         * Path component for "rest_task"-type resources..
         */
        private static final String PATH = "rest_tasks";
        /**
         * MIME type for lists of rest_tasks.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.rest_tasks";
        /**
         * MIME type for individual rest_task.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.rest_task";

        /**
         * Fully qualified URI for "rest_task" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "rest_task" resources.
         */
        public static final String TABLE_NAME = "rest_tasks";

        /**
         * local id of item to make task
         */
        public static final String LOCAL_ID = "local_id";

        /**
         * task's type
         */
        public static final String TYPE = "type";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, LOCAL_ID, TYPE
        };
    }

}
