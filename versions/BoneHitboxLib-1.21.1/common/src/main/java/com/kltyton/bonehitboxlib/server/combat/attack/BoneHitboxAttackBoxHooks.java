package com.kltyton.bonehitboxlib.server.combat.attack;

import com.kltyton.bonehitboxlib.api.context.attack.ObbAttackContext;
import com.kltyton.bonehitboxlib.api.context.damage.ObbDamageInfo;
import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.event.BoneHitboxEvents;
import com.kltyton.bonehitboxlib.api.state.runtime.ObbBoneState;
import com.kltyton.bonehitboxlib.server.sync.snapshot.ServerObbStore;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * CN: 将原版已发生的攻击关联到客户端提供的攻击骨骼/受击骨骼，并派发 OBB 扩展回调。
 * EN: Correlates an already-occurring vanilla attack with client-provided attack/hurt bones and dispatches OBB extension callbacks.
 */
public final class BoneHitboxAttackBoxHooks {
    private BoneHitboxAttackBoxHooks() {
    }

    public static void handleVanillaAttack(Entity attacker, Entity target, ObbBoneState attackBone, ObbBoneState hurtBone,
            ObbDamageInfo damage) {
        if (!(target instanceof BoneHitboxEntity targetHitbox)) {
            return;
        }
        BoneHitboxEntity attackerHitbox = attacker instanceof BoneHitboxEntity value ? value : null;
        ObbAttackContext context = ObbAttackContext.vanilla(
                attacker instanceof Player player ? player : null,
                attacker,
                target,
                attackBone,
                hurtBone,
                damage,
                ServerObbStore.animationState(attacker),
                ServerObbStore.animationState(target));

        if (attackerHitbox != null && !attackerHitbox.bonehitboxlib$beforeAttackBoxAttack(context)) {
            return;
        }
        if (attackerHitbox != null) {
            attackerHitbox.bonehitboxlib$onAttackBoxAttack(context);
        }
        BoneHitboxEvents.fireEntityAttackBoxAttack(context);
        if (attacker instanceof Player) {
            BoneHitboxEvents.firePlayerPartAttack(context);
        }

        if (!context.damage().successful()) {
            if (attackerHitbox != null) {
                attackerHitbox.bonehitboxlib$afterAttackBoxAttack(context);
            }
            return;
        }

        if (!targetHitbox.bonehitboxlib$beforeHurtBoxHurt(context)) {
            if (attackerHitbox != null) {
                attackerHitbox.bonehitboxlib$afterAttackBoxAttack(context);
            }
            return;
        }
        targetHitbox.bonehitboxlib$onHurtBoxHurt(context);
        if (target instanceof Player) {
            BoneHitboxEvents.firePlayerPartHurt(context);
        }
        BoneHitboxEvents.fireEntityHurtBoxHurt(context);
        targetHitbox.bonehitboxlib$afterHurtBoxHurt(context);
        if (attackerHitbox != null) {
            attackerHitbox.bonehitboxlib$afterAttackBoxAttack(context);
        }
    }
}
