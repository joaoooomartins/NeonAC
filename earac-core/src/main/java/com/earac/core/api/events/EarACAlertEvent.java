package com.earac.core.api.events;

import com.earac.api.events.AlertEvent;
import com.earac.api.violation.Violation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit implementation of {@link AlertEvent}. Cancellable.
 */
public final class EarACAlertEvent extends Event implements AlertEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Violation violation;
    private boolean cancelled;

    public EarACAlertEvent(Violation violation) {
        this.violation = violation;
    }

    @Override
    public Violation getViolation() {
        return violation;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
