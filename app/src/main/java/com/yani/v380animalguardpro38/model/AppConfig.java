package com.yani.v380animalguardpro38.model;

public class AppConfig {
    public String baseUrl = "http://47.254.175.150:8080";
    public String accessToken = "8c3d15be541247c07dd15108ff4b90b0d5e8fdda081f4db420f17e081be00256";
    public String userId = "100745185";
    public String deviceId = "120111823";
    public String relayHost = "172.104.202.253";
    public int relayPort = 8800;
    public String deviceHost = "120111823.nvdvr.net";

    public int intervalSec = 30;
    public boolean aggressiveMode = false;
    public boolean smartSnapshot = true;
    public boolean notifyFirstRun = false;
    public boolean notifyErrors = true;
    public boolean autoRestart = true;

    public boolean quietEnabled = false;
    public int quietStart = 23;
    public int quietEnd = 7;

    public boolean ntfyEnabled = true;
    public String ntfyTopic = "v380_yani_devinalarm_7xq9m2p4";

    public boolean armedEnabled = true;
    public int armedStart = 20;
    public int armedEnd = 7;
    public String triggerMode = "SMART_ANIMAL"; // SAFE_NIGHT, ANY_MOTION, SMART_ANIMAL, TEST_NEXT_EVENT
    public boolean autoSiren = true;
    public boolean sirenMasterAllowed = true;
    public boolean officialTapperEnabled = false;
    public boolean testNextEventOnly = false;
    public boolean doubleAlarmTap = false;
    public int cooldownSec = 180;
    public int maxSirenPerHour = 6;
    public int maxSirensPerNight = 20;
    public int openV380DelayMs = 8000;
    public int moreToAlarmDelayMs = 900;
    public int moreXPercent = 88;
    public int moreYPercent = 91;
    public int alarmXPercent = 13;
    public int alarmYPercent = 72;

    // Animal Guard Pro 20
    public boolean animalAlertsEnabled = true;
    public boolean animalLocalLoud = true;
    public boolean animalNtfyEnabled = true;
    public boolean animalHistoryEnabled = true;
    public boolean fieldConsoleMode = true;
    public boolean sirenOnlyForSelectedAnimals = true;
    public String notifyAnimals = "boar,fox,dog,cat,bird,person,unknown";
    public String sirenAnimals = "boar,unknown";
    public int minSmartConfidencePercent = 62;
    public boolean triggerOnUnknownAtNight = true;

    public String boarKeywords = "boar,pig,hog,wild boar,wild pig,suinae";
    public String foxKeywords = "fox,vulpes,wildlife,animal,mammal";
    public String dogKeywords = "dog,canine,pet,animal,mammal";
    public String catKeywords = "cat,feline,pet,animal,mammal";
    public String birdKeywords = "bird,aves,animal,wildlife";
    public String personKeywords = "person,human,people,man,woman";
    public String unknownKeywords = "animal,mammal,wildlife,creature,unknown,no objects,ground activity,large lower-zone object,night object";

    public String sirenMode = "S01_DIRECT_REPLAY";

// Pro 31 BoarVision AI Engine
public String aiProfile = "BALANCED"; // STRICT, BALANCED, SENSITIVE
public boolean aiGroundZoneEnabled = true;
public boolean aiNightBoostEnabled = true;
public boolean aiSaveEventSummary = true;
public int aiBoarThreshold = 72;
public int aiUnknownNightThreshold = 68;
public int aiPersonNoSirenThreshold = 70;
public boolean aiPersonNeverSiren = true;
public boolean aiLearningMode = true;
public String uiHomeMode = "SIMPLE";
public boolean backtestEnabled = true;
public String designMode = "MODERN_SIMPLE";

// Pro32 Camera BoarVision
public boolean boarVisionEnabled = true;
public int boarVisionSirenThreshold = 78;
public int boarVisionReviewThreshold = 58;
public int boarVisionHardBlockPerson = 70;
public int boarVisionHardBlockDog = 82;
public int boarVisionHardBlockCat = 82;
public int boarVisionHardBlockFox = 82;
public boolean boarVisionLearnFromLabels = true;
public String boarVisionMode = "FIELD_SAFE"; // FIELD_SAFE, HIGH_SENSITIVITY

// Pro33 exact-camera boar profile, based on supplied night IR images
public boolean cameraBoarProfileEnabled = true;
public String cameraProfileName = "DevinGarden_IR_Night_Boar";
public int cameraRoiXStartPercent = 6;
public int cameraRoiXEndPercent = 74;
public int cameraRoiYStartPercent = 54;
public int cameraRoiYEndPercent = 96;
public int cameraBoarSignatureThreshold = 88;
public int cameraBoarReviewThreshold = 68;
public boolean cameraIgnoreRightBrightBush = true;
public boolean cameraUseBlobSilhouette = true;
public boolean cameraUseLowerBodyMass = true;
public boolean cameraRejectBorderBlobs = true;
public boolean cameraRequireBodyCore = true;
public boolean cameraRejectEmptyFieldTexture = true;

// Pro34 final field deterrence mode
public boolean phoneAlarmEnabled = true;
public boolean phoneAlarmFullScreen = true;
public boolean phoneAlarmSound = true;
public boolean phoneAlarmVibrate = true;
public boolean phoneAlarmReminderEnabled = true;
public int phoneAlarmReminderSec = 60;
public int phoneAlarmMaxReminders = 5;
public int phoneAlarmSnoozeMin = 5;

// Pro36 final field automation
public boolean fieldAutoDeterrenceEnabled = true;
public boolean fieldTriggerCameraSiren = true;
public boolean fieldTriggerPhoneAlarm = true;
public boolean fieldManualFullTestEnabled = true;

// Pro37 anti-false-alarm mode:
// camera-only boar signature is review/notification only, no camera siren.
public boolean safeConfirmedAlarmMode = true;
public boolean cameraOnlyNoSiren = true;
public boolean requireIndependentAnimalEvidenceForSiren = true;

// Pro38 Detection Zones
public boolean detectionZonesEnabled = true;
public boolean zoneRequireActive = true;
public boolean zoneIgnoreBlocksAlways = true;
public boolean zoneEnergySaverEnabled = true;
// format: name,x1,y1,x2,y2; all coordinates are percentages 0..100
public String activeZones = "main_field,6,54,74,96";
public String ignoreZones = "top_trees,0,0,100,48;right_bush,74,0,100,100;left_light,0,0,20,25";
}
