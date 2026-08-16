package com.earac.core.exemption;

import com.earac.api.check.Check;
import com.earac.api.exemption.ExemptionType;
import com.earac.api.player.EarACPlayer;
import com.earac.core.player.PlayerData;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks exemptions that suppress checks for a player. Exemptions come from:
 * <ul>
 *   <li>permission bypass (earac.bypass.*)</li>
 *   <li>contextual reasons (teleport, velocity, liquid, low TPS...)</li>
 *   <li>3rd-party plugins via the API</li>
 * </ul>
 * A check is exempt when ANY of its relevant reasons is active, or the player holds
 * the bypass permission.
 */
public final class ExemptionManager {

    private final Map<UUID, Set<ExemptionType>> playerExemptions = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> timedExemptions = new ConcurrentHashMap<>();

    public void addExemption(UUID uuid, ExemptionType type) {
        playerExemptions.computeIfAbsent(uuid, k -> EnumSet.noneOf(ExemptionType.class)).add(type);
    }

    public void removeExemption(UUID uuid, ExemptionType type) {
        Set<ExemptionType> set = playerExemptions.get(uuid);
        if (set != null) set.remove(type);
    }

    public void addTimedExemption(UUID uuid, ExemptionType type, long durationMillis) {
        timedExemptions.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(type.name(), System.currentTimeMillis() + durationMillis);
    }

    public boolean isExempt(EarACPlayer player, Check check) {
        UUID uuid = player.getUniqueId();

        // Permission bypass (broad + specific).
        org.bukkit.entity.Player bp = (org.bukkit.entity.Player) player.getPlatformPlayer();
        if (bp != null) {
            if (bp.hasPermission("earac.bypass") ||
                bp.hasPermission("earac.bypass." + check.getCategory().getLowerCaseName()) ||
                bp.hasPermission(check.getBypassPermission())) {
                return true;
            }
        }

        Set<ExemptionType> active = playerExemptions.get(uuid);
        if (active != null && !active.isEmpty()) {
            return true;
        }

        // Timed exemptions.
        Map<String, Long> timed = timedExemptions.get(uuid);
        if (timed != null && !timed.isEmpty()) {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> e : timed.entrySet()) {
                if (e.getValue() > now) return true;
            }
        }

        // Contextual exemptions derived from player state.
        return isContextuallyExempt(player);
    }

    private boolean isContextuallyExempt(EarACPlayer player) {
        if (player.isCreative() || player.isSpectator()) return true;
        if (player.isDead()) return true;
        if (player.getLastTeleportAge() < 1_000_000_000L) return true; // 1s grace after teleport
        if (player.getLastVelocityAge() < 1_200_000_000L) return true; // 1.2s after knockback
        if (player.isInWeb()) return true;
        if (player.isOnLadder()) return true;
        if (player.isInWater()) return true;
        if (player.isInLava()) return true;
        if (player.isGliding()) return true;
        if (player.isOnVehicle()) return true;
        if (com.earac.core.player.TpsTracker.getTPS() < 15.0) return true; // untrustworthy environment
        if (player.getPing() > 400) return true; // timing unreliable
        return false;
    }
}
