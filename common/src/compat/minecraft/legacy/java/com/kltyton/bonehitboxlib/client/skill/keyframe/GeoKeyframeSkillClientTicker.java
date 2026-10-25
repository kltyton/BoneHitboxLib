package com.kltyton.bonehitboxlib.client.skill.keyframe;

import java.util.HashSet;
import java.util.Set;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.api.geckolib.skill.entity.GeoKeyframeSkillEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * CN: 在客户端逻辑 tick 中推进已追踪技能实体的 GeckoLib 控制器，不提交任何实际绘制。
 * EN: Advances tracked skill entities' GeckoLib controllers on client logic ticks without submitting a draw.
 */
public final class GeoKeyframeSkillClientTicker {
    private static final Set<EntityType<?>> WARNED_ENTITY_TYPES = new HashSet<>();

    private static ClientLevel lastLevel;
    private static long lastGameTime = Long.MIN_VALUE;

    private GeoKeyframeSkillClientTicker() {
    }

    /**
     * CN: 以 partial tick 1.0 推进已追踪实体的 GeckoLib 4 控制器和 marker，不提交绘制。
     * EN: Advances tracked GeckoLib 4 controllers and markers at partial tick 1.0 without drawing.
     */
    public static void tick(Minecraft minecraft) {
        if (!com.kltyton.bonehitboxlib.compat.geckolib.GeckoLibCompat.isLoaded()) {
            clear();
            return;
        }
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }

        long gameTime = level.getGameTime();
        if (level == lastLevel && gameTime == lastGameTime) {
            return;
        }
        if (level != lastLevel) {
            WARNED_ENTITY_TYPES.clear();
            lastLevel = level;
        }
        lastGameTime = gameTime;

        for (Entity entity : level.entitiesForRendering()) {
            if (entity.isRemoved() || !(entity instanceof com.kltyton.bonehitboxlib.api.geckolib.entity.GeoBoneHitboxEntity)) {
                continue;
            }

            try {
                com.kltyton.bonehitboxlib.client.compat.geckolib.skill.GeoAnimationTicker.tick(minecraft, entity);
                var states = com.kltyton.bonehitboxlib.client.compat.geckolib.skill.GeoAnimationSnapshots.capture(entity);
                com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient.recordAnimations(entity.getId(), states);
                ((com.kltyton.bonehitboxlib.api.geckolib.entity.GeoBoneHitboxEntity) entity).bonehitboxlib$geoUpdateAnimationStates(states);
            } catch (RuntimeException exception) {
                if (WARNED_ENTITY_TYPES.add(entity.getType())) {
                    Constants.LOG.warn(
                            "Unable to tick off-screen GeckoLib keyframes for entity type {}; BoneHitboxLib will retry on later client ticks.",
                            entity.getType(), exception);
                }
            }
        }
    }

    public static void clear() {
        WARNED_ENTITY_TYPES.clear();
        lastLevel = null;
        lastGameTime = Long.MIN_VALUE;
    }
}
