package com.earac.api.violation;

import com.earac.api.check.Check;

import java.util.Map;

/**
 * A single detection produced by a check. Immutable snapshot used for alerts,
 * logs and API consumers.
 */
public interface Violation {

    String getPlayerUuid();

    String getPlayerName();

    Check getCheck();

    /**
     * @return the VL delta applied by this detection (already multiplied by confidence).
     */
    double getVlAdded();

    /**
     * @return the player's total VL for this check at the moment of detection.
     */
    double getViolationLevel();

    /**
     * Confidence in [0,1]. Lower confidence => smaller VL impact.
     */
    double getConfidence();

    long getTimestamp();

    /**
     * Free-form diagnostic context (position, ping, predicted vs observed, ...).
     */
    Map<String, Object> getContext();
}
