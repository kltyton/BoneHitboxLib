package com.kltyton.bonehitboxlib.mixin.entity.projectile;

import com.kltyton.bonehitboxlib.api.bone.builtin.ObbBuiltinBones;
import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;

import net.minecraft.world.entity.projectile.Projectile;

import org.spongepowered.asm.mixin.Mixin;

/**
 * CN: 为原版投射物注册一个攻击 OBB；客户端以投射物当前 AABB 作为缺少视觉 cube 时的 OBB 回退。
 * EN: Registers one attack OBB for vanilla projectiles; the client uses the current projectile AABB when no visual cube is available.
 */
@Mixin(Projectile.class)
public abstract class ProjectileBoneHitboxMixin implements BoneHitboxEntity {
    @Override
    public void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar) {
        registrar.register(ObbBuiltinBones.projectile((Projectile) (Object) this)).attack();
    }
}
