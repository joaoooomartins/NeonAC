package com.earac.core.punishment;

import com.earac.api.violation.Violation;
import com.earac.core.api.ApiEventDispatcher;
import com.earac.core.config.ConfigManager;
import com.earac.core.config.MessageManager;
import com.earac.core.config.PlaceholderResolver;
import com.earac.core.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Executes configurable punishment commands when a check's VL crosses a threshold.
 * Commands are dispatched on the main thread and support {@code %placeholder%} tokens.
 * Other plugins may cancel a punishment via {@link com.earac.core.api.events.EarACPunishmentEvent}.
 */
public final class PunishmentManager {

    private final ConfigManager config;
    private final MessageManager messages;
    private final PlayerManager playerManager;
    private volatile List<PunishmentRule> rules = new ArrayList<>();

    public PunishmentManager(ConfigManager config, MessageManager messages, PlayerManager playerManager) {
        this.config = config;
        this.messages = messages;
        this.playerManager = playerManager;
        reload();
    }

    public void reload() {
        List<PunishmentRule> parsed = new ArrayList<>();
        List<?> list = config.getRaw().getList("punishments");
        if (list != null) {
            for (Object obj : list) {
                if (!(obj instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> entry = (Map<String, Object>) obj;
                Object t = entry.get("threshold");
                Object c = entry.get("commands");
                if (!(t instanceof Number) || !(c instanceof List)) continue;
                double threshold = ((Number) t).doubleValue();
                @SuppressWarnings("unchecked")
                List<String> commands = (List<String>) c;
                if (threshold > 0 && !commands.isEmpty()) {
                    parsed.add(new PunishmentRule(threshold, commands));
                }
            }
        }
        parsed.sort((a, b) -> Double.compare(b.threshold, a.threshold));
        this.rules = parsed;
    }

    public void punish(Violation v) {
        if (!config.getBoolean("punishments.enabled", true)) return;
        double vl = v.getViolationLevel();
        PunishmentRule rule = null;
        for (PunishmentRule r : rules) {
            if (vl >= r.threshold) {
                rule = r;
                break;
            }
        }
        if (rule == null) return;

        Map<String, Object> ctx = PlaceholderResolver.fromViolation(v, Bukkit.getServer().getName());
        PlayerManager pm = playerManager;
        if (pm.get(java.util.UUID.fromString(v.getPlayerUuid())) != null) {
            ctx.put("ping", pm.get(java.util.UUID.fromString(v.getPlayerUuid())).getPing());
        }

        for (String raw : rule.commands) {
            final String cmd = PlaceholderResolver.resolve(raw, ctx);
            if (ApiEventDispatcher.firePunishment(v, cmd)) {
                final String fcmd = cmd;
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("EarAC"),
                        () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stripSlash(fcmd)));
            }
        }
    }

    private String stripSlash(String cmd) {
        return cmd.startsWith("/") ? cmd.substring(1) : cmd;
    }

    private static final class PunishmentRule {
        final double threshold;
        final List<String> commands;

        PunishmentRule(double threshold, List<String> commands) {
            this.threshold = threshold;
            this.commands = commands;
        }
    }
}
