package com.yani.v380animalguardpro38.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import com.yani.v380animalguardpro38.animal.AnimalClassifier;
import com.yani.v380animalguardpro38.data.AppPrefs;
import com.yani.v380animalguardpro38.data.EventHistoryStore;
import com.yani.v380animalguardpro38.ml.SnapshotIntelligence;
import com.yani.v380animalguardpro38.model.AppConfig;
import com.yani.v380animalguardpro38.model.EventRecord;
import com.yani.v380animalguardpro38.network.NtfyClient;
import com.yani.v380animalguardpro38.network.V380CloudClient;
import com.yani.v380animalguardpro38.notification.GuardianNotifications;
import com.yani.v380animalguardpro38.siren.DirectSirenProtocol;
import com.yani.v380animalguardpro38.util.TimeUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GuardianService extends Service {
    private static final int SERVICE_ID = 1010;
    private static final long ERROR_NOTIFY_GAP_MS = 10 * 60 * 1000L;

    private AppPrefs appPrefs;
    private SharedPreferences prefs;
    private GuardianNotifications notifications;
    private HandlerThread thread;
    private Handler handler;
    private PowerManager.WakeLock wakeLock;
    private boolean running = false;
    private int failures = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        appPrefs = new AppPrefs(this);
        prefs = appPrefs.raw();
        notifications = new GuardianNotifications();
        notifications.createChannels(this);

        thread = new HandlerThread("V380AnimalGuardPro38DetectionZones");
        thread.start();
        handler = new Handler(thread.getLooper());

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "V380AnimalGuardPro38DetectionZones:poll");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(6 * 60 * 60 * 1000L);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : "START";

        if ("STOP".equals(action)) {
            stopGuardian();
            return START_NOT_STICKY;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    SERVICE_ID,
                    notifications.statusNotification(this, "Starting"),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            );
        } else {
            startForeground(SERVICE_ID, notifications.statusNotification(this, "Starting"));
        }

        if ("TEST_ONCE".equals(action)) {
            handler.post(() -> pollOnce(true));
            return START_STICKY;
        }

        if ("TEST_SIREN".equals(action)) {
            handler.post(() -> triggerOfficialSiren(appPrefs.config(), "manual service siren test", true));
            return START_STICKY;
        }

        if ("TEST_PHONE_ALARM".equals(action)) {
            handler.post(() -> notifications.showBoarAlarm(this, "Manual phone alarm test", prefs.getString("lastImageUrl", "")));
            return START_STICKY;
        }

        if ("TEST_FULL_FIELD_ALARM".equals(action)) {
            handler.post(() -> {
                AppConfig cfg = appPrefs.config();
                notifications.showBoarAlarm(this, "Manual FULL FIELD TEST: phone alarm + camera siren", prefs.getString("lastImageUrl", ""));
                triggerOfficialSiren(cfg, "manual full field test", true);
            });
            return START_STICKY;
        }

        if (!running) {
            running = true;
            failures = prefs.getInt("failures", 0);
            prefs.edit().putBoolean("running", true).putString("startedAt", TimeUtils.now()).apply();
            appPrefs.log("Guardian started");
            schedule(1000);
        }

        return START_STICKY;
    }

    private void stopGuardian() {
        running = false;
        prefs.edit()
                .putBoolean("running", false)
                .putString("lastPollStatus", TimeUtils.nowShort() + "  stopped")
                .putLong("testUntilMs", 0L)
                .apply();
        appPrefs.log("Guardian stopped");
        stopForeground(true);
        stopSelf();
    }

    private void schedule(long delayMs) {
        if (!running || handler == null) return;
        handler.postDelayed(() -> {
            if (testWindowExpired()) {
                notifications.showEventNotification(this, "30 minute supervised test finished", "", false);
                stopGuardian();
                return;
            }

            pollOnce(false);
            int interval = currentIntervalSeconds();
            int backoff = failures <= 0 ? interval : Math.min(300, interval * Math.min(8, failures));
            schedule(backoff * 1000L);
        }, delayMs);
    }

    private boolean testWindowExpired() {
        long until = prefs.getLong("testUntilMs", 0L);
        return until > 0L && System.currentTimeMillis() > until;
    }

    private int currentIntervalSeconds() {
        long testUntil = prefs.getLong("testUntilMs", 0L);
        if (testUntil > System.currentTimeMillis()) return 10;
        AppConfig cfg = appPrefs.config();
        if (cfg.aggressiveMode) return 10;
        return Math.max(10, cfg.intervalSec);
    }

    private void pollOnce(boolean manual) {
        try {
            if (!hasNetwork()) {
                fail("No network", manual);
                return;
            }

            AppConfig cfg = appPrefs.config();

            if (empty(cfg.baseUrl) || empty(cfg.accessToken) || empty(cfg.userId) || empty(cfg.deviceId)) {
                fail("Missing settings", manual);
                return;
            }

            V380CloudClient.CloudResult result = new V380CloudClient().queryReplay(cfg);
            if (!result.ok()) {
                fail(result.error == null ? "Cloud error" : result.error, manual);
                return;
            }

            JSONArray items = result.items;
            if (items == null || items.length() == 0) {
                ok("No cloud events");
                if (manual) notifications.showEventNotification(this, "Cloud test OK: no events returned", "", false);
                return;
            }

            handleCloudItems(cfg, items, manual);
        } catch (Throwable t) {
            fail("Error " + t.getClass().getSimpleName() + ": " + t.getMessage(), manual);
        }
    }

    private void handleCloudItems(AppConfig cfg, JSONArray items, boolean manual) throws Exception {
        long lastSeen = prefs.getLong("lastSeenId", 0L);
        long maxId = 0L;
        long newestId = 0L;
        long newestTime = 0L;
        int newCount = 0;
        String newestImage = "";

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            long id = item.optLong("id", 0L);
            if (id > maxId) maxId = id;

            if (id > lastSeen) {
                newCount++;
                newestId = id;
                newestTime = item.optLong("rec_time", 0L);
                newestImage = item.optString("image_url", "");
            }
        }

        if (lastSeen == 0L && !cfg.notifyFirstRun) {
            prefs.edit()
                    .putLong("lastSeenId", maxId)
                    .putString("lastImageUrl", newestImage)
                    .putString("lastPollStatus", TimeUtils.nowShort() + "  Baseline set " + maxId)
                    .apply();

            ok("Baseline set " + maxId + " from " + items.length() + " item(s)");
            if (manual) notifications.showEventNotification(this, "Cloud test OK. Baseline set: " + maxId, newestImage, false);
            return;
        }

        if (newCount > 0) {
            prefs.edit()
                    .putLong("lastSeenId", maxId)
                    .putString("lastImageUrl", newestImage)
                    .putBoolean("pendingAck", true)
                    .putString("guardState", "EVENT_FOUND")
                    .apply();

            String msg = "New V380 event: " + newCount + " item(s), " + TimeUtils.eventTime(newestTime);
            boolean quiet = appPrefs.isQuietNow();
            notifications.showEventNotification(this, msg, newestImage, !quiet);
            new NtfyClient().sendAsync(cfg, msg + (newestImage.length() > 0 ? " " + newestImage : ""));

            EventHistoryStore history = new EventHistoryStore(this);
            history.upsert(new EventRecord(newestId, newestTime, newCount, newestImage, ""));

            boolean smartMode = "SMART_ANIMAL".equals(cfg.triggerMode);
            boolean needsAnalysis = cfg.smartSnapshot && newestImage.length() > 0;

            if (needsAnalysis) {
                long finalNewestId = newestId;
                long finalNewestTime = newestTime;
                String finalNewestImage = newestImage;
                int finalNewCount = newCount;

                prefs.edit().putString("guardState", "ANALYZING").apply();
                new SnapshotIntelligence().analyzeAsync(this, newestImage, summary -> {
                    AppConfig latestCfg = appPrefs.config();
                    AnimalClassifier.Result animal = AnimalClassifier.classify(latestCfg, summary, appPrefs.isArmedNow());
                    String enriched = summary + " | AnimalGuard: " + animal.summaryLine();

                    if (latestCfg.animalHistoryEnabled) {
                        new EventHistoryStore(this).upsert(new EventRecord(finalNewestId, finalNewestTime, finalNewCount, finalNewestImage, enriched));
                    }

                    prefs.edit()
                            .putString("lastAnimal", animal.animal)
                            .putInt("lastAnimalConfidence", animal.confidence)
                            .putString("lastAnalysis", enriched)
                            .putString("lastBoarVisionDecision", animal.reason)
                            .putLong("lastReviewedEventId", finalNewestId)
                            .apply();
                    appPrefs.log("Animal analysis: " + enriched);

                    if (latestCfg.animalAlertsEnabled && animal.shouldNotify) {
                        String animalMsg = "Animal detected: " + animal.animal + " (" + animal.confidence + "%) - " + animal.reason;
                        boolean loud = latestCfg.animalLocalLoud && !appPrefs.isQuietNow();
                        notifications.showEventNotification(this, animalMsg, finalNewestImage, loud);
                        if (latestCfg.animalNtfyEnabled) {
                            new NtfyClient().sendAsync(latestCfg, animalMsg + (finalNewestImage.length() > 0 ? " " + finalNewestImage : ""));
                        }
                    }

                    if ("SMART_ANIMAL".equals(latestCfg.triggerMode)) {
                        if (animal.shouldSiren && latestCfg.fieldAutoDeterrenceEnabled) {
                            if (latestCfg.fieldTriggerPhoneAlarm && latestCfg.phoneAlarmEnabled) {
                                notifications.showBoarAlarm(this, "SMART_ANIMAL " + animal.summaryLine(), finalNewestImage);
                            }
                            if (latestCfg.fieldTriggerCameraSiren) {
                                triggerOfficialSiren(latestCfg, "SMART_ANIMAL " + animal.summaryLine(), false);
                            }
                        } else {
                            prefs.edit().putString("guardState", "NO_ANIMAL_SIREN_MATCH").apply();
                            appPrefs.log("Smart animal mode skipped siren: " + animal.summaryLine());
                        }
                    }
                });
            }

            if (!smartMode) {
                if (shouldTriggerOnEvent(cfg)) {
                    triggerOfficialSiren(cfg, "event mode " + cfg.triggerMode + " for event " + newestId, false);
                } else {
                    appPrefs.log("Auto siren skipped by guard policy");
                }
            } else if (!needsAnalysis && cfg.triggerOnUnknownAtNight && appPrefs.isArmedNow()) {
                triggerOfficialSiren(cfg, "SMART_ANIMAL no snapshot but unknown-at-night enabled", false);
            }

            ok("Alert sent. Last event " + maxId);
        } else {
            ok("No new events. Last " + lastSeen);
            if (manual) notifications.showEventNotification(this, "Cloud test OK. No new events. Last " + lastSeen, prefs.getString("lastImageUrl", ""), false);
        }
    }

    private boolean shouldTriggerOnEvent(AppConfig cfg) {
        if (!cfg.autoSiren) return false;

        if ("TEST_NEXT_EVENT".equals(cfg.triggerMode) || cfg.testNextEventOnly) {
            prefs.edit().putBoolean("testNextEventOnly", false).putString("triggerMode", "SAFE_NIGHT").apply();
            return true;
        }

        if (cfg.sirenOnlyForSelectedAnimals) {
            appPrefs.log("Siren blocked until SMART_ANIMAL selected animal confirms");
            return false;
        }

        if ("ANY_MOTION".equals(cfg.triggerMode)) return appPrefs.isArmedNow();
        if ("SAFE_NIGHT".equals(cfg.triggerMode)) return appPrefs.isArmedNow();

        return false;
    }

    private boolean looksRelevantForSiren(AppConfig cfg, String summary) {
        AnimalClassifier.Result animal = AnimalClassifier.classify(cfg, summary, appPrefs.isArmedNow());
        return animal.shouldSiren;
    }

    private boolean canPassSirenSafety(AppConfig cfg, String reason, boolean manual) {
        if (!manual) {
            if (!cfg.sirenMasterAllowed) {
                prefs.edit().putString("guardState", "SIREN_MASTER_OFF").apply();
                appPrefs.log("Siren blocked: master siren switch OFF");
                return false;
            }

            if (!cfg.autoSiren) {
                appPrefs.log("Siren blocked: Auto siren OFF");
                return false;
            }
            if (!appPrefs.isArmedNow()) {
                prefs.edit().putString("guardState", "OUTSIDE_ARMED_HOURS").apply();
                appPrefs.log("Siren blocked: outside armed hours");
                return false;
            }
        }

        long now = System.currentTimeMillis();
        long last = prefs.getLong("lastSirenAt", 0L);
        if (!manual && cfg.cooldownSec > 0 && now - last < cfg.cooldownSec * 1000L) {
            long left = (cfg.cooldownSec * 1000L - (now - last)) / 1000L;
            prefs.edit().putString("guardState", "COOLDOWN").apply();
            appPrefs.log("Siren blocked: cooldown " + left + " sec left");
            return false;
        }

        String bucket = new SimpleDateFormat("yyyyMMddHH", Locale.getDefault()).format(new Date(now));
        String oldBucket = prefs.getString("sirenHourBucket", "");
        int count = bucket.equals(oldBucket) ? prefs.getInt("sirenHourCount", 0) : 0;
        if (!manual && cfg.maxSirenPerHour > 0 && count >= cfg.maxSirenPerHour) {
            prefs.edit().putString("guardState", "HOURLY_LIMIT").apply();
            appPrefs.log("Siren blocked: hourly limit reached");
            return false;
        }

        String nightBucket = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date(now));
        String oldNightBucket = prefs.getString("sirenNightBucket", "");
        int nightCount = nightBucket.equals(oldNightBucket) ? prefs.getInt("sirenNightCount", 0) : 0;
        if (!manual && cfg.maxSirensPerNight > 0 && nightCount >= cfg.maxSirensPerNight) {
            prefs.edit().putString("guardState", "NIGHT_LIMIT").apply();
            appPrefs.log("Siren blocked: night limit reached");
            return false;
        }

        prefs.edit()
                .putString("sirenHourBucket", bucket)
                .putInt("sirenHourCount", count + 1)
                .putString("sirenNightBucket", nightBucket)
                .putInt("sirenNightCount", nightCount + 1)
                .putLong("lastSirenAt", now)
                .putString("lastSirenStatus", TimeUtils.nowShort() + "  requested: " + reason)
                .putString("guardState", "SIREN_REQUESTED")
                .apply();

        return true;
    }
private void triggerOfficialSiren(AppConfig cfg, String reason, boolean manual) {
    if (!canPassSirenSafety(cfg, reason, manual)) return;

    prefs.edit()
            .putString("guardState", "DIRECT_SIREN_RUNNING")
            .putString("lastSirenStatus", TimeUtils.nowShort() + "  S01 direct siren running: " + reason)
            .apply();

    notifications.showEventNotification(this, "V380 siren triggered: " + reason, prefs.getString("lastImageUrl", ""), true);
    appPrefs.log("Direct siren protocol started: " + reason);

    DirectSirenProtocol.triggerAsync(cfg, reason, (ok, result) -> {
        String shortResult = result == null ? "" : result;
        if (shortResult.length() > 1600) shortResult = shortResult.substring(0, 1600);

        prefs.edit()
                .putString("guardState", ok ? "DIRECT_SIREN_SENT" : "DIRECT_SIREN_ERROR")
                .putString("lastSirenStatus", TimeUtils.nowShort() + (ok ? "  S01 direct siren sent" : "  S01 direct siren error") + ": " + reason)
                .putString("lastSirenProtocolResult", shortResult)
                .apply();

        appPrefs.log((ok ? "Direct siren OK: " : "Direct siren ERROR: ") + reason + "\n" + shortResult);

        AppConfig latest = appPrefs.config();
        if (latest.ntfyEnabled) {
            new NtfyClient().sendAsync(latest, ok
                    ? "V380 siren sent: " + reason
                    : "V380 siren error: " + reason);
        }
    });
}

@Override
    public void onDestroy() {
        running = false;
        try {
            if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        } catch (Throwable ignored) {}

        try {
            if (thread != null) thread.quitSafely();
        } catch (Throwable ignored) {}

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
