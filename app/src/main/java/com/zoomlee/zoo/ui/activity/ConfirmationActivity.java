package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.zoomlee.zoo.ui.view.PasscodeView;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import retrofit.RestAdapter;


public class ConfirmationActivity extends SecuredActionBarActivity implements PasscodeView.OnPasscodeEnteredListener {

    private User user;
    private PasscodeView passcodeView;
    private String login;
    private LoadingView loadingView;
    private TextView resendPasscodeView;
    private TextView codeReceiverView;
    private int tryCounter = 0;

    private UserDataApi api = new RestAdapter.Builder()
            .setEndpoint(ApiUrl.API_URL)
            .build()
            .create(UserDataApi.class);

    public static void startActivity(Activity ctx, String login) {
        Intent intent = new Intent(ctx, ConfirmationActivity.class);
        intent.putExtra(IntentUtils.EXTRA_LOGIN, login);

        ctx.startActivityForResult(intent, RequestCodes.CONFIRMATION_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        login = getIntent().getStringExtra(IntentUtils.EXTRA_LOGIN);
        user = SharedPreferenceUtils.getUtils().getUserSettings();
        initUi();
    }

    private void initUi() {
        passcodeView = (PasscodeView) findViewById(R.id.passcodeView);
        passcodeView.requestFocus();
        loadingView = (LoadingView) findViewById(R.id.loading);
        resendPasscodeView = (TextView) findViewById(R.id.resendPasscodeTv);
        codeReceiverView = (TextView) findViewById(R.id.codeReceiverTv);
        codeReceiverView.setText(getString(R.string.we_have_sent_passcode_to, login));

        passcodeView.setOnPasscodeEnteredListener(this);
        resendPasscodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendPasscode();
            }
        });
    }

    @Override
    public void onPasscodeEntered(String passcode) {
        api.sendPassCode(user.getPrivateKey(), user.getRemoteId(), passcode, sendPasscodeCallback);
    }

    private void resendPasscode() {
        blockUi();
        if (Util.isEmail(login))
            api.updatePhoneEmail(user.getPrivateKey(), user.getRemoteId(), login, null, resendCallback);
        else
            api.updatePhoneEmail(user.getPrivateKey(), user.getRemoteId(), null, login, resendCallback);
    }

    private void blockUi() {
        resendPasscodeView.setVisibility(View.GONE);
        loadingView.show();
    }

    private void unlockUi() {
        resendPasscodeView.setVisibility(View.VISIBLE);
        loadingView.hide();
    }

    private ZoomleeCallback<CommonResponse<Object>> resendCallback = new ZoomleeCallback<CommonResponse<Object>>() {
        @Override
        protected void success(Object response) {
            unlockUi();
        }

        @Override
        protected void error(Error error) {
            unlockUi();
            if (error.getCode() == Error.NO_CONNECTION_CODE)
                showNoConnectionAlert();
            else
                Toast.makeText(ConfirmationActivity.this, error.getReason(), Toast.LENGTH_LONG).show();
        }
    };

    private ZoomleeCallback<CommonResponse<User>> sendPasscodeCallback = new ZoomleeCallback<CommonResponse<User>>() {

        @Override
        protected void error(Error error) {
            if (error.getCode() == Error.NO_CONNECTION_CODE)
                showNoConnectionAlert();
            else
                Toast.makeText(ConfirmationActivity.this, error.getReason(), Toast.LENGTH_LONG).show();
            passcodeWrong();
        }

        @Override
        protected void success(Object response) {
            User user = (User) response;
            User prevUser = ConfirmationActivity.this.user;
            prevUser.setEmail(user.getEmail());
            prevUser.setPhone(user.getPhone());
            SharedPreferenceUtils.getUtils().saveUserSettings(prevUser);

            DeveloperUtil.michaelLog(user);
            setResult(RESULT_OK);
            finish();
        }
    };


    private void showNoConnectionAlert() {
        MaterialDialog mMaterialDialog = new MaterialDialog(this)
                .setTitle(R.string.noconnection_title)
                .setMessage(R.string.noconnection_message)
                .setPositiveButton(getString(R.string.ok).toUpperCase(), null);

        mMaterialDialog.show();
    }


    private void passcodeWrong() {
        if (++tryCounter == 3) {
            showFailPasscodeDialog();
            return;
        }

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        passcodeView.setText("");
        passcodeView.startAnimation(shake);
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

    private void showFailPasscodeDialog() {
        MaterialDialog mMaterialDialog = new MaterialDialog(this)
                .setTitle(R.string.wrong_passcode)
                .setMessage(R.string.wrong_passcode_msg)
                .setPositiveButton(R.string.ok_upper, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });

        mMaterialDialog.show();
    }
}
