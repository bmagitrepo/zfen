package com.zoomlee.zoo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 3/19/15
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(TextUtils.equals(Intent.ACTION_BOOT_COMPLETED, intent.getAction())) {
            Intent serviceIntent = new Intent(context, SetNotificationsService.class);
            context.startService(serviceIntent);
        }
    }

}

