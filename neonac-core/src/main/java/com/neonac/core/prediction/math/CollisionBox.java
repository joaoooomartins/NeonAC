package com.neonac.core.prediction.math;

public final class CollisionBox {

    private static final double EPSILON = 1.0E-7;

    public double minX, minY, minZ, maxX, maxY, maxZ;

    public CollisionBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public CollisionBox offset(double x, double y, double z) {
        minX += x; minY += y; minZ += z;
        maxX += x; maxY += y; maxZ += z;
        return this;
    }

    public CollisionBox expand(double x, double y, double z) {
        minX -= x; minY -= y; minZ -= z;
        maxX += x; maxY += y; maxZ += z;
        return this;
    }

    public CollisionBox copy() {
        return new CollisionBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean isCollided(CollisionBox other) {
        return other.maxX >= this.minX && other.minX <= this.maxX
                && other.maxY >= this.minY && other.minY <= this.maxY
                && other.maxZ >= this.minZ && other.minZ <= this.maxZ;
    }

    public double collideX(CollisionBox other, double offsetX) {
        if (offsetX != 0
                && (other.minY - maxY) < -EPSILON
                && (other.maxY - minY) > EPSILON
                && (other.minZ - maxZ) < -EPSILON
                && (other.maxZ - minZ) > EPSILON) {
            if (offsetX >= 0.0) {
                double maxMove = minX - other.maxX;
                return maxMove < -EPSILON ? offsetX : Math.min(maxMove, offsetX);
            } else {
                double maxMove = maxX - other.minX;
                return maxMove > EPSILON ? offsetX : Math.max(maxMove, offsetX);
            }
        }
        return offsetX;
    }

    public double collideY(CollisionBox other, double offsetY) {
        if (offsetY != 0
                && (other.minX - maxX) < -EPSILON
                && (other.maxX - minX) > EPSILON
                && (other.minZ - maxZ) < -EPSILON
                && (other.maxZ - minZ) > EPSILON) {
            if (offsetY >= 0.0) {
                double maxMove = minY - other.maxY;
                return maxMove < -EPSILON ? offsetY : Math.min(maxMove, offsetY);
            } else {
                double maxMove = maxY - other.minY;
                return maxMove > EPSILON ? offsetY : Math.max(maxMove, offsetY);
            }
        }
        return offsetY;
    }

    public double collideZ(CollisionBox other, double offsetZ) {
        if (offsetZ != 0
                && (other.minX - maxX) < -EPSILON
                && (other.maxX - minX) > EPSILON
                && (other.minY - maxY) < -EPSILON
                && (other.maxY - minY) > EPSILON) {
            if (offsetZ >= 0.0) {
                double maxMove = minZ - other.maxZ;
                return maxMove < -EPSILON ? offsetZ : Math.min(maxMove, offsetZ);
            } else {
                double maxMove = maxZ - other.minZ;
                return maxMove > EPSILON ? offsetZ : Math.max(maxMove, offsetZ);
            }
        }
        return offsetZ;
    }

    public double distanceSquared(double x, double y, double z) {
        double dx = Math.max(0, Math.max(minX - x, x - maxX));
        double dy = Math.max(0, Math.max(minY - y, y - maxY));
        double dz = Math.max(0, Math.max(minZ - z, z - maxZ));
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
