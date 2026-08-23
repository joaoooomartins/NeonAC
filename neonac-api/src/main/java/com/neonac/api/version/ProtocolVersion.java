package com.neonac.api.version;

import java.util.Arrays;
public enum ProtocolVersion {

    UNKNOWN(-1, MinecraftVersion.UNKNOWN),
    M1_7_10(5, MinecraftVersion.V1_7_10),
    M1_8(47, MinecraftVersion.V1_8),
    M1_9(107, MinecraftVersion.V1_9),
    M1_12(335, MinecraftVersion.V1_12),
    M1_13(393, MinecraftVersion.V1_13),
    M1_14(477, MinecraftVersion.V1_14),
    M1_15(573, MinecraftVersion.V1_15),
    M1_16(735, MinecraftVersion.V1_16),
    M1_17(755, MinecraftVersion.V1_17),
    M1_18(757, MinecraftVersion.V1_18),
    M1_19(759, MinecraftVersion.V1_19),
    M1_20(763, MinecraftVersion.V1_20),
    M1_21(767, MinecraftVersion.V1_21),
    M1_22(770, MinecraftVersion.V1_22),
    M1_23(780, MinecraftVersion.V1_23),
    M1_24(790, MinecraftVersion.V1_24),
    M1_25(800, MinecraftVersion.V1_25),
    M1_26(810, MinecraftVersion.V1_26);

    private final int id;
    private final MinecraftVersion minecraftVersion;

    ProtocolVersion(int id, MinecraftVersion minecraftVersion) {
        this.id = id;
        this.minecraftVersion = minecraftVersion;
    }

    public int getId() {
        return id;
    }

    public MinecraftVersion getMinecraftVersion() {
        return minecraftVersion;
    }

    public static ProtocolVersion fromId(int id) {
        return Arrays.stream(values())
                .filter(p -> p.id == id)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
