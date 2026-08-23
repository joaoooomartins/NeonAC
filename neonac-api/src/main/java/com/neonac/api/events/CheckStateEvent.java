package com.neonac.api.events;

import com.neonac.api.check.Check;
public interface CheckStateEvent extends NeonACEvent {

    Check getCheck();

    boolean isEnabled();
}
