package com.zoomlee.zoo.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.syncservice.SyncUtils;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.PicassoUtil;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.UiUtil;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 1/30/15
 */
abstract public class SecuredActionBarActivity extends AppCompatActivity {

    static final String EXTRA_PIN_FAILED = "zoomlee_pin_failed";
    public static final int RESULT_PIN_FAILED = 123;

    private boolean isPinOk = false;
    private boolean isSecure = true;
    protected Toolbar toolbar;
    private View toolbarShadow;
    private View splashCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isPinOk = !getIntent().getBooleanExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getToolbar().setNavigationIcon(R.drawable.ic_nav_up);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SyncUtils.startPeriodicRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isPinOk = isPinOk || SharedPreferenceUtils.getUtils().getBooleanSettings(SharedPreferenceUtils.IN_APP_NAVIGATION_KEY);

        if (!isPinOk && isSecure) {
            startActivityForResult(new Intent(this, PinActivity.class), RequestCodes.PIN_REQUEST_CODE);
        } else {
            uncover();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferenceUtils.getUtils().setBooleanSettings(SharedPreferenceUtils.IN_APP_NAVIGATION_KEY, true);
    }

    @Override
    protected void onStop() {
        SharedPreferenceUtils.getUtils().setBooleanSettings(SharedPreferenceUtils.IN_APP_NAVIGATION_KEY, false);
        isPinOk = false;
        SyncUtils.cancelPeriodicRefresh();

        cover();

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (isSecure && requestCode == RequestCodes.PIN_REQUEST_CODE)
            if (resultCode == RESULT_OK) {
                isPinOk = true;
                tryToRequestSubscription();
            } else {
                closeApp();
            }
    }

    public void setShowShadow(boolean showShadow) {
        if (toolbarShadow != null) {
            toolbarShadow.setVisibility(showShadow ? View.VISIBLE : View.GONE);
        }
    }

    protected void tryToRequestSubscription() {
        if (!SharedPreferenceUtils.getUtils().isShowRenewDialog())
            return;
        User user = SharedPreferenceUtils.getUtils().getUserSettings();
        if (BillingUtils.need2RequestSubscribe(user))
            showRenewDialog();
    }

    private void showRenewDialog() {
        String[] items = getResources().getStringArray(R.array.renew_options);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_renew_subscription)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                SubscriptionActivity.startActivity(SecuredActionBarActivity.this, BillingUtils.ActionType.RENEW);
                                break;
                            case 1:
                                SharedPreferenceUtils.getUtils().setShowRenewDialog(false);
                                break;
                            case 2:
                                SharedPreferenceUtils.getUtils().snoozeRenewDay();
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    protected void closeApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_PIN_FAILED, true);
        startActivity(intent);
        setResult(RESULT_PIN_FAILED);
        finish();
    }

    protected void cover() {
        if (splashCover != null) {
            splashCover.setVisibility(View.VISIBLE);
        }
    }

    protected void uncover() {
        if (splashCover != null) {
            splashCover.setVisibility(View.GONE);
        }
    }

    protected void unsecure() {
        isSecure = false;
    }

    protected void unpin() {
        isPinOk = true;
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_secured_action_bar);
        FrameLayout activityContentLayout = (FrameLayout) findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContentLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarShadow = findViewById(R.id.toolbar_shadow);
        splashCover = findViewById(R.id.splash_cover);
        setSupportActionBar(toolbar);
    }

    public void setCustomizedView(int layoutResID) {
        super.setContentView(layoutResID);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarShadow = findViewById(R.id.toolbar_shadow);
        splashCover = findViewById(R.id.splash_cover);
        setSupportActionBar(toolbar);
    }

    public void setCustomizedView(int layoutResID, boolean withToolbar) {
        if (withToolbar) {
            setCustomizedView(layoutResID);
        } else {
            super.setContentView(layoutResID);
            toolbar = new Toolbar(this);
            setSupportActionBar(toolbar);
        }
    }

    protected void updateABAvatar(Person person) {
        getSupportActionBar().setDisplayUseLogoEnabled(person != null);
        //dirty hack
        Resources res = getResources();

        if (person != null)
            getSupportActionBar().setLogo(R.drawable.person_me);

        int viewCount = toolbar.getChildCount();
        for (int i = 0; i < viewCount; i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView || child instanceof ActionMenuView) continue;
            if (child instanceof ImageButton) {
                Toolbar.LayoutParams homeViewParams = (Toolbar.LayoutParams) child.getLayoutParams();
                homeViewParams.rightMargin = res.getDimensionPixelSize(R.dimen.ab_avatar_margin_left);
                child.setLayoutParams(homeViewParams);
            } else if (child instanceof ImageView && person != null) {
                ImageView logoView = (ImageView) child;
                int personId = person.getId();
                if (personId == Person.ALL_ID)
                    PicassoUtil.getInstance().load(R.drawable.all_persons_ab).into(logoView);
                else {
                    UiUtil.loadPersonIcon(person, logoView, true);
                }

                Toolbar.LayoutParams avAvatarParams = (Toolbar.LayoutParams) logoView.getLayoutParams();
                avAvatarParams.height = res.getDimensionPixelSize(R.dimen.avatar_height);
                avAvatarParams.width = res.getDimensionPixelSize(R.dimen.avatar_width);
                logoView.setLayoutParams(avAvatarParams);
            }
        }
        //end of dirty hack
    }

    protected void showOnlyName(boolean isHide) {
        int viewCount = toolbar.getChildCount();
        for (int i = 0; i < viewCount; i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView || child instanceof ActionMenuView) continue;
            if (child instanceof ImageButton)
                child.setVisibility(isHide ? View.INVISIBLE : View.VISIBLE);
            else if (child instanceof ImageView)
                child.setVisibility(isHide ? View.GONE : View.VISIBLE);
        }
    }

    protected void hideHomeBtn(boolean isHide) {
        int viewCount = toolbar.getChildCount();
        for (int i = 0; i < viewCount; i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof ImageButton) {
                child.setVisibility(isHide ? View.INVISIBLE : View.VISIBLE);
                break;
            }
        }
    }

    protected void updateABAvatar(int personId) {
        Person person;
        if (personId == -1)
            person = SharedPreferenceUtils.getUtils().getUserSettings();
        else if (personId == Person.ALL_ID) {
            person = Person.ALL;
        } else {
            DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
            person = personDaoHelper.getItemByLocalId(this, personId);
        }
        updateABAvatar(person);
    }

    protected void updateABAvatar() {
        Person person = ((ZoomleeApp) getApplication()).getSelectedPerson();
        updateABAvatar(person);
    }
}
