package com.neonac.core.api.events;

import com.neonac.api.events.ViolationEvent;
import com.neonac.api.violation.Violation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
public final class NeonACViolationEvent extends Event implements ViolationEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Violation violation;

    public NeonACViolationEvent(Violation violation) {
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
