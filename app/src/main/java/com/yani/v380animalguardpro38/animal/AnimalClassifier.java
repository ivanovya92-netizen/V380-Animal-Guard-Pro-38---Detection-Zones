package com.yani.v380animalguardpro38.animal;

import com.yani.v380animalguardpro38.model.AppConfig;

import java.util.Locale;

public class AnimalClassifier {
    public static class Result {
        public String animal = "unknown";
        public int confidence = 0;
        public String reason = "no match";
        public boolean shouldNotify = false;
        public boolean shouldSiren = false;

        public String summaryLine() {
            return animal + " " + confidence + "%, " + reason;
        }
    }

    public static Result classify(AppConfig cfg, String mlSummary, boolean armedNow) {
        if (cfg.boarVisionEnabled) {
            BoarVisionEngine.Result b = BoarVisionEngine.classify(cfg, mlSummary, armedNow);
            Result r = new Result();
            r.animal = b.animal;
            r.confidence = b.confidence;
            r.reason = "BoarVision " + b.decision + ": " + b.evidence + (b.needsReview ? ", needs review" : "");
            r.shouldNotify = b.shouldNotify;
            r.shouldSiren = b.shouldSiren;
            return r;
        }

        // Fallback from Pro29. Kept only as safety mode.
        Result r = new Result();
        String s = mlSummary == null ? "" : mlSummary.toLowerCase(Locale.ROOT);
        matchSpecific(r, s, "boar", cfg.boarKeywords, 98);
        matchSpecific(r, s, "person", cfg.personKeywords, 95);
        matchSpecific(r, s, "fox", "fox,vulpes,red fox", 92);
        matchSpecific(r, s, "dog", "dog,canine,dog breed", 88);
        matchSpecific(r, s, "cat", "cat,feline", 88);
        matchSpecific(r, s, "bird", "bird,wing,beak,aves", 84);

        if (r.confidence == 0) {
            matchUnknown(r, s, cfg.unknownKeywords, cfg.triggerOnUnknownAtNight && armedNow ? 60 : 35);
        }

        if (r.confidence == 0) {
            r.animal = "none";
            r.confidence = 0;
            r.reason = "No animal keyword found";
        }

        if (r.confidence < cfg.minSmartConfidencePercent) {
            r.shouldNotify = false;
            r.shouldSiren = false;
            r.reason = r.reason + ", below threshold " + cfg.minSmartConfidencePercent + "%";
            return r;
        }

        r.shouldNotify = cfg.animalAlertsEnabled && listContains(cfg.notifyAnimals, r.animal);
        r.shouldSiren = cfg.autoSiren && cfg.sirenMasterAllowed && armedNow && listContains(cfg.sirenAnimals, r.animal) && !"person".equals(r.animal);
        return r;
    }

    private static void matchSpecific(Result r, String s, String animal, String csv, int confidence) {
        if (r.confidence > 0) return;
        if (containsAny(s, csv)) {
            r.animal = animal;
            r.confidence = confidence;
            r.reason = "fallback match " + animal;
        }
    }

    private static void matchUnknown(Result r, String s, String csv, int confidence) {
        if (containsAny(s, csv)) {
            r.animal = "unknown";
            r.confidence = confidence;
            r.reason = "fallback unknown animal";
        }
    }

    private static boolean containsAny(String s, String csv) {
        if (csv == null) return false;
        for (String part : csv.split(",")) {
            String k = part.trim().toLowerCase(Locale.ROOT);
            if (k.length() > 0 && s.contains(k)) return true;
        }
        return false;
    }

    public static boolean listContains(String csv, String value) {
        if (csv == null || value == null) return false;
        String v = value.trim().toLowerCase(Locale.ROOT);
        for (String part : csv.split(",")) {
            if (part.trim().toLowerCase(Locale.ROOT).equals(v)) return true;
        }
        return false;
    }
}
