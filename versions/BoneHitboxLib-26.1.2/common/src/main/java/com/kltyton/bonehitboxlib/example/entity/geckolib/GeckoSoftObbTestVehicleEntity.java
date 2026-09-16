package com.kltyton.bonehitboxlib.example.entity.geckolib;

import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.data.builtin.key.ObbBuiltinDataKeys;
import com.kltyton.bonehitboxlib.api.data.builtin.health.ObbPartHealth;
import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * CN: GeckoLib 软 OBB 碰撞测试载具。
 * EN: GeckoLib soft OBB collision test vehicle.
 */
public class GeckoSoftObbTestVehicleEntity extends GeckoObbTestEntity {
    public GeckoSoftObbTestVehicleEntity(EntityType<? extends GeckoSoftObbTestVehicleEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar) {
        registrar.register(ObbBoneRegistrar.ALL)
                .collision(ObbCollisionMode.SOFT)
                .dataFactory(
                        ObbBoneRegistrar.ALL,
                        ObbBuiltinDataKeys.PART_HEALTH,
                        () -> new ObbPartHealth(40.0F, 40.0F));
    }
}
