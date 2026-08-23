package com.neonac.core.violation;

import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckConfig;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.NeonACPlugin;
import com.neonac.core.alert.AlertManager;
import com.neonac.core.exemption.ExemptionManager;
import com.neonac.core.mode.ModeManager;
import com.neonac.core.punishment.PunishmentManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViolationManagerTest {

    private ViolationManager manager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;
    private NeonACPlugin plugin;
    private ExemptionManager exemptionManager;
    private ModeManager modeManager;
    private UUID testUuid;
    private NeonACPlayer mockPlayer;
    private Check mockCheck;
    private CheckConfig mockConfig;
    private static MockedStatic<Bukkit> bukkitMock;

    @BeforeAll
    static void setUpStatic() {
        bukkitMock = mockStatic(Bukkit.class);
        PluginManager mockPM = mock(PluginManager.class);
        doNothing().when(mockPM).callEvent(any());
        bukkitMock.when(Bukkit::getPluginManager).thenReturn(mockPM);
    }

    @AfterAll
    static void tearDownStatic() {
        bukkitMock.close();
    }

    @BeforeEach
    void setUp() {
        plugin = mock(NeonACPlugin.class);
        alertManager = mock(AlertManager.class);
        punishmentManager = mock(PunishmentManager.class);
        exemptionManager = mock(ExemptionManager.class);
        modeManager = mock(ModeManager.class);

        when(plugin.getExemptionManager()).thenReturn(exemptionManager);

        manager = new ViolationManager(plugin, alertManager, punishmentManager);
        manager.setModeManager(modeManager);

        testUuid = UUID.randomUUID();
        mockPlayer = mock(NeonACPlayer.class);
        mockCheck = mock(Check.class);
        mockConfig = mock(CheckConfig.class);

        when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        when(mockCheck.getId()).thenReturn("testcheck");
        when(mockCheck.getCategory()).thenReturn(CheckCategory.COMBAT);
        when(mockCheck.getConfig()).thenReturn(mockConfig);
        when(mockConfig.getVlMax()).thenReturn(100.0);
        when(mockConfig.getAlertThreshold()).thenReturn(5.0);
        when(mockConfig.getPunishThreshold()).thenReturn(20.0);
        when(mockConfig.getVlAdd()).thenReturn(1.0);
        when(mockConfig.getVlDecay()).thenReturn(0.05);
    }

    @Test
    void flag_increasesVL() {
        when(modeManager.getVlMultiplier()).thenReturn(1.0);
        when(modeManager.isSilent()).thenReturn(false);
        when(modeManager.isLogging()).thenReturn(false);
        manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        assertEquals(1.0, manager.getVL(testUuid, "testcheck"), 0.001);
    }

    @Test
    void flag_multipleFlags_accumulateVL() {
        when(modeManager.getVlMultiplier()).thenReturn(1.0);
        when(modeManager.isSilent()).thenReturn(false);
        when(modeManager.isLogging()).thenReturn(false);
        manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        assertEquals(3.0, manager.getVL(testUuid, "testcheck"), 0.001);
    }

    @Test
    void flag_vlDoesNotExceedMax() {
        when(modeManager.getVlMultiplier()).thenReturn(1.0);
        when(modeManager.isSilent()).thenReturn(false);
        when(modeManager.isLogging()).thenReturn(false);
        for (int i = 0; i < 200; i++) {
            manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        }
        assertEquals(100.0, manager.getVL(testUuid, "testcheck"), 0.001);
    }

    @Test
    void flag_strictMode_multipliesVl() {
        when(modeManager.getVlMultiplier()).thenReturn(1.5);
        when(modeManager.isSilent()).thenReturn(false);
        when(modeManager.isLogging()).thenReturn(false);
        manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        assertEquals(1.5, manager.getVL(testUuid, "testcheck"), 0.001);
    }

    @Test
    void flag_doesNotPunishInSilentMode() {
        when(modeManager.getVlMultiplier()).thenReturn(1.0);
        when(modeManager.shouldPunish()).thenReturn(false);
        when(modeManager.isSilent()).thenReturn(true);
        when(modeManager.isLogging()).thenReturn(false);

        for (int i = 0; i < 25; i++) {
            manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        }
        verify(punishmentManager, never()).punish(any());
    }

    @Test
    void flag_doesNotAlertInTournamentMode() {
        when(modeManager.getVlMultiplier()).thenReturn(1.0);
        when(modeManager.shouldAlert()).thenReturn(false);
        when(modeManager.isSilent()).thenReturn(false);
        when(modeManager.isLogging()).thenReturn(false);

        for (int i = 0; i < 10; i++) {
            manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        }
        verify(alertManager, never()).sendAlert(any());
    }

    @Test
    void reset_clearsAllVL() {
        when(modeManager.getVlMultiplier()).thenReturn(1.0);
        when(modeManager.isSilent()).thenReturn(false);
        when(modeManager.isLogging()).thenReturn(false);
        manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        manager.reset(testUuid);
        assertEquals(0.0, manager.getVL(testUuid, "testcheck"), 0.001);
    }

    @Test
    void decayTick_reducesVlOverTime() {
        when(modeManager.getVlMultiplier()).thenReturn(1.0);
        when(modeManager.isSilent()).thenReturn(false);
        when(modeManager.isLogging()).thenReturn(false);
        manager.flag(mockCheck, mockPlayer, 5.0, 1.0, Map.of());
        double before = manager.getVL(testUuid, "testcheck");

        try { Thread.sleep(1100); } catch (InterruptedException ignored) {}

        manager.decayTick();
        double after = manager.getVL(testUuid, "testcheck");
        assertTrue(after < before, "VL should decay after 1s idle");
    }

    @Test
    void getAll_returnsAllChecksForPlayer() {
        when(modeManager.getVlMultiplier()).thenReturn(1.0);
        when(modeManager.isSilent()).thenReturn(false);
        when(modeManager.isLogging()).thenReturn(false);
        manager.flag(mockCheck, mockPlayer, 1.0, 1.0, Map.of());
        Map<String, Double> all = manager.getAll(testUuid);
        assertEquals(1, all.size());
        assertEquals(1.0, all.get("testcheck"), 0.001);
    }
}
