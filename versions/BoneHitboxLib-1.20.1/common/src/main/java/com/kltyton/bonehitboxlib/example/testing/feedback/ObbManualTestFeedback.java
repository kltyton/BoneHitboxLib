package com.kltyton.bonehitboxlib.example.testing.feedback;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.api.context.attack.ObbAttackContext;
import com.kltyton.bonehitboxlib.api.context.collision.ObbCollisionContext;
import com.kltyton.bonehitboxlib.api.context.collision.ObbCollisionPhase;
import com.kltyton.bonehitboxlib.api.context.interaction.ObbInteractionContext;
import com.kltyton.bonehitboxlib.api.data.builtin.health.ObbPartHealth;
import com.kltyton.bonehitboxlib.api.data.builtin.key.ObbBuiltinDataKeys;
import com.kltyton.bonehitboxlib.api.event.BoneHitboxEvents;
import com.kltyton.bonehitboxlib.api.geckolib.state.GeoObbAnimationState;
import com.kltyton.bonehitboxlib.api.geckolib.skill.context.GeoKeyframeSkillContext;
import com.kltyton.bonehitboxlib.api.geckolib.skill.context.GeoKeyframeSkillPhase;
import com.kltyton.bonehitboxlib.api.geckolib.skill.event.GeoKeyframeSkillEvents;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

/**
 * CN: 开发运行使用的聊天测试反馈。它只观察事件，不改变事件结果、伤害或碰撞。
 * EN: Chat feedback for development runs. It observes events without changing results, damage, or collision.
 */
public final class ObbManualTestFeedback {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final Map<MessageKey, Long> LAST_MESSAGE_TICKS = new ConcurrentHashMap<>();
    private static final boolean ENABLED = SharedConstants.IS_RUNNING_IN_IDE
            || Boolean.getBoolean("bonehitboxlib.manualTestFeedback");
    private static final long DEFAULT_COOLDOWN_TICKS = 5L;
    private static final long STAY_COOLDOWN_TICKS = 100L;

    private ObbManualTestFeedback() {
    }

    /** CN: 测试专用实体注册可据此添加开发属性。EN: Test entity registration may use this to add development-only attributes. */
    public static boolean enabled() {
        return ENABLED;
    }

    /** CN: 注册全部公开事件的测试观察器。EN: Registers test observers for every public event. */
    public static void register() {
        if (!ENABLED || !REGISTERED.compareAndSet(false, true)) {
            return;
        }

        BoneHitboxEvents.registerPlayerPartAttack(context ->
                reportAttack("EVT-01", "玩家主动攻击实体部位事件", context));
        BoneHitboxEvents.registerPlayerPartHurt(context ->
                reportAttack("EVT-02", "玩家被攻击部位事件", context));
        BoneHitboxEvents.registerPlayerPartInteract(context -> {
            reportInteraction("EVT-03", "玩家部位互动事件", context);
            return InteractionResult.PASS;
        });
        BoneHitboxEvents.registerPlayerPartCollision(context ->
                reportCollision("EVT-04", "玩家部位碰撞事件", context));
        BoneHitboxEvents.registerEntityAttackBoxAttack(context ->
                reportAttack("EVT-05", "实体攻击盒攻击事件", context));
        BoneHitboxEvents.registerEntityHurtBoxHurt(context -> {
            reportAttack("EVT-06", "实体受击盒受击事件", context);
            reportPartHealthAfterHurt(context);
        });
        BoneHitboxEvents.registerEntityPartInteract(context -> {
            reportInteraction("EVT-07", "实体部位互动事件", context);
            reportPartHealthRead(context);
            return InteractionResult.PASS;
        });
        BoneHitboxEvents.registerEntityPartCollision(context ->
                reportCollision("EVT-08", "实体部位碰撞事件", context));
        BoneHitboxEvents.registerPlayerObbCollisionAttack(context ->
                reportAttack("EVT-09", "玩家 OBB 碰撞攻击事件", context));
        BoneHitboxEvents.registerPlayerObbCollisionHurt(context ->
                reportAttack("EVT-10", "玩家 OBB 碰撞受击事件", context));
        BoneHitboxEvents.registerEntityObbCollisionAttack(context ->
                reportAttack("EVT-11", "实体 OBB 碰撞攻击事件", context));
        BoneHitboxEvents.registerEntityObbCollisionHurt(context ->
                reportAttack("EVT-12", "实体 OBB 碰撞受击事件", context));
        GeoKeyframeSkillEvents.register(ObbManualTestFeedback::reportGeoKeyframeSkill);

        Constants.LOG.info("[OBB-TEST] Manual chat feedback is enabled.");
    }

    public static void reportAttackHook(String code, String feature, ObbAttackContext context) {
        report(code, "Hook", feature, context.attacker(), context.target(), attackDetails(context),
                cooldown(context.collision().map(ObbCollisionContext::phase).orElse(null)));
    }

    public static void reportInteractionHook(String code, String feature, ObbInteractionContext context) {
        report(code, "Hook", feature, context.player(), context.target(), interactionDetails(context),
                DEFAULT_COOLDOWN_TICKS);
    }

    public static void reportCollisionHook(String code, String feature, ObbCollisionContext context) {
        report(code + "-" + context.phase(), "Hook", feature, context.firstEntity(), context.secondEntity(),
                collisionDetails(context), cooldown(context.phase()));
    }

    private static void reportAttack(String code, String feature, ObbAttackContext context) {
        ObbCollisionPhase phase = context.collision().map(ObbCollisionContext::phase).orElse(null);
        String messageCode = phase == null ? code : code + "-" + phase;
        report(messageCode, "Event", feature, context.attacker(), context.target(), attackDetails(context),
                cooldown(phase));
    }

    private static void reportInteraction(String code, String feature, ObbInteractionContext context) {
        report(code, "Event", feature, context.player(), context.target(), interactionDetails(context),
                DEFAULT_COOLDOWN_TICKS);
    }

    private static void reportCollision(String code, String feature, ObbCollisionContext context) {
        report(code + "-" + context.phase(), "Event", feature, context.firstEntity(), context.secondEntity(),
                collisionDetails(context), cooldown(context.phase()));
    }

    private static void reportPartHealthAfterHurt(ObbAttackContext context) {
        context.hurtBone().getData(ObbBuiltinDataKeys.PART_HEALTH).ifPresent(health ->
                report("DATA-01", "Data", "独立部位生命值扣除", context.attacker(), context.target(),
                        partHealthDetails(context.hurtBone().key().displayName(), health), DEFAULT_COOLDOWN_TICKS));
    }

    private static void reportPartHealthRead(ObbInteractionContext context) {
        context.bone().getData(ObbBuiltinDataKeys.PART_HEALTH).ifPresent(health ->
                report("DATA-02", "Data", "独立部位数据读取", context.player(), context.target(),
                        partHealthDetails(context.bone().key().displayName(), health), DEFAULT_COOLDOWN_TICKS));
    }

    private static void reportGeoKeyframeSkill(GeoKeyframeSkillContext context) {
        if (!ENABLED || !isLibraryTestEntity(context.entity())) {
            return;
        }
        long cooldown = context.phase() == GeoKeyframeSkillPhase.TICK
                ? STAY_COOLDOWN_TICKS
                : DEFAULT_COOLDOWN_TICKS;
        long now = context.serverGameTime();
        String code = "SKILL-01-" + context.phase();
        MessageKey key = new MessageKey(
                context.reporter().getUUID(),
                code + "/" + context.entity().getUUID() + "/" + context.skillId());
        Long previous = LAST_MESSAGE_TICKS.get(key);
        if (previous != null && now >= previous && now - previous < cooldown) {
            return;
        }
        LAST_MESSAGE_TICKS.put(key, now);
        long snapshotAge = context.geometrySnapshot()
                .map(snapshot -> snapshot.ageTicks())
                .orElse(-1L);
        String firstPart = context.parts().isEmpty()
                ? "无"
                : context.parts().get(0).key().displayName();
        context.reporter().sendSystemMessage(Component.literal(
                "[BoneHitboxLib 测试][" + code + "][Skill] GeckoLib关键帧技能上下文功能正常"
                        + " | 实体=" + entityName(context.entity())
                        + " | 技能=" + context.skillId()
                        + " | marker=" + context.marker()
                        + " | 控制器=" + context.animationState().controllerName()
                        + " | 动画=" + context.animationState().animationName()
                        + " | marker时间=" + decimal(context.markerTimeSeconds())
                        + " | 动画时间=" + decimal(context.animationState().animationTimeSeconds())
                        + " | timeline=" + decimal(context.animationState().timelineTimeSeconds())
                        + " | 速度=" + decimal(context.animationSpeed())
                        + " | 序列=" + context.clientSequence()
                        + " | 客户端tick=" + context.observedGameTime()
                        + " | 服务端tick=" + context.serverGameTime()
                        + " | OBB数量=" + context.parts().size()
                        + " | 快照年龄=" + snapshotAge
                        + " | 首部位=" + firstPart));
    }

    private static void report(String code, String category, String feature, Entity first, Entity second,
            String details, long cooldownTicks) {
        if (!ENABLED || !isRelevant(first, second)) {
            return;
        }

        Set<ServerPlayer> recipients = recipients(first, second);
        String pairKey = first.getUUID() + "/" + second.getUUID();
        for (ServerPlayer player : recipients) {
            long now = player.level().getGameTime();
            MessageKey key = new MessageKey(player.getUUID(), code + "/" + pairKey);
            Long previous = LAST_MESSAGE_TICKS.get(key);
            if (previous != null && now >= previous && now - previous < cooldownTicks) {
                continue;
            }
            LAST_MESSAGE_TICKS.put(key, now);
            player.sendSystemMessage(Component.literal(
                    "[BoneHitboxLib 测试][" + code + "][" + category + "] "
                            + feature + "功能正常 | " + details));
        }
    }

    private static boolean isRelevant(Entity first, Entity second) {
        return isLibraryTestEntity(first)
                || isLibraryTestEntity(second)
                || first instanceof ServerPlayer && second instanceof ServerPlayer;
    }

    private static boolean isLibraryTestEntity(Entity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id != null && Constants.MOD_ID.equals(id.getNamespace());
    }

    private static Set<ServerPlayer> recipients(Entity first, Entity second) {
        Set<ServerPlayer> result = new LinkedHashSet<>();
        if (first instanceof ServerPlayer player) {
            result.add(player);
        }
        if (second instanceof ServerPlayer player) {
            result.add(player);
        }
        if (!result.isEmpty()) {
            return result;
        }

        if (first.level() instanceof ServerLevel level) {
            result.addAll(level.players());
        }
        return result;
    }

    private static String attackDetails(ObbAttackContext context) {
        StringBuilder details = new StringBuilder()
                .append("攻击方=").append(entityName(context.attacker()))
                .append(" | 目标=").append(entityName(context.target()))
                .append(" | 攻击骨骼=").append(context.attackBone().key().displayName())
                .append(" | 攻击属性=").append(context.attackBone().attributes())
                .append(" | 受击骨骼=").append(context.hurtBone().key().displayName())
                .append(" | 受击属性=").append(context.hurtBone().attributes())
                .append(" | 来源=").append(context.origin())
                .append(" | 请求伤害=").append(decimal(context.damage().requestedDamage()))
                .append(" | 实际伤害=").append(decimal(context.damageReceived()))
                .append(" | hurt成功=").append(context.damage().successful());
        appendAnimation(details, "攻击动画", context.attackerAnimationState());
        appendAnimation(details, "受击动画", context.targetAnimationState());
        context.collision().ifPresent(collision -> details.append(" | 阶段=").append(collision.phase()));
        return details.toString();
    }

    private static String interactionDetails(ObbInteractionContext context) {
        StringBuilder details = new StringBuilder()
                .append("玩家=").append(entityName(context.player()))
                .append(" | 目标=").append(entityName(context.target()))
                .append(" | 骨骼=").append(context.bone().key().displayName())
                .append(" | 手=").append(context.hand());
        appendAnimation(details, "目标动画", context.targetAnimationState());
        return details.toString();
    }

    private static String collisionDetails(ObbCollisionContext context) {
        StringBuilder details = new StringBuilder()
                .append("第一实体=").append(entityName(context.firstEntity()))
                .append(" | 第一骨骼=").append(context.firstBone().key().displayName())
                .append(" | 第一属性=").append(context.firstBone().attributes())
                .append(" | 第二实体=").append(entityName(context.secondEntity()))
                .append(" | 第二骨骼=").append(context.secondBone().key().displayName())
                .append(" | 第二属性=").append(context.secondBone().attributes())
                .append(" | 模式=").append(context.collisionMode())
                .append(" | 阶段=").append(context.phase())
                .append(" | 穿透深度=").append(decimal(context.penetrationDepth()));
        appendAnimation(details, "第一动画", context.firstAnimationState());
        appendAnimation(details, "第二动画", context.secondAnimationState());
        return details.toString();
    }

    private static String partHealthDetails(String bone, ObbPartHealth health) {
        return "骨骼=" + bone
                + " | 部位生命=" + decimal(health.health()) + "/" + decimal(health.maxHealth())
                + " | 已破坏=" + health.broken();
    }

    private static void appendAnimation(StringBuilder details, String label, GeoObbAnimationState state) {
        if (state.active()) {
            details.append(" | ").append(label).append('=').append(state.animationKey())
                    .append('@').append(decimal(state.animationTimeSeconds())).append('s');
        }
    }

    private static String entityName(Entity entity) {
        return entity.getScoreboardName() + "#" + entity.getId();
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static long cooldown(ObbCollisionPhase phase) {
        return phase == ObbCollisionPhase.STAY ? STAY_COOLDOWN_TICKS : DEFAULT_COOLDOWN_TICKS;
    }

    private record MessageKey(UUID playerId, String featureKey) {
    }
}
