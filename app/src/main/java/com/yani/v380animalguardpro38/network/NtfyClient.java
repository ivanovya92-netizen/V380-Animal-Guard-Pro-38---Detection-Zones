package com.yani.v380animalguardpro38.network;

import com.yani.v380animalguardpro38.model.AppConfig;

import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NtfyClient {
    private final OkHttpClient client = new OkHttpClient.Builder().build();

    public void sendAsync(AppConfig cfg, String message) {
        if (!cfg.ntfyEnabled) return;

        String topic = cfg.ntfyTopic == null ? "" : cfg.ntfyTopic.replaceAll("[^A-Za-z0-9_-]", "");
        if (topic.length() == 0) return;

        new Thread(() -> {
            try {
                RequestBody body = RequestBody.create(message.getBytes(StandardCharsets.UTF_8), MediaType.parse("text/plain; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("https://ntfy.sh/" + topic)
                        .header("Title", "V380 camera event")
                        .header("Priority", "urgent")
                        .header("Tags", "warning")
                        .post(body)
                        .build();
                client.newCall(request).execute().close();
            } catch (Throwable ignored) {
            }
        }).start();
    }
}
