# BoneHitboxLib 1.21.1

[English](README.md) | [简体中文](README.zh-CN.md)

适用于 Minecraft **1.21.1** 的 **0.2.0** 版本，支持 **Fabric / NeoForge**。本目录是独立 Gradle 工程，客户端、服务端和依赖须按[版本指南](../README.zh-CN.md)匹配。

## 功能与接入

提供实体模型 OBB、部位选择与高亮、战斗和接触事件、逐部位状态、手持物几何、碰撞与承载、服务端姿态查询、可选 GeckoLib 关键帧技能，以及自动模型多格方块。

先阅读[主接入指南](../../README.zh-CN.md)，具体 API 以本工程文档为准：

- [注册、同步与事件](docs/server_sync_api.md)
- [客户端选择与高亮](docs/client_obb_selection.md)
- [模型方块形状与配置](docs/block_shapes.md)
- [包结构](docs/package_layout.md)

服务端配置包含原版斜面方块模式、默认关闭的强制 OBB 选项，以及统一的方块黑白名单。黑名单优先，同样影响本库模型方块；细节见本版本的方块形状文档。26.2 的 0.2.1 新公开的 ModelShapeCache 元素解析 API 尚未回移到本工程。

## 构建与安装

在当前目录执行随附的 Wrapper：

```powershell
.\gradlew.bat build
.\gradlew.bat :fabric:runClient
.\gradlew.bat :neoforge:runClient
```

可安装包位于 [releases/0.2.0](../releases/0.2.0)，附有 [SHA-256 校验值](../releases/0.2.0/SHA256SUMS.txt)。构建产物位于各加载器的 build/libs 目录。Fabric 需要 Fabric API 和 Forge Config API Port；GeckoLib 为可选依赖。

构建和启动成功不代表功能验收通过。进入存档启动检查与[功能人工验收](测试清单.md)分别进行；多人和性能需要单独验证。

## 鸣谢与许可证

Eden Realm、Mob Battle、Spark-Core、GeckoLib 及加载器和配置基础设施的具体贡献见[项目鸣谢](../../README.zh-CN.md#鸣谢)。许可证：[CC0-1.0](LICENSE)。
