package com.neonac.core.api.events;

import com.neonac.api.check.Check;
import com.neonac.api.events.CheckStateEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
public final class NeonACCheckStateEvent extends Event implements CheckStateEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Check check;

    public NeonACCheckStateEvent(Check check) {
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
