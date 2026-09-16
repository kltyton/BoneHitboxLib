package com.kltyton.bonehitboxlib.example.entity.vanilla.ravager;

import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.data.builtin.key.ObbBuiltinDataKeys;
import com.kltyton.bonehitboxlib.api.data.builtin.health.ObbPartHealth;
import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * CN: Ravager 模型硬 OBB 碰撞测试实体。
 * EN: Ravager-model hard OBB collision test entity.
 */
public class HardObbRavagerTestEntity extends ObbRavagerTestEntity {
    public HardObbRavagerTestEntity(EntityType<? extends HardObbRavagerTestEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar) {
        registrar.register(ObbBoneRegistrar.ALL)
                .collision(ObbCollisionMode.HARD)
                .dataFactory(
                        ObbBoneRegistrar.BASE,
                        ObbBuiltinDataKeys.PART_HEALTH,
                        () -> new ObbPartHealth(2.0F, 2.0F))
                .data(ObbBuiltinDataKeys.CARRIES_ENTITIES, true);
    }
}
