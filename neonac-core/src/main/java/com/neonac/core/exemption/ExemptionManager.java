package com.neonac.core.exemption;

import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.exemption.ExemptionType;
import com.neonac.api.player.NeonACPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ExemptionManager {

    private final Map<UUID, Map<String, Boolean>> globalExemptions = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> timedGlobal = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Map<String, Boolean>>> perCheck = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Map<String, Long>>> timedPerCheck = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Map<String, Boolean>>> perCategory = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Map<String, Long>>> timedPerCategory = new ConcurrentHashMap<>();

    public void addGlobalExemption(UUID uuid, ExemptionType type) {
        globalExemptions.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(type.name(), true);
    }

    public void removeGlobalExemption(UUID uuid, ExemptionType type) {
        Map<String, Boolean> map = globalExemptions.get(uuid);
        if (map != null) map.remove(type.name());
    }

    public void addTimedGlobal(UUID uuid, ExemptionType type, long durationMillis) {
        timedGlobal.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(type.name(), System.currentTimeMillis() + durationMillis);
    }

    public void addCheckExemption(UUID uuid, String checkId, ExemptionType type) {
        perCheck.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(checkId, k -> new ConcurrentHashMap<>())
                .put(type.name(), true);
    }

    public void removeCheckExemption(UUID uuid, String checkId, ExemptionType type) {
        Map<String, Map<String, Boolean>> byCheck = perCheck.get(uuid);
        if (byCheck != null) {
            Map<String, Boolean> byType = byCheck.get(checkId);
            if (byType != null) byType.remove(type.name());
        }
    }

    public void addTimedCheck(UUID uuid, String checkId, ExemptionType type, long durationMillis) {
        timedPerCheck.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(checkId, k -> new ConcurrentHashMap<>())
                .put(type.name(), System.currentTimeMillis() + durationMillis);
    }

    public void addCategoryExemption(UUID uuid, CheckCategory category, ExemptionType type) {
        perCategory.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(category.name(), k -> new ConcurrentHashMap<>())
                .put(type.name(), true);
    }

    public void removeCategoryExemption(UUID uuid, CheckCategory category, ExemptionType type) {
        Map<String, Map<String, Boolean>> byCat = perCategory.get(uuid);
        if (byCat != null) {
            Map<String, Boolean> byType = byCat.get(category.name());
            if (byType != null) byType.remove(type.name());
        }
    }

    public void addTimedCategory(UUID uuid, CheckCategory category, ExemptionType type, long durationMillis) {
        timedPerCategory.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(category.name(), k -> new ConcurrentHashMap<>())
                .put(type.name(), System.currentTimeMillis() + durationMillis);
    }

    public boolean isExempt(NeonACPlayer player, Check check) {
        UUID uuid = player.getUniqueId();
        String checkId = check.getId();
        CheckCategory category = check.getCategory();

        org.bukkit.entity.Player bp = (org.bukkit.entity.Player) player.getPlatformPlayer();
        if (bp != null) {
            if (bp.hasPermission("neonac.bypass") ||
                bp.hasPermission("neonac.bypass." + category.getLowerCaseName()) ||
                bp.hasPermission(check.getBypassPermission())) {
                return true;
            }
        }

        if (hasActive(uuid, globalExemptions.get(uuid))) return true;
        if (hasActiveTimed(uuid, timedGlobal.get(uuid))) return true;

        Map<String, Map<String, Boolean>> bc = perCheck.get(uuid);
        if (bc != null && hasActive(uuid, bc.get(checkId))) return true;

        Map<String, Map<String, Long>> tc = timedPerCheck.get(uuid);
        if (tc != null && hasActiveTimed(uuid, tc.get(checkId))) return true;

        Map<String, Map<String, Boolean>> bcat = perCategory.get(uuid);
        if (bcat != null && hasActive(uuid, bcat.get(category.name()))) return true;

        Map<String, Map<String, Long>> tcat = timedPerCategory.get(uuid);
        if (tcat != null && hasActiveTimed(uuid, tcat.get(category.name()))) return true;

        return isContextuallyExempt(player);
    }

    private boolean hasActive(UUID uuid, Map<String, Boolean> map) {
        return map != null && !map.isEmpty();
    }

    private boolean hasActiveTimed(UUID uuid, Map<String, Long> map) {
        if (map == null || map.isEmpty()) return false;
        long now = System.currentTimeMillis();
        for (Long exp : map.values()) {
            if (exp > now) return true;
        }
        return false;
    }

    public void cleanExpired() {
        long now = System.currentTimeMillis();
        cleanMap(timedGlobal, now);
        cleanMapMap(timedPerCheck, now);
        cleanMapMap(timedPerCategory, now);
    }

    private void cleanMap(Map<UUID, Map<String, Long>> map, long now) {
        map.values().forEach(m -> m.entrySet().removeIf(e -> e.getValue() <= now));
        map.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    private void cleanMapMap(Map<UUID, Map<String, Map<String, Long>>> map, long now) {
        map.values().forEach(inner ->
                inner.values().forEach(m -> m.entrySet().removeIf(e -> e.getValue() <= now)));
        map.values().removeIf(inner -> inner.isEmpty());
        map.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    public void removeAll(UUID uuid) {
        globalExemptions.remove(uuid);
        timedGlobal.remove(uuid);
        perCheck.remove(uuid);
        timedPerCheck.remove(uuid);
        perCategory.remove(uuid);
        timedPerCategory.remove(uuid);
    }

    private boolean isContextuallyExempt(NeonACPlayer player) {
        if (player.isCreative() || player.isSpectator()) return true;
        if (player.isDead()) return true;
        if (player.getLastTeleportAge() < 1_000_000_000L) return true;
        if (player.getLastVelocityAge() < 1_200_000_000L) return true;
        if (player.isInWeb()) return true;
        if (player.isOnLadder()) return true;
        if (player.isInWater()) return true;
        if (player.isInLava()) return true;
        if (player.isGliding()) return true;
        if (player.isOnVehicle()) return true;
        if (com.neonac.core.player.TpsTracker.getTPS() < 15.0) return true;
        if (player.getPing() > 400) return true;
        return false;
    }
}
