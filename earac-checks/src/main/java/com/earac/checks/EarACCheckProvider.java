package com.earac.checks;

import com.earac.api.check.CheckProvider;
import com.earac.api.check.CheckRegistry;
import com.earac.checks.combat.Aim;
import com.earac.checks.combat.AutoClicker;
import com.earac.checks.combat.KillAura;
import com.earac.checks.combat.Reach;
import com.earac.checks.combat.Velocity;
import com.earac.checks.movement.Fly;
import com.earac.checks.movement.Jesus;
import com.earac.checks.movement.NoFall;
import com.earac.checks.movement.Speed;
import com.earac.checks.movement.Spider;
import com.earac.checks.movement.Step;
import com.earac.checks.player.FastBreak;
import com.earac.checks.player.FastPlace;
import com.earac.checks.player.Scaffold;

/**
 * Registers all built-in checks. Loaded by the core via {@link java.util.ServiceLoader}.
 * Adding a new check = construct it here; no core changes required.
 */
public final class EarACCheckProvider implements CheckProvider {

    @Override
    public void registerChecks(CheckRegistry registry) {
        // Combat
        registry.register(new KillAura((com.earac.core.check.CheckEngine) registry));
        registry.register(new Reach((com.earac.core.check.CheckEngine) registry));
        registry.register(new Aim((com.earac.core.check.CheckEngine) registry));
        registry.register(new AutoClicker((com.earac.core.check.CheckEngine) registry));
        registry.register(new Velocity((com.earac.core.check.CheckEngine) registry));
        // Movement
        registry.register(new Fly((com.earac.core.check.CheckEngine) registry));
        registry.register(new Speed((com.earac.core.check.CheckEngine) registry));
        registry.register(new com.earac.checks.movement.NoFall((com.earac.core.check.CheckEngine) registry));
        registry.register(new Step((com.earac.core.check.CheckEngine) registry));
        registry.register(new Jesus((com.earac.core.check.CheckEngine) registry));
        registry.register(new Spider((com.earac.core.check.CheckEngine) registry));
        // Player
        registry.register(new FastPlace((com.earac.core.check.CheckEngine) registry));
        registry.register(new FastBreak((com.earac.core.check.CheckEngine) registry));
        registry.register(new Scaffold((com.earac.core.check.CheckEngine) registry));
    }
}
