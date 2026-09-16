package com.kltyton.bonehitboxlib.server.collision.contact;

import com.kltyton.bonehitboxlib.api.context.collision.ObbCollisionContext;
import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.event.BoneHitboxEvents;

import net.minecraft.world.entity.player.Player;

/**
 * CN: 派发已经由服务端规范化去重的客户端 OBB 接触；本类不执行几何计算。
 * EN: Dispatches client OBB contacts already canonicalized and deduplicated by the server; this class performs no geometry tests.
 */
public final class BoneHitboxCollisionHooks {
    private BoneHitboxCollisionHooks() {
    }

    public static void handleClientContact(ObbCollisionContext context) {
        if (context.firstEntity() instanceof BoneHitboxEntity first) {
            first.bonehitboxlib$onObbCollision(context);
        }
        if (context.secondEntity() instanceof BoneHitboxEntity second) {
            second.bonehitboxlib$onObbCollision(context.reversed());
        }

        BoneHitboxEvents.fireEntityPartCollision(context);
        if (context.firstEntity() instanceof Player) {
            BoneHitboxEvents.firePlayerPartCollision(context);
        }
        if (context.secondEntity() instanceof Player) {
            BoneHitboxEvents.firePlayerPartCollision(context.reversed());
        }
    }
}
