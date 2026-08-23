# NeonAC — Checks

NeonAC ships with a modular check engine. Every check extends `AbstractCheck` (core)
and is registered via the `CheckProvider` SPI — no core changes are needed to add one.

## Implemented checks

### Combat (`com.neonac.checks.combat`)
| Check ID      | Name           | Method                                                                 |
|---------------|----------------|-------------------------------------------------------------------------|
| `killaura`    | KillAura       | Flags attacks on targets beyond reach or outside the view angle.       |
| `reach`       | Reach          | Pure distance check beyond the legitimate interact range.              |
| `aim`         | Aim            | Rotation-speed analysis between consecutive hits (snap detection).     |
| `autoclicker` | AutoClicker    | CPS + inter-click variance (robotic patterns).                         |
| `velocity`    | Velocity       | Verifies server knockback actually moves the player.                   |
| `criticals`   | Criticals      | Critical hit without proper fall conditions.                           |
| `fastbow`     | FastBow        | Bow shot faster than possible.                                         |
| `hitbox`      | HitBox         | Attack angle outside legitimate hitbox.                                |
| `velocitycheck` | VelocityCheck | Invalid velocity cancel patterns.                                     |

### Movement (`com.neonac.checks.movement`)
| Check ID   | Name      | Method                                                                   |
|------------|-----------|--------------------------------------------------------------------------|
| `fly`      | Fly       | Sustained vertical suspension vs gravity prediction.                     |
| `speed`    | Speed     | Horizontal displacement beyond physics-predicted maximum.               |
| `nofall`   | NoFall    | Significant fall with no reported fall distance.                         |
| `step`     | Step      | Abrupt ~1-block vertical step beyond auto-step height.                  |
| `jesus`    | Jesus     | Horizontal movement on liquid surfaces without sinking.                 |
| `spider`   | Spider    | Climbing walls without ladder/vine.                                     |
| `sprint`   | Sprint    | Impossible sprint state changes.                                        |
| `waterwalk` | WaterWalk | Walking on water surface.                                              |
| `boatfly`  | BoatFly   | Flying while in a boat.                                                 |
| `phase`    | Phase     | Clipping through solid blocks.                                          |
| `longjump` | LongJump  | Excessive jump boost or horizontal air speed.                           |

### Player (`com.neonac.checks.player`)
| Check ID        | Name          | Method                                                    |
|-----------------|---------------|-----------------------------------------------------------|
| `fastplace`     | FastPlace     | Block placement rate ceiling.                             |
| `fastbreak`     | FastBreak     | Block break completion rate ceiling.                      |
| `scaffold`      | Scaffold      | Repetitive downward-looking placement pattern.            |
| `autoeat`       | AutoEat       | Eating food faster than possible.                         |
| `nuker`         | Nuker         | Breaking blocks faster than legitimate speed.             |
| `cheststealer`  | ChestStealer  | Taking items from chest faster than possible.             |
| `fasteat`       | FastEat       | Consuming items faster than normal.                       |
| `inventoryclick` | InventoryClick | Inventory click patterns consistent with automation.    |

### World (`com.neonac.checks.world`)
| Check ID  | Name    | Method                                                         |
|-----------|---------|----------------------------------------------------------------|
| `xray`    | Xray    | Looking directly at valuable ores through walls.              |
| `freecam` | Freecam | Camera position diverges from body position.                  |

### Packet (`com.neonac.checks.packet`)
| Check ID    | Name      | Method                                                       |
|-------------|-----------|--------------------------------------------------------------|
| `badpackets` | BadPackets | Invalid packet sequences or impossible states.             |
| `timer`     | Timer     | Server tick rate manipulation detected.                     |

## How detection works

Every check accumulates a **Violation Level (VL)**, never banning directly:

```
detection → VL += vl.add * confidence
no detection for a while → VL -= vl.decay
VL >= alert threshold → staff/console/webhook alert
VL >= punish threshold → configured punishment commands
```

Confidence in `[0,1]` lets weak evidence nudge VL without triggering punishment.
Exemptions (teleport, velocity, liquid, ladder, web, low TPS, high ping, permissions)
suppress checks so legitimate players are not flagged.

## Adding a new check

1. Create a class extending `com.neonac.core.check.AbstractCheck`.
2. Annotate with `@CheckInfo(id=, name=, category=, description=, since=, until=)`.
3. Override the relevant `on*` hook (`onMove`, `onAttack`, `onDig`, `onPlace`,
   `onVelocity`, `onTick`).
4. Call `flag(player, confidence, Map.of("key", value))` on detection.
5. Register it in `NeonACCheckProvider.registerChecks` (or your own `CheckProvider`).

```java
@CheckInfo(id = "aimc", name = "AimC", category = CheckCategory.COMBAT, description = "...")
public final class AimC extends AbstractCheck {
    public AimC(CheckEngine engine) { super(engine); }

    @Override
    public void onMove(NeonACPlayer p, PlayerMovePacket pkt) {
        if (isExempt(p)) return;
        // ... compute confidence ...
        flag(p, confidence, "info", value);
    }
}
```

No `if (version == ...)` is ever needed — version differences live in the
`VersionAdapter` implementations (`neonac-versions`), not in checks.
