# Versions and packages

[English](README.md) | [简体中文](README.zh-CN.md)

Each source directory is an independent Gradle project. The root targets Minecraft 26.2. Choose a package by **Minecraft version and loader**, then check its SHA-256 hash.

| Minecraft | Library | Java target | Native loader | Fabric API | Forge Config API Port | Optional GeckoLib |
| --- | --- | --- | --- | --- | --- | --- |
| [26.2](../README.md) | 0.2.1 | 25 | NeoForge 26.2.0.6-beta | 0.152.1+26.2 | 26.2.1 | 5.5.3 |
| [26.1.2](BoneHitboxLib-26.1.2) | 0.2.0 | 25 | NeoForge 26.1.2.95 | 0.155.2+26.1.2 | 26.1.5 | 5.5.2 |
| [1.21.1](BoneHitboxLib-1.21.1) | 0.2.0 | 21 | NeoForge 21.1.248 | 0.116.15+1.21.1 | 21.1.6 | 4.9.2 |
| [1.20.1](BoneHitboxLib-1.20.1) | 0.2.0 | 17 | Forge 47.4.22 | 0.92.11+1.20.1 | 8.0.3 | 4.8.4 |

Fabric Loader is pinned to 0.19.3 in all projects. Fabric requires the matching Fabric API and Forge Config API Port. GeckoLib is optional; install it for GeckoLib models and keyframe skills. The Forge 1.20.1 package embeds MixinExtras.

## Downloads

- [0.2.1](releases/0.2.1): current 26.2 Fabric and NeoForge packages; [SHA-256 checksums](releases/0.2.1/SHA256SUMS.txt).
- [0.2.0](releases/0.2.0): current packages for the three ports and archived 26.2 packages; [SHA-256 checksums](releases/0.2.0/SHA256SUMS.txt).

Use the installable JAR, not a `-sources.jar` or `-javadoc.jar`. Version 0.2.1 exposes the element-based `ModelShapeCache` API in 26.2; that API change has not been backported. All four projects include the block whitelist, blacklist and default-off forced OBB option.

## Build

Run the bundled Gradle Wrapper from the chosen project:

```powershell
cd .\versions\BoneHitboxLib-1.20.1
.\gradlew.bat build
.\gradlew.bat :fabric:runClient
.\gradlew.bat :forge:runClient
```

Use `:neoforge:runClient` for the other projects. `-Pwithout_geckolib` omits the optional GeckoLib runtime from development startup. Gradle Wrapper 9.6.1 can run on Java 25; each project declares its target toolchain. Installable outputs are under `fabric/build/libs/` and `forge/build/libs/` or `neoforge/build/libs/`.

## Integration and validation

The ports retain model OBBs, selection and highlights, held items, server synchronization and persistence, keyframe skills, and model-derived multi-cell blocks. Rendering, networking, NBT and GeckoLib calls use their respective target APIs. Model, animation and texture resources remain regular runtime assets; vanilla block geometry is generated from each pinned Minecraft version.

Build and package validation do not establish gameplay, multiplayer, visual or performance correctness. `runClient` is only a world-entry/no-crash startup check. Feature acceptance is manual and is specific to the tested version and loader. Each project contains its own API docs and `测试清单.md`.

See the main [README](../README.md) for integration examples, configuration and acknowledgements.
