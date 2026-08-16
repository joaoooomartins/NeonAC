# EarAC — Commands

Base command: `/earac` (alias `/ac`). All sub-commands require the matching
`earac.command.*` permission (see PERMISSIONS.md).

| Command                         | Description                                              |
|---------------------------------|----------------------------------------------------------|
| `/earac help`                   | Show the help list.                                      |
| `/earac reload`                 | Reload configuration, punishments and check states.      |
| `/earac version`                | Print server implementation + detected version.          |
| `/earac checks`                 | List all registered checks with enabled state.           |
| `/earac info <player>`          | Print a live debug snapshot (position, VL, environment). |
| `/earac violations <player>`    | List the player's current VL per check.                  |
| `/earac reset <player>`         | Reset the player's VL and per-check state.               |
| `/earac punish <player> <check>`| Force a punishment for a check (admin tool).              |
| `/earac bypass <player>`        | Grant a 10-minute temporary exemption.                   |
| `/earac alerts`                 | Show whether global alerts are enabled.                  |

## Examples

```
/earac reload
/earac info Notch
/earac violations Steve
/earac reset Steve
/earac punish Cheater killaura
```

`info` reflects the same data the debug engine uses: position delta, yaw/pitch,
on-ground state, environment flags (slime/ice/web), and per-check VL. This is the
primary troubleshooting tool — combine it with `/earac debug <player>`.
