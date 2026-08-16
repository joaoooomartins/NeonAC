package com.earac.core.alert;

import com.earac.api.player.EarACPlayer;
import com.earac.api.violation.Violation;
import com.earac.core.config.ConfigManager;
import com.earac.core.config.MessageManager;
import com.earac.core.config.PlaceholderResolver;
import com.earac.core.player.PlayerData;
import com.earac.core.player.PlayerManager;
import com.earac.core.webhook.DiscordWebhook;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

/**
 * Routes violations to staff chat, console and (optionally) a Discord webhook.
 * Per-category routing is honoured (e.g. {@code alerts.movement: false}).
 */
public final class AlertManager {

    private final ConfigManager config;
    private final MessageManager messages;
    private final PlayerManager playerManager;
    private final String serverName;

    public AlertManager(ConfigManager config, MessageManager messages, PlayerManager playerManager) {
        this.config = config;
        this.messages = messages;
        this.playerManager = playerManager;
        this.serverName = Bukkit.getServer() != null ? Bukkit.getServer().getName() : "unknown";
    }

    public void sendAlert(Violation v) {
        String category = v.getCheck().getCategory().getLowerCaseName();
        if (!config.getBoolean("alerts." + category, true)) return;
        if (!config.getBoolean("alerts.enabled", true)) return;

        // Allow other plugins to suppress the staff alert (not the violation).
        if (!com.earac.core.api.ApiEventDispatcher.fireAlert(v)) return;

        Map<String, Object> ctx = buildContext(v);

        String format = config.getString("alerts.format",
                "%prefix% &7%player% &8failed &b%check% &8VL=%vl%");
        format = format.replace("%prefix%", messages.getPrefix());
        String line = PlaceholderResolver.resolve(format, ctx);

        // Staff with permission
        String perm = "earac.alerts." + category;
        for (org.bukkit.command.CommandSender p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("earac.alerts") || p.hasPermission(perm)) {
                p.sendMessage(line);
            }
        }
        Bukkit.getConsoleSender().sendMessage(line);

        if (config.getBoolean("discord.enabled", false)) {
            String webhook = config.getString("discord.webhook", "");
            if (!webhook.isEmpty()) {
                String content = "[EarAC] " + v.getPlayerName() + " failed " + v.getCheck().getName()
                        + " VL=" + String.format("%.1f", v.getViolationLevel())
                        + " ping=" + ctx.get("ping") + " version=" + ctx.get("version");
                Bukkit.getScheduler().runTaskAsynchronously(
                        org.bukkit.Bukkit.getPluginManager().getPlugin("EarAC"),
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
