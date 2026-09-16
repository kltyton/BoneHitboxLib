# 版本与发行包

[English](README.md) | [简体中文](README.zh-CN.md)

每个源码目录都是独立 Gradle 工程，根工程对应 Minecraft 26.2。按 **Minecraft 版本和加载器**选择发行包，并核对 SHA-256 校验值。

| Minecraft | 库版本 | 目标 Java | 原生加载器 | Fabric API | Forge Config API Port | 可选 GeckoLib |
| --- | --- | --- | --- | --- | --- | --- |
| [26.2](../README.zh-CN.md) | 0.2.1 | 25 | NeoForge 26.2.0.6-beta | 0.152.1+26.2 | 26.2.1 | 5.5.3 |
| [26.1.2](BoneHitboxLib-26.1.2/README.zh-CN.md) | 0.2.0 | 25 | NeoForge 26.1.2.95 | 0.155.2+26.1.2 | 26.1.5 | 5.5.2 |
| [1.21.1](BoneHitboxLib-1.21.1/README.zh-CN.md) | 0.2.0 | 21 | NeoForge 21.1.248 | 0.116.15+1.21.1 | 21.1.6 | 4.9.2 |
| [1.20.1](BoneHitboxLib-1.20.1/README.zh-CN.md) | 0.2.0 | 17 | Forge 47.4.22 | 0.92.11+1.20.1 | 8.0.3 | 4.8.4 |

Fabric Loader 均锁定为 0.19.3。Fabric 需要对应版本的 Fabric API 与 Forge Config API Port。GeckoLib 为可选依赖，使用其模型或关键帧技能时安装。Forge 1.20.1 包已内嵌 MixinExtras。

## 构建产物

安装普通发行 JAR，不要安装 `-sources.jar` 或 `-javadoc.jar`。0.2.1 公开了 26.2 的 `ModelShapeCache` 元素解析 API，此项尚未回移。四个工程均包含方块白名单、黑名单及默认关闭的强制 OBB 选项。

## 构建

进入目标工程后执行随附的 Gradle Wrapper：

```powershell
cd .\versions\BoneHitboxLib-1.20.1
.\gradlew.bat build
.\gradlew.bat :fabric:runClient
.\gradlew.bat :forge:runClient
```

其他工程使用 `:neoforge:runClient`。`-Pwithout_geckolib` 可在开发启动时不加载可选 GeckoLib 运行库。Gradle Wrapper 9.6.1 可由 Java 25 启动，各工程分别声明目标工具链。可安装产物位于 `fabric/build/libs/` 和 `forge/build/libs/` 或 `neoforge/build/libs/`。

## 接入与验证

移植版本保留模型 OBB、选择与高亮、手持物、服务端同步与持久化、关键帧技能和模型多格方块。渲染、网络、NBT、GeckoLib 调用适配各自目标 API。模型、动画和贴图仍作为普通运行时资源维护；原版方块几何从各自锁定的 Minecraft 版本生成。


接入示例、配置和鸣谢见[主 README](../README.zh-CN.md)。
