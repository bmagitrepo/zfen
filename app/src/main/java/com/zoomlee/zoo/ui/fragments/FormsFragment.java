package com.zoomlee.zoo.ui.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.helpers.FormLoader;
import com.zoomlee.zoo.provider.FilesProvider;
import com.zoomlee.zoo.provider.helpers.FormsProviderHelper.FormsContract;
import com.zoomlee.zoo.ui.activity.EditFormActivity;
import com.zoomlee.zoo.ui.activity.ImmigrationFormActivity;
import com.zoomlee.zoo.ui.adapters.FormsListAdapter;
import com.zoomlee.zoo.ui.view.FormItemView;
import com.zoomlee.zoo.ui.view.LoadingView;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.PrintUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.zoomlee.zoo.ui.fragments.NavigationDrawerFragment.PersonChangedMessage;

public class FormsFragment extends Fragment implements AdapterView.OnItemClickListener,
        View.OnTouchListener, AdapterView.OnItemLongClickListener, FormItemView.FormItemListener {

    private FormsListAdapter adapter;
    private LoadDataAsyncTask loadTask;
    private DaoHelper<Form> formDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Form.class);
    private ListView listView;
    private LoadingView loadingView;

    public static FormsFragment newInstance() {
        return new FormsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_form, null);
        listView = (ListView) v.findViewById(R.id.listView);
        loadingView = (LoadingView) v.findViewById(R.id.loading);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ZoomleeApp zoomleeApp = (ZoomleeApp) getActivity().getApplication();
        boolean allPersons = (zoomleeApp.getSelectedPersonId() == Person.ALL_ID);

        adapter = new FormsListAdapter(getActivity(), new ArrayList<Form>(),
                allPersons);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setOnTouchListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        cancelTask();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeveloperUtil.michaelLog("item click");
        if (adapter.getSelectedView() != null) {
            adapter.setSelectedView(null); // deselect actions
            return;
        }
        Form form = adapter.getItem(position);
        ImmigrationFormActivity.startImmigrationForm(getActivity(), form);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && adapter.getSelectedView() != null) {
            // we close actions on any touch
            DeveloperUtil.michaelLog("close " + adapter.getSelectedView());

            adapter.getSelectedView().closeActions(false);
        }
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        openActionsForView(view);
        return true;
    }

    private void openActionsForView(View view) {
        DeveloperUtil.michaelLog("open for " + view);

        adapter.setSelectedView((FormItemView) view);
        adapter.getSelectedView().openActions(this);
    }

    @Override
    public void onPrint(final Form form) {
        loadingView.show();
        listView.setEnabled(false);
        FormLoader formLoader = new FormLoader(getActivity(), form);
        formLoader.getPrintAblePdfFormAsync(new FormLoader.OnLoadCompleteListener() {
            @Override
            public void loadCompleted(File formFile) {
                Uri pdfFormUri = Uri.parse("content://" + FilesProvider.CACHE_DIR_AUTHORITY + "/"
                        + formFile.getName());
                PrintUtils.doPrint(getActivity(), formFile, pdfFormUri, form.getName());

                loadingView.hide();
                listView.setEnabled(true);
            }
        });
    }

    @Override
    public void onEdit(Form form) {
        FormLoader formLoader = new FormLoader(getActivity(), form);
        formLoader.preFillFields();
        EditFormActivity.startToEditForm(getActivity(), form);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PersonChangedMessage personChangedMessage) {
        loadData();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(Events.FormChanged event) {
        ZoomleeApp zoomleeApp = (ZoomleeApp) getActivity().getApplication();
        int selectedPerson = zoomleeApp.getSelectedPersonId();
        if (selectedPerson == Person.ALL_ID || selectedPerson == event.getForm().getLocalPersonId()) {
            if (adapter.getSelectedView() != null) {
                // when adapter will be rebuilt, we have to reopen previously opened document
                Form formToOpen = adapter.getSelectedView().getForm();
                if (!event.getForm().equals(formToOpen) || event.getStatus() != Events.DocumentChanged.DELETED) {
                    adapter.setOpenForm(formToOpen, this);
                }
            }
            loadData();
        }
    }

    private void loadData() {
        cancelTask();
        loadTask = new LoadDataAsyncTask();
        loadTask.execute();
    }

    private class LoadDataAsyncTask extends AsyncTask<Void, Void, List<Form>> {

        @Override
        protected void onPreExecute() {
            adapter.clear();
            loadingView.show();
            listView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected List<Form> doInBackground(Void... params) {
            ZoomleeApp zoomleeApp = (ZoomleeApp) getActivity().getApplication();
            int personId = zoomleeApp.getSelectedPersonId();

            String selection = null;
            String[] args = null;
            if (personId != Person.ALL_ID) {
                selection = FormsContract.PERSON_ID + " = ?";
                args = DBUtil.getArgsArray(personId);
            }

            return formDaoHelper.getAllItems(getActivity(), selection, args, null);
        }

        @Override
        protected void onPostExecute(List<Form> forms) {
            loadingView.hide();
            listView.setVisibility(View.VISIBLE);

            ZoomleeApp zoomleeApp = (ZoomleeApp) getActivity().getApplication();
            boolean allPersons = (zoomleeApp.getSelectedPersonId() == Person.ALL_ID);
            adapter.setAllPersons(allPersons);
            adapter.clear();
            adapter.addAll(forms);
            adapter.notifyDataSetChanged();

            // indicate we're finished
            loadTask = null;
        }
    }
}
