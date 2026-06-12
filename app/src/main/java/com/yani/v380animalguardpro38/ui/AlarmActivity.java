package com.yani.v380animalguardpro38.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yani.v380animalguardpro38.data.AppPrefs;
import com.yani.v380animalguardpro38.notification.GuardianNotifications;

public class AlarmActivity extends Activity {
    private Ringtone ringtone;
    private Vibrator vibrator;
    private AppPrefs prefs;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        prefs = new AppPrefs(this);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );

        String reason = getIntent().getStringExtra("reason");
        if (reason == null || reason.length() == 0) reason = prefs.raw().getString("lastAlarmReason", "Boar alert");

        buildUi(reason);
        startAlarmOutputs();
    }

    private void buildUi(String reason) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(127, 29, 29));

        TextView title = new TextView(this);
        title.setText("⚠️ ДИВО ПРАСЕ / BOAR ALERT");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        TextView body = new TextView(this);
        body.setText(reason);
        body.setTextColor(Color.WHITE);
        body.setTextSize(18);
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, 0, 0, dp(22));
        root.addView(body);

        Button open = new Button(this);
        open.setText("OPEN V380");
        open.setOnClickListener(v -> openV380());
        root.addView(open, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button snooze = new Button(this);
        snooze.setText("SNOOZE 5 MIN");
        snooze.setOnClickListener(v -> snooze());
        root.addView(snooze, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button dismiss = new Button(this);
        dismiss.setText("DISMISS / ACK");
        dismiss.setOnClickListener(v -> ack());
        root.addView(dismiss, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private void startAlarmOutputs() {
        try {
            AppPrefs p = new AppPrefs(this);
            if (p.config().phoneAlarmSound) {
                Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                ringtone = RingtoneManager.getRingtone(this, alarmUri);
                if (ringtone != null && !ringtone.isPlaying()) ringtone.play();
            }
            if (p.config().phoneAlarmVibrate) {
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null) vibrator.vibrate(new long[]{0, 700, 300, 700, 300, 1200}, 0);
            }
        } catch (Throwable ignored) {}
    }

    private void stopAlarmOutputs() {
        try { if (ringtone != null && ringtone.isPlaying()) ringtone.stop(); } catch (Throwable ignored) {}
        try { if (vibrator != null) vibrator.cancel(); } catch (Throwable ignored) {}
    }

    private void openV380() {
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.macrovideo.v380pro");
        if (launch != null) startActivity(launch);
    }

    private void snooze() {
        stopAlarmOutputs();
        prefs.raw().edit()
                .putBoolean("pendingAck", true)
                .putLong("snoozedUntilMs", System.currentTimeMillis() + prefs.config().phoneAlarmSnoozeMin * 60_000L)
                .apply();
        new GuardianNotifications().scheduleReminder(this, prefs.config().phoneAlarmSnoozeMin * 60);
        cancelNotification();
        finish();
    }

    private void ack() {
        stopAlarmOutputs();
        prefs.raw().edit()
                .putBoolean("pendingAck", false)
                .putInt("alarmReminderCount", 0)
                .apply();
        cancelNotification();
        finish();
    }

    private void cancelNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(GuardianNotifications.ALARM_NOTIFICATION_ID);
    }

    @Override
    protected void onDestroy() {
        stopAlarmOutputs();
        super.onDestroy();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
