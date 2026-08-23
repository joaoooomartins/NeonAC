package com.neonac.api.check;

import java.util.List;
public interface CheckConfig {

    boolean getBoolean(String path, boolean def);

    int getInt(String path, int def);

    double getDouble(String path, double def);

    String getString(String path, String def);

    List<String> getStringList(String path, List<String> def);
    double getPunishThreshold();
    double getAlertThreshold();
    double getVlAdd();
    double getVlDecay();
    double getVlMax();
}
