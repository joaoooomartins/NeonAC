# NeonAC — API

NeonAC exposes a public API for other plugins (Maven/Gradle dependency on `neonac-api`).

## Obtaining the API

```java
NeonACAPI api = NeonACAPI.get();
if (api == null || !api.isReady()) return;
```

## Common operations

```java
UUID uuid = player.getUniqueId();

// Query VL
double vl = api.getViolationLevel(uuid, "killaura");

// Inspect a player
NeonACPlayer ep = api.getPlayer(uuid);
MinecraftVersion v = ep.getVersion();
int ping = ep.getPing();

// Register a custom check
api.registerCheck(myCheck);

// Temporary exemption (e.g. minigame plugin)
api.registerExemption(uuid, "killaura", ExemptionType.PLUGIN, 60_000L);

// Version adapter (physics constants)
VersionAdapter adapter = api.getVersionAdapter(ep.getVersion());
double gravity = adapter.getGravity();
```

## Events (Bukkit events in `com.neonac.core.api.events`)

| Event                    | Cancellable | Fired when…                                  |
|--------------------------|-------------|----------------------------------------------|
| `NeonACViolationEvent`    | no          | A violation is recorded (after VL update).   |
| `NeonACAlertEvent`        | yes         | A staff alert is about to be sent.           |
| `NeonACPunishmentEvent`   | yes         | A punishment command is about to run.        |
| `NeonACCheckStateEvent`   | no          | A check is enabled/disabled at runtime.      |

Listening example:

```java
@EventHandler
public void onPunish(NeonACPunishmentEvent e) {
    if (e.getViolation().getCheck().getId().equals("killaura")) {
        e.setCancelled(true); // escalate your own way
    }
}
```

## Extension model

Checks are discovered via `java.util.ServiceLoader` for `CheckProvider`. A module
declares `META-INF/services/com.neonac.api.check.CheckProvider` pointing at its
provider class. The core calls `registerChecks(CheckRegistry)` on startup — so a
separate plugin/extension can add checks without touching NeonAC core.
