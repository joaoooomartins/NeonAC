# EarAC — Installation

## Requirements
- Java 17+
- A Bukkit/Spigot/Paper/Purpur/Folia server (1.7.10 – 26.2)
- Gradle 8.x to build (or use a prebuilt jar)

## Build

```bash
git clone <repo> EarAC
cd EarAC
./gradlew build          # Linux/macOS
gradlew.bat build        # Windows
```

Artifacts are produced per module under `*/build/libs/`. The plugin jar is
`earac-core/build/libs/earac-core-<version>.jar`.

## Install
1. Place `earac-core-*.jar` in your server's `plugins/` folder.
2. (Optional) place `earac-checks-*.jar`, `earac-versions-*.jar`,
   `earac-storage-*.jar` if you build them as separate jars — the core loads them
   via SPI / reflection. Building via the root project bundles them automatically.
3. Start the server. `config.yml` and `storage.properties` are generated under
   `plugins/EarAC/`.
4. JDBC backends: drop the SQLite/MySQL driver jar in `plugins/` if you use
   `storage.type: sqlite|mysql`.

## Select a profile
Set `general.profile: competitive` (or `minigames`) in `config.yml` and restart/reload
to apply `config/profiles/<name>.yml` on top of the base config.
