package com.kltyton.bonehitboxlib.example.entity.vanilla.zombie;

import com.kltyton.bonehitboxlib.api.data.builtin.key.ObbBuiltinDataKeys;
import com.kltyton.bonehitboxlib.api.data.builtin.health.ObbPartHealth;
import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;
import com.kltyton.bonehitboxlib.example.testing.hook.ObbManualTestHookReporter;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

/**
 * CN: 项目内注册的 Zombie 行为测试实体，用于验证原版 ModelPart OBB。
 * EN: Project-owned Zombie-behavior test entity for validating vanilla ModelPart OBBs.
 */
public class ObbZombieTestEntity extends Zombie implements ObbManualTestHookReporter {
    public ObbZombieTestEntity(EntityType<? extends ObbZombieTestEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes();
    }

    @Override
    public void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar) {
        registrar.register(ObbBoneRegistrar.BASE)
                .dataFactory(
                        ObbBoneRegistrar.ALL,
                        ObbBuiltinDataKeys.PART_HEALTH,
                        () -> new ObbPartHealth(2.0F, 2.0F));
        registrar.register("right_arm").attack();
    }
}
