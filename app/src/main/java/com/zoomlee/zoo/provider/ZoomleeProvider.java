package com.zoomlee.zoo.provider;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.zoomlee.zoo.BuildConfig;
import com.zoomlee.zoo.provider.helpers.BaseProviderHelper;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 20.01.15.
 */
public class ZoomleeProvider extends ContentProvider {

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    /**
     * Base URI. (content://com.zoomlee.Zoomlee.provider)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    DBOpenHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DBOpenHelper(getContext());
        return true;
    }

    /**
     * Determine the mime type for entry returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        BaseProviderHelper baseProviderHelper =
                ProviderHelpersContainer.getInstance().getMatchedProviderHelper(uri);

        if (baseProviderHelper == null)
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        return baseProviderHelper.getType(uri);
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all entries (/{table}) and individual entry by ID
     * (/{table}/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        BaseProviderHelper baseProviderHelper =
                ProviderHelpersContainer.getInstance().getMatchedProviderHelper(uri);

        if (baseProviderHelper == null)
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        return baseProviderHelper.query(getContext(), db, uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        BaseProviderHelper baseProviderHelper =
                ProviderHelpersContainer.getInstance().getMatchedProviderHelper(uri);

        if (baseProviderHelper == null)
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        return baseProviderHelper.insert(getContext(), db, uri, values);
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        BaseProviderHelper baseProviderHelper =
                ProviderHelpersContainer.getInstance().getMatchedProviderHelper(uri);

        if (baseProviderHelper == null)
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        return baseProviderHelper.delete(getContext(), db, uri, selection, selectionArgs);
    }

    /**
     * Update an entry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        BaseProviderHelper baseProviderHelper =
                ProviderHelpersContainer.getInstance().getMatchedProviderHelper(uri);

        if (baseProviderHelper == null)
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        return baseProviderHelper.update(getContext(), db, uri, values, selection, selectionArgs);
    }

    public static class Routes {
        /**
         * URI ID for route: /categories
         */
        public static final int ROUTE_CATEGORIES = 1;
        /**
         * URI ID for route: /categories/{ID}
         */
        public static final int ROUTE_CATEGORIES_ID = 2;
        /**
         * URI ID for route: /category2documents_types
         */
        public static final int ROUTE_CATEGORY2DOCUMENTS_TYPES = 3;
        /**
         * URI ID for route: /category2documents_types/{ID}
         */
        public static final int ROUTE_CATEGORY2DOCUMENTS_TYPES_ID = 4;
        /**
         * URI ID for route: /colors
         */
        public static final int ROUTE_COLORS = 5;
        /**
         * URI ID for route: /colors/{ID}
         */
        public static final int ROUTE_COLORS_ID = 6;
        /**
         * URI ID for route: /countries
         */
        public static final int ROUTE_COUNTRIES = 7;
        /**
         * URI ID for route: /countries/{ID}
         */
        public static final int ROUTE_COUNTRIES_ID = 8;
        /**
         * URI ID for route: /documents
         */
        public static final int ROUTE_DOCUMENTS = 9;
        /**
         * URI ID for route: /documents/{ID}
         */
        public static final int ROUTE_DOCUMENTS_ID = 10;
        /**
         * URI ID for route: /documents_types2fields_types
         */
        public static final int ROUTE_DOCUMENTS_TYPES2FIELD_TYPES = 11;
        /**
         * URI ID for route: /documents_types2fields_types/{ID}
         */
        public static final int ROUTE_DOCUMENTS_TYPES2FIELD_TYPES_ID = 12;
        /**
         * URI ID for route: /documents_types_groups
         */
        public static final int ROUTE_DOCUMENTS_TYPES_GROUPS = 13;
        /**
         * URI ID for route: /documents_types_groups/{ID}
         */
        public static final int ROUTE_DOCUMENTS_TYPES_GROUPS_ID = 14;
        /**
         * URI ID for route: /documents_types
         */
        public static final int ROUTE_DOCUMENTS_TYPES = 15;
        /**
         * URI ID for route: /documents_types/{ID}
         */
        public static final int ROUTE_DOCUMENTS_TYPES_ID = 16;
        /**
         * URI ID for route: /fields
         */
        public static final int ROUTE_FIELDS = 17;
        /**
         * URI ID for route: /fields/{ID}
         */
        public static final int ROUTE_FIELDS_ID = 18;
        /**
         * URI ID for route: /field_types
         */
        public static final int ROUTE_FIELD_TYPES = 19;
        /**
         * URI ID for route: /field_types/{ID}
         */
        public static final int ROUTE_FIELD_TYPES_ID = 20;
        /**
         * URI ID for route: /files
         */
        public static final int ROUTE_FILES = 21;
        /**
         * URI ID for route: /files/{ID}
         */
        public static final int ROUTE_FILES_ID = 22;
        /**
         * URI ID for route: /file_types
         */
        public static final int ROUTE_FILE_TYPES = 23;
        /**
         * URI ID for route: /file_types/{ID}
         */
        public static final int ROUTE_FILE_TYPES_ID = 24;
        /**
         * URI ID for route: /persons
         */
        public static final int ROUTE_PERSONS = 25;
        /**
         * URI ID for route: /persons/{ID}
         */
        public static final int ROUTE_PERSONS_ID = 26;
        /**
         * URI ID for route: /rest_tasks
         */
        public static final int ROUTE_REST_TASKS = 27;
        /**
         * URI ID for route: /rest_tasks/{ID}
         */
        public static final int ROUTE_REST_TASKS_ID = 28;
        /**
         * URI ID for route: /full_documents
         */
        public static final int ROUTE_FULL_DOCUMENTS = 29;
        /**
         * URI ID for route: /full_documents/{ID}
         */
        public static final int ROUTE_FULL_DOCUMENTS_ID = 30;
        /**
         * URI ID for route: /full_documents
         */
        public static final int ROUTE_CATEGORY_DOC_ALERTS = 31;
        /**
         * URI ID for route: /alerts
         */
        public static final int ROUTE_ALERTS = 32;
        /**
         * URI ID for route: /fields_history
         */
        public static final int ROUTE_FIELDS_HISTORY = 33;
        /**
         * URI ID for route: /alerts
         */
        public static final int ROUTE_TAGS = 34;
        /**
         * URI ID for route: /fields_history
         */
        public static final int ROUTE_TAGS_ID = 35;
        /**
         * URI ID for route: /alerts
         */
        public static final int ROUTE_TAGS2DOCUMENTS = 36;
        /**
         * URI ID for route: /fields_history
         */
        public static final int ROUTE_TAGS2DOCUMENTS_ID = 37;
        /**
         * URI ID for route: /tags_doc_alerts
         */
        public static final int ROUTE_TAGS_DOC_ALERTS = 38;
        /**
         * URI ID for route: /tag_documents
         */
        public static final int ROUTE_TAG_DOCUMENTS = 39;
        /**
         * URI ID for route: /alerts
         */
        public static final int ROUTE_TAX = 40;
        /**
         * URI ID for route: /fields_history
         */
        public static final int ROUTE_TAX_ID = 41;
        /**
         * URI ID for route: /forms
         */
        public static final int ROUTE_FORMS = 42;
        /**
         * URI ID for route: /forms/{ID}
         */
        public static final int ROUTE_FORMS_ID = 43;
        /**
         * URI ID for route: /form_fields
         */
        public static final int ROUTE_FORM_FIELDS = 44;
        /**
         * URI ID for route: /form_fields/{ID}
         */
        public static final int ROUTE_FORM_FIELDS_ID = 45;
    }
}
