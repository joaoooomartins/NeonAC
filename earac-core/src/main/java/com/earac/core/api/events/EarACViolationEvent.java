package com.earac.core.api.events;

import com.earac.api.events.ViolationEvent;
import com.earac.api.violation.Violation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit implementation of {@link ViolationEvent}.
 */
public final class EarACViolationEvent extends Event implements ViolationEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Violation violation;

    public EarACViolationEvent(Violation violation) {
        this.violation = violation;
    }

    @Override
    public Violation getViolation() {
        return violation;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
