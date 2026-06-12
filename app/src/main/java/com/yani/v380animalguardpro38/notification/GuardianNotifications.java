package com.yani.v380animalguardpro38.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;

import com.yani.v380animalguardpro38.data.AppPrefs;
import com.yani.v380animalguardpro38.service.GuardianActionReceiver;
import com.yani.v380animalguardpro38.ui.AlarmActivity;
import com.yani.v380animalguardpro38.ui.MainActivity;

public class GuardianNotifications {
    public static final String CH_STATUS = "animal_guard_pro38_status";
    public static final String CH_EVENT = "animal_guard_pro38_event";
    public static final String CH_ERROR = "animal_guard_pro38_error";
    public static final String CH_ALARM = "animal_guard_pro38_alarm";
    public static final int ALARM_NOTIFICATION_ID = 9090;

    public void createChannels(Context ctx) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel status = new NotificationChannel(CH_STATUS, "Guardian status", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(status);

            NotificationChannel event = new NotificationChannel(CH_EVENT, "Camera event alerts", NotificationManager.IMPORTANCE_HIGH);
            event.enableVibration(true);
            event.setDescription("Urgent V380 event alerts");
            nm.createNotificationChannel(event);

            NotificationChannel alarm = new NotificationChannel(CH_ALARM, "BOAR ALARM - fullscreen", NotificationManager.IMPORTANCE_HIGH);
            alarm.enableVibration(true);
            alarm.setVibrationPattern(new long[]{0, 700, 300, 700, 300, 1200});
            alarm.setDescription("Fullscreen boar alarm and reminders");
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri != null) {
                AudioAttributes aa = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                alarm.setSound(alarmUri, aa);
            }
            nm.createNotificationChannel(alarm);

            NotificationChannel error = new NotificationChannel(CH_ERROR, "Guardian errors", NotificationManager.IMPORTANCE_HIGH);
            error.setDescription("Cloud, token and network warnings");
            nm.createNotificationChannel(error);
        }
    }

    public Notification statusNotification(Context ctx, String text) {
        Intent open = new Intent(ctx, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(ctx, 100, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stop = new Intent(ctx, GuardianActionReceiver.class);
        stop.setAction(GuardianActionReceiver.ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getBroadcast(ctx, 101, stop, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder b = new Notification.Builder(ctx)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentTitle("V380 Animal Guard Pro 38")
                .setContentText(text)
                .setContentIntent(openPi)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPi);

        if (Build.VERSION.SDK_INT >= 26) b.setChannelId(CH_STATUS);
        return b.build();
    }

    public void showBoarAlarm(Context ctx, String reason, String imageUrl) {
        createChannels(ctx);
        AppPrefs prefs = new AppPrefs(ctx);

        prefs.raw().edit()
                .putBoolean("pendingAck", true)
                .putString("lastAlarmReason", reason)
                .putString("lastImageUrl", imageUrl == null ? "" : imageUrl)
                .apply();

        Intent full = new Intent(ctx, AlarmActivity.class);
        full.putExtra("reason", reason);
        full.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fullPi = PendingIntent.getActivity(ctx, 910, full, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent open = imageUrl != null && imageUrl.length() > 0
                ? new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
                : new Intent(ctx, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(ctx, 911, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent ack = new Intent(ctx, GuardianActionReceiver.class);
        ack.setAction(GuardianActionReceiver.ACTION_ACK);
        PendingIntent ackPi = PendingIntent.getBroadcast(ctx, 912, ack, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent snooze = new Intent(ctx, GuardianActionReceiver.class);
        snooze.setAction(GuardianActionReceiver.ACTION_SNOOZE);
        PendingIntent snoozePi = PendingIntent.getBroadcast(ctx, 913, snooze, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent v380 = ctx.getPackageManager().getLaunchIntentForPackage("com.macrovideo.v380pro");
        PendingIntent v380Pi = null;
        if (v380 != null) v380Pi = PendingIntent.getActivity(ctx, 914, v380, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder b = new Notification.Builder(ctx)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("⚠️ ДИВО ПРАСЕ / BOAR ALARM")
                .setContentText(reason)
                .setContentIntent(fullPi)
                .setFullScreenIntent(fullPi, prefs.config().phoneAlarmFullScreen)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setAutoCancel(false)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "ACK", ackPi)
                .addAction(android.R.drawable.ic_media_pause, "SNOOZE", snoozePi)
                .addAction(android.R.drawable.ic_menu_view, "OPEN", openPi);

        if (v380Pi != null) b.addAction(android.R.drawable.ic_menu_camera, "V380", v380Pi);

        if (prefs.config().phoneAlarmSound || prefs.config().phoneAlarmVibrate) {
            b.setDefaults(Notification.DEFAULT_ALL);
        }
        if (Build.VERSION.SDK_INT >= 26) b.setChannelId(CH_ALARM);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(ALARM_NOTIFICATION_ID, b.build());

        if (prefs.config().phoneAlarmReminderEnabled) {
            scheduleReminder(ctx, prefs.config().phoneAlarmReminderSec);
        }
    }

    public void scheduleReminder(Context ctx, int delaySec) {
        Intent i = new Intent(ctx, GuardianActionReceiver.class);
        i.setAction(GuardianActionReceiver.ACTION_REMINDER);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 920, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        long when = SystemClock.elapsedRealtime() + Math.max(10, delaySec) * 1000L;
        try {
            if (Build.VERSION.SDK_INT >= 23) am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, when, pi);
            else am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, when, pi);
        } catch (Throwable t) {
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, when, pi);
        }
    }

    public void showEventNotification(Context ctx, String reason, String imageUrl, boolean loud) {
        createChannels(ctx);

        Intent open;
        if (imageUrl != null && imageUrl.length() > 0) open = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        else open = new Intent(ctx, MainActivity.class);

        PendingIntent openPi = PendingIntent.getActivity(ctx, (int) (System.currentTimeMillis() % 100000), open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent ack = new Intent(ctx, GuardianActionReceiver.class);
        ack.setAction(GuardianActionReceiver.ACTION_ACK);
        PendingIntent ackPi = PendingIntent.getBroadcast(ctx, 201, ack, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent v380 = ctx.getPackageManager().getLaunchIntentForPackage("com.macrovideo.v380pro");
        PendingIntent v380Pi = null;
        if (v380 != null) v380Pi = PendingIntent.getActivity(ctx, 202, v380, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder b = new Notification.Builder(ctx)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("V380 alert")
                .setContentText(reason)
                .setContentIntent(openPi)
                .setAutoCancel(true)
                .setPriority(loud ? Notification.PRIORITY_MAX : Notification.PRIORITY_HIGH)
                .addAction(android.R.drawable.ic_menu_view, "Open", openPi)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Ack", ackPi);

        if (v380Pi != null) b.addAction(android.R.drawable.ic_menu_camera, "V380", v380Pi);
        if (loud) b.setDefaults(Notification.DEFAULT_ALL);
        if (Build.VERSION.SDK_INT >= 26) b.setChannelId(CH_EVENT);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) (System.currentTimeMillis() % 100000), b.build());
    }

    public void showErrorNotification(Context ctx, String reason) {
        createChannels(ctx);

        Intent open = new Intent(ctx, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(ctx, 301, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder b = new Notification.Builder(ctx)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("V380 Guardian error")
                .setContentText(reason)
                .setContentIntent(openPi)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= 26) b.setChannelId(CH_ERROR);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(303, b.build());
    }
}
