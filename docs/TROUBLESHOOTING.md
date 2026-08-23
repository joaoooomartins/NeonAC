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
- Run `/neonac checks` to confirm checks are enabled and loaded (a missing
  `neonac-checks` jar → "No CheckProvider found" in the console).

## Version adapter warnings
- `Failed to load version adapters` → `neonac-versions` not present; fallback adapter used
  (still functional, less precise).
- `Failed to initialise JDBC storage` → driver jar missing; NeonAC falls back to YAML.

## Debug
- `/neonac debug <player>` toggles a detailed per-player snapshot.
- `general.debug: true` enables verbose internal logging.

## Performance
- NeonAC processes checks on the server tick and keeps per-player state in memory.
- Disable unused checks to reduce work. `metrics.enabled: false` turns off telemetry.
- Storage writes (JDBC) are lazy; for very large networks prefer MySQL over YAML.
