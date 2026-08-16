package com.earac.core.api;

import com.earac.api.violation.Violation;
import com.earac.api.check.Check;
import com.earac.core.api.events.EarACAlertEvent;
import com.earac.core.api.events.EarACCheckStateEvent;
import com.earac.core.api.events.EarACPunishmentEvent;
import com.earac.core.api.events.EarACViolationEvent;
import org.bukkit.Bukkit;

/**
 * Fires the public Bukkit events. Centralised so the rest of the code never
 * references concrete event classes.
 */
public final class ApiEventDispatcher {

    private ApiEventDispatcher() {
    }

    public static void fireViolation(Violation v) {
        Bukkit.getPluginManager().callEvent(new EarACViolationEvent(v));
    }

    public static boolean fireAlert(Violation v) {
        EarACAlertEvent e = new EarACAlertEvent(v);
        Bukkit.getPluginManager().callEvent(e);
        return !e.isCancelled();
    }

    public static boolean firePunishment(Violation v, String command) {
        EarACPunishmentEvent e = new EarACPunishmentEvent(v, command);
        Bukkit.getPluginManager().callEvent(e);
        return !e.isCancelled();
    }

    public static void fireCheckState(Check check) {
        Bukkit.getPluginManager().callEvent(new EarACCheckStateEvent(check));
    }
}
