package com.yani.v380animalguardpro38.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yani.v380animalguardpro38.data.AppPrefs;
import com.yani.v380animalguardpro38.model.AppConfig;
import com.yani.v380animalguardpro38.zones.DetectionZoneGate;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ZoneEditorActivity extends Activity {
    private AppPrefs appPrefs;
    private AppConfig cfg;
    private ZoneCanvas zoneCanvas;
    private boolean drawActive = true;
    private TextView status;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        appPrefs = new AppPrefs(this);
        cfg = appPrefs.config();

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(14), dp(14), dp(22));
        root.setBackgroundColor(Color.rgb(2, 6, 23));
        scroll.addView(root);
        setContentView(scroll);

        UiFactory ui = new UiFactory(this);
        ui.hero(root, "Detection Zones", "Draw green ACTIVE zones and red IGNORE zones over the last snapshot.");

        ui.card(root, "Mode", "Default: draw ACTIVE zones first. Use buttons below to switch mode.");

        ui.button(root, "DRAW ACTIVE ZONE", () -> { drawActive = true; toast("Now draw ACTIVE zone"); });
        ui.dangerButton(root, "DRAW IGNORE ZONE", () -> { drawActive = false; toast("Now draw IGNORE zone"); });

        zoneCanvas = new ZoneCanvas();
        root.addView(zoneCanvas, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(430)));

        ui.button(root, "SAVE ZONES", this::saveZones);
        ui.secondaryButton(root, "LOAD DEFAULT FIELD ZONES", this::loadDefaults);
        ui.secondaryButton(root, "CLEAR ACTIVE ZONES", () -> { zoneCanvas.active.clear(); zoneCanvas.invalidate(); });
        ui.secondaryButton(root, "CLEAR IGNORE ZONES", () -> { zoneCanvas.ignore.clear(); zoneCanvas.invalidate(); });
        ui.secondaryButton(root, "DISABLE ZONES TEMPORARILY", this::disableZones);
        ui.secondaryButton(root, "CLOSE", this::finish);

        ui.card(root, "How to use",
                "1. Tap DRAW ACTIVE ZONE and drag over the ground area where boars can appear.\n"
                + "2. Tap DRAW IGNORE ZONE and drag over trees, bushes, lamp, bright right side or moving grass.\n"
                + "3. Tap SAVE ZONES.\n"
                + "Alarm only works inside active zones and never inside ignore zones.");

        loadFromPrefs();
        loadSnapshotAsync();
    }

    private void loadFromPrefs() {
        zoneCanvas.active.clear();
        zoneCanvas.ignore.clear();
        zoneCanvas.active.addAll(DetectionZoneGate.parse(cfg.activeZones));
        zoneCanvas.ignore.addAll(DetectionZoneGate.parse(cfg.ignoreZones));
        zoneCanvas.invalidate();
    }

    private void loadDefaults() {
        cfg.detectionZonesEnabled = true;
        cfg.activeZones = DetectionZoneGate.defaultActive();
        cfg.ignoreZones = DetectionZoneGate.defaultIgnore();
        appPrefs.save(cfg);
        loadFromPrefs();
        toast("Default zones loaded");
    }

    private void disableZones() {
        cfg.detectionZonesEnabled = false;
        appPrefs.save(cfg);
        toast("Zones disabled");
    }

    private void saveZones() {
        cfg.detectionZonesEnabled = true;
        cfg.activeZones = DetectionZoneGate.encode(zoneCanvas.active);
        cfg.ignoreZones = DetectionZoneGate.encode(zoneCanvas.ignore);
        appPrefs.save(cfg);
        appPrefs.log("Detection zones saved active=" + cfg.activeZones + " ignore=" + cfg.ignoreZones);
        toast("Detection zones saved");
    }

    private void loadSnapshotAsync() {
        String url = appPrefs.raw().getString("lastImageUrl", "");
        if (url == null || url.length() == 0) return;

        new Thread(() -> {
            try {
                InputStream is = new URL(url).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();
                runOnUiThread(() -> {
                    zoneCanvas.bitmap = bmp;
                    zoneCanvas.invalidate();
                });
            } catch (Throwable t) {
                runOnUiThread(() -> toast("Could not load last snapshot"));
            }
        }).start();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class ZoneCanvas extends View {
        Bitmap bitmap;
        List<DetectionZoneGate.Zone> active = new ArrayList<>();
        List<DetectionZoneGate.Zone> ignore = new ArrayList<>();
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float sx, sy, ex, ey;
        boolean drawing = false;

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawColor(Color.rgb(15, 23, 42));

            RectF img = imageRect();
            if (bitmap != null) c.drawBitmap(bitmap, null, img, null);
            else {
                p.setColor(Color.WHITE);
                p.setTextSize(dp(16));
                c.drawText("No snapshot yet. Use CLOUD TEST ONCE first, then come back.", dp(16), dp(50), p);
            }

            drawZones(c, img, active, Color.argb(90, 34, 197, 94), Color.rgb(34, 197, 94), "ACTIVE");
            drawZones(c, img, ignore, Color.argb(90, 239, 68, 68), Color.rgb(239, 68, 68), "IGNORE");

            if (drawing) {
                p.setStyle(Paint.Style.FILL);
                p.setColor(drawActive ? Color.argb(80, 34, 197, 94) : Color.argb(80, 239, 68, 68));
                c.drawRect(Math.min(sx, ex), Math.min(sy, ey), Math.max(sx, ex), Math.max(sy, ey), p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(dp(2));
                p.setColor(drawActive ? Color.rgb(34, 197, 94) : Color.rgb(239, 68, 68));
                c.drawRect(Math.min(sx, ex), Math.min(sy, ey), Math.max(sx, ex), Math.max(sy, ey), p);
            }
        }

        private void drawZones(Canvas c, RectF img, List<DetectionZoneGate.Zone> zones, int fill, int stroke, String label) {
            int i = 1;
            for (DetectionZoneGate.Zone z : zones) {
                RectF r = new RectF(
                        img.left + img.width() * z.x1 / 100f,
                        img.top + img.height() * z.y1 / 100f,
                        img.left + img.width() * z.x2 / 100f,
                        img.top + img.height() * z.y2 / 100f
                );
                p.setStyle(Paint.Style.FILL);
                p.setColor(fill);
                c.drawRect(r, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(dp(2));
                p.setColor(stroke);
                c.drawRect(r, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                p.setTextSize(dp(13));
                c.drawText(label + " " + i, r.left + dp(6), r.top + dp(18), p);
                i++;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            RectF img = imageRect();
            float x = clamp(e.getX(), img.left, img.right);
            float y = clamp(e.getY(), img.top, img.bottom);

            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                sx = ex = x; sy = ey = y; drawing = true; invalidate(); return true;
            } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                ex = x; ey = y; invalidate(); return true;
            } else if (e.getAction() == MotionEvent.ACTION_UP) {
                ex = x; ey = y; drawing = false;
                float left = Math.min(sx, ex), right = Math.max(sx, ex);
                float top = Math.min(sy, ey), bottom = Math.max(sy, ey);
                if ((right - left) > dp(20) && (bottom - top) > dp(20)) {
                    float x1 = (left - img.left) * 100f / img.width();
                    float y1 = (top - img.top) * 100f / img.height();
                    float x2 = (right - img.left) * 100f / img.width();
                    float y2 = (bottom - img.top) * 100f / img.height();
                    if (drawActive) active.add(new DetectionZoneGate.Zone("active" + (active.size() + 1), x1, y1, x2, y2));
                    else ignore.add(new DetectionZoneGate.Zone("ignore" + (ignore.size() + 1), x1, y1, x2, y2));
                }
                invalidate(); return true;
            }
            return true;
        }

        private RectF imageRect() {
            int w = getWidth();
            int h = getHeight();
            if (bitmap == null) return new RectF(0, 0, w, h);
            float bmpRatio = bitmap.getWidth() / (float) bitmap.getHeight();
            float viewRatio = w / (float) h;
            if (bmpRatio > viewRatio) {
                float ih = w / bmpRatio;
                float top = (h - ih) / 2f;
                return new RectF(0, top, w, top + ih);
            } else {
                float iw = h * bmpRatio;
                float left = (w - iw) / 2f;
                return new RectF(left, 0, left + iw, h);
            }
        }

        private float clamp(float v, float min, float max) {
            return Math.max(min, Math.min(max, v));
        }
    }
}
