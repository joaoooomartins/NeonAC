package com.neonac.api.check;

import com.neonac.api.version.MinecraftVersion;

public interface Check {

    String getId();

    String getName();

    CheckCategory getCategory();

    String getDescription();

    boolean isEnabled();
    CheckConfig getConfig();
    boolean supports(MinecraftVersion version);
    String getBypassPermission();
    void setEnabled(boolean enabled);
    void resetPlayer(String playerUuid);
}
