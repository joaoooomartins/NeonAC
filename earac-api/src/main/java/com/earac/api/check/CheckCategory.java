package com.earac.api.check;

/**
 * High level check categories. Used for permission scoping, alert routing and config grouping.
 */
public enum CheckCategory {
    COMBAT,
    MOVEMENT,
    PLAYER,
    WORLD,
    PACKET;

    public String getLowerCaseName() {
        return name().toLowerCase();
    }
}
