package com.yani.v380animalguardpro38.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class OfficialTapService extends AccessibilityService {
    private static OfficialTapService instance;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static boolean isReady() {
        return instance != null;
    }

    public static boolean tapAlarmOnly(Context ctx, boolean doubleTap) {
        OfficialTapService svc = instance;
        if (svc == null) {
            Toast.makeText(ctx, "Accessibility service is not enabled yet", Toast.LENGTH_LONG).show();
            return false;
        }
        svc.performAlarmOnly(doubleTap);
        return true;
    }

    public static boolean tapMoreOnly(Context ctx) {
        OfficialTapService svc = instance;
        if (svc == null) {
            Toast.makeText(ctx, "Accessibility service is not enabled yet", Toast.LENGTH_LONG).show();
            return false;
        }
        SharedPreferences prefs = svc.getSharedPreferences("v380_animal_guard_pro38_settings", MODE_PRIVATE);
        svc.tapPercent(prefs.getInt("moreXPercent", 88), prefs.getInt("moreYPercent", 91), "More");
        return true;
    }

    public static boolean tapMoreThenAlarm(Context ctx, boolean doubleAlarmTap) {
        OfficialTapService svc = instance;
        if (svc == null) {
            Toast.makeText(ctx, "Accessibility service is not enabled yet", Toast.LENGTH_LONG).show();
            return false;
        }
        svc.performMoreThenAlarm(doubleAlarmTap);
        return true;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Toast.makeText(this, "V380 Animal Guard Pro tapper enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        if (instance == this) instance = null;
        super.onDestroy();
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    private void performAlarmOnly(boolean doubleTap) {
        SharedPreferences prefs = getSharedPreferences("v380_animal_guard_pro38_settings", MODE_PRIVATE);
        int x = prefs.getInt("alarmXPercent", 13);
        int y = prefs.getInt("alarmYPercent", 72);
        tapPercent(x, y, "Alarm");
        if (doubleTap) handler.postDelayed(() -> tapPercent(x, y, "Alarm second"), 900);
    }

    private void performMoreThenAlarm(boolean doubleAlarmTap) {
        SharedPreferences prefs = getSharedPreferences("v380_animal_guard_pro38_settings", MODE_PRIVATE);
        int moreX = prefs.getInt("moreXPercent", 88);
        int moreY = prefs.getInt("moreYPercent", 91);
        int alarmX = prefs.getInt("alarmXPercent", 13);
        int alarmY = prefs.getInt("alarmYPercent", 72);
        int delay = prefs.getInt("moreToAlarmDelayMs", 900);

        tapPercent(moreX, moreY, "More");
        handler.postDelayed(() -> {
            tapPercent(alarmX, alarmY, "Alarm");
            if (doubleAlarmTap) handler.postDelayed(() -> tapPercent(alarmX, alarmY, "Alarm second"), 900);
        }, delay);
    }

    private void tapPercent(int xPercent, int yPercent, String label) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float x = dm.widthPixels * (xPercent / 100f);
        float y = dm.heightPixels * (yPercent / 100f);
        dispatchTap(x, y, label);
    }

    private void dispatchTap(float x, float y, String label) {
        if (Build.VERSION.SDK_INT < 24) {
            Toast.makeText(this, "Gesture tap requires Android 7 or newer", Toast.LENGTH_LONG).show();
            return;
        }

        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 120))
                .build();

        boolean ok = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Toast.makeText(OfficialTapService.this, label + " tap sent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Toast.makeText(OfficialTapService.this, label + " tap cancelled", Toast.LENGTH_SHORT).show();
            }
        }, null);

        if (!ok) Toast.makeText(this, label + " gesture dispatch failed", Toast.LENGTH_LONG).show();
    }
}
