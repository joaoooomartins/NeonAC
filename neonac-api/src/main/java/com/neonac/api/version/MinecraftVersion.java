package com.neonac.api.version;
public enum MinecraftVersion {

    UNKNOWN(-1, -1, -1),
    V1_7_10(1, 7, 10),
    V1_8(1, 8, 0),
    V1_9(1, 9, 0),
    V1_12(1, 12, 0),
    V1_13(1, 13, 0),
    V1_14(1, 14, 0),
    V1_15(1, 15, 0),
    V1_16(1, 16, 0),
    V1_17(1, 17, 0),
    V1_18(1, 18, 0),
    V1_19(1, 19, 0),
    V1_20(1, 20, 0),
    V1_21(1, 21, 0),
    V1_22(1, 22, 0),
    V1_23(1, 23, 0),
    V1_24(1, 24, 0),
    V1_25(1, 25, 0),
    V1_26(1, 26, 0);

    private final int major;
    private final int minor;
    private final int patch;

    MinecraftVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }
    public int getVersionKey() {
        return minor;
    }

    public boolean isAtLeast(MinecraftVersion other) {
        if (this.major != other.major) return this.major > other.major;
        if (this.minor != other.minor) return this.minor > other.minor;
        return this.patch >= other.patch;
    }

    public boolean isBefore(MinecraftVersion other) {
        return !isAtLeast(other);
    }

    public static MinecraftVersion fromProtocol(int protocol) {
        return ProtocolVersion.fromId(protocol).getMinecraftVersion();
    }
}
