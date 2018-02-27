package com.zoomlee.zoo.ui.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.zoomlee.zoo.LocationService;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.Error;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.ZoomleeCallback;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.AuthApi;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.provider.helpers.FormFieldsProviderHelper.FormFieldsContract;
import com.zoomlee.zoo.provider.helpers.FormsProviderHelper.FormsContract;
import com.zoomlee.zoo.provider.helpers.Tags2DocumentsProviderHelper;
import com.zoomlee.zoo.provider.helpers.TagsProviderHelper;
import com.zoomlee.zoo.provider.helpers.TaxProviderHelper;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.view.LoadingView;
import com.zoomlee.zoo.ui.view.PasscodeView;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import java.io.File;

import retrofit.RestAdapter;

import static com.zoomlee.zoo.provider.helpers.DocumentsHelper.DocumentsContract;
import static com.zoomlee.zoo.provider.helpers.FieldsHelper.FieldsContract;
import static com.zoomlee.zoo.provider.helpers.FilesProviderHelper.FilesContract;
import static com.zoomlee.zoo.provider.helpers.PersonsProviderHelper.PersonsContract;
import static com.zoomlee.zoo.provider.helpers.RestTasksHelper.RestTasksContract;


public class PasscodeActivity extends SecuredActionBarActivity implements PasscodeView.OnPasscodeEnteredListener {

    private PasscodeView passcodeView;
    private String login;
    private LoadingView loadingView;
    private TextView resendPasscodeView;
    private int tryCounter = 0;

    private AuthApi api = new RestAdapter.Builder()
            .setEndpoint(ApiUrl.API_URL)
            .build()
            .create(AuthApi.class);

    public static void startActivity(Context ctx, String login) {
        Intent intent = new Intent(ctx, PasscodeActivity.class);
        intent.putExtra(IntentUtils.EXTRA_LOGIN, login);

        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        unsecure();

        login = getIntent().getStringExtra(IntentUtils.EXTRA_LOGIN);
        initUi();
    }

    private void initUi() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        passcodeView = (PasscodeView) findViewById(R.id.passcodeView);
        passcodeView.requestFocus();
        loadingView = (LoadingView) findViewById(R.id.loading);
        resendPasscodeView = (TextView) findViewById(R.id.resendPasscodeTv);
        TextView codeReceiverView = (TextView) findViewById(R.id.codeReceiverTv);
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
        String email = null;
        String phone = null;
        if (Util.isEmail(login))
            email = login;
        else
            phone = login;
            api.sendPasscode(email, phone, passcode, sendPasscodeCallback);

    }

    private void resendPasscode() {
        blockUi();
        if (Util.isEmail(login))
            api.login(login, null, resendCallback);
        else
            api.login(null, login, resendCallback);
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
                Toast.makeText(PasscodeActivity.this, error.getReason(), Toast.LENGTH_LONG).show();
        }
    };

    private ZoomleeCallback<CommonResponse<User>> sendPasscodeCallback = new ZoomleeCallback<CommonResponse<User>>() {

        @Override
        protected void error(Error error) {
            if (error.getCode() == Error.NO_CONNECTION_CODE)
                showNoConnectionAlert();
            else
                Toast.makeText(PasscodeActivity.this, error.getReason(), Toast.LENGTH_LONG).show();
            passcodeWrong();
        }

        @Override
        protected void success(Object response) {
            Log.e("SENDPASSCODEOBJ",response.toString());
            User user = (User) response;
            User prevUser = SharedPreferenceUtils.getUtils().getUserSettings();
            if (prevUser.getRemoteId() != -1 && prevUser.getRemoteId() != user.getRemoteId()) {
                SharedPreferenceUtils.getUtils().clearPreferences();
                for (File file : getFilesDir().listFiles()) {
                    file.delete();
                }
                ContentResolver contentResolver = getContentResolver();
                contentResolver.delete(DocumentsContract.CONTENT_URI, null, null);
                contentResolver.delete(FieldsContract.CONTENT_URI, null, null);
                contentResolver.delete(FilesContract.CONTENT_URI, null, null);
                contentResolver.delete(PersonsContract.CONTENT_URI, null, null);
                contentResolver.delete(RestTasksContract.CONTENT_URI, null, null);
                contentResolver.delete(FormsContract.CONTENT_URI, null, null);
                contentResolver.delete(FormFieldsContract.CONTENT_URI, null, null);
                contentResolver.delete(TagsProviderHelper.TagsContract.CONTENT_URI, null, null);
                contentResolver.delete(Tags2DocumentsProviderHelper.Tags2DocumentsContract.CONTENT_URI, null, null);
                contentResolver.delete(TaxProviderHelper.TaxContract.CONTENT_URI, null, null);
            }

            SharedPreferenceUtils.getUtils().saveUserSettings(user);

            RestTaskPoster.postTask(PasscodeActivity.this, new RestTask(RestTask.Types.USER_DATA_GET), false);
            RestTaskPoster.postTask(PasscodeActivity.this, new RestTask(RestTask.Types.USER_GET), true);

            LocationService.startLocationTracking(PasscodeActivity.this.getApplicationContext());

            DeveloperUtil.michaelLog(user);
            SetPinActivity.start(PasscodeActivity.this, false);
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
                        onBackPressed();
                    }
                });

        mMaterialDialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginActivity.class));
        super.onBackPressed();
    }
}
