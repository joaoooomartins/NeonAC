package com.neonac.core.mode;

import com.neonac.core.config.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ModeManager {

    private final ConfigManager config;
    private final Logger logger;
    private NeonACMode currentMode = NeonACMode.NORMAL;

    public ModeManager(ConfigManager config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void load() {
        String mode = config.getString("general.mode", "normal").toLowerCase();
        try {
            currentMode = NeonACMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            currentMode = NeonACMode.NORMAL;
        }
        logger.info("[NeonAC] Mode: " + currentMode.name().toLowerCase());
    }

    public NeonACMode getMode() {
        return currentMode;
    }

    public void setMode(NeonACMode mode) {
        this.currentMode = mode;
        config.getRaw().set("general.mode", mode.name().toLowerCase());
        try {
            config.getRaw().save(new File(config.getRaw().getCurrentPath() != null
                    ? new File(config.getRaw().getCurrentPath()).getParentFile()
                    : new File("."), "config.yml"));
        } catch (IOException ignored) {
        }
    }

    public boolean isSilent() {
        return currentMode == NeonACMode.SILENT;
    }

    public boolean isLogging() {
        return currentMode == NeonACMode.LOGGING;
    }

    public boolean isStrict() {
        return currentMode == NeonACMode.STRICT;
    }

    public boolean isTournament() {
        return currentMode == NeonACMode.TOURNAMENT;
    }

    public boolean shouldPunish() {
        return currentMode == NeonACMode.NORMAL || currentMode == NeonACMode.STRICT;
    }

    public boolean shouldAlert() {
        return currentMode != NeonACMode.TOURNAMENT;
    }

    public double getVlMultiplier() {
        return currentMode == NeonACMode.STRICT ? 1.5 : 1.0;
    }
}
