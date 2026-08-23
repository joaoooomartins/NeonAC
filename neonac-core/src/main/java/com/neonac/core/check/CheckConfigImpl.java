package com.neonac.core.check;

import com.neonac.api.check.CheckConfig;
import com.neonac.api.version.MinecraftVersion;
import com.neonac.core.config.ConfigManager;

import java.util.List;
public final class CheckConfigImpl implements CheckConfig {

    private final ConfigManager config;
    private final String base;
    private final MinecraftVersion version;

    public CheckConfigImpl(ConfigManager config, String category, String checkName, MinecraftVersion version) {
        this.config = config;
        this.base = "checks." + category + "." + checkName;
        this.version = version;
    }

    private String vpath(String key) {
        if (version != null && version != MinecraftVersion.UNKNOWN) {
            String override = "versions." + version.getMinor() + "." + base + "." + key;
            if (config.getRaw().contains(override)) {
                return override;
            }
        }
        return base + "." + key;
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(vpath(path), def);
    }

    @Override
    public int getInt(String path, int def) {
        return config.getInt(vpath(path), def);
    }

    @Override
    public double getDouble(String path, double def) {
        return config.getDouble(vpath(path), def);
    }

    @Override
    public String getString(String path, String def) {
        return config.getString(vpath(path), def);
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        return config.getStringList(vpath(path), def);
    }

    @Override
    public double getPunishThreshold() {
        return getDouble("punish", 0);
    }

    @Override
    public double getAlertThreshold() {
        return getDouble("alert", 1.0);
    }

    @Override
    public double getVlAdd() {
        return getDouble("vl.add", 1.0);
    }

    @Override
    public double getVlDecay() {
        return getDouble("vl.decay", 0.05);
    }

    @Override
    public double getVlMax() {
        return getDouble("vl.max", 100.0);
    }
}
