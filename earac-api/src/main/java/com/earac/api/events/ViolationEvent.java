package com.earac.api.events;

import com.earac.api.violation.Violation;

/**
 * Fired when a check produces a violation (after VL accumulation).
 * Listeners may inspect but the violation is already applied.
 */
public interface ViolationEvent extends EarACEvent {

    Violation getViolation();
}
