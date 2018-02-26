package com.zoomlee.zoo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.zoomlee.zoo.LocationService;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;


public class PinActivity extends AppCompatActivity {

    private static final int PIN_ITEMS_COUNT = 4;
    private int[] numericBtnIds = new int[]{R.id.button00, R.id.button01, R.id.button02, R.id.button03, R.id.button04, R.id.button05, R.id.button06, R.id.button07, R.id.button08, R.id.button09};
    private int[] pinItemIds = new int[]{R.id.pinItem1, R.id.pinItem2, R.id.pinItem3, R.id.pinItem4};
    private ImageView[] pinItemViews = new ImageView[PIN_ITEMS_COUNT];
    private View pinLayout;
    private StringBuilder enteredPin = new StringBuilder();

    private View forgotPinView;
    private MaterialDialog forgotPinDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        initUi();
        initListeners();

        if (Util.isRooted() && SharedPreferenceUtils.getUtils().isShowRootAlert())
            showSecurityAlert();
    }

    private void showSecurityAlert() {
        MaterialDialog mMaterialDialog = new MaterialDialog(this)
                .setTitle(R.string.device_is_rooted)
                .setMessage(R.string.root_alert_msg)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.accept).toUpperCase(), null)
                .setNegativeButton(getString(R.string.accept_and_dont_show_again).toUpperCase(), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferenceUtils.getUtils().showRootAlert(false);
                    }
                });

        mMaterialDialog.show();
    }

    private void initUi() {
        forgotPinView = findViewById(R.id.forgotPinTv);
        pinLayout = findViewById(R.id.pincodeLayout);
        for (int i = 0; i < PIN_ITEMS_COUNT; i++)
            pinItemViews[i] = (ImageView) findViewById(pinItemIds[i]);
    }

    private void initListeners() {
        forgotPinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPinDialog();
            }
        });

        for (int i = 0; i < numericBtnIds.length; i++)
            findViewById(numericBtnIds[i]).setOnClickListener(numericBtnListener);

        findViewById(R.id.del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enteredPin.length() == 0)
                    return;
                enteredPin.deleteCharAt(enteredPin.length() - 1);
                updatePinView();
            }
        });
    }

    private void showForgotPinDialog() {
        forgotPinDialog = new MaterialDialog(this)
                .setTitle(R.string.pin_reset)
                .setMessage(R.string.log_out_and_log_in_back_to_reset_your_pin)
                .setPositiveButton(R.string.logout_uc, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forgotPinDialog.dismiss();
                        SharedPreferenceUtils.getUtils().resetPin();
                        LocationService.stopLocationTracking(PinActivity.this);
                        finish();
                        startActivity(new Intent(PinActivity.this, LoginActivity.class));
                    }
                })
                .setNegativeButton(R.string.cancel_uc, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forgotPinDialog.dismiss();
                    }
                });

        forgotPinDialog.show();
    }


    private View.OnClickListener numericBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Button curBtn = (Button) v;
            enteredPin.append(curBtn.getText());
            updatePinView();

            int enteredPinLength = enteredPin.length();
            if (enteredPinLength > PIN_ITEMS_COUNT - 1)
                checkPin();

        }
    };

    private void updatePinView() {
        int enteredPinLength = enteredPin.length();
        for (int i = 0; i < PIN_ITEMS_COUNT; i++) {
            if (i > enteredPinLength - 1)
                pinItemViews[i].setImageResource(R.drawable.pin_item_empty);
            else
                pinItemViews[i].setImageResource(R.drawable.pin_item_fill);
        }

    }

    private void checkPin() {
        if (SharedPreferenceUtils.getUtils().isPincodeRight(enteredPin.toString()))
            acceptPin();
        else
            wrongPin();
    }

    private void acceptPin() {
        setResult(RESULT_OK);
        finish();
    }

    private void wrongPin() {
        DeveloperUtil.michaelLog();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        pinLayout.startAnimation(shake);
        enteredPin.delete(0, enteredPin.length());
        updatePinView();
    }

}
