package com.yani.v380animalguardpro38.network;

import android.util.Base64;

import com.yani.v380animalguardpro38.model.AppConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class V380CloudClient {
    private final OkHttpClient client = new OkHttpClient.Builder().build();

    public CloudResult queryReplay(AppConfig cfg) throws Exception {
        String json = "{\"access_token\":\"" + esc(cfg.accessToken) + "\",\"user_id\":" + cfg.userId + ",\"dev_id\":[" + cfg.deviceId + "]}";
        String param = Base64.encodeToString(json.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        String url = trim(cfg.baseUrl) + "/queryReplay?param=" + URLEncoder.encode(param, "UTF-8");

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "okhttp/4.12.0")
                .header("Cache-Control", "no-cache")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            JSONObject root = new JSONObject(body);
            int result = root.optInt("result", 0);

            CloudResult out = new CloudResult();
            out.result = result;
            out.rawLength = body.length();

            if (result != 200) {
                out.error = "V380 result " + result;
                return out;
            }

            JSONObject data = root.optJSONObject("data");
            if (data == null || !data.has(cfg.deviceId)) {
                out.error = "No device data";
                return out;
            }

            JSONObject dev = data.getJSONObject(cfg.deviceId);
            JSONArray items = dev.optJSONArray("items");
            out.items = items;
            out.itemCount = items == null ? 0 : items.length();
            return out;
        }
    }

    private static String trim(String s) {
        if (s == null) return "";
        if (s.endsWith("/")) return s.substring(0, s.length() - 1);
        return s;
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static class CloudResult {
        public int result;
        public String error;
        public int rawLength;
        public int itemCount;
        public JSONArray items;

        public boolean ok() {
            return result == 200 && error == null;
        }
    }
}
