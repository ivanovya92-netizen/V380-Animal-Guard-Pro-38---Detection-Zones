package com.yani.v380animalguardpro38.backtest;

import com.yani.v380animalguardpro38.animal.AnimalClassifier;
import com.yani.v380animalguardpro38.model.AppConfig;

public class BacktestEngine {
    public static class Result {
        public int total;
        public int passed;
        public int aiPassed;
        public int sirenPassed;
        public String details;

        public boolean allPass() { return total > 0 && passed == total; }

        public String summaryLine() {
            int ai = total == 0 ? 0 : Math.round(aiPassed * 100f / total);
            int siren = total == 0 ? 0 : Math.round(sirenPassed * 100f / total);
            return "BoarVision Safe AI " + ai + "% | Siren policy " + siren + "% | Safety " + passed + "/" + total;
        }
    }

    private static class Case {
        String name;
        String expectAnimal;
        boolean expectSiren;
        boolean armed;
        String summary;

        Case(String name, String expectAnimal, boolean expectSiren, boolean armed, String summary) {
            this.name = name;
            this.expectAnimal = expectAnimal;
            this.expectSiren = expectSiren;
            this.armed = armed;
            this.summary = summary;
        }
    }

    public static Result run(AppConfig cfg) {
        Case[] cases = new Case[] {
                new Case("EXPLICIT_WILD_BOAR_LABEL", "boar", true, true, "scene night brightness 44 | labels | wild boar 91% | animal 88% | ground activity | large lower-zone object"),
                new Case("EXPLICIT_PIG_HOG_LABEL", "boar", true, true, "scene night brightness 42 | labels | pig 88% | hog 82% | mammal 80% | ground activity | large lower-zone object"),
                new Case("CAMERA_STRONG_PLUS_ANIMAL", "boar", true, true, "camera profile DevinGarden_IR_Night_Boar roi lower-field-strict | camera-boar-blob score 94 side-boar-silhouette | camera-strong-boar-signature | camera-boar-signature | camera-boar-score 99% | scene night | ground activity | large lower-zone object | labels | animal 86% | mammal 77%"),
                new Case("CAMERA_ONLY_FALSE_POSITIVE_BLOCKED", "none", false, true, "camera profile DevinGarden_IR_Night_Boar roi lower-field-strict | camera-boar-blob score 96 side-boar-silhouette | camera-strong-boar-signature | camera-boar-signature | camera-boar-score 99% | scene night | ground activity | large lower-zone object | labels | plant 83% | soil 78%"),
                new Case("EMPTY_FIELD_REJECTED", "none", false, true, "camera profile DevinGarden_IR_Night_Boar roi lower-field-strict avg 82 threshold 56 lower-dark 26% | no camera boar blob | camera-empty-field-guard | camera-boar-score 31% | scene night brightness 54 | labels | plant 81% | soil 78%"),
                new Case("RIGHT_BRIGHT_BUSH_REJECTED", "none", false, true, "camera profile DevinGarden_IR_Night_Boar roi lower-field-strict | no camera boar blob border-touch | camera-empty-field-guard | labels | tree 88% | plant 82%"),
                new Case("PERSON_BLOCK", "person", false, true, "scene night | labels | person 92% | human 86% | animal 70%"),
                new Case("DOG_BLOCK", "dog", false, true, "scene night | labels | dog 92% | canine 88% | animal 77% | ground activity"),
                new Case("CAT_BLOCK", "cat", false, true, "scene night | labels | cat 90% | feline 83%"),
                new Case("FOX_BLOCK", "fox", false, true, "scene night | labels | fox 88% | vulpes 73%"),
                new Case("BIRD_BLOCK", "bird", false, true, "scene day | labels | bird 88% | wing 75%"),
                new Case("UNKNOWN_ANIMAL_NO_SIREN", "unknown", false, true, "scene night | labels | animal 76% | mammal 69% | ground activity"),
                new Case("BOAR_DAY_NOT_ARMED", "boar", false, false, "scene day | labels | wild boar 94% | animal 80% | ground activity | large lower-zone object")
        };

        StringBuilder details = new StringBuilder();
        Result out = new Result();
        out.total = cases.length;

        for (Case c : cases) {
            AnimalClassifier.Result r = AnimalClassifier.classify(cfg, c.summary, c.armed);
            boolean aiOk = c.expectAnimal.equals(r.animal);
            boolean sirenOk = c.expectSiren == r.shouldSiren;
            if (aiOk) out.aiPassed++;
            if (sirenOk) out.sirenPassed++;
            if (aiOk && sirenOk) out.passed++;
            details.append(c.name)
                    .append(" expected=").append(c.expectAnimal).append("/").append(c.expectSiren ? "siren" : "no-siren")
                    .append(" got=").append(r.animal).append(" ").append(r.confidence).append("% siren=").append(r.shouldSiren)
                    .append(aiOk && sirenOk ? " -> PASS" : " -> CHECK")
                    .append(" | ").append(r.summaryLine())
                    .append("\n");
        }

        out.details = details.toString();
        return out;
    }
}
