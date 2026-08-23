package com.neonac.core.api;

import com.neonac.api.violation.Violation;
import com.neonac.api.check.Check;
import com.neonac.core.api.events.NeonACAlertEvent;
import com.neonac.core.api.events.NeonACCheckStateEvent;
import com.neonac.core.api.events.NeonACPunishmentEvent;
import com.neonac.core.api.events.NeonACViolationEvent;
import org.bukkit.Bukkit;
public final class ApiEventDispatcher {

    private ApiEventDispatcher() {
    }

    public static void fireViolation(Violation v) {
        Bukkit.getPluginManager().callEvent(new NeonACViolationEvent(v));
    }

    public static boolean fireAlert(Violation v) {
        NeonACAlertEvent e = new NeonACAlertEvent(v);
        Bukkit.getPluginManager().callEvent(e);
        return !e.isCancelled();
    }

    public static boolean firePunishment(Violation v, String command) {
        NeonACPunishmentEvent e = new NeonACPunishmentEvent(v, command);
        Bukkit.getPluginManager().callEvent(e);
        return !e.isCancelled();
    }

    public static void fireCheckState(Check check) {
        Bukkit.getPluginManager().callEvent(new NeonACCheckStateEvent(check));
    }
}
