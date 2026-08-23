package com.neonac.core.util;
public final class MathUtils {

    private MathUtils() {
    }

    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double distanceHorizontal(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static float angleDelta(float a, float b) {
        float delta = Math.abs(a - b) % 360f;
        if (delta > 180f) delta = 360f - delta;
        return delta;
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static boolean equal(double a, double b, double epsilon) {
        return Math.abs(a - b) <= epsilon;
    }
    public static double stdDev(double[] samples) {
        if (samples == null || samples.length < 2) return 0.0;
        double mean = 0.0;
        for (double s : samples) mean += s;
        mean /= samples.length;
        double variance = 0.0;
        for (double s : samples) {
            double d = s - mean;
            variance += d * d;
        }
        return Math.sqrt(variance / samples.length);
    }

    public static double mean(double[] samples) {
        if (samples == null || samples.length == 0) return 0.0;
        double sum = 0.0;
        for (double s : samples) sum += s;
        return sum / samples.length;
    }
}
