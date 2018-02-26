package com.zoomlee.zoo.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/14/15
 */
public class Util {

    private static final int ROUNDED_IMAGE_SIZE = 144; //px

    private static Matcher matcher;

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }

    public static boolean isEmail(String login) {
        matcher = Patterns.EMAIL_ADDRESS.matcher(login);
        return matcher.matches();
    }

    public static String formatNumberToE164(String phoneNumber, String defaultCountryIso) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        String result = null;
        try {
            Phonenumber.PhoneNumber pn = util.parse(phoneNumber, defaultCountryIso);
            if (util.isValidNumber(pn)) {
                result = util.format(pn, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (NumberParseException e) {
        }
        return result;
    }

    public static String getCountryIsoCode(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        if (!TextUtils.isEmpty(countryCode))
            return countryCode.toUpperCase();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (location != null) {
            Geocoder code = new Geocoder(context);
            try {
                List<Address> adrs = code.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (adrs != null && adrs.size() > 0)
                    return adrs.get(0).getCountryCode().toUpperCase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Locale.getDefault().getISO3Country().toUpperCase();
    }

    public static boolean isEmpty(List list) {
        if (list == null || list.size() == 0) return true;
        else return false;
    }

    public static int findIndex(String[] array, String strToFind) {
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(strToFind)) return i;

        return -1;
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    public static Bitmap roundBitmap(File sourceFile) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(sourceFile.getAbsolutePath(), o);
        int scale = Math.min(o.outHeight, o.outWidth) / ROUNDED_IMAGE_SIZE;
        o.inJustDecodeBounds = false;
        o.inSampleSize = scale;

        Bitmap source = BitmapFactory.decodeFile(sourceFile.getAbsolutePath(), o);
        Bitmap squaredBitmap = source;
        int size = source.getWidth();
        if (source.getWidth() != source.getHeight()) {
            size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap,
                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    public static boolean isRooted() {
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }

        // check if /system/app/Superuser.apk is present
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e1) {
        }

        // try executing commands
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
    }

    // executes a command on the system
    private static boolean canExecuteCommand(String command) {
        boolean executedSuccesfully;
        try {
            Runtime.getRuntime().exec(command);
            executedSuccesfully = true;
        } catch (Exception e) {
            executedSuccesfully = false;
        }

        return executedSuccesfully;
    }

    public static String trimEnd(String source) {
        if (source == null)
            return null;

        int i = source.length();

        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) ;

        return source.substring(0, i + 1);
    }

    public static boolean checkPlayServices(Activity activity) {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            Dialog playServicesErrorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, RequestCodes.PLAY_SERVICES_UNAVAILABLE);
            playServicesErrorDialog.show();
            return false;
        }

        return true;
    }
}
