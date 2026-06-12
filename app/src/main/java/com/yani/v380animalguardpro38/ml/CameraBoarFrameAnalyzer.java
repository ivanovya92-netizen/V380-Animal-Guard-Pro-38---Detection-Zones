package com.yani.v380animalguardpro38.ml;

import android.graphics.Bitmap;

import com.yani.v380animalguardpro38.model.AppConfig;
import com.yani.v380animalguardpro38.zones.DetectionZoneGate;

import java.util.ArrayDeque;

public class CameraBoarFrameAnalyzer {
    public static class Result {
        public int score;
        public String summary;
    }

    public Result analyze(AppConfig cfg, Bitmap bitmap) {
        Result r = new Result();
        if (bitmap == null || bitmap.getWidth() < 60 || bitmap.getHeight() < 60) {
            r.score = 0;
            r.summary = "camera profile unavailable | no camera boar blob";
            return r;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int x0 = pct(w, cfg.cameraRoiXStartPercent);
        int x1 = pct(w, cfg.cameraRoiXEndPercent);
        int y0 = pct(h, cfg.cameraRoiYStartPercent);
        int y1 = pct(h, cfg.cameraRoiYEndPercent);
        if (cfg.cameraIgnoreRightBrightBush) x1 = Math.min(x1, pct(w, 74));

        int avg = roiAverage(bitmap, x0, y0, x1, y1);
        int threshold = Math.max(22, avg - 26);

        int gw = 110;
        int gh = 64;
        boolean[][] dark = new boolean[gh][gw];

        int darkCount = 0;
        for (int gy = 0; gy < gh; gy++) {
            int y = y0 + (int) ((gy / (float) Math.max(1, gh - 1)) * (y1 - y0));
            for (int gx = 0; gx < gw; gx++) {
                int x = x0 + (int) ((gx / (float) Math.max(1, gw - 1)) * (x1 - x0));
                int lum = lum(bitmap.getPixel(x, y));
                boolean isDark = lum < threshold;
                dark[gy][gx] = isDark;
                if (isDark) darkCount++;
            }
        }

        boolean[][] seen = new boolean[gh][gw];
        int bestScore = 0;
        String best = "no camera boar blob";
        boolean bestStrong = false;
        boolean bestBorder = false;
        DetectionZoneGate.GateResult bestZone = null;

        int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};
        for (int sy = 0; sy < gh; sy++) {
            for (int sx = 0; sx < gw; sx++) {
                if (!dark[sy][sx] || seen[sy][sx]) continue;

                int cells = 0;
                int minX = sx, maxX = sx, minY = sy, maxY = sy;
                ArrayDeque<int[]> q = new ArrayDeque<>();
                q.add(new int[]{sx, sy});
                seen[sy][sx] = true;

                while (!q.isEmpty()) {
                    int[] p = q.removeFirst();
                    int x = p[0], y = p[1];
                    cells++;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                    for (int[] d : dirs) {
                        int nx = x + d[0], ny = y + d[1];
                        if (nx < 0 || ny < 0 || nx >= gw || ny >= gh) continue;
                        if (seen[ny][nx] || !dark[ny][nx]) continue;
                        seen[ny][nx] = true;
                        q.add(new int[]{nx, ny});
                    }
                }

                int bw = maxX - minX + 1;
                int bh = maxY - minY + 1;
                float aspect = bh == 0 ? 0f : bw / (float) bh;
                float centerY = (minY + maxY) / 2f / gh;
                float centerX = (minX + maxX) / 2f / gw;
                float areaPct = cells * 100f / (gw * gh);
                boolean touchesBorder = minX <= 1 || minY <= 1 || maxX >= gw - 2 || maxY >= gh - 2;

                boolean sideShape = aspect >= 1.25f && aspect <= 3.40f && bw >= 9 && bh >= 5;
                boolean rearShape = aspect >= 0.58f && aspect < 1.18f && bw >= 5 && bh >= 8;
                boolean rightPlace = centerY >= 0.52f && centerY <= 0.94f && centerX >= 0.06f && centerX <= 0.76f;
                boolean rightSize = areaPct >= 0.35f && areaPct <= 5.80f && cells >= 24 && cells <= 520;
                boolean compact = (bw >= 7 && bh >= 5) && (sideShape || rearShape);
                boolean notTexture = darkCount > 0 && darkCount < (gw * gh * 0.32f);
                DetectionZoneGate.GateResult zone = DetectionZoneGate.check(cfg, centerX * 100f, centerY * 100f);
                if (cfg.detectionZonesEnabled && zone.ignoreBlock) continue;
                if (cfg.detectionZonesEnabled && cfg.zoneRequireActive && !zone.activePass) continue;

                if (cfg.cameraRejectBorderBlobs && touchesBorder) {
                    continue;
                }
                if (cfg.cameraRequireBodyCore && (!rightPlace || !rightSize || !compact)) {
                    continue;
                }
                if (cfg.cameraRejectEmptyFieldTexture && !notTexture) {
                    continue;
                }

                int score = 0;
                if (rightSize) score += 22;
                if (rightPlace) score += 18;
                if (sideShape) score += 26;
                if (rearShape) score += 22;
                if (compact) score += 14;
                if (notTexture) score += 10;
                if (centerX >= 0.10f && centerX <= 0.62f) score += 8;
                if (centerY >= 0.60f) score += 6;

                if (score > bestScore) {
                    bestScore = score;
                    bestStrong = score >= cfg.cameraBoarSignatureThreshold;
                    bestBorder = touchesBorder;
                    bestZone = zone;
                    String shape = sideShape ? "side-boar-silhouette" : "rear-boar-silhouette";
                    best = "camera-boar-blob score " + score
                            + " " + shape
                            + " cells " + cells
                            + " area " + Math.round(areaPct * 10) / 10.0 + "%"
                            + " aspect " + Math.round(aspect * 10) / 10.0
                            + " cx " + Math.round(centerX * 100) + "%"
                            + " cy " + Math.round(centerY * 100) + "%"
                            + (touchesBorder ? " border-touch" : "");
                }
            }
        }

        int lowerDarkPct = Math.round((darkCount * 100f) / Math.max(1, gw * gh));
        int finalScore = bestScore;
        StringBuilder summary = new StringBuilder();
        summary.append("camera profile ").append(cfg.cameraProfileName)
                .append(" roi lower-field-strict")
                .append(" avg ").append(avg)
                .append(" threshold ").append(threshold)
                .append(" lower-dark ").append(lowerDarkPct).append("%")
                .append(" | ").append(best);
        if (bestZone != null) summary.append(" | ").append(bestZone.summary);
        else if (cfg.detectionZonesEnabled) summary.append(" | zone-no-active-match");

        if (bestScore >= cfg.cameraBoarSignatureThreshold && bestStrong && !bestBorder) {
            finalScore += 12;
            summary.append(" | camera-strong-boar-signature");
            summary.append(" | camera-boar-signature");
        } else if (bestScore >= cfg.cameraBoarReviewThreshold) {
            summary.append(" | camera-boar-review-candidate");
        } else {
            finalScore = Math.min(finalScore, 49);
            summary.append(" | camera-empty-field-guard");
        }

        if (lowerDarkPct >= 3 && lowerDarkPct <= 18 && bestScore >= cfg.cameraBoarReviewThreshold) {
            summary.append(" | dark compact body");
            finalScore += 4;
        }

        r.score = Math.max(0, Math.min(99, finalScore));
        summary.append(" | camera-boar-score ").append(r.score).append("%");
        r.summary = summary.toString();
        return r;
    }

    private int roiAverage(Bitmap b, int x0, int y0, int x1, int y1) {
        long total = 0;
        int count = 0;
        int stepX = Math.max(1, (x1 - x0) / 110);
        int stepY = Math.max(1, (y1 - y0) / 64);
        for (int y = y0; y < y1; y += stepY) {
            for (int x = x0; x < x1; x += stepX) {
                int l = lum(b.getPixel(x, y));
                if (l > 235) continue;
                total += l;
                count++;
            }
        }
        return count == 0 ? 80 : (int) (total / count);
    }

    private int pct(int v, int p) {
        return Math.max(0, Math.min(v, (int) (v * (p / 100f))));
    }

    private int lum(int c) {
        int r = (c >> 16) & 0xff;
        int g = (c >> 8) & 0xff;
        int b = c & 0xff;
        return (r * 30 + g * 59 + b * 11) / 100;
    }
}
