package com.neonac.core.player;

import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.version.ProtocolVersion;
import com.neonac.core.version.VersionDetector;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public final class PlayerManager {

    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public void add(Player player) {
        UUID uuid = player.getUniqueId();
        if (players.containsKey(uuid)) return;
        players.put(uuid, new PlayerData(player,
                VersionDetector.getClientVersion(player),
                ProtocolVersion.fromId(protocolOf(player))));
    }

    private int protocolOf(Player player) {
        try {
            Method m = Player.class.getMethod("getProtocolVersion");
            return (int) m.invoke(player);
        } catch (Exception e) {
            return ProtocolVersion.M1_8.getId();
        }
    }

    public void remove(UUID uuid) {
        players.remove(uuid);
    }

    public PlayerData get(UUID uuid) {
        return players.get(uuid);
    }

    public PlayerData get(String name) {
        for (PlayerData d : players.values()) {
            if (d.getName().equalsIgnoreCase(name)) return d;
        }
        return null;
    }

    public Collection<PlayerData> getAll() {
        return players.values();
    }

    public void markTeleport(UUID uuid) {
        PlayerData d = players.get(uuid);
        if (d != null) d.markTeleport();
    }

    public void markVelocity(UUID uuid) {
        PlayerData d = players.get(uuid);
        if (d != null) d.markVelocity();
    }
}
