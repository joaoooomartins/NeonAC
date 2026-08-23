package com.neonac.api.events;

import com.neonac.api.violation.Violation;
public interface ViolationEvent extends NeonACEvent {

    Violation getViolation();
}
