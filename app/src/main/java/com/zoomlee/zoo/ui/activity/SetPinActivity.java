package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.view.CreatePinView;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

public class SetPinActivity extends SecuredActionBarActivity {

    private final static String PARAM_IS_CHANGE = "pincode_is_change";

    private CreatePinView createPinView;
    private CreatePinView confirmPinView;
    private TextSwitcher textSwitcher;
    private boolean isSecondStep = false;
    private InputMethodManager imm;

    private boolean isChange = false;

    public static void start(Activity activity, boolean isChange) {
        Intent intent = new Intent(activity, SetPinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(PARAM_IS_CHANGE, isChange);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        if (isChange)
            activity.startActivityForResult(intent, RequestCodes.SET_PIN_ACTIVITY);
        else
            activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);

        isChange = getIntent().getBooleanExtra(PARAM_IS_CHANGE, false);
        if (!isChange)
            unsecure();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        initUi();

        createPinView.setOnPinEnteredListener(new CreatePinView.OnPinEnteredListener() {
            @Override
            public void onPinEntered(String pin) {
                goToConfirm();
            }
        });

        confirmPinView.setOnPinEnteredListener(new CreatePinView.OnPinEnteredListener() {
            @Override
            public void onPinEntered(String pin) {
                String originalPin = createPinView.getPin();
                if (TextUtils.equals(originalPin, pin))
                    acceptPin(pin);
                else
                    wrongConfirmed();
            }
        });
    }

    View.OnTouchListener onPinViewListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            imm.showSoftInput(isSecondStep ? confirmPinView : createPinView, 0);
            return true;
        }
    };

    private void initUi() {
        View view = findViewById(R.id.container);
        if (isChange) {
            view.setBackgroundColor(getResources().getColor(R.color.bg_content));
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.bg_grey));
        }

        createPinView = (CreatePinView) findViewById(R.id.createPinView);
        createPinView.requestFocus();
        confirmPinView = (CreatePinView) findViewById(R.id.confirmPinView);

        createPinView.setOnTouchListener(onPinViewListener);
        confirmPinView.setOnTouchListener(onPinViewListener);
        if (isChange) {
            createPinView.setPinItemColorBlack();
            confirmPinView.setPinItemColorBlack();
        }
        textSwitcher = (TextSwitcher) findViewById(R.id.textSwitcher);

        if (isChange) {
            textSwitcher.setBackgroundColor(Color.TRANSPARENT);
        } else {
            textSwitcher.setBackgroundResource(R.drawable.envelope_background);
        }

        textSwitcher.addView(createCustomTextView());
        textSwitcher.addView(createCustomTextView());
        textSwitcher.setInAnimation(this, android.R.anim.fade_in);
        textSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        textSwitcher.setCurrentText(getString(R.string.to_store_your_documents_securely_please_create_zoomlee_pin_code));
    }

    private void acceptPin(String pincode) {
        SharedPreferenceUtils.getUtils().storePincode(pincode);
        if (!isChange)
            MainActivity.startActivity(this, false);
        setResult(RESULT_OK);
        finish();
    }

    private void wrongConfirmed() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        confirmPinView.setText("");
        confirmPinView.startAnimation(shake);
    }

    private TextView createCustomTextView() {
        TextView tv = (TextView) LayoutInflater.from(this).inflate(R.layout.text_view_light_font, null);
        if (isChange) {
            tv.setTextColor(getResources().getColor(R.color.black));
        } else {
            tv.setTextColor(getResources().getColor(R.color.white));
        }
        tv.setLineSpacing(5, 1);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        return tv;
    }

    private void goToConfirm() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        anim.setFillAfter(true);
        createPinView.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        anim.setFillAfter(true);
        confirmPinView.startAnimation(anim);
        confirmPinView.requestFocus();

        textSwitcher.setText(getString(R.string.please_enter_pin_code_again));
        getSupportActionBar().setTitle(R.string.confirm_pincode);
        isSecondStep = true;
    }

    private void backToCreate() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        anim.setFillAfter(true);
        createPinView.startAnimation(anim);
        createPinView.setText("");
        createPinView.requestFocus();

        anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        anim.setFillAfter(true);
        confirmPinView.setText("");
        confirmPinView.startAnimation(anim);

        textSwitcher.setText(getString(R.string.to_store_your_documents_securely_please_create_zoomlee_pin_code));
        getSupportActionBar().setTitle(R.string.create_pincode);
        isSecondStep = false;
    }

    @Override
    public void onBackPressed() {
        if (isSecondStep)
            backToCreate();
        else {
            if (!isChange)
                startActivity(new Intent(this, LoginActivity.class));
            super.onBackPressed();
        }
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
}
