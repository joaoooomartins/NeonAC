package com.neonac.core.api.events;

import com.neonac.api.events.AlertEvent;
import com.neonac.api.violation.Violation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
public final class NeonACAlertEvent extends Event implements AlertEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Violation violation;
    private boolean cancelled;

    public NeonACAlertEvent(Violation violation) {
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
