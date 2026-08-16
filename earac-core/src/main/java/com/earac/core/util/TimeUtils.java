package com.earac.core.util;

/**
 * Time helpers. We avoid {@code System.currentTimeMillis()} in hot paths and prefer
 * a monotonic nanosecond source for age-based exemptions.
 */
public final class TimeUtils {

    private TimeUtils() {
    }

    public static long nanos() {
        return System.nanoTime();
    }

    public static long millis() {
        return System.currentTimeMillis();
    }

    public static long nanosToMillis(long nanos) {
        return nanos / 1_000_000L;
    }
}
