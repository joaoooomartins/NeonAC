# NeonAC — Checks

NeonAC ships with a modular check engine and a simplified prediction engine.
Every check extends `AbstractCheck` (core) and is registered via the `CheckProvider` SPI.

## Implemented checks (44 total)

### Combat (`com.neonac.checks.combat`)
| Check ID        | Name           | Method                                                                 |
|-----------------|----------------|-------------------------------------------------------------------------|
| `killaura`      | KillAura       | Flags attacks on targets beyond reach or outside the view angle.       |
| `reach`         | Reach          | Pure distance check beyond the legitimate interact range.              |
| `aim`           | Aim            | Rotation-speed analysis between consecutive hits (snap detection).     |
| `autoclicker`   | AutoClicker    | CPS + inter-click variance (robotic patterns).                         |
| `velocity`      | Velocity       | Verifies server knockback actually moves the player.                   |
| `criticals`     | Criticals      | Critical hit without proper fall conditions.                           |
| `fastbow`       | FastBow        | Bow shot faster than possible.                                         |
| `hitbox`        | HitBox         | Attack angle outside legitimate hitbox.                                |
| `velocitycheck` | VelocityCheck  | Invalid velocity cancel patterns.                                     |

### Movement (`com.neonac.checks.movement`)
| Check ID      | Name        | Method                                                                   |
|---------------|-------------|--------------------------------------------------------------------------|
| `fly`         | Fly         | Sustained vertical suspension vs gravity prediction.                     |
| `speed`       | Speed       | Horizontal displacement beyond physics-predicted maximum.               |
| `nofall`      | NoFall      | Significant fall with no reported fall distance.                         |
| `step`        | Step        | Abrupt ~1-block vertical step beyond auto-step height.                  |
| `jesus`       | Jesus       | Horizontal movement on liquid surfaces without sinking.                 |
| `spider`      | Spider      | Climbing walls without ladder/vine.                                     |
| `sprint`      | Sprint      | Impossible sprint state changes.                                        |
| `invalidsprint` | InvalidSprint | Sprint while on soul sand, low food, or blocking.                    |
| `waterwalk`   | WaterWalk   | Walking on water surface.                                              |
| `boatfly`     | BoatFly     | Flying while in a boat.                                                 |
| `phase`       | Phase       | Clipping through solid blocks.                                          |
| `longjump`    | LongJump    | Excessive jump boost or horizontal air speed.                           |
| `simulation`  | Simulation  | Movement differs from physics prediction engine (simplified Grim).     |

### Player (`com.neonac.checks.player`)
| Check ID          | Name            | Method                                                    |
|-------------------|-----------------|-----------------------------------------------------------|
| `fastplace`       | FastPlace       | Block placement rate ceiling.                             |
| `fastbreak`       | FastBreak       | Block break completion rate ceiling.                      |
| `scaffold`        | Scaffold        | Repetitive downward-looking placement pattern.            |
| `farplace`        | FarPlace        | Block placed beyond legitimate reach.                     |
| `invalidplacea`   | InvalidPlaceA   | Placing blocks at impossible angles.                      |
| `invalidplaceb`   | InvalidPlaceB   | Placing blocks inside the player.                         |
| `airliquidplace`  | AirLiquidPlace  | Placing blocks in liquid/air where solid needed.          |
| `multiplace`      | MultiPlace      | Multiple blocks placed in a single tick.                  |
| `fabricatedplace` | FabricatedPlace | Block placed without valid line of sight.                 |
| `positionplace`   | PositionPlace   | Block placed at exact player position (ghost block).      |
| `rotationplace`   | RotationPlace   | Block placed with impossible rotation.                    |
| `duplicaterotplace` | DuplicateRotPlace | Same rotation used for consecutive placements.       |
| `autoeat`         | AutoEat         | Eating food faster than possible.                         |
| `nuker`           | Nuker           | Breaking blocks faster than legitimate speed.             |
| `cheststealer`    | ChestStealer    | Taking items from chest faster than possible.             |
| `fasteat`         | FastEat         | Consuming items faster than normal.                       |
| `inventoryclick`  | InventoryClick  | Inventory click patterns consistent with automation.    |

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

## Prediction Engine

The `Simulation` check uses a simplified prediction engine inspired by Grim Anticheat:

```
PlayerData (gravidade, fricção, blocos)
→ PredictionEngine.predict() (estimativa de movimento)
→ resolveCollision() (AABB collision com blocos)
→ CollisionMath.collide() (resolução eixo por eixo)
→ offset = |actual - predicted|
→ UncertaintyHandler (reduz falsos positivos)
→ flag se offset > threshold + uncertainty
```

Components:
- `CollisionBox` — AABB com collideX/Y/Z, collide, distance
- `CollisionMath` — Scan de blocos, detecção de colisão, resolve
- `Vector3d` — Vetor 3D com operações básicas
- `UncertaintyHandler` — Teleporte, velocity, ice, slime, honey

## How detection works

Every check accumulates a **Violation Level (VL)**, never banning directly:

```
detection → VL += vl.add * confidence
no detection for a while → VL -= vl.decay
VL >= alert threshold → staff/console/webhook alert
VL >= punish threshold → configured punishment commands
VL >= 50% punish threshold → setback (teleport to safe position)
```

Confidence in `[0,1]` lets weak evidence nudge VL without triggering punishment.
Exemptions (teleport, velocity, liquid, ladder, web, low TPS, high ping, permissions)
suppress checks so legitimate players are not flagged.

## Adding a new check

1. Create a class extending `com.neonac.core.check.AbstractCheck`.
2. Annotate with `@CheckInfo(id=, name=, category=, description=, since=, decay=, setback=)`.
3. Override the relevant `on*` hook (`onMove`, `onAttack`, `onDig`, `onPlace`,
   `onVelocity`, `onTick`).
4. Call `flag(player, confidence, Map.of("key", value))` on detection.
5. Register it in `NeonACCheckProvider.registerChecks` (or your own `CheckProvider`).

```java
@CheckInfo(id = "aimc", name = "AimC", category = CheckCategory.COMBAT,
           description = "...", decay = 0.05, setback = 50)
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
