package com.kltyton.bonehitboxlib.server.combat.attack;

import com.kltyton.bonehitboxlib.api.context.attack.ObbAttackContext;
import com.kltyton.bonehitboxlib.api.context.collision.ObbCollisionContext;
import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.event.BoneHitboxEvents;

import net.minecraft.world.entity.player.Player;

/**
 * CN: 派发纯 OBB 接触产生的攻击/受击事件；本路径不会调用原版 hurt。
 * EN: Dispatches attack/hurt events produced solely by OBB contact; this path never calls vanilla hurt.
 */
public final class BoneHitboxContactAttackHooks {
    private BoneHitboxContactAttackHooks() {
    }

    public static void handleContact(ObbCollisionContext collision) {
        ObbAttackContext context = ObbAttackContext.obbContact(collision);
        if (collision.firstEntity() instanceof BoneHitboxEntity attacker
                && attacker.bonehitboxlib$beforeObbCollisionAttack(context)) {
            attacker.bonehitboxlib$onObbCollisionAttack(context);
            BoneHitboxEvents.fireEntityObbCollisionAttack(context);
            if (collision.firstEntity() instanceof Player) {
                BoneHitboxEvents.firePlayerObbCollisionAttack(context);
            }
            attacker.bonehitboxlib$afterObbCollisionAttack(context);
        }

        if (collision.secondEntity() instanceof BoneHitboxEntity target
                && target.bonehitboxlib$beforeObbCollisionHurt(context)) {
            target.bonehitboxlib$onObbCollisionHurt(context);
            BoneHitboxEvents.fireEntityObbCollisionHurt(context);
            if (collision.secondEntity() instanceof Player) {
                BoneHitboxEvents.firePlayerObbCollisionHurt(context);
            }
            target.bonehitboxlib$afterObbCollisionHurt(context);
        }
    }
}
