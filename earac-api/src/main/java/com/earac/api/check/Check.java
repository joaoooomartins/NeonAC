package com.earac.api.check;

import com.earac.api.version.MinecraftVersion;

import java.util.Set;

/**
 * Contract for every detection. Concrete checks extend {@code AbstractCheck} in core,
 * but engines and the API only depend on this interface.
 */
public interface Check {

    String getId();

    String getName();

    CheckCategory getCategory();

    String getDescription();

    boolean isEnabled();

    /**
     * @return the resolved configuration for this check (per-version overrides applied).
     */
    CheckConfig getConfig();

    /**
     * Whether this check applies to the given version.
     */
    boolean supports(MinecraftVersion version);

    /**
     * @return the permission node that grants bypass for this check (e.g. earac.bypass.combat.killaura).
     */
    String getBypassPermission();

    /**
     * Called once when the check is enabled/disabled at runtime.
     */
    void setEnabled(boolean enabled);

    /**
     * Reset all state for a player (e.g. on disconnect or /earac reset).
     */
    void resetPlayer(String playerUuid);
}
