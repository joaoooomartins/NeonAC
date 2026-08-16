package com.earac.core.storage;

import com.earac.api.storage.Storage;
import com.earac.api.storage.StorageException;
import com.earac.core.config.ConfigManager;
import com.earac.storage.MysqlStorage;
import com.earac.storage.SqliteStorage;
import com.earac.storage.YamlStorage;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Selects and owns the active {@link Storage} backend. Falls back to YAML if a
 * database backend cannot be initialised (e.g. missing driver), so the plugin
 * always remains operational.
 */
public final class StorageManager {

    private final ConfigManager config;
    private final Logger logger;
    private Storage storage;

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
                        config.getString("storage.mysql.database", "earac"),
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
            logger.info("[EarAC] Storage backend: " + type);
        } catch (StorageException e) {
            logger.log(Level.WARNING, "[EarAC] Failed to init '" + type + "' storage, falling back to YAML.", e);
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

    public void shutdown() {
        if (storage != null) storage.shutdown();
    }
}
