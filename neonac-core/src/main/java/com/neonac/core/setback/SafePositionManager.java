package com.neonac.core.setback;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SafePositionManager {

    private static final int MAX_HISTORY = 20;
    private static final double MIN_DISTANCE_SQ = 0.25;

    private final Map<UUID, Location> lastSafe = new ConcurrentHashMap<>();
    private final Map<UUID, Location[]> history = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> index = new ConcurrentHashMap<>();

    public void update(Player player) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();

        if (!isSafePosition(player, loc)) return;

        Location prev = lastSafe.get(uuid);
        if (prev != null && prev.getWorld().equals(loc.getWorld())
                && prev.distanceSquared(loc) < MIN_DISTANCE_SQ) {
            return;
        }

        lastSafe.put(uuid, loc.clone());

        Location[] hist = history.computeIfAbsent(uuid, k -> new Location[MAX_HISTORY]);
        int idx = index.getOrDefault(uuid, 0);
        hist[idx % MAX_HISTORY] = loc.clone();
        index.put(uuid, idx + 1);
    }

    public Location getLastSafe(UUID uuid) {
        return lastSafe.get(uuid);
    }

    public Location getFurthestSafe(UUID uuid, Location current) {
        Location[] hist = history.get(uuid);
        if (hist == null) return lastSafe.get(uuid);

        Location best = lastSafe.get(uuid);
        double bestDist = 0;

        for (Location loc : hist) {
            if (loc == null) continue;
            if (!loc.getWorld().equals(current.getWorld())) continue;
            double dist = loc.distanceSquared(current);
            if (dist > bestDist) {
                bestDist = dist;
                best = loc;
            }
        }
        return best;
    }

    private boolean isSafePosition(Player player, Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        int bx = loc.getBlockX();
        int by = loc.getBlockY();
        int bz = loc.getBlockZ();

        Block feet = world.getBlockAt(bx, by, bz);
        Block head = world.getBlockAt(bx, by + 1, bz);
        Block below = world.getBlockAt(bx, by - 1, bz);

        if (feet.isLiquid() || head.isLiquid()) return false;
        if (feet.getType().name().contains("WEB")) return false;
        if (below.getType().name().contains("FENCE") || below.getType().name().contains("WALL"))
            return false;

        return true;
    }

    public void remove(UUID uuid) {
        lastSafe.remove(uuid);
        history.remove(uuid);
        index.remove(uuid);
    }
}
