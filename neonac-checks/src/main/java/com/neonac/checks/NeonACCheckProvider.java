package com.neonac.checks;

import com.neonac.api.check.CheckProvider;
import com.neonac.api.check.CheckRegistry;
import com.neonac.checks.combat.Aim;
import com.neonac.checks.combat.AutoClicker;
import com.neonac.checks.combat.KillAura;
import com.neonac.checks.combat.Reach;
import com.neonac.checks.combat.Velocity;
import com.neonac.checks.movement.Fly;
import com.neonac.checks.movement.Jesus;
import com.neonac.checks.movement.NoFall;
import com.neonac.checks.movement.Speed;
import com.neonac.checks.movement.Spider;
import com.neonac.checks.movement.Step;
import com.neonac.checks.player.FastBreak;
import com.neonac.checks.player.FastPlace;
import com.neonac.checks.player.Scaffold;
public final class NeonACCheckProvider implements CheckProvider {

    @Override
    public void registerChecks(CheckRegistry registry) {
        // Combat
        registry.register(new KillAura((com.neonac.core.check.CheckEngine) registry));
        registry.register(new Reach((com.neonac.core.check.CheckEngine) registry));
        registry.register(new Aim((com.neonac.core.check.CheckEngine) registry));
        registry.register(new AutoClicker((com.neonac.core.check.CheckEngine) registry));
        registry.register(new Velocity((com.neonac.core.check.CheckEngine) registry));
        // Movement
        registry.register(new Fly((com.neonac.core.check.CheckEngine) registry));
        registry.register(new Speed((com.neonac.core.check.CheckEngine) registry));
        registry.register(new com.neonac.checks.movement.NoFall((com.neonac.core.check.CheckEngine) registry));
        registry.register(new Step((com.neonac.core.check.CheckEngine) registry));
        registry.register(new Jesus((com.neonac.core.check.CheckEngine) registry));
        registry.register(new Spider((com.neonac.core.check.CheckEngine) registry));
        // Player
        registry.register(new FastPlace((com.neonac.core.check.CheckEngine) registry));
        registry.register(new FastBreak((com.neonac.core.check.CheckEngine) registry));
        registry.register(new Scaffold((com.neonac.core.check.CheckEngine) registry));
    }
}
