# NeonAC — Development

## Module layout
```
NeonAC/
├── neonac-api        pure interfaces, events, version/packet/player contracts
├── neonac-core       plugin, engines, managers, config, commands, packet bridge
├── neonac-protocol   (transport abstraction; PacketManager is the Bukkit bridge)
├── neonac-checks     concrete check implementations (CheckProvider SPI)
├── neonac-storage    YAML / SQLite / MySQL backends behind Storage
└── neonac-versions   per-version physics adapters
```

## Build system
Gradle multi-module. Dependency directions (no cycles):
```
neonac-api  ← neonac-core, neonac-storage, neonac-versions, neonac-checks
neonac-core ← neonac-checks, neonac-storage, neonac-versions
```
`neonac-core` depends on Bukkit/paper API (`compileOnly`). The API module has **no**
external dependencies, so it can be consumed by any plugin.

## Coding rules
- Checks never read NMS or branch on `if (version == ...)`.
- All player-facing text flows through `MessageManager` + `PlaceholderResolver`.
- Detections go through `AbstractCheck#flag` → `CheckEngine#flag` →
  `ViolationManager` (VL) → alerts/punishments. Never ban directly.
- Heavy/blocking work (webhook, JDBC) runs async; Bukkit API calls stay on the main thread.
- Exemptions are consulted before every flag.

## Running tests
```bash
./gradlew test
```
Unit tests live in each module's `src/test/java`. Engine/version/check logic is kept
free of Bukkit where possible to allow headless testing.

## Adding a storage backend
Implement `com.neonac.api.storage.Storage` and register it in
`StorageManager.init()` (keyed by `storage.type`). No other changes required.
