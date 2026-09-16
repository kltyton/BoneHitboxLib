package com.kltyton.bonehitboxlib.mixin.entity.player;

import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;
import com.kltyton.bonehitboxlib.example.testing.feedback.ObbManualTestFeedback;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;

/**
 * CN: 让玩家也显式接入 BoneHitboxEntity；默认 OBB 开关语义适用于所有 OBB 功能。
 * EN: Opts players into BoneHitboxEntity; the default OBB switch applies to every OBB feature.
 */
@Mixin(Player.class)
public abstract class PlayerBoneHitboxMixin implements BoneHitboxEntity {
    @Override
    public void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar) {
        registrar.register(ObbBoneRegistrar.HUMAN_BASE);
        if (ObbManualTestFeedback.enabled()) {
            // CN: 仅用于人工触发玩家 OBB 碰撞攻击事件；正式运行不会添加该属性。
            // EN: Only enables manual player OBB-contact attack tests; production runs do not add this attribute.
            registrar.register("right_arm").attack();
        }
    }
}
