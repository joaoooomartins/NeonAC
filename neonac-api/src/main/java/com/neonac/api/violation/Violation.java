package com.neonac.api.violation;

import com.neonac.api.check.Check;

import java.util.Map;
public interface Violation {

    String getPlayerUuid();

    String getPlayerName();

    Check getCheck();
    double getVlAdded();
    double getViolationLevel();
    double getConfidence();

    long getTimestamp();
    Map<String, Object> getContext();
}
