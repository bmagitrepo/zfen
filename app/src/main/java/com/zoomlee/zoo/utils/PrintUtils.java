package com.zoomlee.zoo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.print.PrintManager;
import android.support.v4.print.PrintHelper;

import com.zoomlee.zoo.ui.activity.PrintDialogActivity;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 5/27/15
 */
public class PrintUtils {

    /**
     *
     * @param activity
     * @param file
     * @param uri - FileContentProvider file uri
     */
    public static void doPrint(Activity activity, File file, Uri uri, String jobName){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (file.getAbsolutePath().endsWith(".png")) {
                PrintHelper photoPrinter = new PrintHelper(activity);
                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                try {
                    photoPrinter.printBitmap(jobName, uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                PrintManager printManager = (PrintManager) activity
                        .getSystemService(Context.PRINT_SERVICE);

                // Start a print job, passing in a PrintDocumentAdapter implementation
                // to handle the generation of a print document
                printManager.print(jobName, new MyPrintDocumentAdapter(file), null);
            }
        } else {
            Intent printIntent = new Intent(activity, PrintDialogActivity.class);
            String type = "application/pdf";
            if (file.getAbsolutePath().endsWith(".png")) type = "image/png";
            printIntent.setDataAndType(uri, type);
            printIntent.putExtra("title", jobName);
            activity.startActivityForResult(printIntent, RequestCodes.PRINT_REQUEST);
        }
    }
}
