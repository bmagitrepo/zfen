package com.zoomlee.zoo.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.AlertsDaoHelper;
import com.zoomlee.zoo.ui.activity.DocumentDetailsActivity;
import com.zoomlee.zoo.ui.adapters.AlertsListAdapter;
import com.zoomlee.zoo.ui.adapters.IncitationsAdapter;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.zoomlee.zoo.dao.AlertsDaoHelper.Alert;
import static com.zoomlee.zoo.ui.fragments.NavigationDrawerFragment.PersonChangedMessage;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.02.15.
 */
public class AlertsFragment extends ListFragment {

    private AlertsDaoHelper alertsDaoHelper;
    private AlertsListAdapter adapter;
    private View stubLayout;
    private DataLoadAsyncTask loadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alertsDaoHelper = new AlertsDaoHelper(getActivity());
        adapter = new AlertsListAdapter(getActivity(), new ArrayList<Alert>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alerts, null);
        stubLayout = v.findViewById(R.id.noDataLayout);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(IncitationsAdapter.wrap(adapter, adapter));

        getListView().setEmptyView(stubLayout);
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
        GAUtil.getUtil().timeSpent(GAEvents.ACTION_NOTIFICATION_SECTION);
    }

    private void loadData() {
        cancelTask();
        loadTask = new DataLoadAsyncTask();
        loadTask.execute();
    }

    private void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // retrieve theListView item
        Alert item = (Alert) l.getAdapter().getItem(position);
        if (!item.isViewed()) {
            item.setViewed(true);
            alertsDaoHelper.markAlertViewed(item);
        }
        DocumentDetailsActivity.startActivity(getActivity(), item.getDocumentId());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PersonChangedMessage personChangedMessage) {
        loadData();
    }

    private class DataLoadAsyncTask extends AsyncTask<Void, Void, List<Alert>> {

        @Override
        protected void onPreExecute() {
            adapter.showIncitations(false);
        }

        @Override
        protected List<Alert> doInBackground(Void... params) {
            ZoomleeApp zoomleeApp = (ZoomleeApp) getActivity().getApplication();
            return alertsDaoHelper.getAlerts((int) TimeUtil.getServerEndDayTimestamp(), zoomleeApp.getSelectedPersonId());
        }

        @Override
        protected void onPostExecute(List<Alert>values) {
            adapter.clear();
            adapter.addAll(values);
            adapter.showIncitations(true);
            adapter.notifyDataSetChanged();

            loadTask = null;
        }
    }

}
