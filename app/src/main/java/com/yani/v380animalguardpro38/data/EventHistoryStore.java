package com.yani.v380animalguardpro38.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yani.v380animalguardpro38.model.EventRecord;
import com.yani.v380animalguardpro38.util.TimeUtils;

public class EventHistoryStore {
    private final SharedPreferences prefs;

    public EventHistoryStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("v380_animal_guard_pro38_settings", Context.MODE_PRIVATE);
    }

    public void add(EventRecord event) {
        upsert(event);
    }

    public void upsert(EventRecord event) {
        String old = prefs.getString("history", "");
        String line = format(event);

        StringBuilder cleaned = new StringBuilder();
        String prefix = "ID " + event.id + " |";
        String[] lines = old.split("\n");

        for (String existing : lines) {
            if (existing == null || existing.trim().length() == 0) continue;
            if (existing.startsWith(prefix)) continue;
            cleaned.append(existing).append("\n");
        }

        String next = line + "\n" + cleaned;
        if (next.length() > 24000) next = next.substring(0, 24000);

        prefs.edit()
                .putString("history", next)
                .putString("lastImageUrl", event.imageUrl)
                .putString("lastAnalysis", event.analysis)
                .putLong("lastEventTime", event.recTime)
                .putLong("lastStoredEventId", event.id)
                .apply();
    }

    private String format(EventRecord event) {
        return "ID " + event.id + " | " + TimeUtils.eventTime(event.recTime)
                + " | +" + event.count + " event(s)"
                + (event.imageUrl.length() > 0 ? " | snapshot" : "")
                + (event.analysis.length() > 0 ? " | " + event.analysis : "");
    }
}
