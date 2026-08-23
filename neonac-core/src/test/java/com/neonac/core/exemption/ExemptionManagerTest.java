package com.neonac.core.exemption;

import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckConfig;
import com.neonac.api.exemption.ExemptionType;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.version.MinecraftVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExemptionManagerTest {

    private ExemptionManager manager;
    private UUID testUuid;
    private NeonACPlayer mockPlayer;
    private Check mockCheck;

    @BeforeEach
    void setUp() {
        manager = new ExemptionManager();
        testUuid = UUID.randomUUID();
        mockPlayer = mock(NeonACPlayer.class);
        mockCheck = mock(Check.class);
        when(mockPlayer.getUniqueId()).thenReturn(testUuid);
        when(mockCheck.getId()).thenReturn("testcheck");
        when(mockCheck.getCategory()).thenReturn(CheckCategory.COMBAT);
        when(mockCheck.getBypassPermission()).thenReturn("neonac.bypass.combat.testcheck");
        when(mockPlayer.getLastTeleportAge()).thenReturn(5_000_000_000L);
        when(mockPlayer.getLastVelocityAge()).thenReturn(5_000_000_000L);
        when(mockPlayer.getPing()).thenReturn(10);
    }

    @Test
    void globalExemption_blocksCheck() {
        manager.addGlobalExemption(testUuid, ExemptionType.PLUGIN);
        assertTrue(manager.isExempt(mockPlayer, mockCheck));
    }

    @Test
    void globalExemption_removable() {
        manager.addGlobalExemption(testUuid, ExemptionType.PLUGIN);
        assertTrue(manager.isExempt(mockPlayer, mockCheck));
        manager.removeGlobalExemption(testUuid, ExemptionType.PLUGIN);
        assertFalse(manager.isExempt(mockPlayer, mockCheck));
    }

    @Test
    void perCheckExemption_blocksOnlyThatCheck() {
        manager.addCheckExemption(testUuid, "testcheck", ExemptionType.PLUGIN);
        assertTrue(manager.isExempt(mockPlayer, mockCheck));

        Check otherCheck = mock(Check.class);
        when(otherCheck.getId()).thenReturn("othercheck");
        when(otherCheck.getCategory()).thenReturn(CheckCategory.COMBAT);
        when(otherCheck.getBypassPermission()).thenReturn("neonac.bypass.combat.othercheck");
        assertFalse(manager.isExempt(mockPlayer, otherCheck));
    }

    @Test
    void perCategoryExemption_blocksAllInCategory() {
        manager.addCategoryExemption(testUuid, CheckCategory.COMBAT, ExemptionType.PLUGIN);
        assertTrue(manager.isExempt(mockPlayer, mockCheck));

        Check otherCombat = mock(Check.class);
        when(otherCombat.getId()).thenReturn("othercombat");
        when(otherCombat.getCategory()).thenReturn(CheckCategory.COMBAT);
        when(otherCombat.getBypassPermission()).thenReturn("neonac.bypass.combat.othercombat");
        assertTrue(manager.isExempt(mockPlayer, otherCombat));

        Check movementCheck = mock(Check.class);
        when(movementCheck.getId()).thenReturn("fly");
        when(movementCheck.getCategory()).thenReturn(CheckCategory.MOVEMENT);
        when(movementCheck.getBypassPermission()).thenReturn("neonac.bypass.movement.fly");
        assertFalse(manager.isExempt(mockPlayer, movementCheck));
    }

    @Test
    void removeAll_clearsEverything() {
        manager.addGlobalExemption(testUuid, ExemptionType.PLUGIN);
        manager.addCheckExemption(testUuid, "testcheck", ExemptionType.PLUGIN);
        manager.addCategoryExemption(testUuid, CheckCategory.COMBAT, ExemptionType.PLUGIN);
        manager.removeAll(testUuid);
        assertFalse(manager.isExempt(mockPlayer, mockCheck));
    }

    @Test
    void cleanExpired_removesExpiredTimedExemptions() {
        manager.addTimedGlobal(testUuid, ExemptionType.PLUGIN, -1000);
        manager.cleanExpired();
        assertFalse(manager.isExempt(mockPlayer, mockCheck));
    }

    @Test
    void cleanExpired_keepsValidTimedExemptions() {
        manager.addTimedGlobal(testUuid, ExemptionType.PLUGIN, 60000);
        assertTrue(manager.isExempt(mockPlayer, mockCheck));
        manager.cleanExpired();
        assertTrue(manager.isExempt(mockPlayer, mockCheck));
    }

    @Test
    void permissionBypass_checked() {
        org.bukkit.entity.Player mockBukkit = mock(org.bukkit.entity.Player.class);
        when(mockPlayer.getPlatformPlayer()).thenReturn(mockBukkit);
        when(mockBukkit.hasPermission("neonac.bypass")).thenReturn(true);
        assertTrue(manager.isExempt(mockPlayer, mockCheck));
    }

    @Test
    void noExemption_returnsFalse() {
        assertFalse(manager.isExempt(mockPlayer, mockCheck));
    }
}
