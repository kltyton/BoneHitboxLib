package com.kltyton.bonehitboxlib.example.entity.vanilla.ravager;

import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;
import com.kltyton.bonehitboxlib.example.testing.hook.ObbManualTestHookReporter;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;

/**
 * CN: 项目内注册的 Ravager 行为测试实体，用于验证大型原版模型 OBB。
 * EN: Project-owned Ravager-behavior test entity for validating large vanilla-model OBBs.
 */
public class ObbRavagerTestEntity extends Ravager implements ObbManualTestHookReporter {
    public ObbRavagerTestEntity(EntityType<? extends ObbRavagerTestEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Ravager.createAttributes();
    }

    @Override
    public void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar) {
        registrar.register(ObbBoneRegistrar.ALL);
    }
}
