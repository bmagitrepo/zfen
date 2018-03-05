package com.zoomlee.zoo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.ui.CustomDrawerLayout;
import com.zoomlee.zoo.ui.fragments.AlertsFragment;
import com.zoomlee.zoo.ui.fragments.BeSafeFragment;
import com.zoomlee.zoo.ui.fragments.CategoriesFragment;
import com.zoomlee.zoo.ui.fragments.FormsFragment;
import com.zoomlee.zoo.ui.fragments.FragmentWithImagePicker;
import com.zoomlee.zoo.ui.fragments.MyTripsFragment;
import com.zoomlee.zoo.ui.fragments.NavigationDrawerFragment;
import com.zoomlee.zoo.ui.fragments.SettingsFragment;
import com.zoomlee.zoo.ui.fragments.TagsFragment;
import com.zoomlee.zoo.ui.fragments.dialog.ImagePickerFragment;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends SecuredActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks
        , ImagePickerFragment.OnImagePickedListener {


    private CustomDrawerLayout drawerLayout;

    private NavigationDrawerFragment menu;

    private CategoriesFragment documentsFragment;
    private Fragment tagsFragment;
    private Fragment formsFragment;
    private MyTripsFragment myTripsFragment;
    private AlertsFragment notificationsFragment;
    private BeSafeFragment beSafeFragment;
    private SettingsFragment settingsFragment;
    private boolean editMode;
    private Menu optionMenu;

    public static void startActivity(Context ctx, boolean withPin) {
        Intent intent = getIntentForStart(ctx, withPin);
        ctx.startActivity(intent);
    }

    public static Intent getIntentForStart(Context ctx, boolean withPin) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, withPin);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomizedView(R.layout.activity_main);

        menu = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        drawerLayout = (CustomDrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                GAUtil.getUtil().timeSpent(GAEvents.ACTION_SIDE_BAR);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        menu.setUp(R.id.navigation_drawer,
                drawerLayout);

        ZoomleeApp app = (ZoomleeApp) getApplication();
        if (app.getSelectedPerson() == null)
            app.setSelectedPerson(SharedPreferenceUtils.getUtils().getUserSettings());
        DeveloperUtil.michaelLog(app.getSelectedPersonId());

        DeveloperUtil.michaelLog("zoomle_key - " + SharedPreferenceUtils.getUtils().getPrivateKey());

        if (!getIntent().getBooleanExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, true)) {
            // open without pin for now is only on the first launch hence we will not be
            // prompted to enter pin hence we will not have "after pin" event when we have
            // to ask to renew, hence we need to do this now
            drawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryToRequestSubscription();
                }
            }, 700);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getToolbar().setNavigationIcon(R.drawable.ic_hamburger);
    }

    @Override
    protected void onStart() {
        if (getIntent().getBooleanExtra(EXTRA_PIN_FAILED, false)) {
            unsecure();
            finish();
        }

        super.onStart();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        String action = null;
        setShowShadow(position != NavigationDrawerFragment.BE_SAFE_MI);
        switch (position) {
            case NavigationDrawerFragment.DOCUMENTS_MI:
                setContentDocuments();
                break;
            case NavigationDrawerFragment.TAGS_MI:
                setContentTags();
                action = GAEvents.ACTION_TAGS_SECTION;
                break;
            case NavigationDrawerFragment.FORMS_MI:
                setContentForms();
                action = GAEvents.ACTION_FORMS_SECTION;
                break;
            case NavigationDrawerFragment.NOTIFICATIONS_MI:
                setContentNotifications();
                action = GAEvents.ACTION_NOTIFICATION_SECTION;
                break;
            case NavigationDrawerFragment.MY_TRIPS_MI:
                setContentMyTrips();
                action = GAEvents.ACTION_MY_TRIPS;
                break;
            case NavigationDrawerFragment.BE_SAFE_MI:
                setContentBeSafe();
                action = GAEvents.ACTION_BESAFE_ACCESS;
                break;
            case NavigationDrawerFragment.SETTINGS_MI:
                setContentSettings();
                action = GAEvents.ACTION_SETTINGS_ACCESS;
                break;
            case NavigationDrawerFragment.COMMUNITY_MI:
                setContentCommunity();
                action = GAEvents.ACTION_COMMUNITY_ACCESS;
                break;
        }

        if (getCurrentFragment() instanceof Editable)
            showOption(R.id.action_edit);
        else
            hideOption(R.id.action_edit);

        if (action != null)
            GAUtil.getUtil().screenAccess(action);
    }

    @Override
    protected void cover() {
        super.cover();
        if (menu.isDrawerOpen()) {
            drawerLayout.closeDrawers();
        }
    }

    @Override
    public void onPersonSelected() {
        Fragment curFragment = getCurrentFragment();
        if (curFragment == null)
            return;

        if (curFragment instanceof CategoriesFragment || curFragment instanceof AlertsFragment
                || curFragment instanceof TagsFragment || curFragment instanceof FormsFragment) {
            updateABAvatar();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //TODO remove this
        try {
            InputStream is = new FileInputStream(getDatabasePath("internal.db"));
            OutputStream os = new FileOutputStream(new File("/sdcard/internal.db"));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int fragmentRequestCode = requestCode & 0xffff;
        switch (fragmentRequestCode) {
            case RequestCodes.PICK_FROM_CAMERA:
            case RequestCodes.PICK_FROM_GALLERY:
            case RequestCodes.CROP_IMAGE_REQUEST:
                unpin();
                return;
        }

        switch (requestCode) {
            case RequestCodes.GET_COUNTRY:
                try {
                    SettingsFragment fragment = (SettingsFragment) getCurrentFragment();
                    fragment.onActivityResult(requestCode, resultCode, data);
                } catch (ClassCastException e) {
                    throw new ClassCastException("Current fragment can't retrieve country");
                }
                unpin();
                break;
            case RequestCodes.DOCUMENTS_ACTIVITY:
            case RequestCodes.SET_PIN_ACTIVITY:
            case RequestCodes.CREATE_DOCUMENT:
            case RequestCodes.DOCUMENT_DETAILS_ACTIVITY:
            case RequestCodes.CATEGORY_TYPES_ACTIVITY:
            case RequestCodes.CHANGE_EMAIL_PHONE:
            case RequestCodes.MANAGE_PERSONS:
            case RequestCodes.TAGS_SETTINGS_ACTIVITY:
            case RequestCodes.INVITE_ACTIVITY:
            case RequestCodes.CREATE_EDIT_TAX:
            case RequestCodes.PRINT_REQUEST:
            case RequestCodes.EDIT_FORM_ACTIVITY:
                unpin();
                break;
            case RequestCodes.SUBSCRIPTION_ACTIVITY:
                unpin();
                BillingUtils.IntendedAction intendedAction = BillingUtils.intendedAction(resultCode, data);
                if (intendedAction.actionType == BillingUtils.ActionType.TAXES) {
                    if (intendedAction.success && BillingUtils.canStartDry(BillingUtils.ActionType.TAXES)) {
                        setContentMyTrips();
                    } else {
                        int previousPosition = menu.getPreviousPosition();
                        menu.selectItem(previousPosition);
                    }
                } else if (intendedAction.actionType == BillingUtils.ActionType.ADD_PERSON && intendedAction.success) {
                    menu.addPerson();
                } else if (intendedAction.actionType == BillingUtils.ActionType.IMMIGRATION_FORMS) {
                    if (intendedAction.success && BillingUtils.canStartDry(BillingUtils.ActionType.IMMIGRATION_FORMS)) {
                        setContentForms();
                    } else {
                        int previousPosition = menu.getPreviousPosition();
                        menu.selectItem(previousPosition);
                    }
                } else if (intendedAction.actionType == BillingUtils.ActionType.MANAGE_FAMILY_MEMBERS && intendedAction.success) {
                    if (settingsFragment != null) {
                        settingsFragment.manageFamilyMembers();
                    }
                }
                break;
            case RequestCodes.INCITATION_ACTIVITY:
                unpin();
                if (resultCode == RESULT_OK && data.hasExtra(CountriesActivity.COUNTRY_ID_KEY)) {
                    int countryId = data.getIntExtra(CountriesActivity.COUNTRY_ID_KEY, -1);
                    User user = SharedPreferenceUtils.getUtils().getUserSettings();
                    if (user.getCountryId() != countryId) {
                        user.setCountryId(countryId);
                        SharedPreferenceUtils.getUtils().saveUserSettings(user);
                        RestTaskPoster.postTask(this, new RestTask(RestTask.Types.USER_PUT), true);
                    }
                }
                break;
            case RequestCodes.CREATE_PERSONS:
                if (resultCode == RESULT_OK && data.hasExtra(CreateEditPersonActivity.PERSON_ID_EXTRA)) {
                    ZoomleeApp zoomleeApp = (ZoomleeApp) getApplication();
                    zoomleeApp.setSelectedPersonId(data.getIntExtra(CreateEditPersonActivity.PERSON_ID_EXTRA, zoomleeApp.getSelectedPersonId()));
                }
                unpin();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            Fragment curFragment = getCurrentFragment();
            if (getCurrentFragment() instanceof Editable)
                ((Editable) curFragment).onCancel();

            applyNormalMode();
            return;
        }

        if (menu.isDrawerOpen()) {
            drawerLayout.closeDrawers();
            return;
        }
        Fragment curFragment = getCurrentFragment();
        if (curFragment instanceof FragmentWithImagePicker && ((FragmentWithImagePicker) curFragment).isDialogOpened()) {
            ((FragmentWithImagePicker) curFragment).closeDialog();
            return;
        }

        super.onBackPressed();
    }

    public void applyEditMode() {
        editMode = true;
        hideOption(R.id.action_edit);
        showOption(R.id.action_save);

        menu.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        hideHomeBtn(true);

        Fragment curFragment = getCurrentFragment();
        if (curFragment instanceof Editable)
            ((Editable) getCurrentFragment()).onEdit();
    }

    public void applyNormalMode() {
        editMode = false;
        hideOption(R.id.action_save);
        showOption(R.id.action_edit);

        menu.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        hideHomeBtn(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.optionMenu = menu;
        if (getCurrentFragment() instanceof Editable)
            showOption(R.id.action_edit);
        else
            hideOption(R.id.action_edit);
        return true;
    }

    private void hideOption(int id) {
        if (optionMenu != null) {
            MenuItem item = optionMenu.findItem(id);
            item.setVisible(false);
        }
    }

    private void showOption(int id) {
        if (optionMenu != null) {
            MenuItem item = optionMenu.findItem(id);
            item.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                applyNormalMode();
                Fragment curFragment = getCurrentFragment();
                if (curFragment instanceof Editable)
                    ((Editable) getCurrentFragment()).onSave();
                return true;
            case R.id.action_edit:
                applyEditMode();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setContentDocuments() {
        getSupportActionBar().setTitle(R.string.documents);
        updateABAvatar();
        documentsFragment = documentsFragment == null ? new CategoriesFragment() : documentsFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, documentsFragment).commit();
    }

    private void setContentTags() {
        getSupportActionBar().setTitle(R.string.tags);
        updateABAvatar();
        tagsFragment = tagsFragment == null ? TagsFragment.newInstance() : tagsFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, tagsFragment).commit();
    }

    private void setContentForms() {

        startActivity(new Intent(getApplicationContext(),Subscriptionone.class));
                if (BillingUtils.canStart(this, BillingUtils.ActionType.IMMIGRATION_FORMS)) {
            getSupportActionBar().setTitle(R.string.forms);
            updateABAvatar();
            formsFragment = formsFragment == null ? FormsFragment.newInstance() : formsFragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, formsFragment).commit();
        }
    }

    private void setContentNotifications() {
        getSupportActionBar().setTitle(R.string.notifications);
        updateABAvatar();
        notificationsFragment = notificationsFragment == null ? new AlertsFragment() : notificationsFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, notificationsFragment).commit();
    }

    private void setContentMyTrips() {
        // here get taxes first
        if (BillingUtils.canStart(this, BillingUtils.ActionType.TAXES)) {
            getSupportActionBar().setTitle(R.string.title_tax_tracking);
            updateABAvatar(null);
            myTripsFragment = myTripsFragment == null ? MyTripsFragment.newInstance() : myTripsFragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, myTripsFragment).commit();
        }
    }

    private void setContentBeSafe() {
        getSupportActionBar().setTitle(R.string.be_safe);
        updateABAvatar(null);
        beSafeFragment = beSafeFragment == null ? BeSafeFragment.newInstance() : beSafeFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, beSafeFragment).commit();
    }

    private void setContentSettings() {
        getSupportActionBar().setTitle(R.string.settings);
        updateABAvatar(null);
        settingsFragment = settingsFragment == null ? SettingsFragment.newInstance() : settingsFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, settingsFragment).commit();
    }

    private void setContentCommunity() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com/pages/Zoomlee/712680342154634")));
    }


    private Fragment getCurrentFragment() {
        if (menu == null)
            return null;
        switch (menu.getCurrentPosition()) {
            case NavigationDrawerFragment.DOCUMENTS_MI:
                return documentsFragment;
            case NavigationDrawerFragment.TAGS_MI:
                return tagsFragment;
            case NavigationDrawerFragment.NOTIFICATIONS_MI:
                return notificationsFragment;
            case NavigationDrawerFragment.MY_TRIPS_MI:
                return myTripsFragment;
            case NavigationDrawerFragment.BE_SAFE_MI:
                return beSafeFragment;
            case NavigationDrawerFragment.SETTINGS_MI:
                return settingsFragment;
            case NavigationDrawerFragment.FORMS_MI:
                return formsFragment;
            case NavigationDrawerFragment.COMMUNITY_MI:
                throw new IllegalStateException("CommunityFragment does not exist, community item should open browser");
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        DeveloperUtil.michaelLog("MainActivity.onTouch X - " + event.getX() + ", Y - " + event.getY());
        return super.onTouchEvent(event);
    }

    @Override
    public void onImagePicked(File image) {
        if (settingsFragment != null) {
            settingsFragment.onImageObtained(image);
        }
    }

    public interface Editable {
        void onEdit();
        void onSave();
        void onCancel();
    }
}
