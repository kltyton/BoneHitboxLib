package com.kltyton.bonehitboxlib.example.testing.hook;

import com.kltyton.bonehitboxlib.api.context.attack.ObbAttackContext;
import com.kltyton.bonehitboxlib.api.context.collision.ObbCollisionContext;
import com.kltyton.bonehitboxlib.api.context.interaction.ObbInteractionContext;
import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.example.testing.feedback.ObbManualTestFeedback;

import net.minecraft.world.InteractionResult;

/**
 * CN: 内置测试实体使用的 hook 链观察器；所有返回值均保持默认放行语义。
 * EN: Hook-chain observer used by built-in test entities; every return value preserves pass-through semantics.
 */
public interface ObbManualTestHookReporter extends BoneHitboxEntity {
    @Override
    default boolean bonehitboxlib$beforeAttackBoxAttack(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-01A", "实体攻击盒 before hook", context);
        return true;
    }

    @Override
    default void bonehitboxlib$onAttackBoxAttack(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-01B", "实体攻击盒 on hook", context);
    }

    @Override
    default void bonehitboxlib$afterAttackBoxAttack(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-01C", "实体攻击盒 after hook", context);
    }

    @Override
    default boolean bonehitboxlib$beforeHurtBoxHurt(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-02A", "实体受击盒 before hook", context);
        return true;
    }

    @Override
    default void bonehitboxlib$onHurtBoxHurt(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-02B", "实体受击盒 on hook", context);
    }

    @Override
    default void bonehitboxlib$afterHurtBoxHurt(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-02C", "实体受击盒 after hook", context);
    }

    @Override
    default InteractionResult bonehitboxlib$beforeObbInteract(ObbInteractionContext context) {
        ObbManualTestFeedback.reportInteractionHook("HOOK-03A", "实体部位互动 before hook", context);
        return InteractionResult.PASS;
    }

    @Override
    default InteractionResult bonehitboxlib$onObbInteract(ObbInteractionContext context) {
        ObbManualTestFeedback.reportInteractionHook("HOOK-03B", "实体部位互动 on hook", context);
        return InteractionResult.PASS;
    }

    @Override
    default void bonehitboxlib$afterObbInteract(ObbInteractionContext context, InteractionResult result) {
        ObbManualTestFeedback.reportInteractionHook("HOOK-03C", "实体部位互动 after hook", context);
    }

    @Override
    default void bonehitboxlib$onObbCollision(ObbCollisionContext context) {
        ObbManualTestFeedback.reportCollisionHook("HOOK-04", "实体部位碰撞 on hook", context);
    }

    @Override
    default boolean bonehitboxlib$beforeObbCollisionAttack(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-05A", "OBB 碰撞攻击 before hook", context);
        return true;
    }

    @Override
    default void bonehitboxlib$onObbCollisionAttack(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-05B", "OBB 碰撞攻击 on hook", context);
    }

    @Override
    default void bonehitboxlib$afterObbCollisionAttack(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-05C", "OBB 碰撞攻击 after hook", context);
    }

    @Override
    default boolean bonehitboxlib$beforeObbCollisionHurt(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-06A", "OBB 碰撞受击 before hook", context);
        return true;
    }

    @Override
    default void bonehitboxlib$onObbCollisionHurt(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-06B", "OBB 碰撞受击 on hook", context);
    }

    @Override
    default void bonehitboxlib$afterObbCollisionHurt(ObbAttackContext context) {
        ObbManualTestFeedback.reportAttackHook("HOOK-06C", "OBB 碰撞受击 after hook", context);
    }
}
