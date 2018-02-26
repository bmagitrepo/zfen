package com.zoomlee.zoo.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.LocationService;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.ui.activity.SubscriptionActivity;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.activity.ChangeEmailPhoneActivity;
import com.zoomlee.zoo.ui.activity.CountriesActivity;
import com.zoomlee.zoo.ui.activity.InviteActivity;
import com.zoomlee.zoo.ui.activity.LoginActivity;
import com.zoomlee.zoo.ui.activity.PersonListActivity;
import com.zoomlee.zoo.ui.activity.SetPinActivity;
import com.zoomlee.zoo.ui.activity.TagsSettingsActivity;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.UiUtil;
import com.zoomlee.zoo.utils.Util;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;


public class SettingsFragment extends FragmentWithImagePicker implements View.OnClickListener {

    public static final String ZOOMLEE_GREEN = "#5cbc6b";
    public static final String TRAVELER = "Traveler";

    private ImageView avatar;
    private TextView phone;
    private TextView email;
    private TextView country;
    private TextView changePin;
    private TextView manageFamily;
    private TextView writeGooglePlay;
    private TextView writeEmail;
    private TextView subscriptionBadge;
    private Button logout;
    private View editPictureView;
    private TextView tagsTv;
    private View inviteTv;
    private View subscriptionLayout;
    private boolean flag = true;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {
        super(User.USER_PIC_NAME, true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_settings, container, false);
        initUi(mView);
        initListeners();

        return mView;
    }

    private void initUi(View mView) {
        editPictureView = mView.findViewById(R.id.edit_picture_view);
        avatar = (ImageView) mView.findViewById(R.id.avatarIv);
        phone = (TextView) mView.findViewById(R.id.phoneTv);
        email = (TextView) mView.findViewById(R.id.emailTv);
        country = (TextView) mView.findViewById(R.id.country);
        subscriptionBadge = (TextView) mView.findViewById(R.id.subscriptionBadge);
        changePin = (TextView) mView.findViewById(R.id.changePin);
        manageFamily = (TextView) mView.findViewById(R.id.managerFamilyTv);
        writeGooglePlay = (TextView) mView.findViewById(R.id.writeGooglePlayTv);
        writeEmail = (TextView) mView.findViewById(R.id.writeEmailTv);
        logout = (Button) mView.findViewById(R.id.logout);
        tagsTv = (TextView) mView.findViewById(R.id.tagsTv);
        inviteTv = mView.findViewById(R.id.inviteTv);
        subscriptionLayout = mView.findViewById(R.id.subscriptionLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        loadUi();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        GAUtil.getUtil().timeSpent(GAEvents.ACTION_SETTINGS_SECTION);
    }

    public void onEventMainThread(Events.PersonChanged event) {
        if (event.getPerson().getId() == Person.ME_ID)
            loadUi();
    }

    private void loadUi() {
        User user = SharedPreferenceUtils.getUtils().getUserSettings();
        if (!TextUtils.isEmpty(user.getPhone())) {
            phone.setText(user.getPhone());
            phone.setTextColor(getResources().getColor(R.color.text_gray));
        }
        if (!TextUtils.isEmpty(user.getEmail())) {
            email.setText(user.getEmail());
            email.setTextColor(getResources().getColor(R.color.text_gray));
        }
        DaoHelper<Country> countryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Country.class);
        Country countryObj = countryDaoHelper.getItemByRemoteId(getActivity(), user.getCountryId());
        if (countryObj != null && countryObj.getName() != null)
            country.setText(countryObj.getName());
        else
            country.setText(R.string.tap_to_select_country);

        UiUtil.loadPersonIcon(user, avatar, R.drawable.settings_me);
        updateSubscriptionBadge(BillingUtils.getBillingPlanName(user), ZOOMLEE_GREEN);
    }

    private void updateSubscriptionBadge(String name, String color){
        subscriptionBadge.setVisibility(View.GONE);
        /*ZA-403 subscriptionBadge.setText(name);
        int colorValue = Color.parseColor(color);
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(colorValue, PorterDuff.Mode.MULTIPLY);
        subscriptionBadge.getBackground().setColorFilter(colorFilter);
        subscriptionBadge.setTextColor(colorValue);*/
    }

    private void initListeners() {
        editPictureView.setOnClickListener(this);
        phone.setOnClickListener(this);
        email.setOnClickListener(this);
        country.setOnClickListener(this);
        changePin.setOnClickListener(this);
        manageFamily.setOnClickListener(this);
        writeGooglePlay.setOnClickListener(this);
        writeEmail.setOnClickListener(this);
        logout.setOnClickListener(this);
        tagsTv.setOnClickListener(this);
        inviteTv.setOnClickListener(this);
        subscriptionLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_picture_view:
                if (flag) {
                    openDialog();
                    hideKeyboard();
                } else {
                    closeDialog();
                }
                flag = !flag;
                break;
            case R.id.phoneTv:
                changePhone();
                break;
            case R.id.emailTv:
                changeEmail();
                break;
            case R.id.country:
                changeCountry();
                break;
            case R.id.changePin:
                changePinCode();
                break;
            case R.id.managerFamilyTv:
                manageFamilyMembers();
                break;
            case R.id.writeGooglePlayTv:
                writeGooglePlayFeedback();
                break;
            case R.id.writeEmailTv:
                writeEmailFeedback();
                break;
            case R.id.tagsTv:
                TagsSettingsActivity.startActivity(getActivity());
                break;
            case R.id.inviteTv:
                InviteActivity.startActivity(getActivity());
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.subscriptionLayout:
                SubscriptionActivity.startActivity(getActivity(), BillingUtils.ActionType.MANAGE_SUBSCRIPTION);
                break;
        }
    }

    private void changePhone() {
        DeveloperUtil.michaelLog();
        ChangeEmailPhoneActivity.startActivity(getActivity(), true);
    }

    private void changeEmail() {
        DeveloperUtil.michaelLog();
        ChangeEmailPhoneActivity.startActivity(getActivity(), false);
    }

    private void changeCountry() {
        DeveloperUtil.michaelLog();
        CountriesActivity.startForResult(getActivity(), SharedPreferenceUtils.getUtils().getUserSettings().getCountryId());
    }

    private void changePinCode() {
        DeveloperUtil.michaelLog();
        SetPinActivity.start(getActivity(), true);
    }

    public void manageFamilyMembers() {
        DeveloperUtil.michaelLog();
//        if (BillingUtils.canStart(getActivity(), BillingUtils.ActionType.MANAGE_FAMILY_MEMBERS)) {
            PersonListActivity.startForMangerPersons(getActivity());
//        }
    }

    private void writeGooglePlayFeedback() {
        DeveloperUtil.michaelLog();
        final String appPackageName = getActivity().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void writeEmailFeedback() {
        DeveloperUtil.michaelLog();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@zoomlee.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about Zoomlee");

        startActivityForResult(Intent.createChooser(intent, "Send Email"), RequestCodes.SEND_EMAIL);
    }

    private MaterialDialog mMaterialDialog;

    private void logout() {
        DeveloperUtil.michaelLog();
        final Activity activity = getActivity();
        mMaterialDialog = new MaterialDialog(activity)
                .setTitle(activity.getString(R.string.title_logout))
                .setMessage(R.string.log_out_and_log_in_back_to_reset_your_pin)
                .setPositiveButton(R.string.logout_uc, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                        SharedPreferenceUtils.getUtils().resetPin();
                        LocationService.stopLocationTracking(activity);
                        activity.finish();
                        startActivity(new Intent(activity, LoginActivity.class));
                    }
                })
                .setNegativeButton(getString(R.string.cancel_uc), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });

        mMaterialDialog.show();
    }

    @Override
    public void onImageObtained(final File image) {
        if (image == null) {
            flag = true;
            closeDialog();
        } else {
            new AsyncTask<Void,Bitmap,Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    User user = SharedPreferenceUtils.getUtils().getUserSettings();
                    user.setImageLocalPath(getActivity().getFileStreamPath(User.USER_PIC_NAME).getAbsolutePath());

                    Bitmap croppedBitmap = Util.roundBitmap(image);
                    publishProgress(croppedBitmap);

                    try {
                        FileUtil.writeBitmapToFile(croppedBitmap, new File(user.getImageLocal144Path()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    SharedPreferenceUtils.getUtils().saveUserSettings(user);
                    RestTaskPoster.postTask(getActivity(), new RestTask(RestTask.Types.USER_PUT), true);
                    return null;
                }

                @Override
                protected void onProgressUpdate(Bitmap... values) {
                    avatar.setImageBitmap(values[0]);
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    flag = true;
                    closeDialog();
                }
            }.execute();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.GET_COUNTRY && resultCode == Activity.RESULT_OK) {
            int countryId = data.getIntExtra(CountriesActivity.COUNTRY_ID_KEY, -1);
            User user = SharedPreferenceUtils.getUtils().getUserSettings();
            if (user.getCountryId() != countryId) {
                user.setCountryId(countryId);
                SharedPreferenceUtils.getUtils().saveUserSettings(user);
                RestTaskPoster.postTask(getActivity(), new RestTask(RestTask.Types.USER_PUT), true);

                DaoHelper<Country> countryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Country.class);
                Country countryObj = countryDaoHelper.getItemByRemoteId(getActivity(), user.getCountryId());
                if (countryObj != null && countryObj.getName() != null)
                    country.setText(countryObj.getName());
                else
                    country.setText(R.string.tap_to_select_country);
            }
        }
    }
}
