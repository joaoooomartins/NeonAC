package com.neonac.core.prediction.math;

public final class Vector3d {

    public double x, y, z;

    public Vector3d() { this(0, 0, 0); }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d add(double x, double y, double z) {
        this.x += x; this.y += y; this.z += z;
        return this;
    }

    public Vector3d add(Vector3d v) {
        this.x += v.x; this.y += v.y; this.z += v.z;
        return this;
    }

    public Vector3d subtract(Vector3d v) {
        this.x -= v.x; this.y -= v.y; this.z -= v.z;
        return this;
    }

    public Vector3d multiply(double m) {
        this.x *= m; this.y *= m; this.z *= m;
        return this;
    }

    public Vector3d multiply(double mx, double my, double mz) {
        this.x *= mx; this.y *= my; this.z *= mz;
        return this;
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double distanceSquared(Vector3d o) {
        double dx = x - o.x, dy = y - o.y, dz = z - o.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(Vector3d o) {
        return Math.sqrt(distanceSquared(o));
    }

    public Vector3d clone() {
        return new Vector3d(x, y, z);
    }

    public Vector3d zero() {
        x = 0; y = 0; z = 0;
        return this;
    }

    public Vector3d normalize() {
        double len = length();
        if (len > 0) {
            x /= len; y /= len; z /= len;
        }
        return this;
    }

    public static Vector3d clamp(Vector3d v, double min, double max) {
        return new Vector3d(
                Math.max(min, Math.min(max, v.x)),
                Math.max(min, Math.min(max, v.y)),
                Math.max(min, Math.min(max, v.z)));
    }
}
