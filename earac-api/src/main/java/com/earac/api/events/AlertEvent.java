package com.earac.api.events;

import com.earac.api.violation.Violation;

/**
 * Fired when a violation crosses the alert threshold. Can be cancelled to suppress
 * the staff alert (not the violation itself).
 */
public interface AlertEvent extends EarACEvent {

    Violation getViolation();

    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
