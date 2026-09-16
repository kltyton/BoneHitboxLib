# Package Layout and Migration / 包结构与迁移

The source tree is grouped by responsibility. Parent packages are namespaces only; implementation classes live in focused leaf packages. The only intentional root classes are mod entrypoints and shared constants. / 源码按职责分类；父包只作为命名空间，具体类位于职责明确的叶子包。根包仅保留 Mod 入口和共享常量。

## Public API / 公共 API

- `api.block.shape`, `api.block.shape.geometry`: packaged model extraction, AABB/OBB modes, automatic multicell blocks and convex shape operations / 打包模型提取、AABB/OBB 选择、自动多格方块与凸几何运算。
- `api.block.data`: automatic multipart blockstate and item-model data provider / 自动 multipart blockstate 与物品模型资源生成器。
- `api.bone.attribute`: composable OBB attributes / 可组合 OBB 属性。
- `api.bone.builtin`: built-in model-bone keys / 内置模型骨骼键。
- `api.bone.collision`: collision response policies / 碰撞响应策略。
- `api.bone.key`: stable bone identity / 稳定骨骼标识。
- `api.context.attack`: attack context and origin / 攻击上下文与来源。
- `api.context.collision`: contact context, phase, and kind / 接触上下文、阶段与类别。
- `api.context.damage`: captured vanilla damage data / 捕获的原版伤害数据。
- `api.context.interaction`: part interaction context / 部位交互上下文。
- `api.data.key`: extension-data key and registry infrastructure / 扩展数据键与注册基础设施。
- `api.data.builtin.key`: built-in data keys / 内置数据键。
- `api.data.builtin.health`: built-in per-part health value / 内置逐部位生命值。
- `api.registration.registrar`: fluent registration entrypoints / 链式注册入口。
- `api.registration.selector`: `ALL`, `BASE`, `HUMAN_BASE`, exact, and named selectors / `ALL`、`BASE`、`HUMAN_BASE`、精确与名称选择器。
- `api.registration.definition`: frozen registration definitions / 冻结后的注册定义。
- `api.registration.data`: selector-scoped data definitions / 选择器范围数据定义。
- `api.state.runtime`: tightly coupled entity/bone state and persistence / 紧耦合的实体、骨骼状态与持久化。
- `api.geckolib.entity`: Gecko-independent entity contract and its internal runtime table / 不直接依赖 GeckoLib 类的实体契约及内部运行时表。
- `api.geckolib.state`: public animation snapshot value / 公共动画快照值。
- `api.geckolib.skill.entity|context|handler|registration|event`: optional-Gecko keyframe skill contract, lifecycle context, handlers, registrations, and global events / 可选 Gecko 关键帧技能契约、生命周期上下文、处理器、注册与全局事件。
- `api.geometry.snapshot|query`: immutable server-readable entity/part snapshots and query entrypoints / 服务端可读的不可变实体/部位快照与查询入口。
- `api.entity`, `api.event`: opt-in entity contract and loader-neutral events / 实体接入契约与 loader 无关事件。

## Runtime / 运行时

- `geometry.obb`: OBB transforms, discrete SAT, surface queries, and continuous sweep SAT / OBB 变换、离散 SAT、表面查询与连续 sweep SAT。
- `client.geometry.bounds`: captured visual-cube bounds / 捕获的视觉 cube 边界。
- `client.selection.model`, `client.selection.service`: selection values and orchestration / 选择数据与编排服务。
- `client.render.item`: hand/item-layer capture and first-person world-held pose extraction / 手臂、物品层捕获与第一人称世界持物姿态提取。
- `client.render.outline`, `client.render.vanilla`, `client.render.example.vanilla`: render responsibilities / 轮廓、原版模型层与示例渲染。
- `client.compat.geckolib.layer|model|renderer`: optional GeckoLib render integration / 可选 GeckoLib 渲染层、模型与渲染器。
- `client.compat.geckolib.skill`, `client.skill.keyframe`: optional GeckoLib marker extraction, optional logical animation ticker, and deferred send queue / 可选 GeckoLib marker 提取、可选逻辑动画 ticker 与延迟发送队列。
- `network.payload.contact|entity|selection|skill`, `network.protocol`: payloads separated from transport wiring / 按用途拆分的数据包与传输桥。
- `server.combat.attack|damage`, `server.collision.contact|solver`, `server.hook.vanilla`: server hooks and the persistent-manifold impulse solver by gameplay responsibility / 按战斗、伤害、接触、冲量求解和原版挂钩职责拆分。
- `server.sync.contact|snapshot`, `server.network`, `server.selection`: contact deduplication, model snapshots, transport, and selected-part state / 接触去重、模型快照、网络与选择状态。
- `server.skill.keyframe`: marker deduplication, point/window dispatch, and server-tick lifecycle / marker 去重、单点/窗口分发与服务端 tick 生命周期。
- `mixin.block`: model shapes, exact compound intersections and native entity movement integration / 模型形状、凸几何相交与原版实体移动接入。
- `mixin.combat`, `mixin.combat.projectile`, `mixin.entity`, `mixin.entity.player|projectile`: common mixins by target behavior / 按目标行为拆分的公共 Mixin。
- `mixin.client.collision`, `mixin.client.render`: client movement and rendering hooks / 客户端移动与渲染挂钩。
- `example.entity.vanilla.ravager|zombie`, `example.entity.geckolib`: test entities kept outside library runtime packages / 与库运行时代码隔离的测试实体。
- Fabric uses `event`, `network`, and `registry`; NeoForge uses `registry`. Loader root packages contain only their entrypoint. / Fabric 使用 `event`、`network`、`registry`，NeoForge 使用 `registry`；loader 根包仅保留入口类。

## Breaking Package Migration / 破坏性包迁移

The package migration changes Java imports. Registry and persisted-data IDs remain unchanged. The current controller-list extension uses entity snapshot payload `bonehitboxlib:entity_parts_v3` and NeoForge transport version `3`; server and clients need matching builds. / 包迁移会改变 Java import；注册 ID 和持久化数据 ID 不变。本次多控制器扩展使用 `bonehitboxlib:entity_parts_v3` 和 NeoForge 传输版本 `3`，两端需使用匹配构建。

| Previous package / 旧包 | New package family / 新包族 |
| --- | --- |
| `api.bone` | `api.bone.attribute|builtin|collision|key` |
| `api.context` | `api.context.attack|collision|damage|interaction` |
| `api.registration` | `api.registration.registrar|selector|definition|data` |
| `api.state` | `api.state.runtime` |
| `api.geckolib` | `api.geckolib.entity|state|skill.*` |
| `client.hitbox`, `client.selection`, `client.render` | `client.geometry.bounds`, `client.selection.model|service`, focused render packages |
| `network` | `network.protocol`, `network.payload.contact|entity|selection|skill` |
| `server`, `server.sync` | focused `server.combat|collision|hook|network|selection|sync` packages |
| `entity`, `entity.geckolib` | `example.entity.vanilla.*`, `example.entity.geckolib` |
| root/common client mixin packages | focused `mixin.combat|entity|client` packages |
