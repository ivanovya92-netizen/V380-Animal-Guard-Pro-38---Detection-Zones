package com.yani.v380animalguardpro38.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yani.v380animalguardpro38.model.AppConfig;
import com.yani.v380animalguardpro38.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppPrefs {
    private final Context context;
    private final SharedPreferences prefs;

    public AppPrefs(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences("v380_animal_guard_pro38_settings", Context.MODE_PRIVATE);
    }

    public SharedPreferences raw() {
        return prefs;
    }

    public AppConfig config() {
        AppConfig c = new AppConfig();
        c.baseUrl = prefs.getString("baseUrl", c.baseUrl);
        c.accessToken = prefs.getString("accessToken", c.accessToken);
        c.userId = prefs.getString("userId", c.userId);
        c.deviceId = prefs.getString("deviceId", c.deviceId);
        c.relayHost = prefs.getString("relayHost", c.relayHost);
        c.relayPort = prefs.getInt("relayPort", c.relayPort);
        c.deviceHost = prefs.getString("deviceHost", c.deviceHost);
        c.intervalSec = prefs.getInt("intervalSec", c.intervalSec);
        c.aggressiveMode = prefs.getBoolean("aggressiveMode", c.aggressiveMode);
        c.smartSnapshot = prefs.getBoolean("smartSnapshot", c.smartSnapshot);
        c.notifyFirstRun = prefs.getBoolean("notifyFirstRun", c.notifyFirstRun);
        c.notifyErrors = prefs.getBoolean("notifyErrors", c.notifyErrors);
        c.autoRestart = prefs.getBoolean("autoRestart", c.autoRestart);
        c.quietEnabled = prefs.getBoolean("quietEnabled", c.quietEnabled);
        c.quietStart = prefs.getInt("quietStart", c.quietStart);
        c.quietEnd = prefs.getInt("quietEnd", c.quietEnd);
        c.ntfyEnabled = prefs.getBoolean("ntfyEnabled", c.ntfyEnabled);
        c.ntfyTopic = prefs.getString("ntfyTopic", c.ntfyTopic);

        c.armedEnabled = prefs.getBoolean("armedEnabled", c.armedEnabled);
        c.armedStart = prefs.getInt("armedStart", c.armedStart);
        c.armedEnd = prefs.getInt("armedEnd", c.armedEnd);
        c.triggerMode = prefs.getString("triggerMode", c.triggerMode);
        c.autoSiren = prefs.getBoolean("autoSiren", c.autoSiren);
        c.sirenMasterAllowed = prefs.getBoolean("sirenMasterAllowed", c.sirenMasterAllowed);
        c.officialTapperEnabled = prefs.getBoolean("officialTapperEnabled", c.officialTapperEnabled);
        c.testNextEventOnly = prefs.getBoolean("testNextEventOnly", c.testNextEventOnly);
        c.doubleAlarmTap = prefs.getBoolean("doubleAlarmTap", c.doubleAlarmTap);
        c.cooldownSec = prefs.getInt("cooldownSec", c.cooldownSec);
        c.maxSirenPerHour = prefs.getInt("maxSirenPerHour", c.maxSirenPerHour);
        c.maxSirensPerNight = prefs.getInt("maxSirensPerNight", c.maxSirensPerNight);
        c.openV380DelayMs = prefs.getInt("openV380DelayMs", c.openV380DelayMs);
        c.moreToAlarmDelayMs = prefs.getInt("moreToAlarmDelayMs", c.moreToAlarmDelayMs);
        c.moreXPercent = prefs.getInt("moreXPercent", c.moreXPercent);
        c.moreYPercent = prefs.getInt("moreYPercent", c.moreYPercent);
        c.alarmXPercent = prefs.getInt("alarmXPercent", c.alarmXPercent);
        c.alarmYPercent = prefs.getInt("alarmYPercent", c.alarmYPercent);

        c.animalAlertsEnabled = prefs.getBoolean("animalAlertsEnabled", c.animalAlertsEnabled);
        c.animalLocalLoud = prefs.getBoolean("animalLocalLoud", c.animalLocalLoud);
        c.animalNtfyEnabled = prefs.getBoolean("animalNtfyEnabled", c.animalNtfyEnabled);
        c.animalHistoryEnabled = prefs.getBoolean("animalHistoryEnabled", c.animalHistoryEnabled);
        c.fieldConsoleMode = prefs.getBoolean("fieldConsoleMode", c.fieldConsoleMode);
        c.sirenOnlyForSelectedAnimals = prefs.getBoolean("sirenOnlyForSelectedAnimals", c.sirenOnlyForSelectedAnimals);
        c.notifyAnimals = prefs.getString("notifyAnimals", c.notifyAnimals);
        c.sirenAnimals = prefs.getString("sirenAnimals", c.sirenAnimals);
        c.minSmartConfidencePercent = prefs.getInt("minSmartConfidencePercent", c.minSmartConfidencePercent);
        c.triggerOnUnknownAtNight = prefs.getBoolean("triggerOnUnknownAtNight", c.triggerOnUnknownAtNight);
        c.boarKeywords = prefs.getString("boarKeywords", c.boarKeywords);
        c.foxKeywords = prefs.getString("foxKeywords", c.foxKeywords);
        c.dogKeywords = prefs.getString("dogKeywords", c.dogKeywords);
        c.catKeywords = prefs.getString("catKeywords", c.catKeywords);
        c.birdKeywords = prefs.getString("birdKeywords", c.birdKeywords);
        c.personKeywords = prefs.getString("personKeywords", c.personKeywords);
        c.unknownKeywords = prefs.getString("unknownKeywords", c.unknownKeywords);

        c.sirenMode = prefs.getString("sirenMode", c.sirenMode);

c.aiProfile = prefs.getString("aiProfile", c.aiProfile);
c.aiGroundZoneEnabled = prefs.getBoolean("aiGroundZoneEnabled", c.aiGroundZoneEnabled);
c.aiNightBoostEnabled = prefs.getBoolean("aiNightBoostEnabled", c.aiNightBoostEnabled);
c.aiSaveEventSummary = prefs.getBoolean("aiSaveEventSummary", c.aiSaveEventSummary);
c.aiBoarThreshold = prefs.getInt("aiBoarThreshold", c.aiBoarThreshold);
c.aiUnknownNightThreshold = prefs.getInt("aiUnknownNightThreshold", c.aiUnknownNightThreshold);
c.aiPersonNoSirenThreshold = prefs.getInt("aiPersonNoSirenThreshold", c.aiPersonNoSirenThreshold);
c.aiPersonNeverSiren = prefs.getBoolean("aiPersonNeverSiren", c.aiPersonNeverSiren);
c.aiLearningMode = prefs.getBoolean("aiLearningMode", c.aiLearningMode);
c.uiHomeMode = prefs.getString("uiHomeMode", c.uiHomeMode);
        return c;
    }

    public void save(AppConfig c) {
        prefs.edit()
                .putString("baseUrl", c.baseUrl)
                .putString("accessToken", c.accessToken)
                .putString("userId", c.userId)
                .putString("deviceId", c.deviceId)
                .putString("relayHost", c.relayHost)
                .putInt("relayPort", c.relayPort)
                .putString("deviceHost", c.deviceHost)
                .putInt("intervalSec", c.intervalSec)
                .putBoolean("aggressiveMode", c.aggressiveMode)
                .putBoolean("smartSnapshot", c.smartSnapshot)
                .putBoolean("notifyFirstRun", c.notifyFirstRun)
                .putBoolean("notifyErrors", c.notifyErrors)
                .putBoolean("autoRestart", c.autoRestart)
                .putBoolean("quietEnabled", c.quietEnabled)
                .putInt("quietStart", c.quietStart)
                .putInt("quietEnd", c.quietEnd)
                .putBoolean("ntfyEnabled", c.ntfyEnabled)
                .putString("ntfyTopic", c.ntfyTopic)

                .putBoolean("armedEnabled", c.armedEnabled)
                .putInt("armedStart", c.armedStart)
                .putInt("armedEnd", c.armedEnd)
                .putString("triggerMode", c.triggerMode)
                .putBoolean("autoSiren", c.autoSiren)
                .putBoolean("sirenMasterAllowed", c.sirenMasterAllowed)
                .putBoolean("officialTapperEnabled", c.officialTapperEnabled)
                .putBoolean("testNextEventOnly", c.testNextEventOnly)
                .putBoolean("doubleAlarmTap", c.doubleAlarmTap)
                .putInt("cooldownSec", c.cooldownSec)
                .putInt("maxSirenPerHour", c.maxSirenPerHour)
                .putInt("maxSirensPerNight", c.maxSirensPerNight)
                .putInt("openV380DelayMs", c.openV380DelayMs)
                .putInt("moreToAlarmDelayMs", c.moreToAlarmDelayMs)
                .putInt("moreXPercent", c.moreXPercent)
                .putInt("moreYPercent", c.moreYPercent)
                .putInt("alarmXPercent", c.alarmXPercent)
                .putInt("alarmYPercent", c.alarmYPercent)

                .putBoolean("animalAlertsEnabled", c.animalAlertsEnabled)
                .putBoolean("animalLocalLoud", c.animalLocalLoud)
                .putBoolean("animalNtfyEnabled", c.animalNtfyEnabled)
                .putBoolean("animalHistoryEnabled", c.animalHistoryEnabled)
                .putBoolean("fieldConsoleMode", c.fieldConsoleMode)
                .putBoolean("sirenOnlyForSelectedAnimals", c.sirenOnlyForSelectedAnimals)
                .putString("notifyAnimals", c.notifyAnimals)
                .putString("sirenAnimals", c.sirenAnimals)
                .putInt("minSmartConfidencePercent", c.minSmartConfidencePercent)
                .putBoolean("triggerOnUnknownAtNight", c.triggerOnUnknownAtNight)
                .putString("boarKeywords", c.boarKeywords)
                .putString("foxKeywords", c.foxKeywords)
                .putString("dogKeywords", c.dogKeywords)
                .putString("catKeywords", c.catKeywords)
                .putString("birdKeywords", c.birdKeywords)
                .putString("personKeywords", c.personKeywords)
                .putString("unknownKeywords", c.unknownKeywords)

                .putString("sirenMode", c.sirenMode)

.putString("aiProfile", c.aiProfile)
.putBoolean("aiGroundZoneEnabled", c.aiGroundZoneEnabled)
.putBoolean("aiNightBoostEnabled", c.aiNightBoostEnabled)
.putBoolean("aiSaveEventSummary", c.aiSaveEventSummary)
.putInt("aiBoarThreshold", c.aiBoarThreshold)
.putInt("aiUnknownNightThreshold", c.aiUnknownNightThreshold)
.putInt("aiPersonNoSirenThreshold", c.aiPersonNoSirenThreshold)
.putBoolean("aiPersonNeverSiren", c.aiPersonNeverSiren)
.putBoolean("aiLearningMode", c.aiLearningMode)
.putString("uiHomeMode", c.uiHomeMode)
                .apply();
    }

    public void log(String line) {
        String old = prefs.getString("log", "");
        String next = TimeUtils.nowShort() + "  " + line + "\n" + old;
        if (next.length() > 45000) next = next.substring(0, 45000);
        prefs.edit().putString("log", next).apply();
    }

    public boolean isQuietNow() {
        AppConfig c = config();
        return isHourWindowNow(c.quietEnabled, c.quietStart, c.quietEnd);
    }

    public boolean isArmedNow() {
        AppConfig c = config();
        return isHourWindowNow(c.armedEnabled, c.armedStart, c.armedEnd);
    }

    public boolean isHourWindowNow(boolean enabled, int start, int end) {
        if (!enabled) return false;
        try {
            int hour = Integer.parseInt(new SimpleDateFormat("H", Locale.getDefault()).format(new Date()));
            if (start == end) return true;
            if (start < end) return hour >= start && hour < end;
            return hour >= start || hour < end;
        } catch (Throwable t) {
            return false;
        }
    }

    public String tokenMasked() {
        String token = prefs.getString("accessToken", new AppConfig().accessToken);
        if (token.length() < 12) return "missing";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
    }

    public String diagnostics() {
        AppConfig c = config();
        return "V380 Animal Guard Pro 38 diagnostics\n"
                + "Generated: " + TimeUtils.now() + "\n"
                + "Running: " + prefs.getBoolean("running", false) + "\n"
                + "Armed now: " + isArmedNow() + "\n"
                + "Trigger mode: " + c.triggerMode + "\n"
                + "Auto siren: " + c.autoSiren + "\n"
                + "Siren master allowed: " + c.sirenMasterAllowed + "\n"
                + "Animal alerts enabled: " + c.animalAlertsEnabled + "\n"
                + "Notify animals: " + c.notifyAnimals + "\n"
                + "Siren animals: " + c.sirenAnimals + "\n"
                + "Last animal: " + prefs.getString("lastAnimal", "") + "\n"
                + "Last animal confidence: " + prefs.getInt("lastAnimalConfidence", 0) + "\n"
                + "Last siren: " + prefs.getString("lastSirenStatus", "never") + "\n"
                + "Night siren count: " + prefs.getInt("sirenNightCount", 0) + "\n"
                + "Last seen ID: " + prefs.getLong("lastSeenId", 0L) + "\n"
                + "Last poll: " + prefs.getString("lastPollStatus", "") + "\n"
                + "Last analysis: " + prefs.getString("lastAnalysis", "") + "\n"
                + "Last image URL: " + prefs.getString("lastImageUrl", "") + "\n"
                + "Siren mode: " + c.sirenMode + "\n"
                + "AI profile: " + c.aiProfile + " | ground zone " + c.aiGroundZoneEnabled + " | night boost " + c.aiNightBoostEnabled + "\n"
                + "BoarVision: " + c.boarVisionEnabled + " | threshold " + c.boarVisionSirenThreshold + " | mode " + c.boarVisionMode + "\n"
                + "Camera profile: " + c.cameraProfileName + " | " + c.cameraBoarProfileEnabled + " | ROI " + c.cameraRoiXStartPercent + "-" + c.cameraRoiXEndPercent + "/" + c.cameraRoiYStartPercent + "-" + c.cameraRoiYEndPercent + "\n"
                + "Phone alarm: " + c.phoneAlarmEnabled + " | full screen " + c.phoneAlarmFullScreen + " | reminders " + c.phoneAlarmReminderEnabled + " every " + c.phoneAlarmReminderSec + "s max " + c.phoneAlarmMaxReminders + "\n"
                + "Field auto deterrence: " + c.fieldAutoDeterrenceEnabled + " | camera siren " + c.fieldTriggerCameraSiren + " | phone alarm " + c.fieldTriggerPhoneAlarm + "\n"
                + "Safe confirmed alarm: " + c.safeConfirmedAlarmMode + " | camera-only no siren " + c.cameraOnlyNoSiren + "\n"
                + "Detection zones: " + c.detectionZonesEnabled + " | active=" + c.activeZones + " | ignore=" + c.ignoreZones + "\n"
                + "Boar labels: " + prefs.getInt("labelBoarCount", 0) + " | not-boar labels: " + prefs.getInt("labelNotBoarCount", 0) + "\n"
                + "Base URL: " + c.baseUrl + "\n"
                + "Device ID: " + c.deviceId + "\n"
                + "Token: " + tokenMasked() + "\n"
                + "Failures: " + prefs.getInt("failures", 0) + "\n\n"
                + "History:\n" + prefs.getString("history", "") + "\n\n"
                + "Log:\n" + prefs.getString("log", "");
    }

    public void resetRuntimeState() {
        prefs.edit()
                .remove("lastSeenId")
                .remove("lastImageUrl")
                .remove("lastAnalysis")
                .remove("lastAnimal")
                .remove("lastAnimalConfidence")
                .remove("lastPollStatus")
                .remove("lastSirenAt")
                .remove("lastSirenStatus")
                .remove("sirenHourBucket")
                .remove("sirenHourCount")
                .remove("sirenNightBucket")
                .remove("sirenNightCount")
                .remove("guardState")
                .apply();
        log("Runtime state reset");
    }
}
