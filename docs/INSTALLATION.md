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

Artifacts are produced per module under `*/build/libs/`. The plugin jar is
`neonac-core/build/libs/neonac-core-<version>.jar`.

## Install
1. Place `neonac-core-*.jar` in your server's `plugins/` folder.
2. (Optional) place `neonac-checks-*.jar`, `neonac-versions-*.jar`,
   `neonac-storage-*.jar` if you build them as separate jars — the core loads them
   via SPI / reflection. Building via the root project bundles them automatically.
3. Start the server. `config.yml` and `storage.properties` are generated under
   `plugins/neonac/`.
4. JDBC backends: drop the SQLite/MySQL driver jar in `plugins/` if you use
   `storage.type: sqlite|mysql`.

## Select a profile
Set `general.profile: competitive` (or `minigames`) in `config.yml` and restart/reload
to apply `config/profiles/<name>.yml` on top of the base config.
