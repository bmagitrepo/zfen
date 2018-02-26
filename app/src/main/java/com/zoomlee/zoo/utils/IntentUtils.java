package com.zoomlee.zoo.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.provider.FilesProvider;
import com.zoomlee.zoo.ui.activity.AttachmentActivity;
import com.zoomlee.zoo.ui.fragments.dialog.AppPickerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/14/15
 */
public class IntentUtils {
    public static final String EXTRA_LOGIN = "zoomlee_extra_login";
    public static final String EXTRA_OPEN_WITH_PIN = "EXTRA_OPEN_WITH_PIN";
    public static final String EXTRA_CATEGORY_SELECTED = "zoomlee_extra_category_selected";
    public static final String EXTRA_DOC_TYPE_SELECTED = "zoomlee_extra_type_selected";
    public static final String EXTRA_ACTION_TYPE = "extra_action_type";

    /**
     * Gets list of available apps that can open this mime type.
     *
     * @param context  to use
     * @param mimeType to open
     * @return list of apps as {@link ResolveInfo}
     */
    private static ArrayList<ResolveInfo> appsToOpen(Context context, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(mimeType);
        List<ResolveInfo> otherApps = context.getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<ResolveInfo> apps = new ArrayList<>();
        for (ResolveInfo otherApp : otherApps) {
            if (!otherApp.activityInfo.applicationInfo.packageName.equals(context.getPackageName())) {
                apps.add(otherApp);
            }
        }

        return apps;
    }

    /**
     * Opens activity from given resolve info.
     *
     * @param context     to use
     * @param resolveInfo to open
     * @param path        to file to be opened
     * @param mimeType    of the file
     */
    public static void openInApp(Context context, ResolveInfo resolveInfo, String path, String mimeType) {
        Uri fileUri = Uri.parse("content://" + FilesProvider.FILE_DIR_AUTHORITY + "/"
                + new java.io.File(path).getName());

        ActivityInfo activity = resolveInfo.activityInfo;
        ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setComponent(name);
        intent.setDataAndType(fileUri, mimeType);

        context.startActivity(intent);
    }

    /**
     * Opens Google Play to install app.
     *
     * @param context        to use
     * @param appPackageName to be installed
     */
    public static void openToInstall(Context context, String appPackageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    /**
     * Open file in the external application.
     *
     * @param activity     to open from
     * @param file         to be opened
     * @param documentName where this if file from
     * @return fileLoader if file is not openable by attachment activity or null otherwise. User is
     * responsible for calling {@link FileLoader#onStop()} in the corresponding component callback.
     */
    public static FileLoader openFile(final AppCompatActivity activity, final File file, String documentName) {
        if (file.getType() == null || file.isOpenable()) {
            // in case of type == null, we have to open it and show error image
            AttachmentActivity.startWithFile(activity, file, documentName);
            return null;
        } else {
            FileLoader fileLoader = new FileLoader(activity, file, new FileLoader.FileLoadListener() {
                private AlertDialog loadingDialog;
                private boolean dismissed;

                @Override
                public void onFileLoaded(File file) {
                    if (dismissed) {
                        // if user dismissed this dialog we just don't touch him
                        return;
                    }

                    dismiss();

                    String mimeType = file.getType().mimeType;

                    ArrayList<ResolveInfo> apps = IntentUtils.appsToOpen(activity, mimeType);
                    if (apps.isEmpty()) {
                        // open to install
                        AppPickerFragment.show(activity.getSupportFragmentManager(), null, file);
                    } else if (apps.size() == 1) {
                        IntentUtils.openInApp(activity, apps.get(0), file.getLocalPath(), mimeType);
                    } else {
                        AppPickerFragment.show(activity.getSupportFragmentManager(), apps, file);
                    }
                }

                private void dismiss() {
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                }

                @Override
                public void onLoadStarted() {
                    loadingDialog = new AlertDialog.Builder(activity)
                            .setView(R.layout.dialog_loading)
                            .setTitle(R.string.title_loading)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    dismissed = true;
                                }
                            })
                            .show();
                }

                @Override
                public void onFileGone() {
                    dismiss();
                }
            });
            // it started living, the same as activity started
            fileLoader.onStart();

            return fileLoader;
        }
    }
}
