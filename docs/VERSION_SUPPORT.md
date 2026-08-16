# EarAC — Version Support

EarAC targets **Minecraft 1.7.10 → 26.2**. Version concerns are isolated in adapters
so checks never branch on a raw version number.

## Three distinct concepts

| Concept            | Detected by                                  |
|--------------------|----------------------------------------------|
| Minecraft version  | `Bukkit.getBukkitVersion()` (server)         |
| Protocol version   | `Player#getProtocolVersion()` (Paper, per-client) |
| Server impl        | `Bukkit.getName()` (Paper/Spigot/Purpur/Folia)|

## Adapters

`VersionAdapter` (`earac-api`) exposes physics constants and capability queries:
gravity, friction, step height, base speeds, elytra support, dig timing, tolerance.

`earac-versions` registers concrete adapters:
- `LegacyVersionAdapter` for 1.7.10 – 1.8 (no elytra, old dig timing).
- `ModernVersionAdapter` for 1.9+ (elytra, modern movement, modern dig timing).

If `earac-versions` is absent, the core falls back to `FallbackVersionAdapter`
(modern-ish, conservative). `VersionAdapterRegistry.get(version)` always returns the
closest registered adapter at or below the requested version.

## Per-version config overrides

```yaml
versions:
  "20":
    movement:
      tolerance: 0.05
```
These are applied transparently by `CheckConfigImpl` — checks read a single
`getConfig()` and automatically get the version-appropriate values.

## Known limitations / honest notes
- Per-client protocol detection uses Paper's `Player#getProtocolVersion()` when present;
  otherwise it falls back to the server version (acceptable for single-version networks).
- Packet-level analysis (transactions, true ordering) is provided through the Bukkit
  event transport in this build. A ProtocolLib-backed transport can replace
  `PacketManager` without any check changes for deeper 1.7/1.8 packet semantics.
- JDBC backends (SQLite/MySQL) require the corresponding driver jar on the server
  classpath; if unavailable, EarAC automatically falls back to YAML storage.
