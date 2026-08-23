# NeonAC — Commands

Base command: `/neonac` (alias `/ac`). All sub-commands require the matching
`neonac.command.*` permission (see PERMISSIONS.md).

| Command                         | Description                                              |
|---------------------------------|----------------------------------------------------------|
| `/neonac help`                   | Show the help list.                                      |
| `/neonac reload`                 | Reload configuration, punishments and check states.      |
| `/neonac version`                | Print server implementation + detected version.          |
| `/neonac checks`                 | List all registered checks with enabled state.           |
| `/neonac info <player>`          | Print a live debug snapshot (position, VL, environment). |
| `/neonac violations <player>`    | List the player's current VL per check.                  |
| `/neonac reset <player>`         | Reset the player's VL and per-check state.               |
| `/neonac punish <player> <check>`| Force a punishment for a check (admin tool).              |
| `/neonac bypass <player> [target]`| Grant a 10-minute exemption. Target: nothing (global), check ID, or `category:<cat>` |
| `/neonac alerts`                 | Show whether global alerts are enabled.                  |

## Examples

```
/neonac reload
/neonac info Notch
/neonac violations Steve
/neonac reset Steve
/neonac punish Cheater killaura
/neonac bypass Notch
/neonac bypass Notch killaura
/neonac bypass Notch category:movement
```

`info` reflects the same data the debug engine uses: position delta, yaw/pitch,
on-ground state, environment flags (slime/ice/web), and per-check VL. This is the
primary troubleshooting tool — combine it with `/neonac debug <player>`.
