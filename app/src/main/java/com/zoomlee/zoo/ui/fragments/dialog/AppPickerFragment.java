package com.zoomlee.zoo.ui.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.ui.view.AppItemView;
import com.zoomlee.zoo.ui.view.BottomSheet;
import com.zoomlee.zoo.utils.IntentUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * Author vbevans94.
 */
public class AppPickerFragment extends DialogFragment {

    private static final String TAG = AppPickerFragment.class.getSimpleName();
    private static final String ARG_APPS = "arg_apps";
    private static final String ARG_FILE = "arg_file";

    @BindView(R.id.list_apps)
    ListView listApps;

    @BindView(R.id.text_title)
    TextView textTitle;

    private ArrayAdapter adapter;

    /**
     * Starts open with/install to open dialog.
     *
     * @param manager to use
     * @param apps    to list to open file, or null to go in install with mode
     * @param file    to be opened
     */
    public static void show(FragmentManager manager, ArrayList<ResolveInfo> apps, File file) {
        AppPickerFragment fragment = new AppPickerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_APPS, apps);
        args.putParcelable(ARG_FILE, file);
        fragment.setArguments(args);
        fragment.show(manager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_dialog_app_chooser, null);
        ButterKnife.bind(this, view);

        ArrayList<ResolveInfo> apps = getArguments().getParcelableArrayList(ARG_APPS);
        if (apps == null) {
            textTitle.setText(R.string.title_install_to_open);
            File file = getArguments().getParcelable(ARG_FILE);
            assert file != null;
            adapter = new InstallAdapter(getActivity(), file.appsThatOpen());
        } else {
            textTitle.setText(R.string.title_open_with);
            adapter = new AppAdapter(getActivity(), apps);
        }
        listApps.setAdapter(adapter);

        return new BottomSheet(view);
    }

    private boolean isForInstall() {
        return getArguments().getParcelableArrayList(ARG_APPS) == null;
    }

    @OnItemClick(R.id.list_apps)
    @SuppressWarnings("unused")
    void onAppClicked(int position) {
        File file = getArguments().getParcelable(ARG_FILE);

        assert file != null;
        if (isForInstall()) {
            IntentUtils.openToInstall(getActivity(), ((File.App) adapter.getItem(position)).getAppId());
        } else {
            IntentUtils.openInApp(getActivity(), (ResolveInfo) adapter.getItem(position), file.getLocalPath(), file.getType().mimeType);
        }

        dismiss();
    }

    private static class AppAdapter extends ArrayAdapter<ResolveInfo> {

        private final LayoutInflater inflater;
        private final PackageManager packageManager;

        public AppAdapter(Context context, List<ResolveInfo> objects) {
            super(context, R.layout.item_app, objects);

            inflater = LayoutInflater.from(context);
            packageManager = context.getPackageManager();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.item_app, parent, false);
            }
            ResolveInfo info = getItem(position);
            AppItemView itemView = (AppItemView) view;
            itemView.bind(info.loadIcon(packageManager), info.loadLabel(packageManager));

            return view;
        }
    }

    private static class InstallAdapter extends ArrayAdapter<File.App> {

        private final LayoutInflater inflater;
        private final Resources resources;

        public InstallAdapter(Context context, List<File.App> objects) {
            super(context, R.layout.item_app, objects);

            inflater = LayoutInflater.from(context);
            resources = getContext().getResources();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.item_app, parent, false);
            }
            File.App app = getItem(position);
            AppItemView itemView = (AppItemView) view;
            itemView.bind(resources.getDrawable(app.getIconResId()), resources.getString(app.getTitleResId()));

            return view;
        }
    }


}
