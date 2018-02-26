package com.zoomlee.zoo.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.zoomlee.zoo.AlarmReceiver;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Field;

import java.util.Calendar;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 3/16/15
 */
public class NotificationsUtil {

    public static void addReminder(Context context, Field field) {
        DeveloperUtil.michaelLog();
        DeveloperUtil.michaelLog(field);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        Intent alarmIntent = new Intent(AlarmReceiver.ACTION);
        Calendar calendar = TimeUtil.getCalendarForServerTime(Long.parseLong(field.getValue()));
        String formattedDate = TimeUtil.formatDateUTC(calendar.getTime());

        alarmIntent.putExtra(AlarmReceiver.EXTRA_MESSAGE, field.getName() + " " + formattedDate);
        alarmIntent.putExtra(AlarmReceiver.EXTRA_DOC_ID, field.getLocalDocumentId());
        DeveloperUtil.michaelLog("message = " + field.getName() + " " + formattedDate);
        DeveloperUtil.michaelLog("EXTRA_DOC_ID = " + field.getLocalDocumentId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, field.getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Long l = TimeUtil.getLocalTimestamp(field.getLongNotifyOn());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        c.set(Calendar.HOUR, 10);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Long localNotifyTimestamp = c.getTimeInMillis();

        alarmManager.set(AlarmManager.RTC_WAKEUP, localNotifyTimestamp, pendingIntent);
    }

    public static void removeReminder(Context context, Field field) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(AlarmReceiver.ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, field.getId(), alarmIntent, 0);

        alarmManager.cancel(pendingIntent);
    }

    public static void showNotification(Context context, PendingIntent pendingIntent, String title, String message) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_zoomlee)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(context.getResources().getColor(R.color.green_zoomlee))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        Notification notification = builder.build();
        manager.notify((int) System.currentTimeMillis(), notification);
    }
}
