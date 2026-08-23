# NeonAC — Configuration

All configuration lives in `config.yml` (generated on first start). Messages, alerts,
punishments, checks, storage and version overrides are all editable **without recompiling**.

## Top-level structure

```yaml
general:
  prefix: "&8[&bNeonAC&8]"
  language: "pt-BR"
  debug: false
  mode: "normal"         # normal | silent | logging | strict | tournament
  profile: ""            # optional: competitive | minigames | <name>

metrics:
  enabled: true

storage:
  type: yaml             # yaml | sqlite | mysql | mariadb
  mysql:
    host: "localhost"
    port: 3306
    database: "NeonAC"
    user: "root"
    password: ""
    params: "useSSL=false&serverTimezone=UTC"

alerts:
  enabled: true
  format: "%prefix% &7%player% &8falhou em &b%check% &8VL=&c%vl%"
  combat: true
  movement: true
  player: true

discord:
  enabled: false
  webhook: ""

punishments:
  enabled: true
  - threshold: 10
    commands:
      - "kick %player% [NeonAC] Comportamento suspeito"
  - threshold: 25
    commands:
      - "tempban %player% 1h [NeonAC] %check%"
  - threshold: 50
    commands:
      - "ban %player% [NeonAC] %check%"

checks:
  <category>:
    <checkId>:
      enabled: true
      threshold: 10
      punish: 30
      alert: 2
      max-reach: 3.2        # check-specific keys
      vl:
        add: 1.0
        decay: 0.05
        max: 100.0

versions:
  "20":                   # per-version overrides (optional)
    movement:
      tolerance: 0.05

messages:
  prefix: "&8[&bNeonAC&8]"
  no-permission: "%prefix% &cVocê não possui permissão."
  reload: "%prefix% &aConfiguração recarregada."
```

## Configurable keys per check

| Key              | Meaning                                              |
|------------------|------------------------------------------------------|
| `enabled`        | Enable/disable the check.                            |
| `threshold`      | VL at which alerts become meaningful (display).      |
| `punish`         | VL at which punishment is triggered.                 |
| `alert`          | VL at which staff alerts are sent.                   |
| `vl.add`         | VL added per detection (× confidence).               |
| `vl.decay`       | VL removed per decay cycle when idle.                |
| `vl.max`         | VL clamp.                                            |

Any check may define extra keys (e.g. `max-reach`, `max-cps`, `min-ticks`) which are
read via `getConfig().getDouble("key", default)`.

## Reload

`/neonac reload` reloads the file and re-applies `enabled`/`threshold` per check.

## Placeholders

`%player% %uuid% %check% %check_id% %category% %vl% %confidence% %ping% %tps%
%version% %server% %prefix% %date% %time% %reason%` — usable in alerts, messages and
punishment commands.

## Operation Modes

| Mode | Behavior |
|---|---|
| `normal` | Default. Detects, alerts, and punishes. |
| `silent` | Detects but no alerts or punishments. Logs only. |
| `logging` | Same as silent but writes detailed logs to file. |
| `strict` | VL multiplier 1.5x. Alerts and punishes. |
| `tournament` | Exempts everyone. No alerts or punishments. |

Switch via `/neonac mode <mode>` or config `general.mode`.
