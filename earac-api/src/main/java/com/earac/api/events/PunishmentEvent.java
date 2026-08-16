package com.earac.api.events;

import com.earac.api.violation.Violation;

/**
 * Fired before a punishment command is dispatched. Can be cancelled by other
 * plugins (e.g. to escalate or to ignore).
 */
public interface PunishmentEvent extends EarACEvent {

    Violation getViolation();

    String getCommand();

    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
