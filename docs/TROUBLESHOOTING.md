# NeonAC — Troubleshooting

## High false-positive rate
- Raise `vl.decay` / lower `vl.add` for the noisy check in `config.yml`.
- Increase `general` tolerances or set `general.profile: minigames` for game modes
  with unusual movement.
- Use `/neonac info <player>` to inspect environment flags — if `slime`/`ice`/`web`
  read incorrectly for your version, the issue is in a `VersionAdapter`, not the check.

## No alerts / no punishments
- Check `alerts.enabled` and per-category toggles (`alerts.combat`, …).
- Confirm staff have `neonac.alerts` / `neonac.alerts.<category>`.
- Confirm `punishments.enabled: true` and that the console can run the commands
  (op/exempt from `neonac.bypass`).
- Check if mode is `silent`, `logging`, or `tournament` — these suppress alerts/punishments.
- Run `/neonac mode` to check current mode, switch to `normal` if needed.
- Run `/neonac checks` to confirm checks are enabled and loaded (a missing
  `neonac-checks` jar → "No CheckProvider found" in the console).

## Version adapter warnings
- `Failed to load version adapters` → `neonac-versions` not present; fallback adapter used
  (still functional, less precise).
- `Failed to initialise JDBC storage` → driver jar missing; NeonAC falls back to YAML.
- `Connection lost, reconnecting...` → MySQL/SQLite connection dropped; NeonAC auto-reconnects.
- `saveViolation failed (attempt X)` → storage temporarily unavailable; retries automatically.

## Debug
- `/neonac debug <player>` toggles a detailed per-player snapshot.
- `general.debug: true` enables verbose internal logging.

## Performance
- NeonAC processes checks on the server tick and keeps per-player state in memory.
- Dirty set in ViolationManager skips decay for players with VL=0.
- Material cache in PlayerData reduces world.getBlock() calls.
- Alert rate limiting prevents spam (2s cooldown per player:check).
- Disable unused checks to reduce work. `metrics.enabled: false` turns off telemetry.
- Storage writes (JDBC) are async; for very large networks prefer MySQL over YAML.
