package com.zoomlee.zoo.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import retrofit.mime.TypedInput;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 23.01.15.
 */
public class FileUtil {

    public static final String TO_REMOVE_FOLDER = "/toremove/";

    public static void copyFromAssetsToData(Context context, String assetsFileName, File destFile) {
        try {
            destFile.getParentFile().mkdirs();
            destFile.createNewFile();
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(assetsFileName);
            OutputStream os = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
        } catch (Exception exc) {
            Log.e("FileUtils", "copyFromAssetsToData error", exc);
        }
    }

    public static String saveFileInternaly(Context context, String fileName, TypedInput typedInput) {
        File destFile = new File(context.getFilesDir(), fileName);
        InputStream is = null;
        try {
            OutputStream os = new FileOutputStream(destFile);
            is = typedInput.in();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
        } catch (IOException exc) {
            Log.e("FileUtils", "saveFileInternaly error", exc);
            return null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {
            }
        }
        return destFile.getAbsolutePath();
    }

    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {

        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            if (inputChannel != null)
                inputChannel.close();

            if (outputChannel != null)
                outputChannel.close();
        }
    }

    public static String writeBitmapToFile(Bitmap bitmap, File file) throws IOException {

        FileOutputStream fOut = new FileOutputStream(file);

        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
        return file.getAbsolutePath();
    }

    public static File fileFromContentUri(Context context, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor pathCursor = context.getContentResolver().query(uri, projection, null, null, null);
        File file = null;
        if (pathCursor != null) {
            if (pathCursor.moveToFirst()) {
                int filenameIndex = pathCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                String fileName = pathCursor.getString(filenameIndex);
                if (fileName == null) {
                    // this can be mail attachment or so
                    int nameIndex = pathCursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    String name = pathCursor.getString(nameIndex);

                    file = uriToFileWithName(context, name, uri);
                } else {
                    file = new File(fileName);
                }
            }

            pathCursor.close();
        }
        return file;
    }

    /**
     * Creates temp file and copies data into it from uri's input stream.
     *
     * @param context to use
     * @param name    of file to be created and returned
     * @param uri     to get input stream from
     * @return file or null if something went wrong
     */
    private static File uriToFileWithName(Context context, String name, Uri uri) {
        InputStream is = null;
        FileOutputStream os = null;
        String fullPath = null;
        try {
            fullPath = context.getFilesDir().getPath() + TO_REMOVE_FOLDER;
            new File(fullPath).mkdirs(); // create directory
            fullPath += name; // path with file name

            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(fullPath);

            byte[] buffer = new byte[4096];
            int count;

            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }

            os.close();
            is.close();
        } catch (Exception e) {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e1) {
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (Exception e1) {
                }
            }
        }
        if (fullPath != null) {
            return new File(fullPath);
        } else {
            return null;
        }
    }

    /**
     * Checks if file is to be deleted and if so deletes it.
     *
     * @param context to use
     * @param file to be checked
     */
    public static void deleteIfNeeded(Context context, File file) {
        File toRemoveDir = new File(context.getFilesDir().getPath() + TO_REMOVE_FOLDER);
        if (file.getParentFile() != null && file.getParentFile().equals(toRemoveDir)) {
            file.delete();
        }
    }

    public static File fileFromFileUri(Uri uri) {
        String fileName = uri.getPath();
        return new File(fileName);
    }

    public static File fileFromUri(Context context, Uri uri) {
        if (TextUtils.equals(uri.getScheme(), "file")) {
            return fileFromFileUri(uri);
        } else if (TextUtils.equals(uri.getScheme(), "content")) {
            return fileFromContentUri(context, uri);
        }
        return null;
    }

    public static void deleteFile(com.zoomlee.zoo.net.model.File file) {
        File androidFile = new File(file.getLocalPath());
        androidFile.delete();
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
