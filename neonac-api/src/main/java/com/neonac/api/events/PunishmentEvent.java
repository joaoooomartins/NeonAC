package com.neonac.api.events;

import com.neonac.api.violation.Violation;
public interface PunishmentEvent extends NeonACEvent {

    Violation getViolation();

    String getCommand();

    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
