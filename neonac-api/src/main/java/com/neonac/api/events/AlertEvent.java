package com.neonac.api.events;

import com.neonac.api.violation.Violation;
public interface AlertEvent extends NeonACEvent {

    Violation getViolation();

    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
