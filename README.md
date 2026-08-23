# NeonAC — Anti-Cheat Minecraft Multiversão

NeonAC é um anti-cheat modular, configurável e extensível para servidores Minecraft
(1.7.10 até 26.2), focado em **precisão**, **baixos falsos positivos** e
**compatibilidade**, usando predição de física por versão, acúmulo de Violation Level
(VL), análise de contexto e múltiplas evidências.

## Módulos
| Módulo           | Responsabilidade                                                       |
|------------------|------------------------------------------------------------------------|
| `neonac-api`      | Interfaces puras, eventos, contratos de check/player/version/storage.  |
| `neonac-core`     | Plugin Bukkit, engines, managers, config, comandos, **camada de pacotes** (`com.neonac.core.packet`). |
| `neonac-checks`   | Implementações concretas de checks (combat/movement/player).           |
| `neonac-storage`  | Backends YAML / SQLite / MySQL atrás de `Storage`.                    |
| `neonac-versions` | Adapters de física/constantes por versão.                             |

> Nota de arquitetura: a "Packet/Protocol Layer" vive em `neonac-core.packet`
> (`PacketManager` traduz eventos Bukkit em pacotes abstratos). Isso evita um ciclo
> de dependência e mantém os checks isolados de NMS.

## Build
```bash
./gradlew build        # requer Gradle 9.x e Java 21
```
O jar do plugin é `neonac-core/build/libs/neonac-core-<versao>.jar`. Os módulos
`neonac-checks`, `neonac-storage` e `neonac-versions` são carregados via SPI/reflection.

## Detection flow
```
pacote → PacketManager → pacote abstrato → CheckEngine.dispatch
       → Check (onMove/onAttack/...) → flag(confiança)
       → ViolationManager (VL += add*confiança, decay)
       → alerta (threshold) → punição (threshold)
```

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

## Status de implementação
- ✅ Núcleo compilável (api/core/checks/storage/versions validados contra paper-api 1.21).
- ✅ 34 checks reais (combat/movement/player/world/packet) com VL, confidence, exemptions.
- ✅ Sistema de VL, alertas (staff/console/webhook), punições configuráveis.
- ✅ Storage YAML/SQLite/MySQL com reconexão, retry e fallback automático.
- ✅ Exemptions granulares (global, per-check, per-category, timed).
- ✅ 5 modos de operação (normal/silent/logging/strict/tournament).
- ✅ Adapters de versão (legacy/modern) + fallback.
- ✅ API pública + eventos Bukkit canceláveis.
- ✅ Perfis de configuração (`config/profiles/`).
