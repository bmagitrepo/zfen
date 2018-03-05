package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.util.IabBroadcastReceiver;
import com.util.IabBroadcastReceiver.IabBroadcastListener;
import com.util.IabHelper;
import com.util.IabResult;
import com.util.Inventory;
import com.util.Purchase;
import com.util.SkuDetails;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.Error;
import com.zoomlee.zoo.net.ZoomleeCallback;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.BillingApi;
import com.zoomlee.zoo.net.api.UserDataApi;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.ui.view.ZMButton;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;

public class Subscriptionone extends AppCompatActivity implements IabBroadcastListener,DialogInterface.OnClickListener {
    ZMButton proMonthBtn;
    ZMButton proYearBtn;

    private final BillingApi billingApi = new RestAdapter.Builder()
            .setEndpoint(ApiUrl.API_URL)
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build().create(BillingApi.class);
    private final UserDataApi userApi = new RestAdapter.Builder()
            .setEndpoint(ApiUrl.API_URL)
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build().create(UserDataApi.class);

    static final String TAG = "Subscriptionone";

    String base64EncodedPublicKey;

    static final String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
    static final String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearlyy";

    static final int RC_REQUEST = 10001;

    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;

    boolean mAutoRenewEnabled = false;
    boolean mSubscribedToInfiniteGas = false;
    private Purchase currentPurchase;
    String mInfiniteGasSku = "";

    String mFirstChoiceSku = "";
    String mSecondChoiceSku = "";
    private String zoomleeKey;
    String mSelectedSubscriptionPeriod = "";
    private final Map<String, SkuDetails> skuDetails = new HashMap<>();
    int mTank;
    static final int TANK_MAX = 4;
    private BillingUtils.ActionType actionType;
    private final Handler handler = new Handler();

    public static void startActivity(Activity activity, BillingUtils.ActionType actionType) {
        Intent intent = new Intent(activity, Subscriptionone.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(IntentUtils.EXTRA_ACTION_TYPE, actionType);
        activity.startActivityForResult(intent, RequestCodes.SUBSCRIPTION_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptionone);

        actionType = (BillingUtils.ActionType) getIntent().getSerializableExtra(IntentUtils.EXTRA_ACTION_TYPE);
        proMonthBtn = (ZMButton)findViewById(R.id.proMonthBtn);
        proYearBtn = (ZMButton)findViewById(R.id.proYearBtn);
        zoomleeKey = SharedPreferenceUtils.getUtils().getPrivateKey();
        Log.d("ZOOMLEEEEYY",zoomleeKey);
        base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgY/I+ogdQETkcPzaw/m4CZGWHD2tMTGC39vjPMmEvyesz8Xv+L79FPklB/2eiCsYZRqhRVOwYdU4K8qR1SwHJbKhhR9xWJAm+oC2qcxJT53UAZSML+UMnV4GFFDB5oM4ljPjwHOyzllsPBDT9L6lTLlw6WGyTvXvgHXn8eG8FFjPtggGWDe2mm/FWLZKhSvhtrE6+6JEsyQyOKnceir00fYbeaX6/kSURmK9zE5S6VU6+PRedp09QZ4zjMwAOaZGGQckoqh57G15qNzKHr5WqVGY4EZuR7as1BLhMF7RG/Xd1jN1PnJUz0DakAg8btjmPvh60g9B9GveFFGw9gfsDQIDAQAB";
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        Log.d(TAG, "Starting setup.");


        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }
                if (mHelper == null) return;
                mBroadcastReceiver = new IabBroadcastReceiver(Subscriptionone.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });

        proMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"PROMONTH",Toast.LENGTH_SHORT).show();


                if (!mHelper.subscriptionsSupported()) {
                    complain("Subscriptions not supported on your device yet. Sorry!");
                    return;
                }

                CharSequence[] options;
                if (!mSubscribedToInfiniteGas || !mAutoRenewEnabled) {
                    // Both subscription options should be available
                    options = new CharSequence[2];
                    options[0] = getString(R.string.subscription_period_monthly);
                    options[1] = getString(R.string.subscription_period_yearly);

                    mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
                    mSecondChoiceSku = SKU_INFINITE_GAS_YEARLY;

                } else {
                    // This is the subscription upgrade/downgrade path, so only one option is valid
                    options = new CharSequence[1];
                    if (mInfiniteGasSku.equals(SKU_INFINITE_GAS_MONTHLY)) {
                        // Give the option to upgrade to yearly
                        options[0] = getString(R.string.subscription_period_yearly);
                        mFirstChoiceSku = SKU_INFINITE_GAS_YEARLY;
                    } else {
                        // Give the option to downgrade to monthly
                        options[0] = getString(R.string.subscription_period_monthly);
                        mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
                    }
                    mSecondChoiceSku = "";
                }

                int titleResId;
                if (!mSubscribedToInfiniteGas) {
                    titleResId = R.string.subscription_period_prompt;
                } else if (!mAutoRenewEnabled) {
                    titleResId = R.string.subscription_resignup_prompt;
                } else {
                    titleResId = R.string.subscription_update_prompt;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(Subscriptionone.this);
                builder.setTitle(titleResId)
                        .setSingleChoiceItems(options, 0 /* checkedItem */, Subscriptionone.this)
                        .setPositiveButton(R.string.subscription_prompt_continue, Subscriptionone.this)
                        .setNegativeButton(R.string.subscription_prompt_cancel, Subscriptionone.this);
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });


        proYearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"PROYEAR",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    void complain(String message) {
        Log.e(TAG, "**** Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener(){
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (mHelper == null) return;
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }
            Log.d(TAG, "Query inventory was successful.");

            Purchase gasMonthly = inventory.getPurchase(SKU_INFINITE_GAS_MONTHLY);
            Purchase gasYearly = inventory.getPurchase(SKU_INFINITE_GAS_YEARLY);

            if (gasMonthly != null && gasMonthly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (gasYearly != null && gasYearly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_YEARLY;
                mAutoRenewEnabled = true;
            } else {
                mInfiniteGasSku = "";
                mAutoRenewEnabled = false;
            }

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            mSubscribedToInfiniteGas = (gasMonthly != null && verifyDeveloperPayload(gasMonthly))
                    || (gasYearly != null && verifyDeveloperPayload(gasYearly));
            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");

            updateUi();
//            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + info);
            if (mHelper == null) return;
            if (result.isFailure()) {
                complain("Error purchasing: " + result);
//                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(info)) {
                complain("Error purchasing. Authenticity verification failed.");
//                setWaitScreen(false);
                return;
            }
            Log.d(TAG, "Purchase successful.");
            currentPurchase = info;
            billingApi.enableMonthPro(zoomleeKey, info.getToken(), setBillingCallback);
            if (info.getSku().equals(SKU_INFINITE_GAS_MONTHLY) || info.getSku().equals(SKU_INFINITE_GAS_YEARLY)) {
                Log.d(TAG, "Infinite gas subscription purchased.");
                alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGas = true;
                mAutoRenewEnabled = info.isAutoRenewing();
                mInfiniteGasSku = info.getSku();
                mTank = TANK_MAX;
                updateUi();
//                setWaitScreen(false);
            }
        }
    };

    private void updateUi() {
        if (mSubscribedToInfiniteGas) {
                Log.d("Purchased","verified");
        } else {
                Log.d("Purchased","not completed");
        }
    }

    void setWaitScreen(boolean set) {
        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }

    @Override
    public void receivedBroadcast() {
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == 0 /* First choice item */) {
            mSelectedSubscriptionPeriod = mFirstChoiceSku;

        } else if (which == 1 /* Second choice item */) {
            mSelectedSubscriptionPeriod = mSecondChoiceSku;
        } else if (which == DialogInterface.BUTTON_POSITIVE /* continue button */) {

            String payload = "";

            if (TextUtils.isEmpty(mSelectedSubscriptionPeriod)) {
                // The user has not changed from the default selection
                mSelectedSubscriptionPeriod = mFirstChoiceSku;
            }

            List<String> oldSkus = null;
            if (!TextUtils.isEmpty(mInfiniteGasSku) && !mInfiniteGasSku.equals(mSelectedSubscriptionPeriod)) {
                oldSkus = new ArrayList<String>();
                oldSkus.add(mInfiniteGasSku);
            }
            Log.d("OLDSKUSSS", oldSkus.toString());
            Log.d("mselectionsubsription", mSelectedSubscriptionPeriod);
//            setWaitScreen(true);
            Log.d(TAG, "Launching purchase flow for gas subscription.");
            try {
                mHelper.launchPurchaseFlow(this, mSelectedSubscriptionPeriod, IabHelper.ITEM_TYPE_SUBS,
                        oldSkus, RC_REQUEST, mPurchaseFinishedListener, payload);
            } catch (IabHelper.IabAsyncInProgressException e) {
                complain("Error launching purchase flow. Another async operation in progress.");
//                setWaitScreen(false);
            }
            // Reset the dialog options
            mSelectedSubscriptionPeriod = "";
            mFirstChoiceSku = "";
            mSecondChoiceSku = "";
        } else if (which != DialogInterface.BUTTON_NEGATIVE) {
            // There are only four buttons, this should not happen
            Log.e(TAG, "Unknown button clicked in subscription dialog: " + which);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }

    }

    private ZoomleeCallback<CommonResponse<Object>> setBillingCallback = new ZoomleeCallback<CommonResponse<Object>>() {
        @Override
        protected void success(Object response) {
            Log.e("Enteredforbilling","Success");
            DeveloperUtil.michaelLog(response);
            consumeProducut(currentPurchase);
            userApi.getUser(zoomleeKey, SharedPreferenceUtils.getUtils().getUserSettings().getRemoteId(), getUserCallBack);
        }
        @Override
        protected void error(Error error) {
            Log.e("Enteredforbilling","Failure");
            DeveloperUtil.michaelLog(error);
//            applySubscriptionState();
        }
    };

    private void consumeProducut(Purchase purchase) {
        if (purchase != null)
            try {
                mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        DeveloperUtil.michaelLog();
                        DeveloperUtil.michaelLog(purchase);
                        DeveloperUtil.michaelLog(result);

                        SkuDetails details = skuDetails.get(purchase.getSku());
                        if (details != null) {
                            double price = 1.f * details.getPriceAmountMicros() / 1000000;
                            price = (double) Math.round(price * 100) / 100;
                            String currency = details.getPriceCurrencyCode();
                            String orderId = purchase.getOrderId();

                            GAUtil.getUtil().sendData(new HitBuilders.TransactionBuilder()
                                    .setTransactionId(orderId)
                                    .setAffiliation("In-App Billing")
                                    .setRevenue(price)
                                    .setCurrencyCode(currency)
                                    .build());

                            GAUtil.getUtil().sendData(new HitBuilders.ItemBuilder()
                                    .setTransactionId(orderId)
                                    .setName(details.getTitle())
                                    .setSku(details.getSku())
                                    .setCategory(details.getType())
                                    .setPrice(price)
                                    .setQuantity(1)
                                    .setCurrencyCode(currency)
                                    .build());
                        }
                    }
                });
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
    }


    private ZoomleeCallback<CommonResponse<User>> getUserCallBack = new ZoomleeCallback<CommonResponse<User>>() {
        @Override
        protected void success(Object response) {
            User newUser = (User) response;
            SharedPreferenceUtils.getUtils().saveUserSettings(newUser);
            if (BillingUtils.isPro(newUser) || !BillingUtils.isProUsed(newUser))
                SharedPreferenceUtils.getUtils().setShowRenewDialog(true);
//            applySubscriptionState();

            DeveloperUtil.michaelLog(actionType);

            if (actionType == BillingUtils.ActionType.MANAGE_SUBSCRIPTION)
                return;
            // wait and quit, with the corresponding result
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }, 1000);
        }

        @Override
        protected void error(Error error) {
            DeveloperUtil.michaelLog(error);
//            applySubscriptionState();
        }
    };




}
