package com.yani.v380animalguardpro38.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UiFactory {
    private final Context context;

    public UiFactory(Context context) {
        this.context = context;
    }

    public TextView hero(LinearLayout root, String title, String body) {
        TextView tv = box(title + "\n" + body, Color.rgb(15, 23, 42), Color.rgb(148, 163, 184), Color.WHITE, 20, true, true);
        LinearLayout.LayoutParams lp = paramsFull();
        lp.setMargins(0, dp(8), 0, dp(14));
        root.addView(tv, lp);
        return tv;
    }

    public TextView card(LinearLayout root, String title, String body) {
        TextView tv = box(title + "\n" + body, Color.rgb(30, 41, 59), Color.rgb(100, 116, 139), Color.rgb(226, 232, 240), 16, false, false);
        tv.setGravity(Gravity.START);
        LinearLayout.LayoutParams lp = paramsFull();
        lp.setMargins(0, dp(6), 0, dp(12));
        root.addView(tv, lp);
        return tv;
    }

    public TextView metricCard(LinearLayout root, String label, String value) {
        TextView tv = box(label + "\n" + value, Color.rgb(15, 23, 42), Color.rgb(51, 65, 85), Color.WHITE, 15, true, false);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(dp(4), dp(4), dp(4), dp(8));
        root.addView(tv, lp);
        return tv;
    }

    public TextView button(LinearLayout root, String text, Runnable action) {
        TextView tv = box(text, Color.rgb(37, 99, 235), Color.rgb(59, 130, 246), Color.WHITE, 17, true, true);
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = paramsFull();
        lp.setMargins(0, dp(6), 0, dp(6));
        root.addView(tv, lp);
        return tv;
    }

    public TextView dangerButton(LinearLayout root, String text, Runnable action) {
        TextView tv = box(text, Color.rgb(185, 28, 28), Color.rgb(248, 113, 113), Color.WHITE, 17, true, true);
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = paramsFull();
        lp.setMargins(0, dp(6), 0, dp(6));
        root.addView(tv, lp);
        return tv;
    }

    public TextView secondaryButton(LinearLayout root, String text, Runnable action) {
        TextView tv = box(text, Color.rgb(51, 65, 85), Color.rgb(100, 116, 139), Color.WHITE, 16, true, true);
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = paramsFull();
        lp.setMargins(0, dp(6), 0, dp(6));
        root.addView(tv, lp);
        return tv;
    }

    public TextView chip(LinearLayout root, String text, Runnable action) {
        TextView tv = box(text, Color.rgb(30, 64, 175), Color.rgb(96, 165, 250), Color.WHITE, 14, true, false);
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), dp(8), dp(8));
        root.addView(tv, lp);
        return tv;
    }

    public void section(LinearLayout root, String title) {
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(18);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(dp(4), dp(18), dp(4), dp(6));
        root.addView(tv);
    }

    public LinearLayout row(LinearLayout root) {
        LinearLayout r = new LinearLayout(context);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setBaselineAligned(false);
        root.addView(r, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return r;
    }

    public TextView pill(LinearLayout root, String text) {
        TextView tv = box(text, Color.rgb(5, 150, 105), Color.rgb(52, 211, 153), Color.WHITE, 13, true, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), dp(8), dp(8));
        root.addView(tv, lp);
        return tv;
    }

    private TextView box(String text, int bgColor, int strokeColor, int textColor, int textSize, boolean bold, boolean large) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(textColor);
        tv.setTextSize(textSize);
        tv.setLineSpacing(dp(2), 1.0f);
        tv.setIncludeFontPadding(true);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setPadding(dp(18), large ? dp(16) : dp(14), dp(18), large ? dp(16) : dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(bgColor);
        bg.setStroke(dp(1), strokeColor);
        bg.setCornerRadius(dp(20));
        tv.setBackground(bg);
        return tv;
    }

    private LinearLayout.LayoutParams paramsFull() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int v) {
        return (int) (v * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
