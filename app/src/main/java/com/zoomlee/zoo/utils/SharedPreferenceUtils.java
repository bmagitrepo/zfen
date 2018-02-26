package com.zoomlee.zoo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zoomlee.zoo.net.model.BillingPlan;
import com.zoomlee.zoo.net.model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/15/15
 */
public class SharedPreferenceUtils {
    public static final String PREF_NAME = "zoomlee_preferences";
    private static final String PINCODE_KEY = "pincode";
    private static final String SHOW_RENEW_DIALOG = "show_renew_dialog_zoomlee";
    public static final String CURRENT_COUNTRY_KEY = "CURRENT_COUNTRY_KEY";
    private static final String SHOW_ROOT_ALERT = "Zoomlee_SHOW_ROOT_ALERT";
    private static final String NEXT_RENEW_TIME = "Zoomlee_next_renew_time";
    public static final String IN_APP_NAVIGATION_KEY = "in_app_navigation";

    public static final String REQUIRE_SYNC = "require_sync";

    private static SharedPreferenceUtils utils;

    private Context context;
    private Gson gson = new Gson();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private volatile String privateKey;

    public static void init(Context context) {
        utils = new SharedPreferenceUtils(context);
    }

    public static SharedPreferenceUtils getUtils() {
        return utils;
    }

    private SharedPreferenceUtils(Context ctx) {
        context = ctx;
        preferences = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        privateKey = preferences.getString(UsersKeys.PRIVATE_KEY, null);
    }

    public void setIntSetting(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * @param key
     * @return Returns the preference value if it exists, or 0.
     */
    public int getIntSetting(String key) {
        return preferences.getInt(key, 0);
    }

    /**
     * Saves key/string value pair in preferences.
     *
     * @param key   to save value under
     * @param value to save
     */
    public void setStringSetting(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Gets value.
     *
     * @param key to get value by
     * @return preference value or null if not found
     */
    public String getStringSetting(String key) {
        return preferences.getString(key, null);
    }

    /**
     * Gets long setting or 0 if not set.
     *
     * @param key to get by
     * @return value
     */
    public long getLongSetting(String key) {
        return preferences.getLong(key, 0l);
    }

    /**
     * Sets long value.
     *
     * @param key   to put under
     * @param value to be saved
     */
    public void setLongSetting(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * Gets value.
     *
     * @param key
     * @return Returns the preference value if it exists, or false.
     */
    public boolean getBooleanSettings(String key) {
        return preferences.getBoolean(key, false);
    }

    /**
     * Saves key/boolean value pair in preferences.
     *
     * @param key   to save value under
     * @param value to save
     */
    public void setBooleanSettings(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void storePincode(String pincode) {
        editor.putString(PINCODE_KEY, pincode);
        editor.commit();
    }

    public boolean isShowRenewDialog() {
        return preferences.getBoolean(SHOW_RENEW_DIALOG, true) && preferences.getLong(NEXT_RENEW_TIME, 0) < System.currentTimeMillis();
    }

    public void setShowRenewDialog(boolean isShow) {
        editor.putBoolean(SHOW_RENEW_DIALOG, isShow);
        editor.commit();
    }

    public void snoozeRenewDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        editor.putLong(NEXT_RENEW_TIME, calendar.getTimeInMillis());
        editor.commit();
    }

    public String getPincode() {
        return preferences.getString(PINCODE_KEY, null);
    }

    public boolean isPincodeRight(String pincode) {
        return TextUtils.equals(getPincode(), pincode);
    }

    public boolean isPinSetuped() {
        return getPincode() != null;
    }

    public void resetPin() {
        editor.remove(PINCODE_KEY);
        editor.commit();
    }

    public void clearPreferences() {
        editor.clear();
        editor.commit();
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public User getUserSettings() {
        User user = new User();
        user.setRemoteId(preferences.getInt(UsersKeys.REMOTE_ID, -1));
        user.setCountryId(preferences.getInt(UsersKeys.COUNTRY_ID, -1));
        user.setStatus(preferences.getInt(UsersKeys.STATUS, -1));
        user.setCreateTime(preferences.getInt(UsersKeys.CREATE_TIME, -1));
        user.setUpdateTime(preferences.getInt(UsersKeys.UPDATE_TIME, -1));
        user.setName(preferences.getString(UsersKeys.NAME, "Me"));
        user.setEmail(preferences.getString(UsersKeys.EMAIL, null));
        user.setPhone(preferences.getString(UsersKeys.PHONE, null));
        user.setImageRemotePath(preferences.getString(UsersKeys.IMAGE_REMOTE_PATH, null));
        user.setImageLocalPath(preferences.getString(UsersKeys.IMAGE_LOCAL_PATH, null));
        user.setPrivateKey(preferences.getString(UsersKeys.PRIVATE_KEY, null));
        user.setInvitesCount(preferences.getInt(UsersKeys.INVITES_COUNT, 0));

        String jsonPlans = preferences.getString(UsersKeys.BILLING_PLANS, null);
        BillingPlan[] planArray = gson.fromJson(jsonPlans, BillingPlan[].class);
        List<BillingPlan> plans;
        if (planArray == null)
            plans = new ArrayList<>();
        else
            plans = Arrays.asList(planArray);
        user.setPlans(plans);
        DeveloperUtil.michaelLog();
        DeveloperUtil.michaelLog(user);
        return user;
    }

    public void showRootAlert(boolean isShow) {
        editor.putBoolean(SHOW_ROOT_ALERT, isShow);
        editor.commit();
    }

    public boolean isShowRootAlert() {
        return preferences.getBoolean(SHOW_ROOT_ALERT, true);
    }

    public void saveUserSettings(User user) {
        if (privateKey == null || !privateKey.equals(user.getPrivateKey()))
            PicassoUtil.init(context);
        privateKey = user.getPrivateKey();
        editor.putInt(UsersKeys.REMOTE_ID, user.getRemoteId());
        editor.putInt(UsersKeys.COUNTRY_ID, user.getCountryId());
        editor.putInt(UsersKeys.STATUS, user.getStatus());
        editor.putInt(UsersKeys.CREATE_TIME, user.getCreateTime());
        editor.putInt(UsersKeys.UPDATE_TIME, user.getUpdateTime());
        editor.putString(UsersKeys.NAME, user.getName());
        editor.putString(UsersKeys.EMAIL, user.getEmail());
        editor.putString(UsersKeys.PHONE, user.getPhone());
        editor.putString(UsersKeys.IMAGE_REMOTE_PATH, user.getImageRemotePath());
        editor.putString(UsersKeys.IMAGE_LOCAL_PATH, user.getImageLocalPath());
        editor.putString(UsersKeys.PRIVATE_KEY, user.getPrivateKey());
        editor.putString(UsersKeys.BILLING_PLANS, gson.toJson(user.getPlans()));
        editor.putInt(UsersKeys.INVITES_COUNT, user.getInvitesCount());

        editor.commit();

        if (user.getImageLocal144Path() != null)
            PicassoUtil.getInstance().invalidate(new File(user.getImageLocal144Path()));
        EventBus.getDefault().post(new Events.PersonChanged(Events.PersonChanged.UPDATED, user));

        DeveloperUtil.michaelLog();
        DeveloperUtil.michaelLog(user);
    }

    public boolean isUserLearnedDrawer() {
        return true;
    }

    public interface UsersKeys {
        String REMOTE_ID = "user_id";
        String NAME = "user_name";
        String EMAIL = "user_email";
        String PHONE = "user_phone";
        String IMAGE_REMOTE_PATH = "user_image_remote_path";
        String IMAGE_LOCAL_PATH = "user_image_local_path";
        String COUNTRY_ID = "user_country_id";
        String PRIVATE_KEY = "user_private_key";
        String STATUS = "user_status";
        String CREATE_TIME = "user_create_time";
        String UPDATE_TIME = "user_update_time";
        String BILLING_PLANS = "user_billing_plans";
        String INVITES_COUNT = "invites_count";
    }

    public interface LastSyncTimeKeys {
        String STATIC_DATA = "last_static_sync_time";
        String CATEGORIES_DOCS_TYPE = "last_categories_docs_type_sync";
        String CATEGORY = "last_category_sync";
        String COLORS = "last_colors_sync";
        String COUNTRIES = "last_countries_sync";
        String DOCUMENTS = "last_documents_sync";
        String DOCS_TYPE2FIELDS = "last_docs_type2fields_sync";
        String DOCS_TYPE = "last_docs_type_sync";
        String FIELDS_TYPES = "last_fields_types_sync";
        String FILES_TYPES = "last_files_types_sync";
        String GROUPS = "last_groups_sync";
        String PERSONS = "last_persons_sync";
        String TAX = "last_tax_sync";
    }
}