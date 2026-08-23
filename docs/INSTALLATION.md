# NeonAC — Installation

## Requirements
- Java 21+
- A Bukkit/Spigot/Paper/Purpur/Folia server (1.7.10 – 26.2)
- Gradle 9.x to build (or use a prebuilt jar)

## Build

```bash
git clone <repo> NeonAC
cd NeonAC
./gradlew build          # Linux/macOS
gradlew.bat build        # Windows
```

The Shadow plugin produces a single fat JAR: `neonac-core/build/libs/NeonAC-1.0.0.jar`.
All modules (api, core, checks, storage, versions) are bundled into this single JAR.

## Install
1. Place `NeonAC-1.0.0.jar` in your server's `plugins/` folder.
2. Start the server. `config.yml` is generated under `plugins/neonac/`.
3. JDBC backends: drop the SQLite/MySQL driver jar in `plugins/` if you use
   `storage.type: sqlite|mysql`.

## Select a profile
Set `general.profile: competitive` (or `minigames`) in `config.yml` and restart/reload
to apply `config/profiles/<name>.yml` on top of the base config.
