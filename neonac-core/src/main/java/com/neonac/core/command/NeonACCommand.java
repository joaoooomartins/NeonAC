package com.neonac.core.command;

import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.exemption.ExemptionType;
import com.neonac.api.violation.Violation;
import com.neonac.core.NeonACPlugin;
import com.neonac.core.config.MessageManager;
import com.neonac.core.debug.DebugManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public final class NeonACCommand implements TabExecutor {

    private final NeonACPlugin plugin;

    public NeonACCommand(NeonACPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageManager msg = plugin.getMessageManager();
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help":
                sendHelp(sender);
                break;
            case "reload":
                if (!sender.hasPermission("neonac.command.reload")) return deny(sender, msg);
                plugin.getConfigManager().reload();
                plugin.getPunishmentManager().reload();
                reloadChecks();
                sender.sendMessage(msg.get("reload"));
                break;
            case "version":
                sender.sendMessage(msg.getPrefix() + " &7Server: &f"
                        + com.neonac.core.version.VersionDetector.getServerImplementation()
                        + " &7Version: &f" + plugin.getServerVersion());
                break;
            case "checks":
                if (!sender.hasPermission("neonac.command.checks")) return deny(sender, msg);
                for (Check c : plugin.getCheckEngine().getAll()) {
                    sender.sendMessage(" &8- &b" + c.getId() + " &8[&7"
                            + c.getCategory() + "&8] &8enabled=" + c.isEnabled());
                }
                break;
            case "info":
            case "debug":
                if (!sender.hasPermission("neonac.command.debug")) return deny(sender, msg);
                handleDebug(sender, args);
                break;
            case "violations":
            case "vl":
                if (!sender.hasPermission("neonac.command.violations")) return deny(sender, msg);
                handleViolations(sender, args);
                break;
            case "reset":
                if (!sender.hasPermission("neonac.command.reset")) return deny(sender, msg);
                handleReset(sender, args);
                break;
            case "punish":
                if (!sender.hasPermission("neonac.command.punish")) return deny(sender, msg);
                handlePunish(sender, args);
                break;
            case "bypass":
                if (!sender.hasPermission("neonac.command.bypass")) return deny(sender, msg);
                handleBypass(sender, args);
                break;
            case "alerts":
                sender.sendMessage(msg.getPrefix() + " &7Alerts enabled globally: &f"
                        + plugin.getConfigManager().getBoolean("alerts.enabled", true));
                break;
            default:
                sendHelp(sender);
        }
        return true;
    }

    private void reloadChecks() {
        for (Check c : plugin.getCheckEngine().getAll()) {
            boolean en = plugin.getConfigManager().getBoolean(
                    "checks." + c.getCategory().getLowerCaseName() + "." + c.getId() + ".enabled", true);
            plugin.getCheckEngine().setEnabled(c.getId(), en);
        }
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &cUsage: /NeonAC debug <player>");
            return;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (p == null) {
            sender.sendMessage(plugin.getMessageManager().get("player-not-found", Map.of("player", args[1])));
            return;
        }
        UUID uuid = p.getUniqueId();
        DebugManager dm = plugin.getDebugManager();
        dm.toggle(uuid);
        com.neonac.core.player.PlayerData pd = plugin.getPlayerManager().get(uuid);
        if (pd != null && dm.isDebugging(uuid)) {
            sender.sendMessage(dm.snapshot(pd, plugin.getMovementEngine(), plugin.getViolationManager()));
        } else {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &7Debug toggled for &f" + args[1]);
        }
    }

    private void handleViolations(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &cUsage: /NeonAC violations <player>");
            return;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (p == null) {
            sender.sendMessage(plugin.getMessageManager().get("player-not-found", Map.of("player", args[1])));
            return;
        }
        Map<String, Double> vls = plugin.getViolationManager().getAll(p.getUniqueId());
        sender.sendMessage(plugin.getMessageManager().getPrefix() + " &7VL for &f" + p.getName() + ":");
        if (vls.isEmpty()) sender.sendMessage(" &8- &7none");
        for (Map.Entry<String, Double> e : vls.entrySet()) {
            sender.sendMessage(" &8- &b" + e.getKey() + ": &c" + String.format("%.1f", e.getValue()));
        }
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &cUsage: /NeonAC reset <player>");
            return;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (p == null) {
            sender.sendMessage(plugin.getMessageManager().get("player-not-found", Map.of("player", args[1])));
            return;
        }
        plugin.getCheckEngine().resetPlayer(p.getUniqueId().toString());
        sender.sendMessage(plugin.getMessageManager().getPrefix() + " &aReset &f" + p.getName());
    }

    private void handlePunish(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &cUsage: /NeonAC punish <player> <checkId>");
            return;
        }
        Player p = Bukkit.getPlayer(args[1]);
        Check c = plugin.getCheckEngine().get(args[2]);
        if (p == null || c == null) {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &cInvalid player or check.");
            return;
        }
        double punish = c.getConfig().getPunishThreshold() > 0 ? c.getConfig().getPunishThreshold() : 50.0;
        plugin.getViolationManager().setVL(p.getUniqueId(), c.getId(), punish);
        Violation v = new com.neonac.core.violation.ViolationImpl(
                p.getUniqueId().toString(), p.getName(), c, 0.0, punish, 1.0, Map.of("manual", true));
        plugin.getPunishmentManager().punish(v);
        sender.sendMessage(plugin.getMessageManager().getPrefix() + " &aPunished &f" + p.getName() + " &7(" + c.getId() + ")");
    }

    private void handleBypass(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &cUsage: /neonac bypass <player> [check|category:<cat>]");
            return;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (p == null) {
            sender.sendMessage(plugin.getMessageManager().get("player-not-found", Map.of("player", args[1])));
            return;
        }
        UUID uuid = p.getUniqueId();
        long duration = 10 * 60 * 1000L;

        if (args.length < 3) {
            plugin.getExemptionManager().addTimedGlobal(uuid, ExemptionType.PLUGIN, duration);
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &aBypassed &f" + p.getName() + " &7(10m, global)");
            return;
        }

        String target = args[2].toLowerCase();
        if (target.startsWith("category:")) {
            String catName = target.substring("category:".length());
            try {
                CheckCategory cat = CheckCategory.valueOf(catName.toUpperCase());
                plugin.getExemptionManager().addTimedCategory(uuid, cat, ExemptionType.PLUGIN, duration);
                sender.sendMessage(plugin.getMessageManager().getPrefix() + " &aBypassed &f" + p.getName() + " &7from " + cat.name() + " (10m)");
            } catch (IllegalArgumentException e) {
                sender.sendMessage(plugin.getMessageManager().getPrefix() + " &cInvalid category. Valid: combat, movement, player, world, packet");
            }
        } else {
            Check check = plugin.getCheckEngine().get(target);
            if (check == null) {
                sender.sendMessage(plugin.getMessageManager().getPrefix() + " &cCheck '" + target + "' not found.");
                return;
            }
            plugin.getExemptionManager().addTimedCheck(uuid, target, ExemptionType.PLUGIN, duration);
            sender.sendMessage(plugin.getMessageManager().getPrefix() + " &aBypassed &f" + p.getName() + " &7from " + target + " (10m)");
        }
    }

    private boolean deny(CommandSender sender, MessageManager msg) {
        sender.sendMessage(msg.get("no-permission"));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        String p = plugin.getMessageManager().getPrefix();
        sender.sendMessage(p + " &bNeonAC &7commands:");
        sender.sendMessage(" &8/&bNeonAC help &7- show this help");
        sender.sendMessage(" &8/&bNeonAC reload &7- reload configuration");
        sender.sendMessage(" &8/&bNeonAC version &7- version info");
        sender.sendMessage(" &8/&bNeonAC checks &7- list checks");
        sender.sendMessage(" &8/&bNeonAC info <player> &7- debug snapshot");
        sender.sendMessage(" &8/&bNeonAC violations <player> &7- VL list");
        sender.sendMessage(" &8/&bNeonAC reset <player> &7- reset VL");
        sender.sendMessage(" &8/&bneonac punish <player> <check> &7- force punishment");
        sender.sendMessage(" &8/&bneonac bypass <player> [check|category:<cat>] &7- exemption");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : new String[]{"help", "reload", "version", "checks", "info",
                    "violations", "reset", "punish", "bypass", "alerts"}) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("bypass")) {
            String prefix = args[2].toLowerCase();
            for (Check c : plugin.getCheckEngine().getAll()) {
                String id = c.getId().toLowerCase();
                if (id.startsWith(prefix)) out.add(c.getId());
            }
            for (CheckCategory cat : CheckCategory.values()) {
                String key = "category:" + cat.name().toLowerCase();
                if (key.startsWith(prefix)) out.add(key);
            }
        }
        return out;
    }
}
