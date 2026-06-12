package com.yani.v380animalguardpro38.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yani.v380animalguardpro38.backtest.BacktestEngine;
import com.yani.v380animalguardpro38.data.AppPrefs;
import com.yani.v380animalguardpro38.model.AppConfig;
import com.yani.v380animalguardpro38.service.GuardianService;

public class MainActivity extends AppCompatActivity {
    private AppPrefs appPrefs;
    private android.content.SharedPreferences prefs;
    private UiFactory ui;
    private LinearLayout root;
    private LinearLayout content;
    private TextView hero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPrefs = new AppPrefs(this);
        prefs = appPrefs.raw();
        ui = new UiFactory(this);
        requestNotificationPermission();
        applyModernDefaults(false);
        buildUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1010);
        }
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.setBackgroundColor(Color.rgb(2, 6, 23));
        root.setBackgroundColor(Color.rgb(2, 6, 23));
        root.setPadding(dp(16), dp(16), dp(16), dp(28));
        scroll.addView(root);
        setContentView(scroll);

        hero = ui.hero(root, "V380 Animal Guard Pro 38", "Detection Zones: monitor only selected areas");

        LinearLayout nav1 = ui.row(root);
        ui.metricCard(nav1, "Home", "Overview").setOnClickListener(v -> setPage("HOME"));
        ui.metricCard(nav1, "Guard", "Control").setOnClickListener(v -> setPage("GUARD"));

        LinearLayout nav2 = ui.row(root);
        ui.metricCard(nav2, "AI", "Animals").setOnClickListener(v -> setPage("AI"));
        ui.metricCard(nav2, "Events", "History").setOnClickListener(v -> setPage("EVENTS"));

        LinearLayout nav3 = ui.row(root);
        ui.metricCard(nav3, "Backtest", "Quality").setOnClickListener(v -> setPage("BACKTEST"));
        ui.metricCard(nav3, "Review", "Train").setOnClickListener(v -> setPage("REVIEW"));

        LinearLayout nav4 = ui.row(root);
        ui.metricCard(nav4, "Zones", "Area").setOnClickListener(v -> setPage("ZONES"));
        ui.metricCard(nav4, "Settings", "Simple").setOnClickListener(v -> setPage("SETTINGS"));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content);
        render();
    }

    private void setPage(String page) {
        prefs.edit().putString("page", page).apply();
        render();
    }

    private void render() {
        if (content == null) return;
        content.removeAllViews();
        String page = prefs.getString("page", "HOME");
        if ("GUARD".equals(page)) renderGuard();
        else if ("AI".equals(page)) renderAi();
        else if ("EVENTS".equals(page)) renderEvents();
        else if ("BACKTEST".equals(page)) renderBacktest();
        else if ("REVIEW".equals(page)) renderReview();
        else if ("ZONES".equals(page)) renderZones();
        else if ("SETTINGS".equals(page)) renderSettings();
        else renderHome();
    }

    private void renderHome() {
        AppConfig c = appPrefs.config();
        boolean running = prefs.getBoolean("running", false);
        hero.setText("V380 Animal Guard Pro 38\n" + (running ? "🟢 Guard is running" : "⚪ Guard is stopped") + "\nZONES: active only, ignore trees/grass");

        ui.section(content, "Quick actions");
        ui.button(content, "START GUARD", this::startGuard);
        ui.button(content, "EDIT DETECTION ZONES", this::openZoneEditor);
        ui.button(content, "TEST FULL FIELD ALARM", this::testFullFieldAlarm);
        ui.button(content, "TEST SIREN NOW", this::testSiren);
        ui.button(content, "TEST PHONE ALARM", this::testPhoneAlarm);
        ui.dangerButton(content, "STOP", this::stopGuard);

        LinearLayout metrics = ui.row(content);
        ui.metricCard(metrics, "Siren", c.autoSiren ? "ON" : "OFF");
        ui.metricCard(metrics, "BoarVision", c.boarVisionEnabled ? "ON" : "OFF");
        LinearLayout metrics2 = ui.row(content);
        ui.metricCard(metrics2, "Armed", appPrefs.isArmedNow() ? "YES" : "NO");
        ui.metricCard(metrics2, "Cloud", prefs.getString("lastPollStatus", "not tested").contains("OK") ? "OK" : "WAIT");

        ui.card(content, "Last decision",
                "Animal: " + prefs.getString("lastAnimal", "none") + " " + prefs.getInt("lastAnimalConfidence", 0) + "%\n"
                + "Decision: " + trim(prefs.getString("lastBoarVisionDecision", "none"), 180) + "\n"
                + "State: " + prefs.getString("guardState", "idle") + "\n"
                + "Siren: " + prefs.getString("lastSirenStatus", "never"));

        ui.card(content, "Phone alarm mode",
                "Boar detection automatically triggers the CAMERA SIREN and also opens a fullscreen phone alarm.\n"
                + "Reminder interval: " + c.phoneAlarmReminderSec + " sec, max " + c.phoneAlarmMaxReminders + "\n"
                + "Full screen: " + c.phoneAlarmFullScreen + ", sound: " + c.phoneAlarmSound + ", vibrate: " + c.phoneAlarmVibrate);

        ui.card(content, "Simple mode",
                "No technical fields are shown. Token, device ID, relay host, ports, S01 frames and AI thresholds are handled inside the app.");
    }

    private void renderGuard() {
        AppConfig c = appPrefs.config();
        ui.section(content, "Guard control");
        ui.button(content, "START GUARD", this::startGuard);
        ui.dangerButton(content, "STOP", this::stopGuard);
        ui.button(content, "TEST FULL FIELD ALARM", this::testFullFieldAlarm);
        ui.button(content, "TEST SIREN NOW", this::testSiren);
        ui.button(content, "TEST PHONE ALARM", this::testPhoneAlarm);
        ui.secondaryButton(content, "CLOUD TEST ONCE", () -> startServiceAction("TEST_ONCE"));

        ui.card(content, "Guard policy",
                "Mode: Smart Animal\n"
                + "Siren protocol: S01 direct replay\n"
                + "Auto siren animals: " + c.sirenAnimals + "\n"
                + "Notify animals: " + c.notifyAnimals + "\n"
                + "Armed hours: " + c.armedStart + ":00-" + c.armedEnd + ":00\n"
                + "Cooldown: " + c.cooldownSec + " sec");

        ui.section(content, "Profiles");
        ui.secondaryButton(content, "Smart Night Guard", () -> setProfile("SMART_NIGHT"));
        ui.secondaryButton(content, "Monitoring Only", () -> setProfile("MONITOR_ONLY"));
        ui.secondaryButton(content, "Sensitive Night Guard", () -> setProfile("SENSITIVE"));
    }

    private void renderAi() {
        AppConfig c = appPrefs.config();
        ui.section(content, "AI Engine");
        ui.card(content, "Current AI setup",
                "Engine: Pro38 Zone-Gated Safe BoarVision\n"
                + "Profile: " + c.aiProfile + "\n"
                + "Ground-zone boost: " + (c.aiGroundZoneEnabled ? "ON" : "OFF") + "\n"
                + "Night boost: " + (c.aiNightBoostEnabled ? "ON" : "OFF") + "\n"
                + "Boar siren threshold: " + c.boarVisionSirenThreshold + "%\n"
                + "Camera profile: " + c.cameraProfileName + "\n"
                + "Camera ROI: x " + c.cameraRoiXStartPercent + "-" + c.cameraRoiXEndPercent + "%, y " + c.cameraRoiYStartPercent + "-" + c.cameraRoiYEndPercent + "%\n"
                + "Review threshold: " + c.boarVisionReviewThreshold + "%\n"
                + "Unknown-night threshold: " + c.aiUnknownNightThreshold + "%");

        ui.card(content, "Detection strategy",
                "ML Kit object detection + image labels + exact camera IR profile + boar silhouette scoring.\n"
                + "Person is safety-blocked from siren.\n"
                + "Boar and unknown at night can trigger S01 direct siren.");

        ui.section(content, "AI profiles");
        ui.secondaryButton(content, "Balanced", () -> setAiProfile("BALANCED"));
        ui.secondaryButton(content, "Sensitive", () -> setAiProfile("SENSITIVE"));
        ui.secondaryButton(content, "Strict", () -> setAiProfile("STRICT"));

        ui.card(content, "Last AI result", prefs.getString("lastAnalysis", "No snapshot analysis yet."));
    }

    private void renderEvents() {
        ui.section(content, "Events");
        ui.secondaryButton(content, "Open last snapshot", this::openLastSnapshot);
        ui.secondaryButton(content, "Copy diagnostics", this::copyDiagnostics);
        ui.secondaryButton(content, "Open V380 Pro", this::openV380);
        ui.secondaryButton(content, "Open battery settings", this::openBatterySettings);

        ui.card(content, "Last event",
                "ID: " + prefs.getLong("lastSeenId", 0L) + "\n"
                + "Cloud: " + prefs.getString("lastPollStatus", "not tested") + "\n"
                + "Protocol result: " + trim(prefs.getString("lastSirenProtocolResult", "none"), 900));

        ui.card(content, "History", trim(prefs.getString("history", "No events stored yet."), 1600));
    }


private void renderBacktest() {
    ui.section(content, "Backtesting");
    ui.card(content, "Why this matters",
            "This checks the AI decision logic before using it in the field.\n"
            + "The S01 siren protocol is not changed by backtesting.");

    ui.button(content, "RUN AI BACKTEST", this::runBacktest);
    ui.secondaryButton(content, "Copy backtest result", this::copyBacktest);

    ui.card(content, "Last backtest",
            prefs.getString("lastBacktestSummary", "Not run yet.") + "\n\n"
            + trim(prefs.getString("lastBacktestDetails", "Press RUN AI BACKTEST."), 1800));

    ui.card(content, "Protected siren model",
            "Siren mode: S01_DIRECT_REPLAY\n"
            + "Accessibility: OFF\n"
            + "UI tap: OFF\n"
            + "S01 frames: preserved from Lab 27 / Pro 28.");
}


private void renderZones() {
    AppConfig c = appPrefs.config();
    ui.section(content, "Detection Zones");
    ui.card(content, "Current zone gate",
            "Enabled: " + c.detectionZonesEnabled + "\n"
            + "Energy saver: " + c.zoneEnergySaverEnabled + "\n"
            + "Alarm requires ACTIVE zone: " + c.zoneRequireActive + "\n"
            + "IGNORE zones always block: " + c.zoneIgnoreBlocksAlways + "\n"
            + "Active zones: " + c.activeZones + "\n"
            + "Ignore zones: " + c.ignoreZones);

    ui.button(content, "EDIT DETECTION ZONES", this::openZoneEditor);
    ui.secondaryButton(content, "LOAD DEFAULT FIELD ZONES", this::loadDefaultZones);
    ui.secondaryButton(content, "ENABLE ZONES", () -> setZonesEnabled(true));
    ui.secondaryButton(content, "DISABLE ZONES", () -> setZonesEnabled(false));
    ui.secondaryButton(content, "OPEN LAST SNAPSHOT", this::openLastSnapshot);

    ui.card(content, "How it saves battery",
            "The app first checks if detected movement/object is inside your ACTIVE area and outside IGNORE areas.\n"
            + "Objects in trees, bushes, grass edge or bright right side are rejected before siren logic, which reduces useless work and false alarms.");
}

private void renderSettings() {
    AppConfig c = appPrefs.config();
    ui.section(content, "Simple settings");
    ui.card(content, "Current profile",
            "Guard profile: " + (c.autoSiren ? "Smart Night Guard" : "Monitoring Only") + "\n"
            + "AI profile: " + c.aiProfile + "\n"
            + "Design: High Contrast Safe\n"
            + "Technical backend: hidden\nPhone alarm: " + c.phoneAlarmEnabled + "\nReminders: " + c.phoneAlarmReminderEnabled + " every " + c.phoneAlarmReminderSec + " sec");

    ui.secondaryButton(content, "Smart Night Guard", () -> setProfile("SMART_NIGHT"));
    ui.secondaryButton(content, "Monitoring Only", () -> setProfile("MONITOR_ONLY"));
    ui.secondaryButton(content, "Sensitive Night Guard", () -> setProfile("SENSITIVE"));

    ui.card(content, "Advanced values are hidden",
            "No token, device ID, port, relay host, hex frames or protocol timing is shown here.\n"
            + "This keeps the app usable and prevents accidental breakage.");
}

private void runBacktest() {
    AppConfig c = appPrefs.config();
    BacktestEngine.Result r = BacktestEngine.run(c);
    prefs.edit()
            .putString("lastBacktestSummary", r.summaryLine() + (r.allPass() ? " | PASS" : " | REVIEW"))
            .putString("lastBacktestDetails", r.details)
            .apply();
    Toast.makeText(this, "Backtest finished: " + r.summaryLine(), Toast.LENGTH_LONG).show();
    render();
}

private void copyBacktest() {
    String d = prefs.getString("lastBacktestSummary", "No backtest") + "\n\n"
            + prefs.getString("lastBacktestDetails", "");
    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    cm.setPrimaryClip(ClipData.newPlainText("V380 Pro30 backtest", d));
    Toast.makeText(this, "Backtest copied", Toast.LENGTH_SHORT).show();
}

    private void setProfile(String profile) {
        AppConfig c = appPrefs.config();
        if ("MONITOR_ONLY".equals(profile)) {
            c.autoSiren = false;
            c.sirenMasterAllowed = false;
            c.aiProfile = "BALANCED";
            c.triggerMode = "SMART_ANIMAL";
        } else if ("SENSITIVE".equals(profile)) {
            c.autoSiren = true;
            c.sirenMasterAllowed = true;
            c.aiProfile = "SENSITIVE";
            c.minSmartConfidencePercent = 55;
            c.aiUnknownNightThreshold = 60;
            c.triggerMode = "SMART_ANIMAL";
        } else {
            c.autoSiren = true;
            c.sirenMasterAllowed = true;
            c.aiProfile = "BALANCED";
            c.minSmartConfidencePercent = 62;
            c.aiUnknownNightThreshold = 68;
            c.triggerMode = "SMART_ANIMAL";
        }
        appPrefs.save(c);
        Toast.makeText(this, "Profile applied: " + profile, Toast.LENGTH_SHORT).show();
        render();
    }

    private void setAiProfile(String profile) {
        AppConfig c = appPrefs.config();
        c.aiProfile = profile;
        if ("STRICT".equals(profile)) c.minSmartConfidencePercent = 70;
        else if ("SENSITIVE".equals(profile)) c.minSmartConfidencePercent = 55;
        else c.minSmartConfidencePercent = 62;
        appPrefs.save(c);
        Toast.makeText(this, "AI profile: " + profile, Toast.LENGTH_SHORT).show();
        render();
    }

    private void applyModernDefaults(boolean force) {
        if (!force && prefs.getBoolean("pro38Configured", false)) return;
        AppConfig c = new AppConfig();
        c.baseUrl = "http://47.254.175.150:8080";
        c.accessToken = "8c3d15be541247c07dd15108ff4b90b0d5e8fdda081f4db420f17e081be00256";
        c.userId = "100745185";
        c.deviceId = "120111823";
        c.relayHost = "170.187.189.98";
        c.relayPort = 8800;
        c.deviceHost = "120111823.nvdvr.net";
        c.intervalSec = 30;
        c.smartSnapshot = true;
        c.notifyErrors = true;
        c.autoRestart = true;
        c.ntfyEnabled = true;
        c.ntfyTopic = "v380_yani_devinalarm_7xq9m2p4";
        c.armedEnabled = true;
        c.armedStart = 20;
        c.armedEnd = 7;
        c.triggerMode = "SMART_ANIMAL";
        c.autoSiren = true;
        c.sirenMasterAllowed = true;
        c.officialTapperEnabled = false;
        c.cooldownSec = 180;
        c.maxSirenPerHour = 6;
        c.maxSirensPerNight = 20;
        c.animalAlertsEnabled = true;
        c.animalLocalLoud = true;
        c.animalNtfyEnabled = true;
        c.animalHistoryEnabled = true;
        c.sirenOnlyForSelectedAnimals = true;
        c.notifyAnimals = "boar,fox,dog,cat,bird,person,unknown";
        c.sirenAnimals = "boar,unknown";
        c.minSmartConfidencePercent = 62;
        c.triggerOnUnknownAtNight = true;
        c.sirenMode = "S01_DIRECT_REPLAY";
        c.aiProfile = "BALANCED";
        c.aiGroundZoneEnabled = true;
        c.aiNightBoostEnabled = true;
        c.aiSaveEventSummary = true;
        c.aiBoarThreshold = 72;
        c.aiUnknownNightThreshold = 68;
        c.aiPersonNoSirenThreshold = 70;
        c.aiPersonNeverSiren = true;
        c.aiLearningMode = true;
        c.boarVisionEnabled = true;
        c.boarVisionSirenThreshold = 78;
        c.boarVisionReviewThreshold = 58;
        c.boarVisionMode = "FIELD_SAFE";
        c.boarVisionLearnFromLabels = true;
        c.cameraBoarProfileEnabled = true;
        c.cameraProfileName = "DevinGarden_IR_Night_Boar";
        c.cameraRoiXStartPercent = 6;
        c.cameraRoiXEndPercent = 74;
        c.cameraRoiYStartPercent = 54;
        c.cameraRoiYEndPercent = 96;
        c.cameraBoarSignatureThreshold = 88;
        c.cameraBoarReviewThreshold = 68;
        c.phoneAlarmEnabled = true;
        c.phoneAlarmFullScreen = true;
        c.phoneAlarmSound = true;
        c.phoneAlarmVibrate = true;
        c.phoneAlarmReminderEnabled = true;
        c.phoneAlarmReminderSec = 60;
        c.phoneAlarmMaxReminders = 5;
        c.phoneAlarmSnoozeMin = 5;
        c.fieldAutoDeterrenceEnabled = true;
        c.fieldTriggerCameraSiren = true;
        c.fieldTriggerPhoneAlarm = true;
        c.fieldManualFullTestEnabled = true;
        c.safeConfirmedAlarmMode = true;
        c.cameraOnlyNoSiren = true;
        c.requireIndependentAnimalEvidenceForSiren = true;
        c.detectionZonesEnabled = true;
        c.zoneRequireActive = true;
        c.zoneIgnoreBlocksAlways = true;
        c.zoneEnergySaverEnabled = true;
        c.activeZones = "main_field,6,54,74,96";
        c.ignoreZones = "top_trees,0,0,100,48;right_bush,74,0,100,100;left_light,0,0,20,25";
        c.cameraRejectBorderBlobs = true;
        c.cameraRequireBodyCore = true;
        c.cameraRejectEmptyFieldTexture = true;
        appPrefs.save(c);
        prefs.edit().putBoolean("pro38Configured", true).putString("page", "HOME").apply();
        appPrefs.log("Pro38 DetectionZones defaults applied");
    }

    private void startGuard() {
        applyModernDefaults(false);
        startServiceAction("START");
        Toast.makeText(this, "Guard started", Toast.LENGTH_SHORT).show();
        render();
    }

    private void stopGuard() {
        startServiceAction("STOP");
        Toast.makeText(this, "Guard stopped", Toast.LENGTH_SHORT).show();
        render();
    }

    private void testSiren() {
        applyModernDefaults(false);
        startServiceAction("TEST_SIREN");
        Toast.makeText(this, "S01 siren test started", Toast.LENGTH_LONG).show();
        render();
    }


private void testPhoneAlarm() {
    applyModernDefaults(false);
    startServiceAction("TEST_PHONE_ALARM");
    Toast.makeText(this, "Phone alarm test started", Toast.LENGTH_LONG).show();
    render();
}

    private void startServiceAction(String action) {
        Intent svc = new Intent(this, GuardianService.class);
        svc.setAction(action);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc); else startService(svc);
    }

    private void openV380() {
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.macrovideo.v380pro");
        if (launch != null) startActivity(launch);
        else Toast.makeText(this, "V380 Pro not installed", Toast.LENGTH_LONG).show();
    }

    private void openLastSnapshot() {
        String url = prefs.getString("lastImageUrl", "");
        if (url.length() == 0) {
            Toast.makeText(this, "No snapshot yet", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void openBatterySettings() {
        try {
            Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Throwable t) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private void copyDiagnostics() {
        String d = appPrefs.diagnostics()
                .replaceAll("Token: .*", "Token: hidden")
                .replaceAll("Last image URL: .*", "Last image URL: hidden");
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("V380 Pro38 diagnostics", d));
        Toast.makeText(this, "Diagnostics copied", Toast.LENGTH_SHORT).show();
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
