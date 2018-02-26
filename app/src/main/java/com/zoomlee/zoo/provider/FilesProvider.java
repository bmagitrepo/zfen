package com.zoomlee.zoo.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.zoomlee.zoo.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 17.02.15.
 */
public class FilesProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".files_provider";
    public static final String FILE_DIR_AUTHORITY = AUTHORITY + "/files";
    public static final String CACHE_DIR_AUTHORITY = AUTHORITY + "/cache";

    private static final int ROUTE_FILES = 1;
    private static final int ROUTE_CACHE = 2;

    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "files/*", ROUTE_FILES);
        uriMatcher.addURI(AUTHORITY, "cache/*", ROUTE_CACHE);

        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {

        File parentDir = null;
        switch (uriMatcher.match(uri)) {
            case ROUTE_FILES:
                parentDir = getContext().getFilesDir();
            case ROUTE_CACHE:
                if (parentDir == null) parentDir = getContext().getCacheDir();
                String fileLocation = parentDir + File.separator + uri.getLastPathSegment();
                ParcelFileDescriptor pfd = ParcelFileDescriptor.open(new File(
                        fileLocation), ParcelFileDescriptor.MODE_READ_ONLY);
                return pfd;
            default:
                throw new FileNotFoundException("Unsupported uri: "
                        + uri.toString());
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s,
                      String[] as) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String s, String[] as1,
                        String s1) {
        return null;
    }
}
