package com.yani.v380animalguardpro38.zones;

import com.yani.v380animalguardpro38.model.AppConfig;

import java.util.ArrayList;
import java.util.List;

public class DetectionZoneGate {
    public static class Zone {
        public String name;
        public float x1, y1, x2, y2;

        public Zone(String name, float x1, float y1, float x2, float y2) {
            this.name = name == null ? "zone" : name;
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
        }

        public boolean contains(float x, float y) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2;
        }

        public String encode() {
            return name + "," + Math.round(x1) + "," + Math.round(y1) + "," + Math.round(x2) + "," + Math.round(y2);
        }
    }

    public static class GateResult {
        public boolean enabled;
        public boolean activePass;
        public boolean ignoreBlock;
        public String activeName = "";
        public String ignoreName = "";
        public String summary = "zones off";
    }

    public static List<Zone> parse(String data) {
        ArrayList<Zone> out = new ArrayList<>();
        if (data == null || data.trim().length() == 0) return out;
        String[] parts = data.split(";");
        for (String part : parts) {
            try {
                String[] v = part.trim().split(",");
                if (v.length < 5) continue;
                String name = v[0].trim();
                float x1 = clamp(Float.parseFloat(v[1].trim()));
                float y1 = clamp(Float.parseFloat(v[2].trim()));
                float x2 = clamp(Float.parseFloat(v[3].trim()));
                float y2 = clamp(Float.parseFloat(v[4].trim()));
                out.add(new Zone(name, x1, y1, x2, y2));
            } catch (Throwable ignored) {}
        }
        return out;
    }

    public static String encode(List<Zone> zones) {
        if (zones == null || zones.size() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (Zone z : zones) {
            if (sb.length() > 0) sb.append(";");
            sb.append(z.encode());
        }
        return sb.toString();
    }

    public static GateResult check(AppConfig cfg, float centerXPercent, float centerYPercent) {
        GateResult r = new GateResult();
        r.enabled = cfg != null && cfg.detectionZonesEnabled;
        if (!r.enabled) {
            r.activePass = true;
            r.summary = "zones off";
            return r;
        }

        List<Zone> active = parse(cfg.activeZones);
        List<Zone> ignore = parse(cfg.ignoreZones);

        for (Zone z : ignore) {
            if (z.contains(centerXPercent, centerYPercent)) {
                r.ignoreBlock = true;
                r.ignoreName = z.name;
                break;
            }
        }

        if (active.size() == 0) {
            r.activePass = !cfg.zoneRequireActive;
        } else {
            for (Zone z : active) {
                if (z.contains(centerXPercent, centerYPercent)) {
                    r.activePass = true;
                    r.activeName = z.name;
                    break;
                }
            }
        }

        if (r.ignoreBlock) {
            r.summary = "zone-ignore-block " + r.ignoreName + " cx " + Math.round(centerXPercent) + " cy " + Math.round(centerYPercent);
        } else if (r.activePass) {
            r.summary = "zone-active-pass " + (r.activeName.length() > 0 ? r.activeName : "no-active-required") + " cx " + Math.round(centerXPercent) + " cy " + Math.round(centerYPercent);
        } else {
            r.summary = "zone-no-active-match cx " + Math.round(centerXPercent) + " cy " + Math.round(centerYPercent);
        }
        return r;
    }

    public static String defaultActive() {
        return "main_field,6,54,74,96";
    }

    public static String defaultIgnore() {
        return "top_trees,0,0,100,48;right_bush,74,0,100,100;left_light,0,0,20,25";
    }

    private static float clamp(float v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }
}
