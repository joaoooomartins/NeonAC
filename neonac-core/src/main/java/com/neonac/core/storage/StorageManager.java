package com.neonac.core.storage;

import com.neonac.api.storage.Storage;
import com.neonac.api.storage.StorageException;
import com.neonac.api.violation.Violation;
import com.neonac.core.config.ConfigManager;
import com.neonac.storage.MysqlStorage;
import com.neonac.storage.SqliteStorage;
import com.neonac.storage.YamlStorage;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StorageManager {

    private final ConfigManager config;
    private final Logger logger;
    private Storage storage;
    private final ExecutorService asyncPool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "NeonAC-Storage");
        t.setDaemon(true);
        return t;
    });

    public StorageManager(ConfigManager config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void init(File dataFolder) {
        String type = config.getString("storage.type", "yaml").toLowerCase();
        Storage candidate;
        switch (type) {
            case "sqlite":
                candidate = new SqliteStorage(dataFolder);
                break;
            case "mysql":
            case "mariadb":
                candidate = new MysqlStorage(
                        config.getString("storage.mysql.host", "localhost"),
                        config.getInt("storage.mysql.port", 3306),
                        config.getString("storage.mysql.database", "neonac"),
                        config.getString("storage.mysql.user", "root"),
                        config.getString("storage.mysql.password", ""),
                        config.getString("storage.mysql.params", "useSSL=false&serverTimezone=UTC"));
                break;
            default:
                candidate = new YamlStorage(dataFolder);
        }
        try {
            candidate.init();
            this.storage = candidate;
            logger.info("[NeonAC] Storage backend: " + type);
        } catch (StorageException e) {
            logger.log(Level.WARNING, "[NeonAC] Failed to init '" + type + "' storage, falling back to YAML.", e);
            YamlStorage yaml = new YamlStorage(dataFolder);
            try {
                yaml.init();
                this.storage = yaml;
            } catch (StorageException ex) {
                throw new RuntimeException("Could not initialise fallback YAML storage", ex);
            }
        }
    }

    public Storage get() {
        return storage;
    }

    public boolean isHealthy() {
        if (storage == null) return false;
        try {
            storage.getViolationLevel(UUID.fromString("00000000-0000-0000-0000-000000000000"), "healthcheck");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public void saveViolationAsync(Violation v) {
        if (storage == null) return;
        asyncPool.execute(() -> {
            try {
                storage.saveViolation(v);
            } catch (Throwable ignored) {
            }
        });
    }

    public void setVLAsync(UUID uuid, String checkId, double vl) {
        if (storage == null) return;
        asyncPool.execute(() -> {
            try {
                storage.setViolationLevel(uuid, checkId, vl);
            } catch (Throwable ignored) {
            }
        });
    }

    public void shutdown() {
        asyncPool.shutdown();
        try {
            asyncPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        if (storage != null) storage.shutdown();
    }
}
