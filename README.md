# NeonAC — Anti-Cheat Minecraft Multiversão

NeonAC é um anti-cheat modular, configurável e extensível para servidores Minecraft
(1.7.10 até 26.2), focado em **precisão**, **baixos falsos positivos** e
**compatibilidade**, usando predição de física simplificada, acúmulo de Violation Level
(VL), análise de contexto e múltiplas evidências.

## Módulos
| Módulo           | Responsabilidade                                                       |
|------------------|------------------------------------------------------------------------|
| `neonac-api`      | Interfaces puras, eventos, contratos de check/player/version/storage.  |
| `neonac-core`     | Plugin Bukkit, engines, managers, config, comandos, prediction engine, camada de pacotes. |
| `neonac-checks`   | Implementações concretas de checks (combat/movement/player/world/packet). |
| `neonac-storage`  | Backends YAML / SQLite / MySQL atrás de `Storage`.                    |
| `neonac-versions` | Adapters de física/constantes por versão.                             |

> Arquitetura: Single JAR via Shadow plugin — `neonac-checks` e `neonac-versions`
> são bundled no `neonac-core` (sem dependências circulares).

## Build
```bash
./gradlew build        # requer Gradle 9.x e Java 21
```
O jar do plugin é `neonac-core/build/libs/NeonAC-1.0.0.jar` (single fat JAR com todos os módulos).

## Detection flow
```
pacote → PacketManager → pacote abstrato → CheckEngine.dispatch
       → Check (onMove/onAttack/...) → flag(confiança)
       → ViolationManager (VL += add*confiança, decay)
       → alerta (threshold) → punição (threshold)
       → setback (se aplicável, teleporta para posição segura)
```

## Prediction Engine (simplified from Grim)
```
PlayerData (gravidade, fricção, blocos) → PredictionEngine.predict()
→ resolveCollision() → CollisionMath.collide(AABB)
→ SimulationCheck: offset = |actual - predicted|
→ UncertaintyHandler reduz falsos positivos
→ SetbackManager teleporta para última posição segura
```

## Features
- 44 checks (combat/movement/player/world/packet) com VL, confidence, exemptions
- Sistema de VL, alertas (staff/console/webhook), punições configuráveis
- **Setback com confirmação** — teleporta para posição segura antes de punir
- **Prediction engine** — simula física do vanilla (gravidade, fricção, colisão)
- 5 modos de operação (normal/silent/logging/strict/tournament)
- Storage YAML/SQLite/MySQL com reconexão, retry e fallback automático
- Exemptions granulares (global, per-check, per-category, timed)
- Adapters de versão (legacy/modern) + fallback
- API pública + eventos Bukkit canceláveis
- Perfis de configuração (`config/profiles/`)

## Documentação (`docs/`)
- [INSTALLATION.md](docs/INSTALLATION.md)
- [CONFIGURATION.md](docs/CONFIGURATION.md)
- [COMMANDS.md](docs/COMMANDS.md)
- [PERMISSIONS.md](docs/PERMISSIONS.md)
- [API.md](docs/API.md)
- [CHECKS.md](docs/CHECKS.md)
- [VERSION_SUPPORT.md](docs/VERSION_SUPPORT.md)
- [DEVELOPMENT.md](docs/DEVELOPMENT.md)
- [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
