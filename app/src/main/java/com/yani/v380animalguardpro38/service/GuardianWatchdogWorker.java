package com.yani.v380animalguardpro38.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yani.v380animalguardpro38.data.AppPrefs;

public class GuardianWatchdogWorker extends Worker {
    public GuardianWatchdogWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        AppPrefs prefs = new AppPrefs(ctx);
        if (prefs.config().autoRestart && prefs.raw().getBoolean("running", false)) {
            Intent svc = new Intent(ctx, GuardianService.class);
            svc.setAction("START");
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(svc); else ctx.startService(svc);
            prefs.log("Watchdog checked guardian service");
        }
        return Result.success();
    }
}
