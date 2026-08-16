package com.earac.core.violation;

import com.earac.api.check.Check;
import com.earac.api.violation.Violation;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable snapshot of a single detection.
 */
public final class ViolationImpl implements Violation {

    private final String playerUuid;
    private final String playerName;
    private final Check check;
    private final double vlAdded;
    private final double violationLevel;
    private final double confidence;
    private final long timestamp;
    private final Map<String, Object> context;

    public ViolationImpl(String playerUuid, String playerName, Check check,
                         double vlAdded, double violationLevel, double confidence,
                         Map<String, ?> context) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.check = check;
        this.vlAdded = vlAdded;
        this.violationLevel = violationLevel;
        this.confidence = confidence;
        this.timestamp = System.currentTimeMillis();
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
    }

    @Override
    public String getPlayerUuid() {
        return playerUuid;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public Check getCheck() {
        return check;
    }

    @Override
    public double getVlAdded() {
        return vlAdded;
    }

    @Override
    public double getViolationLevel() {
        return violationLevel;
    }

    @Override
    public double getConfidence() {
        return confidence;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Map<String, Object> getContext() {
        return context;
    }
}
