# NeonAC — Permissions

All permissions are configurable and default to `op` for staff commands.

## Command permissions
| Permission                | Default | Purpose                       |
|---------------------------|---------|-------------------------------|
| `neonac.command`           | op      | Base access to `/neonac`.      |
| `neonac.command.reload`    | op      | `/neonac reload`.              |
| `neonac.command.checks`    | op      | `/neonac checks`.              |
| `neonac.command.debug`     | op      | `/neonac info` / `debug`.      |
| `neonac.command.violations`| op      | `/neonac violations`.          |
| `neonac.command.reset`     | op      | `/neonac reset`.               |
| `neonac.command.punish`    | op      | `/neonac punish`.              |
| `neonac.command.bypass`    | op      | `/neonac bypass`.              |
| `neonac.command.mode`     | op      | `/neonac mode`.                |
| `neonac.command.verbose`  | op      | `/neonac verbose`.             |

## Alert permissions
| Permission             | Purpose                                  |
|------------------------|------------------------------------------|
| `neonac.alerts`        | Receive all staff alerts.                |
| `neonac.alerts.combat` | Receive combat-category alerts.          |
| `neonac.alerts.movement` | Receive movement-category alerts.     |
| `neonac.alerts.player` | Receive player-category alerts.          |
| `neonac.alerts.world`  | Receive world-category alerts.           |
| `neonac.alerts.packet` | Receive packet-category alerts.          |

## Bypass permissions (never granted by default)
Bypass is **opt-in only** — a player must explicitly be given the node.
| Permission                  | Effect                              |
|-----------------------------|-------------------------------------|
| `neonac.bypass`              | Bypass every check.                 |
| `neonac.bypass.combat`       | Bypass all combat checks.           |
| `neonac.bypass.combat.killaura` | Bypass a single check (id).     |
| `neonac.bypass.movement`     | Bypass all movement checks.         |
| `neonac.bypass.player`       | Bypass all player checks.           |

## Setback permissions (never granted by default)
| Permission                  | Effect                              |
|-----------------------------|-------------------------------------|
| `neonac.nosetback`           | Immune to all setbacks.             |
| `neonac.nosetback.<id>`      | Immune to setback for a specific check. |

## Verbose permissions
| Permission                  | Effect                              |
|-----------------------------|-------------------------------------|
| `neonac.verbose`             | Toggle verbose mode per player.     |

Bypass is applied per check via `Check#getBypassPermission()`, which is
`neonac.bypass.<category>.<id>`.
