package com.kltyton.bonehitboxlib.event;

import com.kltyton.bonehitboxlib.server.hook.vanilla.BoneHitboxServerHooks;
import com.kltyton.bonehitboxlib.server.sync.contact.ServerObbContactStore;
import com.kltyton.bonehitboxlib.server.skill.keyframe.GeoKeyframeSkillDispatcher;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;

/**
 * CN: Fabric 玩法事件桥。
 * EN: Fabric gameplay event bridge.
 */
public final class BoneHitboxLibFabricEvents {
    private BoneHitboxLibFabricEvents() {
    }

    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(
                server -> com.kltyton.bonehitboxlib.server.sync.ObbServerLifecycle.clear());
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            com.kltyton.bonehitboxlib.server.sync.ObbServerLifecycle.tick(server);
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                level.isClientSide() ? InteractionResult.PASS : BoneHitboxServerHooks.handlePlayerInteract(player, hand, entity));
    }
}
