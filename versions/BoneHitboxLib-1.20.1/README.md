# BoneHitboxLib 1.20.1

[English](README.md) | [简体中文](README.zh-CN.md)

Version **0.2.0** for Minecraft **1.20.1**, supporting **Fabric / Forge**. This is an independent Gradle project. Use matching client, server and dependency versions from the [version guide](../README.md).

## Features and integration

Model-derived entity OBBs, part selection and highlights, combat/contact events, per-part state, held-item geometry, collision and carrying, server pose queries, optional GeckoLib keyframe skills, and automatic model-based multi-cell blocks.

Start with the [main integration guide](../../README.md), then use the API documentation in this project:

- [Registration, synchronization and events](docs/server_sync_api.md)
- [Client selection and highlights](docs/client_obb_selection.md)
- [Model block shapes and configuration](docs/block_shapes.md)
- [Package layout](docs/package_layout.md)

The server configuration includes the vanilla slanted-block mode, a default-off forced OBB option, and shared block whitelist/blacklist filters. The blacklist takes priority and also affects library model blocks. See the local block-shape guide for details. The public element-based ModelShapeCache API added in 26.2 version 0.2.1 is not included in this port.

## Build and install

Run the bundled wrapper from this directory:

```powershell
.\gradlew.bat build
.\gradlew.bat :fabric:runClient
.\gradlew.bat :forge:runClient
```

Installable packages are in [releases/0.2.0](../releases/0.2.0), with [SHA-256 checksums](../releases/0.2.0/SHA256SUMS.txt). Build outputs are under each loader's build/libs directory. Fabric requires Fabric API and Forge Config API Port; GeckoLib is optional.

Build and startup success do not prove feature correctness. World-entry startup checks and [manual feature acceptance](测试清单.md) are separate; multiplayer and performance require their own validation.

## Acknowledgements and license

See the [project acknowledgements](../../README.md#acknowledgements) for Eden Realm, Mob Battle, Spark-Core, GeckoLib and the loader/configuration infrastructure. License: [CC0-1.0](LICENSE).
