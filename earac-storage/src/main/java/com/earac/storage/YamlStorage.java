package com.earac.storage;

import com.earac.api.storage.Storage;
import com.earac.api.storage.StorageException;
import com.earac.api.violation.Violation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * YAML/file-backed storage using {@link Properties} (no Bukkit dependency). Persists
 * current VL snapshots and a bounded log of recent violations. Suitable for small/medium
 * networks and as the always-available fallback.
 */
public final class YamlStorage implements Storage {

    private final File file;
    private final Properties data = new Properties();

    public YamlStorage(File dataFolder) {
        this.file = new File(dataFolder, "storage.properties");
    }

    @Override
    public void init() throws StorageException {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new StorageException("Cannot create storage.properties", e);
            }
        }
        try (FileInputStream in = new FileInputStream(file)) {
            data.load(in);
        } catch (IOException e) {
            throw new StorageException("Cannot read storage.properties", e);
        }
    }

    @Override
    public void shutdown() {
        save();
    }

    @Override
    public void saveViolation(Violation violation) {
        String key = "log." + violation.getPlayerUuid() + "." + System.nanoTime();
        data.setProperty(key, violation.getCheck().getId() + ":" + violation.getViolationLevel()
                + ":" + violation.getConfidence());
        // Bound the log to the 200 most recent entries.
        List<String> keys = new ArrayList<>(data.stringPropertyNames());
        if (keys.size() > 400) {
            keys.stream().filter(k -> k.startsWith("log.")).sorted()
                    .limit(keys.size() - 200).forEach(data::remove);
        }
        save();
    }

    @Override
    public double getViolationLevel(UUID playerUuid, String checkId) {
        String v = data.getProperty("vl." + playerUuid + "." + checkId);
        return v == null ? 0.0 : Double.parseDouble(v);
    }

    @Override
    public Map<String, Double> getAllViolationLevels(UUID playerUuid) {
        Map<String, Double> result = new LinkedHashMap<>();
        String prefix = "vl." + playerUuid + ".";
        for (String k : data.stringPropertyNames()) {
            if (k.startsWith(prefix)) {
                result.put(k.substring(prefix.length()), Double.parseDouble(data.getProperty(k)));
            }
        }
        return result;
    }

    @Override
    public void setViolationLevel(UUID playerUuid, String checkId, double vl) {
        data.setProperty("vl." + playerUuid + "." + checkId, Double.toString(vl));
        save();
    }

    @Override
    public void resetViolationLevels(UUID playerUuid) {
        String prefix = "vl." + playerUuid + ".";
        data.stringPropertyNames().removeIf(k -> k.startsWith(prefix));
        save();
    }

    @Override
    public List<Violation> getRecentViolations(UUID playerUuid, int limit) {
        // Properties backend stores VL snapshots; full Violation reconstruction is delegated
        // to JDBC backends. Returns empty for API parity.
        return new ArrayList<>();
    }

    @Override
    public Object getRawHandle() {
        return data;
    }

    private void save() {
        try (FileOutputStream out = new FileOutputStream(file)) {
            data.store(out, "EarAC storage");
        } catch (IOException ignored) {
        }
    }
}
