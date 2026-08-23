package com.neonac.api.storage;

import com.neonac.api.violation.Violation;

import java.util.List;
import java.util.Map;
import java.util.UUID;
public interface Storage {

    void init() throws StorageException;

    void shutdown();

    void saveViolation(Violation violation);
    double getViolationLevel(UUID playerUuid, String checkId);

    Map<String, Double> getAllViolationLevels(UUID playerUuid);

    void setViolationLevel(UUID playerUuid, String checkId, double vl);

    void resetViolationLevels(UUID playerUuid);

    List<Violation> getRecentViolations(UUID playerUuid, int limit);
    Object getRawHandle();
}
