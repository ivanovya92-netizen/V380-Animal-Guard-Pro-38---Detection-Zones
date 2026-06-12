package com.yani.v380animalguardpro38.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.yani.v380animalguardpro38.data.AppPrefs;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        AppPrefs prefs = new AppPrefs(context);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            prefs.log("Boot/package event received");
            if (prefs.config().autoRestart) {
                Intent svc = new Intent(context, GuardianService.class);
                svc.setAction("START");
                if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(svc); else context.startService(svc);
            }
        }
    }
}
