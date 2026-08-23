package com.neonac.checks;

import com.neonac.api.check.CheckProvider;
import com.neonac.api.check.CheckRegistry;
import com.neonac.checks.combat.Aim;
import com.neonac.checks.combat.AutoClicker;
import com.neonac.checks.combat.Criticals;
import com.neonac.checks.combat.FastBow;
import com.neonac.checks.combat.HitBox;
import com.neonac.checks.combat.KillAura;
import com.neonac.checks.combat.Reach;
import com.neonac.checks.combat.Velocity;
import com.neonac.checks.combat.VelocityCheck;
import com.neonac.checks.movement.BoatFly;
import com.neonac.checks.movement.Fly;
import com.neonac.checks.movement.Jesus;
import com.neonac.checks.movement.LongJump;
import com.neonac.checks.movement.NoFall;
import com.neonac.checks.movement.Phase;
import com.neonac.checks.movement.Speed;
import com.neonac.checks.movement.Spider;
import com.neonac.checks.movement.Sprint;
import com.neonac.checks.movement.Step;
import com.neonac.checks.movement.WaterWalk;
import com.neonac.checks.packet.BadPackets;
import com.neonac.checks.packet.Timer;
import com.neonac.checks.player.AutoEat;
import com.neonac.checks.player.ChestStealer;
import com.neonac.checks.player.FastBreak;
import com.neonac.checks.player.FastEat;
import com.neonac.checks.player.FastPlace;
import com.neonac.checks.player.InventoryClick;
import com.neonac.checks.player.Nuker;
import com.neonac.checks.player.Scaffold;
import com.neonac.checks.world.Freecam;
import com.neonac.checks.world.Xray;

public final class NeonACCheckProvider implements CheckProvider {

    @Override
    public void registerChecks(CheckRegistry registry) {
        com.neonac.core.check.CheckEngine engine = (com.neonac.core.check.CheckEngine) registry;

        registry.register(new KillAura(engine));
        registry.register(new Reach(engine));
        registry.register(new Aim(engine));
        registry.register(new AutoClicker(engine));
        registry.register(new Velocity(engine));
        registry.register(new Criticals(engine));
        registry.register(new FastBow(engine));
        registry.register(new HitBox(engine));
        registry.register(new VelocityCheck(engine));

        registry.register(new Fly(engine));
        registry.register(new Speed(engine));
        registry.register(new com.neonac.checks.movement.NoFall(engine));
        registry.register(new Step(engine));
        registry.register(new Jesus(engine));
        registry.register(new Spider(engine));
        registry.register(new Sprint(engine));
        registry.register(new WaterWalk(engine));
        registry.register(new BoatFly(engine));
        registry.register(new Phase(engine));
        registry.register(new LongJump(engine));

        registry.register(new FastPlace(engine));
        registry.register(new FastBreak(engine));
        registry.register(new Scaffold(engine));
        registry.register(new AutoEat(engine));
        registry.register(new Nuker(engine));
        registry.register(new ChestStealer(engine));
        registry.register(new FastEat(engine));
        registry.register(new InventoryClick(engine));

        registry.register(new Xray(engine));
        registry.register(new Freecam(engine));

        registry.register(new BadPackets(engine));
        registry.register(new Timer(engine));
    }
}
