package com.neonac.api.check;
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
