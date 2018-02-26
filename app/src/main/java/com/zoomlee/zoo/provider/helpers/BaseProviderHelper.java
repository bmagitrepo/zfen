package com.zoomlee.zoo.provider.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.zoomlee.zoo.db.SelectionBuilder;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public abstract class BaseProviderHelper {

    public static final String TYPE_TEXT = " TEXT";
    public static final String TYPE_INTEGER = " INTEGER";
    static final String COMMA_SEP = ",";

    static final String SQL_BASE_COLUMN_CREATION =
            DataColumns._ID + " INTEGER PRIMARY KEY," +
                    DataColumns.REMOTE_ID + TYPE_INTEGER + COMMA_SEP +
                    DataColumns.STATUS + TYPE_INTEGER + COMMA_SEP +
                    DataColumns.UPDATE_TIME + TYPE_INTEGER + COMMA_SEP;

    /**
     * Determine the mime type for entry returned by a given URI.
     */
    public String getType(Uri uri) {
        final int match = match(uri);
        if (match == getRouteItemsCode())
            return getEntityContentType();
        else if (match == getRouteItemsIdCode())
            return getEntityContentItemType();
        else
            throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all entries (/{table}) and individual entry by ID
     * (/{table}/{ID}).
     */
    public Cursor query(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = match(uri);
        if (uriMatch == getRouteItemsIdCode()) {
            // Return a single person, by ID.
            String id = uri.getLastPathSegment();
            builder.where(BaseColumns._ID + "=?", id);
        }
        if (uriMatch == getRouteItemsCode() || uriMatch == getRouteItemsIdCode()) {
            // Return all known persons.
            if (projection == null) projection = getAllColumnsProjection();
            builder.table(getTableName())
                    .where(selection, selectionArgs);
            Cursor c = builder.query(db, projection, sortOrder);
            // Note: Notification URI must be manually set here for loaders to correctly
            // register ContentObservers.
            assert context != null;
            c.setNotificationUri(context.getContentResolver(), uri);
            return c;
        }

        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    /**
     * Insert a new entry into the database or update exist one by {@linkplain DataColumns#REMOTE_ID}
     * or {@linkplain DataColumns#_ID}.
     */
    public Uri insert(Context context, SQLiteDatabase db, Uri uri, ContentValues values) {
        assert db != null;
        final int match = match(uri);
        Uri result;
        if (match == getRouteItemsCode()) {
            long id = updateOrInsertByRemoteID(db, values);
            result = Uri.parse(getContentUri() + "/" + id);
        } else if (match == getRouteItemsIdCode())
            throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
        else
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        // Send broadcast to registered ContentObservers, to refresh UI.
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    protected long updateOrInsertByRemoteID(SQLiteDatabase db, ContentValues values) {
        long localId = -1;
        long remoteId = -1;
        if (values.containsKey(DataColumns.REMOTE_ID))
            remoteId = values.getAsLong(DataColumns.REMOTE_ID);
        if (values.containsKey(DataColumns._ID)) {
            localId = values.getAsLong(DataColumns._ID);
        } else if (remoteId != -1) {
            SelectionBuilder builder = new SelectionBuilder();
            Cursor cursor = builder.table(getTableName())
                    .where(DataColumns.REMOTE_ID + "=?", String.valueOf(remoteId))
                    .query(db, new String[]{DataColumns._ID}, null);
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
                    .where(DataColumns._ID + "=?", String.valueOf(localId))
                    .update(db, values);
        }
        if (updateRowsCount == 0)
            localId = db.insert(getTableName(), null, values);

        return localId;
    }

    /**
     * Delete an entry by database by URI.
     */
    public int delete(Context context, SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final int match = match(uri);
        int count;
        if (match == getRouteItemsCode()) {
            count = builder.table(getTableName())
                    .where(selection, selectionArgs)
                    .delete(db);
        } else if (match == getRouteItemsIdCode()) {
            String id = uri.getLastPathSegment();
            count = builder.table(getTableName())
                    .where(BaseColumns._ID + "=?", id)
                    .where(selection, selectionArgs)
                    .delete(db);
        } else
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        // Send broadcast to registered ContentObservers, to refresh UI.
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an entry in the database by URI.
     */
    public int update(Context context, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final int match = match(uri);
        int count;
        if (match == getRouteItemsCode()) {
            count = builder.table(getTableName())
                    .where(selection, selectionArgs)
                    .update(db, values);
        } else if (match == getRouteItemsIdCode()) {
            String id = uri.getLastPathSegment();
            count = builder.table(getTableName())
                    .where(BaseColumns._ID + "=?", id)
                    .where(selection, selectionArgs)
                    .update(db, values);
        } else
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    public String getDropTableSQL() {
        return "DROP TABLE IF EXISTS " + getTableName();
    }

    protected abstract int getRouteItemsCode();

    protected abstract int getRouteItemsIdCode();

    protected abstract String[] getAllColumnsProjection();

    public abstract int match(Uri uri);

    public abstract String getEntityContentType();

    public abstract String getEntityContentItemType();

    public abstract Uri getContentUri();

    public abstract String getTableName();

    public abstract String getCreateTableSQL();

    public interface DataColumns extends BaseColumns {

        /**
         * Id got from server
         */
        public static final String REMOTE_ID = "remote_id";
        /**
         * status of item
         */
        public static final String STATUS = "status";
        /**
         * last update time
         */
        public static final String UPDATE_TIME = "update_time";
    }
}
