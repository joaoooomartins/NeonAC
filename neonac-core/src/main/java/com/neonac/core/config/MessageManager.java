package com.neonac.core.config;

import java.util.Map;
public final class MessageManager {

    private final ConfigManager config;

    public MessageManager(ConfigManager config) {
        this.config = config;
    }
    public String get(String key, Map<String, Object> placeholders) {
        String raw = config.getString("messages." + key, "&cMissing message: " + key);
        raw = raw.replace("%prefix%", config.getString("general.prefix", "&8[&bNeonAC&8]"));
        return PlaceholderResolver.resolve(raw, placeholders != null ? placeholders : Map.of());
    }

    public String get(String key) {
        return get(key, Map.of());
    }

    public String getPrefix() {
        return PlaceholderResolver.color(config.getString("general.prefix", "&8[&bNeonAC&8]"));
    }
}
