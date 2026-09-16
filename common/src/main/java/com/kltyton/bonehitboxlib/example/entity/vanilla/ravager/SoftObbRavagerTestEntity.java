package com.kltyton.bonehitboxlib.example.entity.vanilla.ravager;

import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * CN: Ravager 模型软 OBB 碰撞测试实体。
 * EN: Ravager-model soft OBB collision test entity.
 */
public class SoftObbRavagerTestEntity extends ObbRavagerTestEntity {
    public SoftObbRavagerTestEntity(EntityType<? extends SoftObbRavagerTestEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar) {
        registrar.register(ObbBoneRegistrar.ALL).collision(ObbCollisionMode.SOFT);
    }
}
