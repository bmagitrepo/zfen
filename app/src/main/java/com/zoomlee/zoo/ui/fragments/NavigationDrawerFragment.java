package com.zoomlee.zoo.ui.fragments;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.AlertsDaoHelper;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.provider.helpers.BaseProviderHelper;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.syncservice.SyncEvents;
import com.zoomlee.zoo.ui.CustomDrawerLayout;
import com.zoomlee.zoo.ui.activity.CreateEditPersonActivity;
import com.zoomlee.zoo.ui.view.AnyProgressView;
import com.zoomlee.zoo.ui.view.NavigationDrawerItemView;
import com.zoomlee.zoo.ui.view.PersonView;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.TimeUtil;
import com.zoomlee.zoo.utils.Util;

import java.util.List;

import de.greenrobot.event.EventBus;

import static com.zoomlee.zoo.dao.AlertsDaoHelper.Alert;
import static com.zoomlee.zoo.provider.helpers.PersonsProviderHelper.PersonsContract;
import static com.zoomlee.zoo.utils.Events.DocumentChanged;
import static com.zoomlee.zoo.utils.Events.PersonChanged;


public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final long MIN_SYNCING_STATUS_DURATION = 500l;//0.5 seconds

    private NavigationDrawerCallbacks mCallbacks;
    public static final int DOCUMENTS_MI = 0;
    public static final int TAGS_MI = DOCUMENTS_MI + 1;
    public static final int FORMS_MI = TAGS_MI + 1;
    public static final int NOTIFICATIONS_MI = FORMS_MI + 1;
    public static final int MY_TRIPS_MI = NOTIFICATIONS_MI + 1;
    public static final int BE_SAFE_MI = MY_TRIPS_MI + 1;
    public static final int SETTINGS_MI = BE_SAFE_MI + 1;

    public static final int COMMUNITY_MI = SETTINGS_MI + 1;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private CustomDrawerLayout mDrawerLayout;

    private View mFragmentContainerView;
    private int mCurrentSelectedPosition = DOCUMENTS_MI;
    private int mPreviousPosition;
    private boolean mFromSavedInstanceState;

    private NavigationDrawerItemView[] itemViews = new NavigationDrawerItemView[COMMUNITY_MI + 1];
    private boolean mUserLearnedDrawer;
    private HorizontalScrollView personContainerScroll;
    private LinearLayout personContainer;
    private AnyProgressView progressLoading;

    private Handler handler;
    private long syncingStatusStartTime;
    private LoadAlertsAsyncTask loadAlertsTask;
    private UpdatePersonsAsyncTask loadPersonsTask;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferenceUtils spUtil = SharedPreferenceUtils.getUtils();
        mUserLearnedDrawer = spUtil.isUserLearnedDrawer();

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
        mPreviousPosition = mCurrentSelectedPosition;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(mCurrentSelectedPosition);
        }

        itemViews[mCurrentSelectedPosition].setChecked(true);
    }

    public void setDrawerLockMode(int lockMode) {
        mDrawerLayout.setDrawerLockMode(lockMode);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DeveloperUtil.michaelLog();
        final View mView = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        personContainerScroll = (HorizontalScrollView) mView.findViewById(R.id.personContainerScroll);
        personContainer = (LinearLayout) mView.findViewById(R.id.personContainer);

        progressLoading = (AnyProgressView) mView.findViewById(R.id.progress_loading);

        itemViews[DOCUMENTS_MI] = (NavigationDrawerItemView) mView.findViewById(R.id.documentsMi);
        itemViews[TAGS_MI] = (NavigationDrawerItemView) mView.findViewById(R.id.tagsMi);
        itemViews[FORMS_MI] = (NavigationDrawerItemView) mView.findViewById(R.id.formsMi);
        itemViews[NOTIFICATIONS_MI] = (NavigationDrawerItemView) mView.findViewById(R.id.notificationsMi);
        itemViews[MY_TRIPS_MI] = (NavigationDrawerItemView) mView.findViewById(R.id.myTripsMi);
        itemViews[BE_SAFE_MI] = (NavigationDrawerItemView) mView.findViewById(R.id.beSafeMi);
        itemViews[SETTINGS_MI] = (NavigationDrawerItemView) mView.findViewById(R.id.settingsMi);
        itemViews[COMMUNITY_MI] = (NavigationDrawerItemView) mView.findViewById(R.id.communityMi);

        for (NavigationDrawerItemView item : itemViews)
            item.setOnClickListener(onMenuItemClickListener);

        mView.findViewById(R.id.addPerson).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPerson();
            }
        });

        return mView;
    }

    public void addPerson() {
        //if (BillingUtils.canStart(getActivity(), BillingUtils.ActionType.ADD_PERSON)) {
            CreateEditPersonActivity.startToCreatePerson(getActivity());
     //   }
    }

    private View.OnClickListener personClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectPerson((PersonView) v, false);
        }
    };

    /**
     * Try to update existing views and remove unused
     * @param persons - Person ME (default user) and list of custom persons
     */
    private void updatePersonViews(List<Person> persons) {
        int curCount = personContainer.getChildCount() - 1;
        int personsSize = persons.size();
        int i = 0;
        PersonView curentView = null;

        if (curCount > personsSize) {
            personContainer.removeViews(personsSize, curCount - personsSize);
            curCount = personsSize;
        }

        for (; i < curCount; i++) {
            curentView = (PersonView) personContainer.getChildAt(i);
            curentView.setPerson(persons.get(i));
            curentView.setSelected(false);
        }
        for (; i < personsSize; i++) {
            curentView = createPersonView(persons.get(i));
            personContainer.addView(curentView, curCount++);
        }

        if (personsSize > 1)
            personContainer.addView(createPersonView(Person.ALL), 0);

        ZoomleeApp app = (ZoomleeApp) getActivity().getApplication();
        PersonView personView = (PersonView) personContainer.findViewWithTag(app.getSelectedPersonId());
        selectPerson(personView, true);
    }

    private void selectPerson(PersonView view, boolean scrollTo) {
        ZoomleeApp app = (ZoomleeApp) getActivity().getApplication();
        View prevPersonView = personContainer.findViewWithTag(app.getSelectedPersonId());
        if (prevPersonView != null)
            prevPersonView.setSelected(false);
        if (view == null)
            view = (PersonView) personContainer.findViewWithTag(Person.ME_ID);
        app.setSelectedPersonId(view.getPersonId());
        app.setSelectedPerson(view.getPerson());
        view.setSelected(true);
        mCallbacks.onPersonSelected();

        loadAlerts();

        EventBus.getDefault().post(new PersonChangedMessage());

        if (scrollTo)
            scrollToPersonView(view);
    }

    private void scrollToPersonView(final PersonView personView) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                int index = personContainer.indexOfChild(personView);
                int scrollPosition = index * personView.getWidth()
                        - personContainerScroll.getWidth() / 2 + personView.getWidth() / 2;
                personContainerScroll.smoothScrollTo(scrollPosition, 0);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        handler = new Handler();
        loadPersons();

        EventBus.getDefault().registerSticky(this);
        RestTaskPoster.postTask(getActivity(), new RestTask(RestTask.Types.STATIC_DATA_GET), false);
        RestTaskPoster.postTask(getActivity(), new RestTask(RestTask.Types.USER_DATA_GET), false);
        RestTaskPoster.postTask(getActivity(), new RestTask(RestTask.Types.USER_GET), true);
        if (!Util.hasInternetConnection(getActivity())) {
            onEventMainThread(new SyncEvents.SyncServiceStatus(SyncEvents.SYNC_STARTED));
            onEventMainThread(new SyncEvents.SyncServiceStatus(SyncEvents.SYNC_ERROR));
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);

        cancelLoadPersons();
        cancelLoadAlerts();
        super.onPause();
    }

    private PersonView createPersonView(Person person) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        PersonView personView = (PersonView) inflater.inflate(R.layout.include_person_view, personContainer, false);
        personView.setPerson(person);
        personView.setOnClickListener(personClickListener);
        return personView;
    }

    private View.OnClickListener onMenuItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.documentsMi:
                    selectItem(DOCUMENTS_MI);
                    break;
                case R.id.tagsMi:
                    selectItem(TAGS_MI);
                    break;
                case R.id.formsMi:
                    selectItem(FORMS_MI);
                    break;
                case R.id.notificationsMi:
                    selectItem(NOTIFICATIONS_MI);
                    break;
                case R.id.myTripsMi:
                    selectItem(MY_TRIPS_MI);
                    break;
                case R.id.beSafeMi:
                    selectItem(BE_SAFE_MI);
                    break;
                case R.id.settingsMi:
                    selectItem(SETTINGS_MI);
                    break;
                case R.id.communityMi:
                    if (mDrawerLayout != null) {
                        mDrawerLayout.closeDrawer(mFragmentContainerView);
                    }
                    if (mCallbacks != null) {
                        mCallbacks.onNavigationDrawerItemSelected(COMMUNITY_MI);
                    }
                    break;
            }
        }
    };

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public int getPreviousPosition() {
        return mPreviousPosition;
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, CustomDrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setView(personContainerScroll);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(), mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                ZoomleeApp app = (ZoomleeApp) getActivity().getApplication();
                PersonView personView = (PersonView) personContainer.findViewWithTag(app.getSelectedPersonId());
                scrollToPersonView(personView);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mFragmentContainerView);
            }
        });

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void selectItem(int position) {
        if (mCurrentSelectedPosition == position) {
            if (mDrawerLayout != null) {
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            }
            return;
        }
        mPreviousPosition = mCurrentSelectedPosition; // remember previous position
        itemViews[mCurrentSelectedPosition].setChecked(false);
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }

        itemViews[position].setChecked(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;

        progressLoading.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onEventMainThread(SyncEvents.SyncServiceStatus syncServiceStatus) {
        switch (syncServiceStatus.getStatus()) {
            case SyncEvents.SYNC_STARTED:
                syncingStatusStartTime = System.currentTimeMillis();
                progressLoading.setProgressFrame(getResources().getDrawable(R.drawable.progress_frame));
                progressLoading.start();

                break;
            case SyncEvents.SYNC_ERROR:
                long delayTime = Math.max(0,
                        MIN_SYNCING_STATUS_DURATION - (System.currentTimeMillis() - syncingStatusStartTime));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressLoading.setProgressFrame(getResources().getDrawable(R.drawable.error_frame));
                        progressLoading.start();
                    }
                }, delayTime);
                break;
            case SyncEvents.SYNC_FINISHED:
                progressLoading.stop();
                break;
            default:
                break;
        }
    }

    public int getCurrentPosition() {
        return mCurrentSelectedPosition;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final PersonChanged event) {
        Person changedPerson = event.getPerson();
        PersonView personView = (PersonView) personContainer.findViewWithTag(changedPerson.getId());
        switch (event.getStatus()) {
            case PersonChanged.UPDATED:
                if (personView != null) {
                    personView.setPerson(changedPerson);
                    personContainer.requestLayout();
                } else {
                    loadPersons();
                }
                break;
            case PersonChanged.DELETED:
                if (personView != null) {
                    ZoomleeApp app = (ZoomleeApp) getActivity().getApplication();
                    if (app.getSelectedPersonId() == personView.getPersonId()) {
                        selectPerson((PersonView) personContainer.findViewWithTag(Person.ME_ID), true);
                    }

                    if (personContainer.getChildCount() == 4)
                        personContainer.removeViewAt(0);
                    personContainer.removeView(personView);
                    personContainer.requestLayout();
                }
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(DocumentChanged event) {
        loadAlerts();
    }

    private void loadPersons() {
        cancelLoadPersons();
        loadPersonsTask = new UpdatePersonsAsyncTask();
        loadPersonsTask.execute();
    }

    private void cancelLoadPersons() {
        if (loadPersonsTask != null) {
            loadPersonsTask.cancel(true);
            loadPersonsTask = null;
        }
    }

    private void loadAlerts() {
        cancelLoadAlerts();
        loadAlertsTask = new LoadAlertsAsyncTask();
        loadAlertsTask.execute();
    }

    private void cancelLoadAlerts() {
        if (loadAlertsTask != null) {
            loadAlertsTask.cancel(true);
            loadAlertsTask = null;
        }
    }

    public interface NavigationDrawerCallbacks {

        void onNavigationDrawerItemSelected(int position);

        void onPersonSelected();
    }

    public static class PersonChangedMessage {
    }

    private class LoadAlertsAsyncTask extends AsyncTask<Void, Void, List<Alert>> {

        @Override
        protected List<Alert> doInBackground(Void... params) {
            AlertsDaoHelper alertsDaoHelper = new AlertsDaoHelper(getActivity());
            ZoomleeApp zoomleeApp = (ZoomleeApp) getActivity().getApplication();

            return alertsDaoHelper.getAlerts((int) TimeUtil.getServerEndDayTimestamp(), zoomleeApp.getSelectedPersonId());
        }

        @Override
        protected void onPostExecute(List<Alert> alerts) {
            int newAlertsCount = 0;
            for (Alert alert : alerts) {
                if (!alert.isViewed()) newAlertsCount++;
            }

            itemViews[DOCUMENTS_MI].setUpdatesCount(alerts.size());
            itemViews[NOTIFICATIONS_MI].setUpdatesCount(newAlertsCount);
        }
    }

    private class UpdatePersonsAsyncTask extends AsyncTask<Void, Void, List<Person>> {
        @Override
        protected List<Person> doInBackground(Void... params) {
            DaoHelper<Person> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
            String selection = BaseProviderHelper.DataColumns.STATUS + "=1";
            String sortOrder = PersonsContract.NAME + " COLLATE NOCASE ASC";
            List<Person> persons = daoHelper.getAllItems(getActivity(), selection, null, sortOrder);
            Person me = SharedPreferenceUtils.getUtils().getUserSettings();
            persons.add(0, me);
            return persons;
        }

        @Override
        protected void onPostExecute(List<Person> persons) {
            updatePersonViews(persons);
        }
    }
}
