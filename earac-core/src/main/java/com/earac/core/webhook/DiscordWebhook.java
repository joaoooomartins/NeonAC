package com.earac.core.webhook;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Minimal, dependency-free Discord webhook sender. Runs off the main thread.
 * Only posts a single {@code content} field; never forwards raw client data.
 */
public final class DiscordWebhook {

    private DiscordWebhook() {
    }

    public static void send(String webhookUrl, String content) {
        if (webhookUrl == null || webhookUrl.isEmpty() || content == null) return;
        // Basic guard: must be a Discord/webhook URL.
        if (!webhookUrl.startsWith("https://")) return;
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            String payload = "{\"content\":" + quote(content) + "}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            // Webhook failures must never affect gameplay.
        }
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                default: sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
