# EarAC — Permissions

All permissions are configurable and default to `op` for staff commands.

## Command permissions
| Permission                | Default | Purpose                       |
|---------------------------|---------|-------------------------------|
| `earac.command`           | op      | Base access to `/earac`.      |
| `earac.command.reload`    | op      | `/earac reload`.              |
| `earac.command.checks`    | op      | `/earac checks`.              |
| `earac.command.debug`     | op      | `/earac info` / `debug`.      |
| `earac.command.violations`| op      | `/earac violations`.          |
| `earac.command.reset`     | op      | `/earac reset`.               |
| `earac.command.punish`    | op      | `/earac punish`.              |
| `earac.command.bypass`    | op      | `/earac bypass`.              |

## Alert permissions
| Permission           | Purpose                                  |
|----------------------|------------------------------------------|
| `earac.alerts`       | Receive all staff alerts.                |
| `earac.alerts.combat`| Receive combat-category alerts.          |
| `earac.alerts.movement` | Receive movement-category alerts.     |
| `earac.alerts.player`| Receive player-category alerts.          |

## Bypass permissions (never granted by default)
Bypass is **opt-in only** — a player must explicitly be given the node.
| Permission                  | Effect                              |
|-----------------------------|-------------------------------------|
| `earac.bypass`              | Bypass every check.                 |
| `earac.bypass.combat`       | Bypass all combat checks.           |
| `earac.bypass.combat.killaura` | Bypass a single check (id).     |
| `earac.bypass.movement`     | Bypass all movement checks.         |
| `earac.bypass.player`       | Bypass all player checks.           |

Bypass is applied per check via `Check#getBypassPermission()`, which is
`earac.bypass.<category>.<id>`.
