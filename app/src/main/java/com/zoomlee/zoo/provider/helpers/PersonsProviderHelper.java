package com.zoomlee.zoo.provider.helpers;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

import com.zoomlee.zoo.provider.ZoomleeProvider;

import static com.zoomlee.zoo.provider.ZoomleeProvider.Routes.*;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 21.01.15.
 */
public class PersonsProviderHelper extends BaseProviderHelper {

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + PersonsContract.TABLE_NAME + " (" +
                    SQL_BASE_COLUMN_CREATION +
                    PersonsContract.USER_ID + TYPE_INTEGER + COMMA_SEP +
                    PersonsContract.NAME + TYPE_TEXT + COMMA_SEP +
                    PersonsContract.IMAGE_REMOTE_PATH + TYPE_TEXT + COMMA_SEP +
                    PersonsContract.IMAGE_LOCAL_PATH + TYPE_TEXT + ")";

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "persons", ROUTE_PERSONS);
        sUriMatcher.addURI(ZoomleeProvider.CONTENT_AUTHORITY, "persons/*", ROUTE_PERSONS_ID);
    }

    @Override
    protected int getRouteItemsCode() {
        return ROUTE_PERSONS;
    }

    @Override
    protected int getRouteItemsIdCode() {
        return ROUTE_PERSONS_ID;
    }

    @Override
    protected String[] getAllColumnsProjection() {
        return PersonsContract.ALL_COLUMNS_PROJECTION;
    }

    @Override
    public int match(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override
    public String getEntityContentType() {
        return PersonsContract.CONTENT_TYPE;
    }

    @Override
    public String getEntityContentItemType() {
        return PersonsContract.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri getContentUri() {
        return PersonsContract.CONTENT_URI;
    }

    @Override
    public String getTableName() {
        return PersonsContract.TABLE_NAME;
    }

    @Override
    public String getCreateTableSQL() {
        return SQL_CREATE_TABLE;
    }

    /**
     * Columns supported by "persons" records.
     */
    public static class PersonsContract implements DataColumns {

        /**
         * Path component for "person"-type resources..
         */
        private static final String PATH = "persons";
        /**
         * MIME type for lists of persons.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.persons";
        /**
         * MIME type for individual persons.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.zoomlee.Zoomlee.provider.person";

        /**
         * Fully qualified URI for "person" resources.
         */
        public static final Uri CONTENT_URI =
                ZoomleeProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        /**
         * Table name where records are stored for "person" resources.
         */
        public static final String TABLE_NAME = "persons";

        /**
         * user's id, that contains this user
         */
        public static final String USER_ID = "user_id";
        /**
         * Person's name
         */
        public static final String NAME = "name";
        /**
         * Person's image url
         */
        public static final String IMAGE_REMOTE_PATH = "image_remote_path";
        public static final String IMAGE_LOCAL_PATH = "image_local_path";

        public static final String[] ALL_COLUMNS_PROJECTION = new String[]{
                _ID, REMOTE_ID, UPDATE_TIME, STATUS, USER_ID, NAME, IMAGE_REMOTE_PATH,
                IMAGE_LOCAL_PATH
        };
    }
}
