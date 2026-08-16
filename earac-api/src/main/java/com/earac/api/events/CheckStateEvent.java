package com.earac.api.events;

import com.earac.api.check.Check;

/**
 * Fired when a check is enabled or disabled at runtime.
 */
public interface CheckStateEvent extends EarACEvent {

    Check getCheck();

    boolean isEnabled();
}
