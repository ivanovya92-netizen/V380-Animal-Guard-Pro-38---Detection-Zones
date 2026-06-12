package com.yani.v380animalguardpro38.animal;

import com.yani.v380animalguardpro38.model.AppConfig;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoarVisionEngine {
    public static class Result {
        public String animal = "none";
        public int confidence = 0;
        public boolean shouldNotify = false;
        public boolean shouldSiren = false;
        public boolean needsReview = false;
        public String evidence = "";
        public String decision = "NO_MATCH";

        public String summaryLine() {
            return animal + " " + confidence + "%, " + decision + ", " + evidence;
        }
    }

    public static Result classify(AppConfig cfg, String summary, boolean armedNow) {
        String s = summary == null ? "" : summary.toLowerCase(Locale.ROOT);
        String semantic = stripCameraMeta(s);
        Result r = new Result();

        if (cfg.detectionZonesEnabled && (s.contains("zone-ignore-block") || s.contains("zone-no-active-match"))) {
            decide(r, "none", 0, "ZONE_REJECTED", "outside active detection zones or inside ignore zone");
            return finalizeDecision(cfg, r, armedNow);
        }

        boolean cameraStrong = hasStrongCameraBoarEvidence(s);
        boolean cameraReview = hasCameraReviewEvidence(s);
        boolean explicitBoar = hasExplicitBoarEvidence(semantic);
        boolean independentAnimal = hasIndependentAnimalEvidence(semantic);
        boolean groundLike = s.contains("ground activity");
        boolean lowerMass = s.contains("large lower-zone object") || s.contains("lower-zone body mass") || s.contains("wide low body");
        boolean emptyGuard = s.contains("no camera boar blob") || s.contains("camera-empty-field-guard") || semantic.contains("no objects");

        int person = score(semantic, "person,human,people,man,woman,face,clothing") + pctBoost(semantic, "person", "human");
        int dog = score(semantic, "dog,canine,dog breed") + pctBoost(semantic, "dog", "canine");
        int cat = score(semantic, "cat,feline") + pctBoost(semantic, "cat", "feline");
        int fox = score(semantic, "fox,vulpes,red fox") + pctBoost(semantic, "fox", "vulpes");
        int bird = score(semantic, "bird,aves,wing,beak") + pctBoost(semantic, "bird", "wing", "beak");

        int hardNegative = Math.max(Math.max(person, dog), Math.max(Math.max(cat, fox), bird));

        if (person >= cfg.boarVisionHardBlockPerson) {
            decide(r, "person", clamp(person), "PERSON_BLOCK", "person/human detected - siren blocked");
            return finalizeDecision(cfg, r, armedNow);
        }
        if (dog >= cfg.boarVisionHardBlockDog) {
            decide(r, "dog", clamp(dog), "DOG_BLOCK", "dog/canine detected - siren blocked");
            return finalizeDecision(cfg, r, armedNow);
        }
        if (cat >= cfg.boarVisionHardBlockCat) {
            decide(r, "cat", clamp(cat), "CAT_BLOCK", "cat/feline detected - siren blocked");
            return finalizeDecision(cfg, r, armedNow);
        }
        if (fox >= cfg.boarVisionHardBlockFox) {
            decide(r, "fox", clamp(fox), "FOX_BLOCK", "fox detected - siren blocked");
            return finalizeDecision(cfg, r, armedNow);
        }
        if (bird >= 72) {
            decide(r, "bird", clamp(bird), "BIRD_BLOCK", "bird detected - siren blocked");
            return finalizeDecision(cfg, r, armedNow);
        }

        if (emptyGuard && !explicitBoar) {
            decide(r, "none", 0, "EMPTY_FIELD_REJECTED", "empty field / vegetation / no independent animal evidence");
            return finalizeDecision(cfg, r, armedNow);
        }

        int boarScore = 0;
        boarScore += explicitBoar ? 85 : 0;
        boarScore += explicitBoar ? pctBoost(semantic, "wild boar", "boar", "pig", "hog", "swine") : 0;
        boarScore += explicitBoar ? score(semantic, "wild boar,boar,pig,wild pig,hog,swine,snout,tusk,bristle,hoof") : 0;

        int cameraScore = cameraBoarScore(s);
        boolean cameraCanSiren = cameraStrong && independentAnimal && groundLike && lowerMass && hardNegative < 60;

        if (explicitBoar && boarScore >= cfg.boarVisionSirenThreshold && hardNegative < 60) {
            decide(r, "boar", clamp(boarScore + (cameraStrong ? 35 : 0)), "BOAR_CONFIRMED_EXPLICIT", "explicit boar/pig evidence + safe siren gate");
            return finalizeDecision(cfg, r, armedNow);
        }

        if (cameraCanSiren && cameraScore >= 92) {
            decide(r, "boar", clamp(cameraScore), "BOAR_CONFIRMED_CAMERA_AND_ANIMAL", "strong camera silhouette + independent animal evidence");
            return finalizeDecision(cfg, r, armedNow);
        }

        if (cameraStrong || cameraReview || (groundLike && lowerMass)) {
            decide(r, independentAnimal ? "unknown" : "none", clamp(Math.max(cameraScore, 50)), "REVIEW_NO_SIREN_CAMERA_ONLY", "possible boar shape but not independently confirmed - camera siren blocked to avoid false alarm");
            r.needsReview = true;
            return finalizeDecision(cfg, r, armedNow);
        }

        if (independentAnimal) {
            int unknown = score(semantic, "animal,mammal,wildlife,creature,unknown");
            decide(r, "unknown", clamp(unknown), "UNKNOWN_ANIMAL_NO_SIREN", "animal evidence but not confirmed boar");
            return finalizeDecision(cfg, r, armedNow);
        }

        decide(r, "none", 0, "NO_ANIMAL", "no reliable animal evidence");
        return finalizeDecision(cfg, r, armedNow);
    }

    private static Result finalizeDecision(AppConfig cfg, Result r, boolean armedNow) {
        r.shouldNotify = cfg.animalAlertsEnabled && AnimalClassifier.listContains(cfg.notifyAnimals, r.animal);

        boolean selected = AnimalClassifier.listContains(cfg.sirenAnimals, r.animal);
        boolean personBlock = "person".equals(r.animal);
        boolean confirmedBoar = "boar".equals(r.animal)
                && (r.decision.equals("BOAR_CONFIRMED_EXPLICIT") || r.decision.equals("BOAR_CONFIRMED_CAMERA_AND_ANIMAL"))
                && r.confidence >= cfg.boarVisionSirenThreshold;

        r.shouldSiren = cfg.autoSiren
                && cfg.sirenMasterAllowed
                && armedNow
                && selected
                && !personBlock
                && confirmedBoar;

        if (r.shouldSiren) r.evidence += ", S01 siren eligible";
        else if (personBlock) r.evidence += ", siren blocked for person";
        else if (r.decision.contains("REVIEW")) r.evidence += ", camera-only alarm blocked";
        else r.evidence += ", siren not eligible";

        return r;
    }

    private static void decide(Result r, String animal, int confidence, String decision, String evidence) {
        r.animal = animal;
        r.confidence = confidence;
        r.decision = decision;
        r.evidence = evidence;
    }

    private static String stripCameraMeta(String s) {
        return s.replace("devlingarden_ir_night_boar", "")
                .replace("devingarden_ir_night_boar", "")
                .replace("devin_garden_ir_night_boar", "")
                .replace("devin garden ir night boar", "")
                .replace("camera profile", "")
                .replace("camera-strong-boar-signature", "")
                .replace("camera-boar-signature", "")
                .replace("camera-boar-review-candidate", "")
                .replace("camera-boar-blob", "")
                .replace("camera-boar-score", "")
                .replace("no camera boar blob", "")
                .replace("side-boar-silhouette", "")
                .replace("rear-boar-silhouette", "")
                .replace("lower-field-strict", "")
                .replace("camera-empty-field-guard", "");
    }

    private static boolean hasStrongCameraBoarEvidence(String s) {
        if (s.contains("no camera boar blob") || s.contains("camera-empty-field-guard")) return false;
        return s.contains("camera-strong-boar-signature")
                && (s.contains("side-boar-silhouette") || s.contains("rear-boar-silhouette"));
    }

    private static boolean hasCameraReviewEvidence(String s) {
        if (s.contains("no camera boar blob") || s.contains("camera-empty-field-guard")) return false;
        return s.contains("camera-boar-review-candidate")
                || s.contains("side-boar-silhouette")
                || s.contains("rear-boar-silhouette");
    }

    private static boolean hasIndependentAnimalEvidence(String semantic) {
        return semantic.contains("animal")
                || semantic.contains("mammal")
                || semantic.contains("wildlife")
                || semantic.contains("creature")
                || semantic.contains("pig")
                || semantic.contains("hog")
                || semantic.contains("boar")
                || semantic.contains("swine")
                || semantic.contains("snout")
                || semantic.contains("tusk")
                || semantic.contains("bristle")
                || semantic.contains("hoof");
    }

    private static boolean hasExplicitBoarEvidence(String s) {
        return s.contains("wild boar")
                || s.contains("boar")
                || s.contains("wild pig")
                || s.contains(" pig")
                || s.contains("| pig")
                || s.contains("hog")
                || s.contains("swine")
                || s.contains("suinae")
                || s.contains("snout")
                || s.contains("tusk")
                || s.contains("bristle")
                || s.contains("hoof");
    }

    private static int cameraBoarScore(String s) {
        if (s.contains("no camera boar blob") || s.contains("camera-empty-field-guard")) return 0;
        int score = 0;
        score += score(s, "camera-strong-boar-signature,camera-boar-signature,side-boar-silhouette,rear-boar-silhouette,camera-boar-blob,lower-zone body mass,wide low body,dark compact body,lower-field-strict");
        score += nearbyScoreForToken(s, "camera-boar-score");
        score += nearbyScoreForToken(s, "camera-boar-blob score");
        return score;
    }

    private static int nearbyScoreForToken(String s, String token) {
        int idx = s.indexOf(token);
        if (idx < 0) return 0;
        int end = Math.min(s.length(), idx + token.length() + 12);
        Matcher m = Pattern.compile("(\\d{2,3})").matcher(s.substring(idx + token.length(), end));
        if (m.find()) {
            try { return Math.min(99, Math.max(0, Integer.parseInt(m.group(1)))); }
            catch (Throwable ignored) {}
        }
        return 0;
    }

    private static int score(String s, String csv) {
        int score = 0;
        for (String token : csv.split(",")) {
            String t = token.trim();
            if (t.length() > 0 && s.contains(t)) score += 22;
        }
        return score;
    }

    private static int pctBoost(String s, String... labels) {
        int boost = 0;
        for (String l : labels) {
            Pattern p = Pattern.compile(Pattern.quote(l) + "\\s+(\\d{2,3})%");
            Matcher m = p.matcher(s);
            while (m.find()) {
                try {
                    int pct = Integer.parseInt(m.group(1));
                    if (pct >= 80) boost += 35;
                    else if (pct >= 65) boost += 22;
                    else if (pct >= 50) boost += 10;
                } catch (Throwable ignored) {}
            }
        }
        return boost;
    }

    private static int clamp(int v) {
        if (v < 0) return 0;
        return Math.min(99, v);
    }
}
