package com.neonac.core.punishment;

import com.neonac.api.violation.Violation;
import com.neonac.core.NeonACPlugin;
import com.neonac.core.api.ApiEventDispatcher;
import com.neonac.core.config.ConfigManager;
import com.neonac.core.config.MessageManager;
import com.neonac.core.config.PlaceholderResolver;
import com.neonac.core.player.PlayerManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PunishmentManager {

    private final NeonACPlugin plugin;
    private final ConfigManager config;
    private final MessageManager messages;
    private final PlayerManager playerManager;
    private volatile List<PunishmentRule> rules = new ArrayList<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public PunishmentManager(NeonACPlugin plugin, ConfigManager config, MessageManager messages, PlayerManager playerManager) {
        this.plugin = plugin;
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

        if (parsed.isEmpty()) {
            var rulesSection = config.getRaw().getConfigurationSection("punishments.rules");
            if (rulesSection != null) {
                for (String key : rulesSection.getKeys(false)) {
                    var ruleSection = rulesSection.getConfigurationSection(key);
                    if (ruleSection == null) continue;
                    double threshold = ruleSection.getDouble("threshold", 0);
                    List<String> commands = ruleSection.getStringList("commands");
                    if (threshold > 0 && !commands.isEmpty()) {
                        parsed.add(new PunishmentRule(threshold, commands));
                    }
                }
            }
        }

        parsed.sort((a, b) -> Double.compare(b.threshold, a.threshold));
        this.rules = parsed;
    }

    public void punish(Violation v) {
        if (!config.getBoolean("punishments.enabled", true)) return;

        UUID uuid = java.util.UUID.fromString(v.getPlayerUuid());
        long now = System.currentTimeMillis();
        long cooldownMs = config.getLong("punishments.cooldown", 30000);
        Long lastPunish = cooldowns.get(uuid);
        if (lastPunish != null && now - lastPunish < cooldownMs) return;

        double vl = v.getViolationLevel();
        PunishmentRule rule = null;
        for (PunishmentRule r : rules) {
            if (vl >= r.threshold) {
                rule = r;
                break;
            }
        }
        if (rule == null) return;

        cooldowns.put(uuid, now);

        Map<String, Object> ctx = PlaceholderResolver.fromViolation(v, Bukkit.getServer().getName());
        if (playerManager.get(uuid) != null) {
            ctx.put("ping", playerManager.get(uuid).getPing());
        }

        for (String raw : rule.commands) {
            final String cmd = PlaceholderResolver.resolve(raw, ctx);
            if (ApiEventDispatcher.firePunishment(v, cmd)) {
                final String fcmd = cmd;
                Bukkit.getScheduler().runTask(plugin,
                        () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stripSlash(fcmd)));
            }
        }
    }

    public void clearCooldown(UUID uuid) {
        cooldowns.remove(uuid);
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
