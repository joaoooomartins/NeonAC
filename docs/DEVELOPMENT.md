# NeonAC — Development

## Module layout
```
NeonAC/
├── neonac-api        pure interfaces, events, version/packet/player contracts
├── neonac-core       plugin, engines, managers, config, commands, prediction engine, packet bridge
├── neonac-checks     concrete check implementations (CheckProvider SPI)
├── neonac-storage    YAML / SQLite / MySQL backends behind Storage
└── neonac-versions   per-version physics adapters
```

## Build system
Gradle multi-module. Shadow plugin bundles checks+versions into core:
```
neonac-api  ← neonac-core, neonac-storage, neonac-versions, neonac-checks
neonac-core ← neonac-checks, neonac-storage, neonac-versions (via source sets)
```
`neonac-core` depends on Bukkit/paper API (`compileOnly`). The API module has **no**
external dependencies, so it can be consumed by any plugin.

Output: `neonac-core/build/libs/NeonAC-1.0.0.jar` (single fat JAR).

## Prediction engine
The simplified prediction engine (`com.neonac.core.prediction`) simulates vanilla
physics for movement checks:

```
PlayerData → gravidade, fricção, blocos, flags
→ PredictionEngine.predict() → movimento estimado
→ resolveCollision() → AABB collision com blocos
→ SimulationCheck: offset = |actual - predicted|
→ UncertaintyHandler reduz falsos positivos
```

Key classes:
- `CollisionBox` — AABB com collideX/Y/Z
- `CollisionMath` — Scan de blocos, detecção de colisão
- `PredictionEngine` — Movimento com input, gravidade, fricção
- `UncertaintyHandler` — Teleporte, velocity, ice, slime
- `SetbackManager` — Teleporta para posição segura antes de punir

## Coding rules
- Checks never read NMS or branch on `if (version == ...)`.
- All player-facing text flows through `MessageManager` + `PlaceholderResolver`.
- Detections go through `AbstractCheck#flag` → `CheckEngine#flag` →
  `ViolationManager` (VL) → alerts/punishments. Never ban directly.
- Heavy/blocking work (webhook, JDBC) runs async; Bukkit API calls stay on the main thread.
- Exemptions are consulted before every flag.
- Operation modes (normal/silent/logging/strict/tournament) control alert/punish behavior.

## Running tests
```bash
./gradlew test
```
Unit tests live in each module's `src/test/java`. Engine/version/check logic is kept
free of Bukkit where possible to allow headless testing.

## Adding a storage backend
Implement `com.neonac.api.storage.Storage` and register it in
`StorageManager.init()` (keyed by `storage.type`). No other changes required.
