package com.earac.api.storage;

import com.earac.api.violation.Violation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Storage backend contract. The core never depends on a concrete database.
 */
public interface Storage {

    void init() throws StorageException;

    void shutdown();

    void saveViolation(Violation violation);

    /**
     * @return the current VL for a player/check, or 0 if none.
     */
    double getViolationLevel(UUID playerUuid, String checkId);

    Map<String, Double> getAllViolationLevels(UUID playerUuid);

    void setViolationLevel(UUID playerUuid, String checkId, double vl);

    void resetViolationLevels(UUID playerUuid);

    List<Violation> getRecentViolations(UUID playerUuid, int limit);

    /**
     * @return an opaque handle for backends that expose a raw connection (null otherwise).
     */
    Object getRawHandle();
}
