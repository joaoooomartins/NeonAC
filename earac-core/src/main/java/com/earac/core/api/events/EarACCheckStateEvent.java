package com.earac.core.api.events;

import com.earac.api.check.Check;
import com.earac.api.events.CheckStateEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit implementation of {@link CheckStateEvent}.
 */
public final class EarACCheckStateEvent extends Event implements CheckStateEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Check check;

    public EarACCheckStateEvent(Check check) {
        this.check = check;
    }

    @Override
    public Check getCheck() {
        return check;
    }

    @Override
    public boolean isEnabled() {
        return check.isEnabled();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
