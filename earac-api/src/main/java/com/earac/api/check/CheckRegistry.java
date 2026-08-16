package com.earac.api.check;

/**
 * Used by the core to accept externally registered checks (3rd-party plugins or modules).
 */
public interface CheckRegistry {

    void register(Check check);
}
