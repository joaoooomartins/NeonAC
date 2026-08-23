package com.neonac.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathUtilsTest {

    @Test
    void distance3D_zeroForSamePoint() {
        assertEquals(0.0, MathUtils.distance3D(0, 0, 0, 0, 0, 0), 1e-9);
    }

    @Test
    void distance3D_knownValue() {
        assertEquals(1.0, MathUtils.distance3D(0, 0, 0, 1, 0, 0), 1e-9);
    }

    @Test
    void angleDelta_wrapsAround() {
        assertEquals(10.0, MathUtils.angleDelta(355f, 5f), 1e-6);
    }

    @Test
    void clampBounds() {
        assertEquals(0.0, MathUtils.clamp(-1, 0, 1), 1e-9);
        assertEquals(1.0, MathUtils.clamp(5, 0, 1), 1e-9);
    }

    @Test
    void stdDev_nonZeroForSpread() {
        double[] s = {1.0, 2.0, 3.0};
        assertTrue(MathUtils.stdDev(s) > 0.0);
    }
}
