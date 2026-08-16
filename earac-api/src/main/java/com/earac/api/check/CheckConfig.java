package com.earac.api.check;

import java.util.List;

/**
 * Type-safe per-check configuration accessor. Implementations read from the
 * hierarchical YAML config with per-version overrides.
 */
public interface CheckConfig {

    boolean getBoolean(String path, boolean def);

    int getInt(String path, int def);

    double getDouble(String path, double def);

    String getString(String path, String def);

    List<String> getStringList(String path, List<String> def);

    /**
     * @return the configured punishment threshold for VL.
     */
    double getPunishThreshold();

    /**
     * @return the configured alert threshold for VL.
     */
    double getAlertThreshold();

    /**
     * @return VL added per detection.
     */
    double getVlAdd();

    /**
     * @return VL decayed per decay cycle when no detection occurs.
     */
    double getVlDecay();

    /**
     * @return maximum VL before the value is clamped.
     */
    double getVlMax();
}
