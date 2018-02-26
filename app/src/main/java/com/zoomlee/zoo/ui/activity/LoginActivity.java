package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.Error;
import com.zoomlee.zoo.net.ZoomleeCallback;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.AuthApi;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.view.LoadingView;
import com.zoomlee.zoo.ui.view.ZMEditText;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.PreferencesKeys;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import retrofit.RestAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;

public class LoginActivity extends Activity {

    private ZMEditText emailPhoneView;
    private Button actionButton;
    private LoadingView loadingView;
    private String login;


    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private FirebaseAnalytics firebaseAnalytics;

    private final AuthApi api = new RestAdapter.Builder()
            .setEndpoint(ApiUrl.API_URL)
            .build().create(AuthApi.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isTaskRoot()) {
            // Android launched another instance of the root activity into an existing task
            //  so just quietly finish and go away, dropping the user back into the activity
            //  at the top of the stack (ie: the last state of this task)
            finish();
            return;
        }

        setContentView(R.layout.activity_login);


        if (SharedPreferenceUtils.getUtils().isPinSetuped()) {
            MainActivity.startActivity(this, true);
            finish();
            return;
        }

        initUi();
        initListeners();
        try2Prefill();
    }

//    private void displayFirebaseRegId() {
//        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
//        String regId = pref.getString("regId", null);
//
//        Log.e(TAG, "Firebase reg id: " + regId);
//
//        if (!TextUtils.isEmpty(regId))
////            txtRegId.setText("Firebase Reg Id: " + regId);
//            Toast.makeText(getApplicationContext(),regId,Toast.LENGTH_SHORT).show();
//        else {
//            Toast.makeText(getApplicationContext(),"Firebase Reg Id is not received yet!",Toast.LENGTH_SHORT).show();
////            txtRegId.setText("Firebase Reg Id is not received yet!");
//        }
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        // register GCM registration complete receiver
//        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
//                new IntentFilter(Config.REGISTRATION_COMPLETE));
//
//        // register new push message receiver
//        // by doing this, the activity will be notified each time a new message arrives
//        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
//                new IntentFilter(Config.PUSH_NOTIFICATION));
//
//        // clear the notification area when the app is opened
//        NotificationUtils.clearNotifications(getApplicationContext());
//    }

//    @Override
//    protected void onPause() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
//        super.onPause();
//    }

    private void try2Prefill() {
        // first try to get from saved session
        String savedEmailPhone = SharedPreferenceUtils.getUtils().getStringSetting(PreferencesKeys.LOGIN);

        // if not found try to get my phone number
        if (TextUtils.isEmpty(savedEmailPhone)) {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            savedEmailPhone = tMgr.getLine1Number();
        }

        // set found number
        if (savedEmailPhone != null) {
            emailPhoneView.setText(savedEmailPhone);
            emailPhoneView.setSelection(emailPhoneView.length());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        CharSequence emailPhone = emailPhoneView.getText();
        if (!TextUtils.isEmpty(emailPhone)) {
            SharedPreferenceUtils.getUtils().setStringSetting(PreferencesKeys.LOGIN, emailPhone.toString());
        }
    }

    private void initUi() {
        emailPhoneView = (ZMEditText) findViewById(R.id.emailPhoneEt);
        actionButton = (Button) findViewById(R.id.actionLogin);
        loadingView = (LoadingView) findViewById(R.id.loading);
    }

    private void initListeners() {
        DeveloperUtil.michaelLog();
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeveloperUtil.michaelLog();
                login = emailPhoneView.getText().toString();
                blockUi();
                emailPhoneView.setError(false);
                if (Util.isEmail(login))
                    loginByEmail();
                else
                    loginByPhone();
            }

        });
    }

    private void loginByPhone() {
        String formatedPhone = Util.formatNumberToE164(login, Util.getCountryIsoCode(this));
        if (formatedPhone != null)
            login = formatedPhone;
        DeveloperUtil.michaelLog(login);
        api.login(null, login, loginCallback);
    }

    private void loginByEmail() {
        DeveloperUtil.michaelLog();
        api.login(login, null, loginCallback);
    }

    private void blockUi() {
        actionButton.setVisibility(View.INVISIBLE);
        loadingView.show();
    }

    private void unlockUi() {
        actionButton.setVisibility(View.VISIBLE);
        loadingView.hide();
    }

    private ZoomleeCallback<CommonResponse<Object>> loginCallback = new ZoomleeCallback<CommonResponse<Object>>() {

        @Override
        protected void success(Object response) {

            Log.d("RESONSE_LOGIN",response.toString());

            DeveloperUtil.michaelLog(response);
            PasscodeActivity.startActivity(LoginActivity.this, login);
            finish();
        }

        @Override
        protected void error(Error error) {
            unlockUi();
            emailPhoneView.setError(true);
            if (error.getCode() == Error.NO_CONNECTION_CODE)
                showNoConnectionAlert();
            else
                Toast.makeText(LoginActivity.this, error.getReason(), Toast.LENGTH_LONG).show();
        }
    };

    private void showNoConnectionAlert() {
        MaterialDialog mMaterialDialog = new MaterialDialog(this)
                .setTitle(R.string.noconnection_title)
                .setMessage(R.string.noconnection_message)
                .setPositiveButton(getString(R.string.ok).toUpperCase(), null);

        mMaterialDialog.show();
    }
}
