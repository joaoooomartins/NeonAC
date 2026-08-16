package com.earac.core.api.events;

import com.earac.api.events.PunishmentEvent;
import com.earac.api.violation.Violation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit implementation of {@link PunishmentEvent}. Cancellable.
 */
public final class EarACPunishmentEvent extends Event implements PunishmentEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Violation violation;
    private final String command;
    private boolean cancelled;

    public EarACPunishmentEvent(Violation violation, String command) {
        this.violation = violation;
        this.command = command;
    }

    @Override
    public Violation getViolation() {
        return violation;
    }

    @Override
    public String getCommand() {
        return command;
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
