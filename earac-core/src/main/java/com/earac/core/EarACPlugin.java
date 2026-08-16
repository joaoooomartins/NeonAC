package com.earac.core;

import com.earac.api.EarACAPI;
import com.earac.api.check.CheckProvider;
import com.earac.api.version.MinecraftVersion;
import com.earac.core.alert.AlertManager;
import com.earac.core.check.CheckEngine;
import com.earac.core.config.ConfigManager;
import com.earac.core.config.MessageManager;
import com.earac.core.debug.DebugManager;
import com.earac.core.exemption.ExemptionManager;
import com.earac.core.metrics.Metrics;
import com.earac.core.movement.MovementEngine;
import com.earac.core.packet.PacketManager;
import com.earac.core.player.PlayerManager;
import com.earac.core.punishment.PunishmentManager;
import com.earac.core.storage.StorageManager;
import com.earac.core.version.VersionAdapterRegistry;
import com.earac.core.version.VersionDetector;
import com.earac.core.violation.ViolationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ServiceLoader;

/**
 * EarAC main plugin. Wires every subsystem, loads checks via {@link CheckProvider}
 * (SPI) and owns the per-tick + per-second scheduler tasks. Never depends on a
 * specific Minecraft version directly — all version logic is delegated to adapters.
 */
public final class EarACPlugin extends JavaPlugin implements Listener {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private PlayerManager playerManager;
    private ExemptionManager exemptionManager;
    private ViolationManager violationManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;
    private MovementEngine movementEngine;
    private Metrics metrics;
    private DebugManager debugManager;
    private StorageManager storageManager;
    private CheckEngine checkEngine;
    private PacketManager packetManager;

    private MinecraftVersion serverVersion;

    @Override
    public void onEnable() {
        VersionDetector.detect();
        serverVersion = VersionDetector.getServerVersion();

        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveResourceIfAbsent("config.yml");
        configManager = new ConfigManager(getDataFolder());
        configManager.load();
        configManager.loadProfile(configManager.getString("general.profile", ""));
        messageManager = new MessageManager(configManager);

        registerVersionAdapters();
        VersionAdapterRegistry.setFallback(new VersionAdapterRegistry.FallbackVersionAdapter());

        playerManager = new PlayerManager();
        exemptionManager = new ExemptionManager();
        movementEngine = new MovementEngine();
        metrics = new Metrics();
        metrics.setEnabled(configManager.getBoolean("metrics.enabled", true));
        debugManager = new DebugManager();
        storageManager = new StorageManager(configManager, getLogger());
        storageManager.init(getDataFolder());

        alertManager = new AlertManager(configManager, messageManager, playerManager);
        punishmentManager = new PunishmentManager(configManager, messageManager, playerManager);
        violationManager = new ViolationManager(this, alertManager, punishmentManager);

        checkEngine = new CheckEngine(this, exemptionManager, violationManager,
                playerManager, movementEngine, metrics);

        loadChecks();

        packetManager = new PacketManager(checkEngine, playerManager);
        Bukkit.getPluginManager().registerEvents(packetManager, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("earac") != null) {
            getCommand("earac").setExecutor(new com.earac.core.command.EarACCommand(this));
        }

        scheduleTasks();

        EarACAPI.set(new com.earac.core.api.EarACApiImpl(this));

        getLogger().info("[EarAC] Enabled. Server=" + VersionDetector.getServerImplementation()
                + " Version=" + serverVersion + " Checks=" + checkEngine.getAll().size());
    }

    @Override
    public void onDisable() {
        if (storageManager != null) storageManager.shutdown();
        EarACAPI.set(null);
    }

    private void registerVersionAdapters() {
        try {
            Class<?> cls = Class.forName("com.earac.versions.VersionAdapters");
            cls.getMethod("registerAll").invoke(null);
        } catch (ClassNotFoundException ignored) {
            // versions module not present — fallback adapter used.
        } catch (Exception e) {
            getLogger().warning("[EarAC] Failed to load version adapters: " + e.getMessage());
        }
    }

    private void loadChecks() {
        ServiceLoader<CheckProvider> loader = ServiceLoader.load(CheckProvider.class,
                Thread.currentThread().getContextClassLoader());
        int count = 0;
        for (CheckProvider provider : loader) {
            provider.registerChecks(checkEngine);
            count++;
        }
        if (count == 0) {
            getLogger().warning("[EarAC] No CheckProvider found. Is earac-checks installed?");
        }
    }

    private void scheduleTasks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                packetManager.tick();
            } catch (Throwable t) {
                getLogger().warning("[EarAC] tick error: " + t.getMessage());
            }
        }, 1L, 1L);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            violationManager.decayTick();
            double tps = Math.min(20.0, Bukkit.getTPS()[0]);
            com.earac.core.player.TpsTracker.update(tps);
        }, 20L, 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        playerManager.add(p);
        playerManager.markTeleport(p.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playerManager.remove(e.getPlayer().getUniqueId());
        debugManager.remove(e.getPlayer().getUniqueId());
    }

    private void saveResourceIfAbsent(String name) {
        File f = new File(getDataFolder(), name);
        if (!f.exists()) {
            saveResource(name, false);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ExemptionManager getExemptionManager() {
        return exemptionManager;
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public MovementEngine getMovementEngine() {
        return movementEngine;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public DebugManager getDebugManager() {
        return debugManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public CheckEngine getCheckEngine() {
        return checkEngine;
    }

    public MinecraftVersion getServerVersion() {
        return serverVersion;
    }
}
