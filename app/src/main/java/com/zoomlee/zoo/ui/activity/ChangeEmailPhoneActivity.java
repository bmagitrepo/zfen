package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.Error;
import com.zoomlee.zoo.net.ZoomleeCallback;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.UserDataApi;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.view.LoadingView;
import com.zoomlee.zoo.ui.view.ZMEditText;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import retrofit.RestAdapter;


public class ChangeEmailPhoneActivity extends SecuredActionBarActivity {

    private static final String CHANGE_PHONE_ACTION = "change_phone";

    private boolean changePhoneRequest;
    private boolean updateData;
    private User user;
    private ZMEditText emailPhoneView;
    private TextView noteTextView;
    private Button actionButton;
    private LoadingView loadingView;
    private String login;
    private UserDataApi api = new RestAdapter.Builder()
            .setEndpoint(ApiUrl.API_URL)
            .build()
            .create(UserDataApi.class);

    public static void startActivity(Activity activity, boolean changePhoneRequest) {
        Intent intent = new Intent(activity, ChangeEmailPhoneActivity.class);
        intent.putExtra(CHANGE_PHONE_ACTION, changePhoneRequest);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        activity.startActivityForResult(intent, RequestCodes.CHANGE_EMAIL_PHONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_login);

        changePhoneRequest = getIntent().getBooleanExtra(CHANGE_PHONE_ACTION, false);
        user = SharedPreferenceUtils.getUtils().getUserSettings();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initUi();
        initListeners();
        try2Prefill();
        applyState();
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

    private void applyState() {
        emailPhoneView.setHint(changePhoneRequest ? R.string.your_phone_number : R.string.your_email);
        noteTextView.setText(changePhoneRequest ? R.string.update_phone_note : R.string.update_email_note);
        actionButton.setText(updateData ? R.string.update_btn_text : R.string.add_btn_text);
        getSupportActionBar().setTitle(changePhoneRequest ? R.string.phone_title : R.string.email_title);
    }

    private void try2Prefill() {
        String prevValue = null;
        if (changePhoneRequest) {
            if (!TextUtils.isEmpty(user.getPhone())) {
                updateData = true;
                prevValue = user.getPhone();
            } else {
                updateData = false;
                TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String mPhoneNumber = tMgr.getLine1Number();
                if (mPhoneNumber != null)
                    prevValue = mPhoneNumber;
            }
        } else {
            if (!TextUtils.isEmpty(user.getEmail())) {
                updateData = true;
                prevValue = user.getEmail();
            } else {
                updateData = false;
            }
        }

        emailPhoneView.setText(prevValue);
        emailPhoneView.setSelection(emailPhoneView.length());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.CONFIRMATION_ACTIVITY) {
            if (resultCode == RESULT_CANCELED)
                unpin();
            else
                finish();
        }
    }

    private void initUi() {
        emailPhoneView = (ZMEditText) findViewById(R.id.emailPhoneEt);
        actionButton = (Button) findViewById(R.id.actionChangeLogin);
        loadingView = (LoadingView) findViewById(R.id.loading);
        noteTextView = (TextView) findViewById(R.id.noteTv);
    }

    private void initListeners() {
        DeveloperUtil.michaelLog();
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeveloperUtil.michaelLog();
                login = emailPhoneView.getText().toString();
                blockUi();
                if (changePhoneRequest)
                    updatePhone();
                else
                    updateEmail();
            }
        });
    }

    private void updatePhone() {
        String formatedPhone = Util.formatNumberToE164(login, Util.getCountryIsoCode(this));
        if (formatedPhone != null)
            login = formatedPhone;
        DeveloperUtil.michaelLog(login);
       api.updatePhoneEmail(user.getPrivateKey(), user.getRemoteId(), null, login, loginCallback);

    }


    private void updateEmail() {
        DeveloperUtil.michaelLog();
        api.updatePhoneEmail(user.getPrivateKey(), user.getRemoteId(), login, null, loginCallback);
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
            DeveloperUtil.michaelLog(response);
            unlockUi();
            ConfirmationActivity.startActivity(ChangeEmailPhoneActivity.this, login);
        }

        @Override
        protected void error(Error error) {
            unlockUi();
            emailPhoneView.setError(true);
            if (error.getCode() == Error.NO_CONNECTION_CODE)
                showNoConnectionAlert();
            else
                Toast.makeText(ChangeEmailPhoneActivity.this, error.getReason(), Toast.LENGTH_LONG).show();
            Log.d("CHANGEEMIALHONE",error.getReason());
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
