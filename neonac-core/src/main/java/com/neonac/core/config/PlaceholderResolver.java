package com.neonac.core.config;

import com.neonac.api.violation.Violation;
import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public final class PlaceholderResolver {

    private PlaceholderResolver() {
    }

    public static String resolve(String input, Map<String, Object> values) {
        if (input == null) return null;
        String out = input;
        for (Map.Entry<String, Object> e : values.entrySet()) {
            out = out.replace("%" + e.getKey() + "%", String.valueOf(e.getValue()));
        }
        return color(out);
    }

    public static String color(String input) {
        if (input == null) return null;
        return ChatColor.translateAlternateColorCodes('&', input);
    }
    public static Map<String, Object> fromViolation(Violation v, String serverName) {
        Map<String, Object> m = new HashMap<>();
        m.put("player", v.getPlayerName());
        m.put("uuid", v.getPlayerUuid());
        m.put("check", v.getCheck().getName());
        m.put("check_id", v.getCheck().getId());
        m.put("category", v.getCheck().getCategory().name());
        m.put("vl", String.format("%.1f", v.getViolationLevel()));
        m.put("confidence", String.format("%.2f", v.getConfidence()));
        m.put("ping", "?"); // filled by caller with player data
        m.put("tps", "20.0");
        m.put("version", v.getCheck().getCategory().name());
        m.put("server", serverName);
        m.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        m.put("time", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        m.put("reason", v.getCheck().getName());
        return m;
    }
}
