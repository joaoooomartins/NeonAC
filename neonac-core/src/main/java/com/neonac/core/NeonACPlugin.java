package com.neonac.core;

import com.neonac.api.NeonACAPI;
import com.neonac.api.check.CheckProvider;
import com.neonac.api.version.MinecraftVersion;
import com.neonac.core.alert.AlertManager;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.config.ConfigManager;
import com.neonac.core.config.MessageManager;
import com.neonac.core.debug.DebugManager;
import com.neonac.core.exemption.ExemptionManager;
import com.neonac.core.metrics.Metrics;
import com.neonac.core.movement.MovementEngine;
import com.neonac.core.packet.PacketManager;
import com.neonac.core.player.PlayerManager;
import com.neonac.core.punishment.PunishmentManager;
import com.neonac.core.storage.StorageManager;
import com.neonac.core.version.VersionAdapterRegistry;
import com.neonac.core.version.VersionDetector;
import com.neonac.core.violation.ViolationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ServiceLoader;
public final class NeonACPlugin extends JavaPlugin implements Listener {

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

        alertManager = new AlertManager(this, configManager, messageManager, playerManager);
        punishmentManager = new PunishmentManager(this, configManager, messageManager, playerManager);
        violationManager = new ViolationManager(this, alertManager, punishmentManager);

        checkEngine = new CheckEngine(this, exemptionManager, violationManager,
                playerManager, movementEngine, metrics);

        loadChecks();

        packetManager = new PacketManager(checkEngine, playerManager);
        Bukkit.getPluginManager().registerEvents(packetManager, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("neonac") != null) {
            getCommand("neonac").setExecutor(new com.neonac.core.command.NeonACCommand(this));
        }

        scheduleTasks();

        NeonACAPI.set(new com.neonac.core.api.NeonACAPIImpl(this));

        getLogger().info("[NeonAC] Enabled. Server=" + VersionDetector.getServerImplementation()
                + " Version=" + serverVersion + " Checks=" + checkEngine.getAll().size());
    }

    @Override
    public void onDisable() {
        persistAllVLs();
        if (storageManager != null) storageManager.shutdown();
        NeonACAPI.set(null);
    }

    private void persistAllVLs() {
        if (storageManager == null || storageManager.get() == null) return;
        for (java.util.Map.Entry<java.util.UUID, java.util.Map<String, Double>> entry : violationManager.getAllLevels().entrySet()) {
            for (java.util.Map.Entry<String, Double> check : entry.getValue().entrySet()) {
                if (check.getValue() > 0) {
                    storageManager.get().setViolationLevel(entry.getKey(), check.getKey(), check.getValue());
                }
            }
        }
    }

    private void registerVersionAdapters() {
        try {
            Class<?> cls = Class.forName("com.neonac.versions.VersionAdapters");
            cls.getMethod("registerAll").invoke(null);
        } catch (ClassNotFoundException ignored) {
            // versions module not present — fallback adapter used.
        } catch (Exception e) {
            getLogger().warning("[NeonAC] Failed to load version adapters: " + e.getMessage());
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
            getLogger().warning("[NeonAC] No CheckProvider found. Is NeonAC-checks installed?");
        }
    }

    private void scheduleTasks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                packetManager.tick();
            } catch (Throwable t) {
                getLogger().warning("[NeonAC] tick error: " + t.getMessage());
            }
        }, 1L, 20L);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            violationManager.decayTick();
            double tps = Math.min(20.0, Bukkit.getTPS()[0]);
            com.neonac.core.player.TpsTracker.update(tps);
            violationManager.cleanExpiredTimedExemptions();
        }, 20L, 20L);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            persistAllVLs();
        }, 6000L, 6000L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        playerManager.add(p);
        playerManager.markTeleport(p.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        java.util.UUID uuid = e.getPlayer().getUniqueId();
        playerManager.remove(uuid);
        debugManager.remove(uuid);
        exemptionManager.removeAll(uuid);
        violationManager.reset(uuid);
        punishmentManager.clearCooldown(uuid);
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
