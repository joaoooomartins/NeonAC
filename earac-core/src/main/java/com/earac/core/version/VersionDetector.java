package com.earac.core.version;

import com.earac.api.version.MinecraftVersion;
import com.earac.api.version.ProtocolVersion;
import com.earac.api.version.ServerImplementation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Detects the running server implementation and resolves Minecraft/Protocol versions.
 * Client version detection prefers Paper's {@code Player#getProtocolVersion()} when
 * available and otherwise falls back to the server version (acceptable for single-version
 * networks). A more precise per-client resolution can be plugged in via
 * {@link #setClientVersionResolver}.
 */
public final class VersionDetector {

    private static ServerImplementation serverImplementation = ServerImplementation.UNKNOWN;
    private static ClientVersionResolver clientVersionResolver = null;

    private VersionDetector() {
    }

    public static void detect() {
        String serverName = Bukkit.getName();
        if (serverName != null) {
            String lower = serverName.toLowerCase();
            if (lower.contains("purpur")) serverImplementation = ServerImplementation.PURPUR;
            else if (lower.contains("paper")) serverImplementation = ServerImplementation.PAPER;
            else if (lower.contains("spigot")) serverImplementation = ServerImplementation.SPIGOT;
            else if (lower.contains("forge")) serverImplementation = ServerImplementation.FORGE;
            else serverImplementation = ServerImplementation.BUKKIT;
        }
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            serverImplementation = ServerImplementation.FOLIA;
        } catch (ClassNotFoundException ignored) {
            // not folia
        }

        // Paper exposes Player#getProtocolVersion() — wire a resolver if present.
        try {
            Method m = Player.class.getMethod("getProtocolVersion");
            clientVersionResolver = player -> {
                try {
                    int proto = (int) m.invoke(player);
                    return ProtocolVersion.fromId(proto).getMinecraftVersion();
                } catch (Exception e) {
                    return null;
                }
            };
        } catch (NoSuchMethodException ignored) {
            clientVersionResolver = null;
        }
    }

    public static ServerImplementation getServerImplementation() {
        return serverImplementation;
    }

    public static void setClientVersionResolver(ClientVersionResolver resolver) {
        clientVersionResolver = resolver;
    }

    public static MinecraftVersion getClientVersion(Player player) {
        if (clientVersionResolver != null) {
            MinecraftVersion v = clientVersionResolver.resolve(player);
            if (v != null && v != MinecraftVersion.UNKNOWN) return v;
        }
        return getServerVersion();
    }

    public static MinecraftVersion getServerVersion() {
        String ver = Bukkit.getBukkitVersion();
        for (MinecraftVersion v : MinecraftVersion.values()) {
            if (v == MinecraftVersion.UNKNOWN) continue;
            if (ver.startsWith(v.getMajor() + "." + v.getMinor())) {
                return v;
            }
        }
        return MinecraftVersion.UNKNOWN;
    }

    @FunctionalInterface
    public interface ClientVersionResolver {
        MinecraftVersion resolve(Player player);
    }
}
