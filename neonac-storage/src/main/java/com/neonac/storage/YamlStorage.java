package com.neonac.storage;

import com.neonac.api.storage.Storage;
import com.neonac.api.storage.StorageException;
import com.neonac.api.violation.Violation;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class YamlStorage implements Storage {

    private static final Logger logger = Logger.getLogger("NeonAC-Storage");
    private final File file;
    private final Properties data = new Properties();
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private volatile long lastSave = System.currentTimeMillis();
    private static final long SAVE_INTERVAL_MS = 5000;
    private final List<Violation> recentViolations = new ArrayList<>();

    public YamlStorage(File dataFolder) {
        this.file = new File(dataFolder, "neonac.properties");
    }

    @Override
    public void init() throws StorageException {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new StorageException("Cannot create neonac.properties", e);
            }
        }
        try (FileInputStream in = new FileInputStream(file)) {
            data.load(in);
        } catch (IOException e) {
            throw new StorageException("Cannot read neonac.properties", e);
        }
    }

    @Override
    public void shutdown() {
        saveNow();
    }

    @Override
    public void saveViolation(Violation violation) {
        synchronized (recentViolations) {
            recentViolations.add(0, violation);
            while (recentViolations.size() > 200) recentViolations.remove(recentViolations.size() - 1);
        }
        String key = "log." + violation.getPlayerUuid() + "." + System.nanoTime();
        data.setProperty(key, violation.getCheck().getId() + ":" + violation.getViolationLevel()
                + ":" + violation.getConfidence());
        trimLogs();
        markDirty();
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
        markDirty();
    }

    @Override
    public void resetViolationLevels(UUID playerUuid) {
        String prefix = "vl." + playerUuid + ".";
        data.stringPropertyNames().removeIf(k -> k.startsWith(prefix));
        markDirty();
    }

    @Override
    public List<Violation> getRecentViolations(UUID playerUuid, int limit) {
        synchronized (recentViolations) {
            return recentViolations.stream()
                    .filter(v -> v.getPlayerUuid().equals(playerUuid.toString()))
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    @Override
    public Object getRawHandle() {
        return data;
    }

    private void markDirty() {
        dirty.set(true);
        if (System.currentTimeMillis() - lastSave > SAVE_INTERVAL_MS) {
            saveNow();
        }
    }

    private void saveNow() {
        if (!dirty.compareAndSet(true, false)) return;
        try (FileOutputStream out = new FileOutputStream(file)) {
            data.store(out, "NeonAC storage");
            lastSave = System.currentTimeMillis();
        } catch (IOException e) {
            logger.warning("[NeonAC] Failed to save storage.properties: " + e.getMessage());
            dirty.set(true);
        }
    }

    private void trimLogs() {
        List<String> keys = new ArrayList<>(data.stringPropertyNames());
        if (keys.size() > 500) {
            keys.stream().filter(k -> k.startsWith("log.")).sorted()
                    .limit(keys.size() - 300).forEach(data::remove);
        }
    }
}
