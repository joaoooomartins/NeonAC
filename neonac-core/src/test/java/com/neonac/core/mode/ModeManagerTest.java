package com.neonac.core.mode;

import com.neonac.core.config.ConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ModeManagerTest {

    private ModeManager manager;
    private ConfigManager config;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        config = new ConfigManager(tempDir);
        config.load();
        manager = new ModeManager(config, Logger.getAnonymousLogger());
    }

    @Test
    void defaultMode_isNormal() {
        assertEquals(NeonACMode.NORMAL, manager.getMode());
    }

    @Test
    void setMode_changesMode() {
        manager.setMode(NeonACMode.SILENT);
        assertEquals(NeonACMode.SILENT, manager.getMode());
    }

    @Test
    void isSilent_trueWhenSilent() {
        manager.setMode(NeonACMode.SILENT);
        assertTrue(manager.isSilent());
    }

    @Test
    void shouldPunish_trueForNormalAndStrict() {
        manager.setMode(NeonACMode.NORMAL);
        assertTrue(manager.shouldPunish());
        manager.setMode(NeonACMode.STRICT);
        assertTrue(manager.shouldPunish());
    }

    @Test
    void shouldPunish_falseForSilentAndLogging() {
        manager.setMode(NeonACMode.SILENT);
        assertFalse(manager.shouldPunish());
        manager.setMode(NeonACMode.LOGGING);
        assertFalse(manager.shouldPunish());
    }

    @Test
    void shouldAlert_falseForTournament() {
        manager.setMode(NeonACMode.TOURNAMENT);
        assertFalse(manager.shouldAlert());
    }

    @Test
    void shouldAlert_trueForNormal() {
        manager.setMode(NeonACMode.NORMAL);
        assertTrue(manager.shouldAlert());
    }

    @Test
    void getVlMultiplier_strictIsOnePointFive() {
        manager.setMode(NeonACMode.STRICT);
        assertEquals(1.5, manager.getVlMultiplier(), 0.001);
    }

    @Test
    void getVlMultiplier_normalIsOne() {
        manager.setMode(NeonACMode.NORMAL);
        assertEquals(1.0, manager.getVlMultiplier(), 0.001);
    }
}
