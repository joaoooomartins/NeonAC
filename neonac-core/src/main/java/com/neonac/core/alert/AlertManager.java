package com.neonac.core.alert;

import com.neonac.api.violation.Violation;
import com.neonac.core.NeonACPlugin;
import com.neonac.core.config.ConfigManager;
import com.neonac.core.config.MessageManager;
import com.neonac.core.config.PlaceholderResolver;
import com.neonac.core.player.PlayerData;
import com.neonac.core.player.PlayerManager;
import com.neonac.core.webhook.DiscordWebhook;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AlertManager {

    private final NeonACPlugin plugin;
    private final ConfigManager config;
    private final MessageManager messages;
    private final PlayerManager playerManager;
    private final String serverName;
    private final Map<String, Long> alertCooldowns = new ConcurrentHashMap<>();

    public AlertManager(NeonACPlugin plugin, ConfigManager config, MessageManager messages, PlayerManager playerManager) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.playerManager = playerManager;
        this.serverName = Bukkit.getServer() != null ? Bukkit.getServer().getName() : "unknown";
    }

    public void sendAlert(Violation v) {
        String category = v.getCheck().getCategory().getLowerCaseName();
        if (!config.getBoolean("alerts." + category, true)) return;
        if (!config.getBoolean("alerts.enabled", true)) return;

        String key = v.getPlayerUuid() + ":" + v.getCheck().getId();
        long now = System.nanoTime();
        Long last = alertCooldowns.get(key);
        if (last != null && now - last < 2_000_000_000L) return;
        alertCooldowns.put(key, now);

        if (!com.neonac.core.api.ApiEventDispatcher.fireAlert(v)) return;

        Map<String, Object> ctx = buildContext(v);

        String format = config.getString("alerts.format",
                "%prefix% &7%player% &8failed &b%check% &8VL=%vl%");
        format = format.replace("%prefix%", messages.getPrefix());
        String line = PlaceholderResolver.resolve(format, ctx);

        String perm = "neonac.alerts." + category;
        for (org.bukkit.command.CommandSender p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("neonac.alerts") || p.hasPermission(perm)) {
                p.sendMessage(line);
            }
        }
        Bukkit.getConsoleSender().sendMessage(line);

        if (config.getBoolean("discord.enabled", false)) {
            String webhook = config.getString("discord.webhook", "");
            if (!webhook.isEmpty()) {
                String content = "[NeonAC] " + v.getPlayerName() + " failed " + v.getCheck().getName()
                        + " VL=" + String.format("%.1f", v.getViolationLevel())
                        + " ping=" + ctx.get("ping") + " version=" + ctx.get("version");
                Bukkit.getScheduler().runTaskAsynchronously(plugin,
                        () -> DiscordWebhook.send(webhook, content));
            }
        }
    }

    private Map<String, Object> buildContext(Violation v) {
        Map<String, Object> ctx = PlaceholderResolver.fromViolation(v, serverName);
        PlayerData pd = playerManager.get(java.util.UUID.fromString(v.getPlayerUuid()));
        if (pd != null) {
            ctx.put("ping", pd.getPing());
            ctx.put("tps", String.format("%.1f", pd.getTPS()));
            ctx.put("version", pd.getVersion().name());
        } else {
            ctx.put("ping", "?");
            ctx.put("tps", "20.0");
            ctx.put("version", "?");
        }
        return ctx;
    }
}
