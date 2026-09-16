package com.kltyton.bonehitboxlib.example.entity.geckolib;

import java.util.List;
import java.util.Locale;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.kltyton.bonehitboxlib.api.data.builtin.key.ObbBuiltinDataKeys;
import com.kltyton.bonehitboxlib.api.data.builtin.health.ObbPartHealth;
import com.kltyton.bonehitboxlib.api.geckolib.skill.context.GeoKeyframeSkillPhase;
import com.kltyton.bonehitboxlib.api.geckolib.skill.entity.GeoKeyframeSkillEntity;
import com.kltyton.bonehitboxlib.api.geckolib.skill.registration.GeoKeyframeSkillRegistrar;
import com.kltyton.bonehitboxlib.client.compat.geckolib.skill.GeoKeyframeSkillClientBridge;
import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;
import com.kltyton.bonehitboxlib.example.testing.hook.ObbManualTestHookReporter;

import net.minecraft.world.entity.EntityType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * CN: 内置 GeckoLib 模型测试实体，仅在 GeckoLib 存在时注册。
 * EN: Built-in GeckoLib model test entity, registered only when GeckoLib is present.
 */
public class GeckoObbTestEntity extends PathfinderMob implements GeoEntity,
        GeoKeyframeSkillEntity, ObbManualTestHookReporter {
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public GeckoObbTestEntity(EntityType<? extends GeckoObbTestEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<GeckoObbTestEntity> controller = new AnimationController<GeckoObbTestEntity>("walk", 5, state -> state.setAndContinue(WALK))
                .setCustomInstructionKeyframeHandler(event -> {
                    bonehitboxlib$geoHandleInstruction(event.keyframeData().getInstructions());
                    GeoKeyframeSkillClientBridge.forward(event);
                });
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar) {
        registrar.register(ObbBoneRegistrar.ALL)
                .dataFactory(
                        ObbBoneRegistrar.ALL,
                        ObbBuiltinDataKeys.PART_HEALTH,
                        () -> new ObbPartHealth(40.0F, 40.0F));
    }

    @Override
    public void bonehitboxlib$registerGeoKeyframeSkills(GeoKeyframeSkillRegistrar registrar) {
        registrar.window("test_attack_window", "runAttack", "clearAttack", context -> {
            if (context.phase() == GeoKeyframeSkillPhase.BEGIN || context.phase() == GeoKeyframeSkillPhase.END) {
/*                context.reporter().sendSystemMessage(Component.literal(
                        "[BoneHitboxLib] GeckoLib关键帧技能" + context.phase()
                                + "功能正常，服务端OBB数量=" + context.parts().size()));*/
            }
        });
    }

    /**
     * CN: 测试模型动画里的自定义关键帧指令示例；用户项目可以在自己的关键帧 handler 中调用同样的临时攻击盒 API。
     * EN: Example custom-keyframe instruction mapping for the test model; user projects can call the same temporary attack-box API from their own keyframe handler.
     */
    protected void bonehitboxlib$geoHandleInstruction(String instruction) {
        String normalized = instruction == null ? "" : instruction.toLowerCase(Locale.ROOT);
        if (normalized.contains("runmaxattack")) {
            bonehitboxlib$geoSetTemporaryAttackGroups(List.of("right-arm2", "left-arm2", "right_muzzle", "left_muzzle", "pao", "pao2"));
        } else if (normalized.contains("runsuperattack")) {
            bonehitboxlib$geoSetTemporaryAttackGroups(List.of("right_muzzle", "left_muzzle", "pao", "pao2"));
        } else if (normalized.contains("runminiattack")) {
            bonehitboxlib$geoSetTemporaryAttackGroups(List.of("right-arm2"));
        } else if (normalized.contains("runattack")) {
            bonehitboxlib$geoSetTemporaryAttackGroups(List.of("right-arm2", "left-arm2"));
        } else if (normalized.contains("clearattack")) {
            bonehitboxlib$geoClearTemporaryAttackGroups();
        }
    }
}
