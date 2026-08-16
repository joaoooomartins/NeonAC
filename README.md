# EarAC — Anti-Cheat Minecraft Multiversão

EarAC é um anti-cheat modular, configurável e extensível para servidores Minecraft
(1.7.10 até 26.2), focado em **precisão**, **baixos falsos positivos** e
**compatibilidade**, usando predição de física por versão, acúmulo de Violation Level
(VL), análise de contexto e múltiplas evidências.

## Módulos
| Módulo           | Responsabilidade                                                       |
|------------------|------------------------------------------------------------------------|
| `earac-api`      | Interfaces puras, eventos, contratos de check/player/version/storage.  |
| `earac-core`     | Plugin Bukkit, engines, managers, config, comandos, **camada de pacotes** (`com.earac.core.packet`). |
| `earac-checks`   | Implementações concretas de checks (combat/movement/player).           |
| `earac-storage`  | Backends YAML / SQLite / MySQL atrás de `Storage`.                    |
| `earac-versions` | Adapters de física/constantes por versão.                             |

> Nota de arquitetura: a "Packet/Protocol Layer" vive em `earac-core.packet`
> (`PacketManager` traduz eventos Bukkit em pacotes abstratos). Isso evita um ciclo
> de dependência e mantém os checks isolados de NMS.

## Build
```bash
./gradlew build        # requer Gradle 8.5+ e Java 17
```
O jar do plugin é `earac-core/build/libs/earac-core-<versao>.jar`. Os módulos
`earac-checks`, `earac-storage` e `earac-versions` são carregados via SPI/reflection.

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
- ✅ Núcleo compilável (api/core/checks/storage/versions validados com javac contra paper-api 1.21).
- ✅ 15 checks reais (combat/movement/player) com VL, confidence, exemptions.
- ✅ Sistema de VL, alertas (staff/console/webhook), punições configuráveis.
- ✅ Storage YAML/SQLite/MySQL com fallback automático.
- ✅ Adapters de versão (legacy/modern) + fallback.
- ✅ API pública + eventos Bukkit canceláveis.
- ✅ Perfis de configuração (`config/profiles/`).
- ✅ Testes unitários (sem servidor) para util/math e version adapters.
- ⚠️ Transporte de pacotes via eventos Bukkit (sem NMS). Um transporte ProtocolLib
  pode substituir `PacketManager` sem mudar nenhum check.
- ⚠️ Detecção aprofundada de transações/packet-order requer transporte de pacotes
  de baixo nível (ProtocolLib) — o esqueleto de `PlayerTransactionPacket` já existe na API.
