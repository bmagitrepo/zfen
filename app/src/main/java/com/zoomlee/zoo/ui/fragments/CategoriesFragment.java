package com.zoomlee.zoo.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.TouchViewDraggableManager;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.CategoriesDocAlertsDaoHelper;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.syncservice.SyncEvents;
import com.zoomlee.zoo.ui.activity.CreateEditDocActivity;
import com.zoomlee.zoo.ui.activity.DocumentsActivity;
import com.zoomlee.zoo.ui.activity.MainActivity;
import com.zoomlee.zoo.ui.adapters.CategoriesListAdapter;
import com.zoomlee.zoo.ui.adapters.CategoriesOrderListAdapter;
import com.zoomlee.zoo.ui.view.LoadingView;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.zoomlee.zoo.dao.CategoriesDocAlertsDaoHelper.CategoriesDocAlerts;
import static com.zoomlee.zoo.provider.helpers.CategoriesProviderHelper.CategoriesContract;
import static com.zoomlee.zoo.ui.fragments.NavigationDrawerFragment.PersonChangedMessage;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.02.15.
 */
public class CategoriesFragment extends ListFragment implements MainActivity.Editable {

    private List<CategoriesDocAlerts> items = new ArrayList<>();        // ListView items list
    private CategoriesDocAlertsDaoHelper categoriesDocAlertsDaoHelper;
    private DaoHelper<Category> categoryDaoHelper;
    private CategoriesListAdapter categoriesListAdapter;
    private CategoriesOrderListAdapter categoriesOrderListAdapter;
    private View stubLayout;
    private LoadingView loadView;
    private LoadDataAsyncTask loadTask;
    private View newDocumentBtn;
    private View listLayout;
    private boolean editMode;
    private boolean syncing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoriesDocAlertsDaoHelper = new CategoriesDocAlertsDaoHelper(getActivity());
        categoryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Category.class);
        categoriesListAdapter = new CategoriesListAdapter(getActivity(), items);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_categories, null);
        stubLayout = v.findViewById(R.id.noDataLayout);
        loadView = (LoadingView) v.findViewById(R.id.loading);
        listLayout = v.findViewById(R.id.listLayout);
        newDocumentBtn = v.findViewById(R.id.addNewBtn);
        newDocumentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDocument();
            }
        });
        return v;
    }

    private void createDocument(){
        CreateEditDocActivity.startActivity(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(categoriesListAdapter);
        getListView().setEmptyView(stubLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!editMode && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
            loadData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!editMode) {
            EventBus.getDefault().unregister(this);
            hideLoadingView();
            syncing = false;
            cancelTask();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GAUtil.getUtil().timeSpent(GAEvents.ACTION_CATEGORY_LIST);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (!editMode) {
            ZoomleeApp zoomleeApp = (ZoomleeApp) getActivity().getApplication();
            CategoriesDocAlerts item = items.get(position);
            DocumentsActivity.startActivityWithCategory(getActivity(), item.getCategoryRemoteId(), zoomleeApp.getSelectedPersonId());
        }
    }

    public void onEventMainThread(SyncEvents.SyncServiceStatus syncServiceStatus) {
        switch (syncServiceStatus.getStatus()) {
            case SyncEvents.SYNC_STARTED:
                // show load view only on first doc synchronization
                if (SharedPreferenceUtils.getUtils().getIntSetting(LastSyncTimeKeys.DOCUMENTS) <= 0) {
                    syncing = true;
                    showLoadingView();
                }
                break;
            case SyncEvents.SYNC_ERROR:
            case SyncEvents.SYNC_FINISHED:
                hideLoadingView();
                syncing = false;
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(PersonChangedMessage personChangedMessage) {
        if (!editMode) loadData();
    }

    public void onEventMainThread(Events.DocumentChanged event) {
        if (!editMode) loadData();
    }

    public void onEventMainThread(Events.CategoriesChanged event) {
        if (!editMode) loadData();
    }

    private void loadData() {
        cancelTask();
        loadTask = new LoadDataAsyncTask();
        loadTask.execute();
    }

    private void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    @Override
    public void onEdit() {
        hideLoadingView();
        editMode = true;

        // hide new document action btn
        newDocumentBtn.animate()
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        newDocumentBtn.setVisibility(View.GONE);
                    }
                });

        cancelTask();

        if (categoriesOrderListAdapter == null)
            categoriesOrderListAdapter = new CategoriesOrderListAdapter(getActivity(), new ArrayList<CategoriesDocAlerts>());
        categoriesOrderListAdapter.clear();
        categoriesOrderListAdapter.addAll(getItemsToOrder());
        getListView().setAdapter(categoriesOrderListAdapter);

        DynamicListView dynamicListView = (DynamicListView) getListView();
        dynamicListView.enableDragAndDrop();
        dynamicListView.setDraggableManager(new TouchViewDraggableManager(R.id.dragAndDropIv));
        dynamicListView.setBackgroundColor(getResources().getColor(R.color.item_pressed));
    }

    private List<CategoriesDocAlerts> getItemsToOrder() {
        List<CategoriesDocAlerts> itemsToOrder = new ArrayList<>();
        for (CategoriesDocAlerts item: items) itemsToOrder.add(new CategoriesDocAlerts(item));

        List<Category> emptyCategories = getEmptyCategories();
        for (Category category: emptyCategories) itemsToOrder.add(new CategoriesDocAlerts(category));

        Collections.sort(itemsToOrder, new Comparator<CategoriesDocAlerts>() {
            @Override
            public int compare(CategoriesDocAlerts lhs, CategoriesDocAlerts rhs) {
                return rhs.getCategoryWeight() - lhs.getCategoryWeight();
            }
        });

        return itemsToOrder;
    }

    private List<Category> getEmptyCategories() {
        String[] categoryIds = new String[items.size()];
        for (int i = 0; i < items.size(); i++)
            categoryIds[i] = String.valueOf(items.get(i).getCategoryRemoteId());
        String selection = CategoriesContract.TABLE_NAME + "." + CategoriesContract.STATUS + "=" + Category.STATUS_NORMAL + " AND "
                + CategoriesContract.TABLE_NAME + "." + CategoriesContract.REMOTE_ID + " NOT IN " + DBUtil.formatArgsAsSet(categoryIds);

        return categoryDaoHelper.getAllItems(getActivity(), selection, null, null);
    }

    @Override
    public void onSave() {
        items.clear();
        Context context = getActivity();
        List<CategoriesDocAlerts> orderedItems = categoriesOrderListAdapter.getItems();
        for (int i = 0; i < orderedItems.size(); i++) {
            CategoriesDocAlerts item = orderedItems.get(i);
            item.setCategoryWeight(orderedItems.size() - i);
            if (!item.getDocsTypesNames().isEmpty()) items.add(item);
            categoryDaoHelper.saveLocalChanges(context, item.getCategory());
        }

        categoriesListAdapter.clear();
        categoriesListAdapter.addAll(items);

        disableEditMode();
    }

    @Override
    public void onCancel() {
        disableEditMode();
    }

    private void disableEditMode() {
        DynamicListView dynamicListView = (DynamicListView) getListView();
        dynamicListView.disableDragAndDrop();
        dynamicListView.setBackgroundColor(Color.TRANSPARENT);
        editMode = false;
        showLoadingView();

        // show new document action btn
        newDocumentBtn.setVisibility(View.VISIBLE);
        newDocumentBtn.setAlpha(0.0f);
        newDocumentBtn.animate()
                .alpha(1.0f)
                .setListener(null);

        getListView().setAdapter(categoriesListAdapter);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            loadData();
        }
    }

    private void showLoadingView() {
        if (syncing && !editMode && items.size() == 0) {
            listLayout.setVisibility(View.GONE);
            if (loadView.getVisibility() != View.VISIBLE)
                loadView.show();
        }
    }

    private void hideLoadingView() {
        if (syncing && !editMode) {
            listLayout.setVisibility(View.VISIBLE);
            loadView.hide();
        }
    }

    private class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ZoomleeApp zoomleeApp = (ZoomleeApp) getActivity().getApplication();
            items = categoriesDocAlertsDaoHelper.getCategoriesDocAlerts(zoomleeApp.getSelectedPersonId());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            categoriesListAdapter.clear();
            categoriesListAdapter.addAll(items);
            categoriesListAdapter.notifyDataSetChanged();

            if (items.size() > 0) hideLoadingView();
            else showLoadingView();
        }
    }
}
