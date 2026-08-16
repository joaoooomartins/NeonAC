package com.earac.core.config;

import java.util.Map;

/**
 * Loads and formats user-facing messages from the configuration. No message is
 * hardcoded in checks; everything flows through here with {@link PlaceholderResolver}.
 */
public final class MessageManager {

    private final ConfigManager config;

    public MessageManager(ConfigManager config) {
        this.config = config;
    }

    /**
     * @return the formatted message for a key from the {@code messages} config section.
     */
    public String get(String key, Map<String, Object> placeholders) {
        String raw = config.getString("messages." + key, "&cMissing message: " + key);
        raw = raw.replace("%prefix%", config.getString("general.prefix", "&8[&bEarAC&8]"));
        return PlaceholderResolver.resolve(raw, placeholders != null ? placeholders : Map.of());
    }

    public String get(String key) {
        return get(key, Map.of());
    }

    public String getPrefix() {
        return PlaceholderResolver.color(config.getString("general.prefix", "&8[&bEarAC&8]"));
    }
}
