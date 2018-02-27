package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;
import com.android.vending.billing.util.SkuDetails;
import com.google.android.gms.analytics.HitBuilders;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.Error;
import com.zoomlee.zoo.net.ZoomleeCallback;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.BillingApi;
import com.zoomlee.zoo.net.api.UserDataApi;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.TrialTypes;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.ui.view.LoadingView;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 4/23/15
 */

public class SubscriptionActivity extends SecuredActionBarActivity implements IabHelper.OnIabSetupFinishedListener, IabHelper.OnIabPurchaseFinishedListener {

//  private static final String PRO_YEAR_SKU = "zoomlee_pro_y";//Managed products
    private static final String PRO_YEAR_SKU = "bma_year ";//Managed products
    private static final String PRO_MONTH_SKU = "abc_month";
//  private static final String PRO_MONTH_SKU = "zoomleelasttest";
//  private static final String PRO_MONTH_SKU = "zoomlee_pro_m";
    private static final String FAMILY_YEAR_SKU = "zoomlee_family_yy";//subscription
    private static final String FAMILY_MONTH_SKU = "zoomlee_family_m";
    public static final int REQUEST_CODE = 1001;
    static final int RC_REQUEST = 10001;
    private final BillingApi billingApi = new RestAdapter.Builder()
            .setEndpoint(ApiUrl.API_URL)
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build().create(BillingApi.class);
    private final UserDataApi userApi = new RestAdapter.Builder()
            .setEndpoint(ApiUrl.API_URL)
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build().create(UserDataApi.class);
    private boolean isSetuped;
    private Purchase currentPurchase;
    @BindView(R.id.mainLayout)
    LinearLayout mainLayout;
    @BindView(R.id.familyLayout)
    View familyLayout;
    @BindView(R.id.familySubscribeLayout)
    View familySubscribeLayout;
    @BindView(R.id.familyMonthBtn)
    Button familyMonthBtn;
    @BindView(R.id.familyYearBtn)
    Button familyYearBtn;
    @BindView(R.id.tryForFreeLayout)
    View tryForFreeLayout;
    @BindView(R.id.tryForFreeBtn)
    Button tryForFreeBtn;
    @BindView(R.id.tryForFreeHint)
    TextView tryForFreeHint;
    @BindView(R.id.familySubscribedTv)
    TextView familySubscribedTv;
    @BindView(R.id.proLayout)
    LinearLayout proLayout;
    @BindView(R.id.proSubscribeLayout)
    View proSubscribeLayout;
    @BindView(R.id.proMonthBtn)
    Button proMonthBtn;
    @BindView(R.id.proYearBtn)
    Button proYearBtn;
    @BindView(R.id.proSubscribedTv)
    TextView proSubscribedTv;
    @BindView(R.id.familyLoader)
    LoadingView familyLoader;
    @BindView(R.id.proLoader)
    LoadingView proLoader;
    @BindView(R.id.backBtn)
    View backBtn;
    private String familyMonthPrice;
    private String familyYearPrice;
    private String proMonthPrice;
    private String proYearPrice;
    private int familyTrialMonths;
    private final Map<String, SkuDetails> skuDetails = new HashMap<>();
    private IabHelper billingHelper;
    private String zoomleeKey;
    private final Handler handler = new Handler();
    private BillingUtils.ActionType actionType;

    /**
     * Starts subscription activity to buy subscription.
     *
     * @param activity   to start from
     * @param actionType to go on success
     */

    public static void startActivity(Activity activity, BillingUtils.ActionType actionType) {
        Intent intent = new Intent(activity, SubscriptionActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(IntentUtils.EXTRA_ACTION_TYPE, actionType);
        activity.startActivityForResult(intent, RequestCodes.SUBSCRIPTION_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("whichfirst","oncreate");

        actionType = (BillingUtils.ActionType) getIntent().getSerializableExtra(IntentUtils.EXTRA_ACTION_TYPE);
        setCustomizedView(R.layout.activity_subscription, false);
        ButterKnife.bind(this);

        showFamilyLoaders();
        showProLoaders();

        String rsa_key = getString(R.string.rsa_key);
        billingHelper = new IabHelper(this, rsa_key);
        billingHelper.startSetup(this);
        zoomleeKey = SharedPreferenceUtils.getUtils().getPrivateKey();
        Log.d("ZOOMLEEEEYY",zoomleeKey);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("whichfirst","resume");

        EventBus.getDefault().register(this);
        applySubscriptionState();
        billingApi.billingTypes(zoomleeKey, getTrialsCallback);

    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        disposeBillingHelper();
        super.onDestroy();
    }

    private void disposeBillingHelper() {
        if (billingHelper != null) {
            billingHelper.dispose();
        }
        billingHelper = null;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    }

    @Override
    public void onIabSetupFinished(IabResult result) {
        DeveloperUtil.michaelLog();
        DeveloperUtil.michaelLog(result);
        if (result.isSuccess()) {
            Log.d("Loadsuccess", "success");
            isSetuped = true;
            loadPrices();
        }
        else {
            Log.d("Loadsuccess","failure");
        }
    }

    private void loadPrices() {
        final List<String> skus = new ArrayList<>();
        skus.add(PRO_MONTH_SKU);
        skus.add(PRO_YEAR_SKU);
        skus.add(FAMILY_MONTH_SKU);
        skus.add(FAMILY_YEAR_SKU);
        billingHelper.queryInventoryAsync(true, skus, new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                if (result.isFailure()) {
                    Log.d("onQueryInventoryFi","failure");
                    Toast.makeText(SubscriptionActivity.this, result.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }else if (!result.isFailure()){
                    Log.d("onQueryInventoryFi","success");
                }

                SkuDetails details = inv.getSkuDetails(FAMILY_MONTH_SKU);
                if (details != null) {
                    familyMonthPrice = details.getPrice();
                    Log.d("Familymonthprice",familyMonthPrice);
                    skuDetails.put(FAMILY_MONTH_SKU, details);
                }

                details = inv.getSkuDetails(FAMILY_YEAR_SKU);
                if (details != null) {
                    familyYearPrice = details.getPrice();
                    Log.d("familyYearprice",familyYearPrice);
                    skuDetails.put(FAMILY_YEAR_SKU, details);
                }

                details = inv.getSkuDetails(PRO_MONTH_SKU);
                if (details != null) {
                    proMonthPrice = details.getPrice();
                    Log.d("promonthprice",proMonthPrice);
                    skuDetails.put(PRO_MONTH_SKU, details);
                }

                details = inv.getSkuDetails(PRO_YEAR_SKU);
                if (details != null) {
                    proYearPrice = details.getPrice();
                    Log.d("proYearPrice",proYearPrice);
                    skuDetails.put(PRO_YEAR_SKU, details);
                }

                familyMonthBtn.setText(getString(R.string.family_month_cost, familyMonthPrice));
                familyYearBtn.setText(getString(R.string.family_year_cost, familyYearPrice));
                updateTrialHint();
                proMonthBtn.setText(getString(R.string.pro_month_cost, proMonthPrice));
                proYearBtn.setText(getString(R.string.pro_year_cost, proYearPrice));
                applySubscriptionState();
            }
        });
    }

    private void consumeProducut(Purchase purchase) {
        if (purchase != null)
            billingHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
                @Override
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    DeveloperUtil.michaelLog();
                    DeveloperUtil.michaelLog(purchase);
                    DeveloperUtil.michaelLog(result);

                    SkuDetails details = skuDetails.get(purchase.getSku());
                    if (details != null) {
                        double price = 1.f * details.getPriceMicros() / 1000000;
                        price = (double) Math.round(price * 100) / 100;
                        String currency = details.getPriceCurrency();
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
    }

    private void applySubscriptionState() {
        if (!isSetuped)
            return;
        enableProBtns(true);
        enableFamilyBtns(true);
        User user = SharedPreferenceUtils.getUtils().getUserSettings();
        if (BillingUtils.isPro(user)) {
            ((FrameLayout.LayoutParams) mainLayout.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
            familyLayout.setVisibility(View.GONE);

            proLayout.setGravity(Gravity.CENTER);
            proSubscribeLayout.setVisibility(View.GONE);
            proSubscribedTv.setVisibility(View.VISIBLE);
            proLoader.hide();
        } else if (BillingUtils.isFamily(user)) {
            ((FrameLayout.LayoutParams) mainLayout.getLayoutParams()).gravity = Gravity.NO_GRAVITY;
            familyLayout.setVisibility(View.VISIBLE);
            familySubscribeLayout.setVisibility(View.GONE);
            tryForFreeLayout.setVisibility(View.GONE);
            familySubscribedTv.setVisibility(View.VISIBLE);
            familyLoader.hide();

            proLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            proSubscribeLayout.setVisibility(View.VISIBLE);
            proSubscribedTv.setVisibility(View.GONE);
            proLoader.hide();
        } else {
            boolean canFamilyTrial = BillingUtils.canFamilyTrial(user);
            ((FrameLayout.LayoutParams) mainLayout.getLayoutParams()).gravity = Gravity.NO_GRAVITY;
            familyLayout.setVisibility(View.VISIBLE);
            familySubscribeLayout.setVisibility(canFamilyTrial ? View.GONE : View.VISIBLE);
            tryForFreeLayout.setVisibility(canFamilyTrial ? View.VISIBLE : View.GONE);
            familySubscribedTv.setVisibility(View.GONE);
            familyLoader.hide();

            proLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            proSubscribeLayout.setVisibility(View.VISIBLE);
            proSubscribedTv.setVisibility(View.GONE);
            proLoader.hide();
        }
    }

    private void showProLoaders() {
        proSubscribeLayout.setVisibility(View.GONE);
        proSubscribedTv.setVisibility(View.GONE);
        proLoader.show();
        enableFamilyBtns(false);
    }

    private void showFamilyLoaders() {
        familySubscribeLayout.setVisibility(View.GONE);
        tryForFreeLayout.setVisibility(View.GONE);
        familySubscribedTv.setVisibility(View.GONE);
        familyLoader.show();
        enableProBtns(false);
    }

    private void enableFamilyBtns(boolean isEnable) {
        familyMonthBtn.setEnabled(isEnable);
        familyYearBtn.setEnabled(isEnable);
        tryForFreeBtn.setEnabled(isEnable);
        familyMonthBtn.setAlpha(isEnable ? 1f : 0.75f);
        familyYearBtn.setAlpha(isEnable ? 1f : 0.75f);
        tryForFreeBtn.setAlpha(isEnable ? 1f : 0.75f);
    }


    private void enableProBtns(boolean isEnable) {
        proMonthBtn.setEnabled(isEnable);
        proYearBtn.setEnabled(isEnable);
        proMonthBtn.setAlpha(isEnable ? 1f : 0.75f);
        proYearBtn.setAlpha(isEnable ? 1f : 0.75f);
    }

    private void hideProLoaders() {
        proSubscribeLayout.setVisibility(View.VISIBLE);
        proSubscribedTv.setVisibility(View.VISIBLE);
        proLoader.hide();
    }

    private void hideFamilyLoaders() {
        familySubscribeLayout.setVisibility(View.VISIBLE);
        tryForFreeLayout.setVisibility(View.VISIBLE);
        familySubscribedTv.setVisibility(View.VISIBLE);
        familyLoader.hide();
    }

    @OnClick({R.id.familyMonthBtn, R.id.familyYearBtn, R.id.tryForFreeBtn, R.id.proMonthBtn, R.id.proYearBtn, R.id.backBtn})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.familyMonthBtn:
                showFamilyLoaders();
                billingHelper.launchPurchaseFlow(this, FAMILY_MONTH_SKU, RequestCodes.PAY_SUBS, this);
                break;
            case R.id.familyYearBtn:
                showFamilyLoaders();
                billingHelper.launchPurchaseFlow(this, FAMILY_YEAR_SKU, RequestCodes.PAY_SUBS, this);
                break;
            case R.id.tryForFreeBtn:
                Log.e("Enteredforbilling","Clicked");
                showFamilyLoaders();
                billingApi.enableBilling(zoomleeKey, 2, 6, setBillingCallback);
//                billingApi.enableBilling("386473c0be30c2f0b16c5ba60e6dc362", 2, 6, setBillingCallback);
                break;
            case R.id.proMonthBtn:
                showProLoaders();
//                billingHelper.launchPurchaseFlow(this, PRO_MONTH_SKU, RequestCodes.PAY_SUBS, this);
                billingHelper.launchPurchaseFlow(this, PRO_MONTH_SKU, RC_REQUEST, this);
                break;
            case R.id.proYearBtn:
                showProLoaders();
                billingHelper.launchPurchaseFlow(this, PRO_YEAR_SKU, RC_REQUEST, this);
                break;
            case R.id.backBtn:
                onBackPressed();
                break;
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
            applySubscriptionState();
        }
    };

    private ZoomleeCallback<CommonResponse<User>> getUserCallBack = new ZoomleeCallback<CommonResponse<User>>() {
        @Override
        protected void success(Object response) {
            User newUser = (User) response;
            SharedPreferenceUtils.getUtils().saveUserSettings(newUser);
            if (BillingUtils.isPro(newUser) || !BillingUtils.isProUsed(newUser))
                SharedPreferenceUtils.getUtils().setShowRenewDialog(true);
            applySubscriptionState();

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
            applySubscriptionState();
        }
    };

    private final ZoomleeCallback<CommonResponse<TrialTypes>> getTrialsCallback = new ZoomleeCallback<CommonResponse<TrialTypes>>() {
        @Override
        protected void success(Object response) {
            TrialTypes trials = (TrialTypes) response;
            familyTrialMonths = trials.getFamilyTrialMonths();

            updateTrialHint();
        }

        @Override
        protected void error(Error error) {
            DeveloperUtil.michaelLog(error);
            applySubscriptionState();
        }
    };

    private void updateTrialHint() {
        if (familyTrialMonths != 0 && familyMonthPrice != null) {
            String trialMonths = getResources().getQuantityString(R.plurals.months, familyTrialMonths, familyTrialMonths);
            tryForFreeHint.setText(getString(R.string.try_for_free_hint, trialMonths, familyMonthPrice, familyYearPrice));
        }
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
        DeveloperUtil.michaelLog();
        Log.d("Onpurchasefinished","start");
        DeveloperUtil.michaelLog(info);
        DeveloperUtil.michaelLog(result);
        Log.d("Onpurchasefinished","finished");
        if (result.isFailure())
        {
            Log.d("Onpurchasefinished","failed");
            return;
        }

        currentPurchase = info;
        switch (info.getSku()) {
            case FAMILY_YEAR_SKU:
                billingApi.enableYearFamily(zoomleeKey, info.getToken(), setBillingCallback);
                break;
            case FAMILY_MONTH_SKU:
                billingApi.enableMonthFamily(zoomleeKey, info.getToken(), setBillingCallback);
                break;
            case PRO_MONTH_SKU:
                billingApi.enableMonthPro(zoomleeKey, info.getToken(), setBillingCallback);
                break;
            case PRO_YEAR_SKU:
                billingApi.enableYearPro(zoomleeKey, info.getToken(), setBillingCallback);
                break;
        }
    }

    public void onEventMainThread(Events.PersonChanged event) {
        if (event.getPerson().getId() == Person.ME_ID)
            applySubscriptionState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        billingHelper.handleActivityResult(requestCode, resultCode, data);
    }
}