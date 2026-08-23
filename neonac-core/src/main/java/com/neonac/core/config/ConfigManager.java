package com.neonac.core.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
public final class ConfigManager {

    private final File dataFolder;
    private YamlConfiguration main;
    private final File mainFile;

    public ConfigManager(File dataFolder) {
        this.dataFolder = dataFolder;
        this.mainFile = new File(dataFolder, "config.yml");
    }

    public void load() {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        if (!mainFile.exists()) {
            saveDefault();
        }
        this.main = YamlConfiguration.loadConfiguration(mainFile);
    }

    private void saveDefault() {
        if (!mainFile.exists()) {
            try {
                mainFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Could not create config.yml", e);
            }
        }
    }

    public void save() {
        try {
            main.save(mainFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not save config.yml", e);
        }
    }

    public void reload() {
        load();
    }
    public boolean loadProfile(String name) {
        if (name == null || name.isEmpty()) return false;
        File pf = new File(dataFolder, "profiles" + File.separator + name + ".yml");
        if (!pf.exists()) return false;
        YamlConfiguration profile = YamlConfiguration.loadConfiguration(pf);
        for (String key : profile.getKeys(true)) {
            if (profile.get(key) != null) {
                main.set(key, profile.get(key));
            }
        }
        return true;
    }

    public YamlConfiguration getRaw() {
        return main;
    }

    public boolean getBoolean(String path, boolean def) {
        return main.contains(path) ? main.getBoolean(path) : def;
    }

    public int getInt(String path, int def) {
        return main.contains(path) ? main.getInt(path) : def;
    }

    public double getDouble(String path, double def) {
        return main.contains(path) ? main.getDouble(path) : def;
    }

    public long getLong(String path, long def) {
        return main.contains(path) ? main.getLong(path) : def;
    }

    public String getString(String path, String def) {
        return main.contains(path) ? main.getString(path) : def;
    }

    public List<String> getStringList(String path, List<String> def) {
        return main.contains(path) ? main.getStringList(path) : def;
    }

    public ConfigurationSection getSection(String path) {
        return main.getConfigurationSection(path);
    }

    public Set<String> getKeys(String path) {
        ConfigurationSection s = main.getConfigurationSection(path);
        return s != null ? s.getKeys(false) : Set.of();
    }
}
