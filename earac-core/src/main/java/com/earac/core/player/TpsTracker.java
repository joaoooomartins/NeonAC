package com.earac.core.player;

/**
 * Rolling TPS estimate updated by the main plugin each tick. Exposed to player
 * data and checks for low-TPS exemptions.
 */
public final class TpsTracker {

    private static double tps = 20.0;

    private TpsTracker() {
    }

    public static void update(double currentTps) {
        tps = currentTps;
    }

    public static double getTPS() {
        return tps;
    }

    public static boolean isLow() {
        return tps < 18.0;
    }
}
