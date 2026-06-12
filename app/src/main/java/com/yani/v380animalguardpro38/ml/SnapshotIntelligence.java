package com.yani.v380animalguardpro38.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.yani.v380animalguardpro38.data.AppPrefs;
import com.yani.v380animalguardpro38.model.AppConfig;
import com.yani.v380animalguardpro38.zones.DetectionZoneGate;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class SnapshotIntelligence {
    public interface Callback {
        void done(String summary);
    }

    public void analyzeAsync(Context ctx, String imageUrl, Callback cb) {
        if (imageUrl == null || imageUrl.length() == 0) {
            if (cb != null) cb.done("no snapshot");
            return;
        }

        new Thread(() -> {
            try {
                InputStream is = new URL(imageUrl).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();

                if (bitmap == null) {
                    if (cb != null) cb.done("snapshot decode failed");
                    return;
                }

                runObjectDetection(ctx, bitmap, cb);
            } catch (Throwable t) {
                String summary = "analysis error: " + t.getClass().getSimpleName();
                new AppPrefs(ctx).log(summary);
                if (cb != null) cb.done(summary);
            }
        }).start();
    }

    private void runObjectDetection(Context ctx, Bitmap bitmap, Callback cb) {
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        ObjectDetector detector = ObjectDetection.getClient(options);
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        AppConfig cfg = new AppPrefs(ctx).config();
        String cameraSummary = cfg.cameraBoarProfileEnabled ? new CameraBoarFrameAnalyzer().analyze(cfg, bitmap).summary : "camera profile off";

        detector.process(image)
                .addOnSuccessListener(objects -> runImageLabeling(ctx, bitmap, cameraSummary + " | " + sceneSummary(bitmap) + " | " + boarVisualSummary(bitmap) + " | " + summarizeObjects(ctx, bitmap, objects), cb))
                .addOnFailureListener(e -> runImageLabeling(ctx, bitmap, cameraSummary + " | " + sceneSummary(bitmap) + " | " + boarVisualSummary(bitmap) + " | objects failed", cb));
    }

    private void runImageLabeling(Context ctx, Bitmap bitmap, String objectSummary, Callback cb) {
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    String summary = objectSummary + " | " + summarizeLabels(labels);
                    AppPrefs prefs = new AppPrefs(ctx);
                    prefs.raw().edit()
                            .putString("lastAnalysis", summary)
                            .putString("lastAiEngine", "Pro32 CameraBoarVision MLKit+exact-camera-blob-profile")
                            .apply();
                    prefs.log("CameraBoarVision snapshot: " + summary);
                    if (cb != null) cb.done(summary);
                })
                .addOnFailureListener(e -> {
                    String summary = objectSummary + " | labels failed";
                    new AppPrefs(ctx).raw().edit().putString("lastAnalysis", summary).apply();
                    if (cb != null) cb.done(summary);
                });
    }

    private String sceneSummary(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        long total = 0;
        long totalSq = 0;
        int count = 0;
        int stepX = Math.max(1, w / 80);
        int stepY = Math.max(1, h / 80);

        for (int y = 0; y < h; y += stepY) {
            for (int x = 0; x < w; x += stepX) {
                int lum = lum(bitmap.getPixel(x, y));
                total += lum;
                totalSq += (long) lum * lum;
                count++;
            }
        }

        int avg = count == 0 ? 0 : (int) (total / count);
        int variance = count == 0 ? 0 : (int) Math.max(0, (totalSq / count) - (avg * avg));
        return "scene " + (avg < 65 ? "night" : "day")
                + " brightness " + avg
                + " contrast " + variance
                + (variance < 500 ? " low-contrast" : "");
    }

    private String boarVisualSummary(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= 0 || h <= 0) return "boar visual unavailable";

        int yStart = (int) (h * 0.48f);
        int darkLower = 0;
        int totalLower = 0;
        int compactRuns = 0;
        int stepX = Math.max(1, w / 120);
        int stepY = Math.max(1, h / 90);

        for (int y = yStart; y < h; y += stepY) {
            int run = 0;
            for (int x = 0; x < w; x += stepX) {
                int lum = lum(bitmap.getPixel(x, y));
                totalLower++;
                if (lum < 80) {
                    darkLower++;
                    run++;
                } else {
                    if (run >= 4) compactRuns++;
                    run = 0;
                }
            }
            if (run >= 4) compactRuns++;
        }

        int darkPct = totalLower == 0 ? 0 : Math.round(darkLower * 100f / totalLower);
        StringBuilder sb = new StringBuilder();
        sb.append("lower dark ").append(darkPct).append("%");
        if (darkPct >= 12) sb.append(" dark compact body");
        if (compactRuns >= 4) sb.append(" wide low body");
        if (darkPct >= 18 && compactRuns >= 5) sb.append(" lower-zone body mass");
        return sb.toString();
    }

    private String summarizeObjects(Context ctx, Bitmap bitmap, List<DetectedObject> objects) {
        if (objects == null || objects.size() == 0) return "no objects";
        AppConfig cfg = new AppPrefs(ctx).config();
        StringBuilder sb = new StringBuilder();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        sb.append(objects.size()).append(" object(s)");

        int lower = 0;
        int large = 0;
        int wideLow = 0;
        int max = Math.min(5, objects.size());

        for (int i = 0; i < max; i++) {
            DetectedObject obj = objects.get(i);
            Rect box = obj.getBoundingBox();
            float area = (box.width() * box.height()) / (float) Math.max(1, w * h);
            float centerY = box.centerY() / (float) Math.max(1, h);
            float aspect = box.height() == 0 ? 0 : box.width() / (float) box.height();

            if (centerY > 0.52f) lower++;
            if (area > 0.035f) large++;
            if (centerY > 0.55f && aspect > 1.15f && area > 0.025f) wideLow++;
            DetectionZoneGate.GateResult zone = DetectionZoneGate.check(cfg, box.centerX() * 100f / Math.max(1, w), box.centerY() * 100f / Math.max(1, h));

            sb.append(" | box").append(i + 1)
                    .append(" area ").append(Math.round(area * 100)).append("%")
                    .append(" y ").append(Math.round(centerY * 100)).append("%")
                    .append(" aspect ").append(Math.round(aspect * 10) / 10.0);

            sb.append(" ").append(zone.summary);

            if (obj.getLabels().size() > 0) {
                DetectedObject.Label label = obj.getLabels().get(0);
                sb.append(" ").append(label.getText().toLowerCase(Locale.ROOT))
                        .append(" ").append(Math.round(label.getConfidence() * 100)).append("%");
            }
        }

        if (lower > 0) sb.append(" | ground activity");
        if (large > 0) sb.append(" | large lower-zone object");
        if (wideLow > 0) sb.append(" | wide low body");
        return sb.toString();
    }

    private String summarizeLabels(List<ImageLabel> labels) {
        if (labels == null || labels.size() == 0) return "no labels";
        StringBuilder sb = new StringBuilder("labels");
        int max = Math.min(8, labels.size());
        for (int i = 0; i < max; i++) {
            ImageLabel label = labels.get(i);
            sb.append(" | ")
                    .append(label.getText().toLowerCase(Locale.ROOT))
                    .append(" ")
                    .append(Math.round(label.getConfidence() * 100)).append("%");
        }
        return sb.toString();
    }

    private int lum(int c) {
        int r = (c >> 16) & 0xff;
        int g = (c >> 8) & 0xff;
        int b = c & 0xff;
        return (r * 30 + g * 59 + b * 11) / 100;
    }
}
