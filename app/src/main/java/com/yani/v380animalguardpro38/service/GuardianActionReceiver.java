package com.yani.v380animalguardpro38.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.yani.v380animalguardpro38.data.AppPrefs;
import com.yani.v380animalguardpro38.notification.GuardianNotifications;

public class GuardianActionReceiver extends BroadcastReceiver {
    public static final String ACTION_ACK = "com.yani.v380animalguardpro38.ACK";
    public static final String ACTION_STOP = "com.yani.v380animalguardpro38.STOP";
    public static final String ACTION_SNOOZE = "com.yani.v380animalguardpro38.SNOOZE";
    public static final String ACTION_REMINDER = "com.yani.v380animalguardpro38.REMINDER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        AppPrefs prefs = new AppPrefs(context);
        String action = intent.getAction();

        if (ACTION_ACK.equals(action)) {
            prefs.raw().edit()
                    .putBoolean("pendingAck", false)
                    .putInt("alarmReminderCount", 0)
                    .apply();
            cancelAlarm(context);
            prefs.log("Boar alarm acknowledged");
        } else if (ACTION_SNOOZE.equals(action)) {
            prefs.raw().edit()
                    .putBoolean("pendingAck", true)
                    .putLong("snoozedUntilMs", System.currentTimeMillis() + prefs.config().phoneAlarmSnoozeMin * 60_000L)
                    .apply();
            cancelAlarm(context);
            new GuardianNotifications().scheduleReminder(context, prefs.config().phoneAlarmSnoozeMin * 60);
            prefs.log("Boar alarm snoozed");
        } else if (ACTION_REMINDER.equals(action)) {
            if (!prefs.raw().getBoolean("pendingAck", false)) return;
            int count = prefs.raw().getInt("alarmReminderCount", 0);
            if (count >= prefs.config().phoneAlarmMaxReminders) {
                prefs.log("Boar alarm reminder max reached");
                return;
            }

            String reason = prefs.raw().getString("lastAlarmReason", "Boar alert still pending");
            String image = prefs.raw().getString("lastImageUrl", "");
            prefs.raw().edit().putInt("alarmReminderCount", count + 1).apply();
            new GuardianNotifications().showBoarAlarm(context, reason + " | reminder " + (count + 1), image);
            prefs.log("Boar alarm reminder fired " + (count + 1));
        } else if (ACTION_STOP.equals(action)) {
            Intent svc = new Intent(context, GuardianService.class);
            svc.setAction("STOP");
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(svc); else context.startService(svc);
            prefs.log("Stop requested from notification");
        }
    }

    private void cancelAlarm(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(GuardianNotifications.ALARM_NOTIFICATION_ID);
    }
}
