package com.kltyton.bonehitboxlib.client.compat.geckolib.layer;

import java.util.function.BiConsumer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.kltyton.bonehitboxlib.api.geckolib.entity.GeoBoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.geckolib.state.GeoObbAnimationState;
import com.kltyton.bonehitboxlib.client.geometry.bounds.PartBounds;
import com.kltyton.bonehitboxlib.client.render.outline.PartOutlineRenderer;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient;
import com.kltyton.bonehitboxlib.client.selection.model.VisualPartId;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.Entity;

/**
 * CN: 使用 GeckoLib 当前渲染姿态捕获并高亮 cuboid bone。
 * EN: Captures and highlights GeckoLib cuboid bones using their current render pose.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class GeckoBoneSelectionLayer extends GeoRenderLayer {

    public GeckoBoneSelectionLayer(GeoRenderer renderer) {
        super(renderer);
    }

    @Override
    public void addPerBoneRender(RenderPassInfo renderPassInfo, BiConsumer consumer) {
        Object instance = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ANIMATABLE_INSTANCE_ID, -1L);
        if (instance instanceof Number number) {
            Entity entity = entityById(number.longValue());
            if (entity instanceof GeoBoneHitboxEntity geo) {
                var states = com.kltyton.bonehitboxlib.client.compat.geckolib.skill.GeoAnimationSnapshots.capture(renderPassInfo);
                geo.bonehitboxlib$geoUpdateAnimationStates(states);
                BonePartSelectionClient.recordAnimations(entity.getId(), states);
            }
        }
        for (GeoBone bone : renderPassInfo.model().boneLookup().get().values()) {
            consumer.accept(bone, (PerBoneRender) this::captureBone);
        }
    }

    private void captureBone(RenderPassInfo renderPassInfo, GeoBone bone, SubmitNodeCollector renderTasks) {
        if (!(bone instanceof CuboidGeoBone cuboidBone) || cuboidBone.cubes.length == 0) {
            return;
        }

        Object instanceId = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ANIMATABLE_INSTANCE_ID, -1L);
        long entityId = instanceId instanceof Number number ? number.longValue() : -1L;
        if (entityId < 0) {
            return;
        }

        GeoObbAnimationState animationState = animationState(renderPassInfo);
        PoseStack poseStack = renderPassInfo.poseStack();
        poseStack.pushPose();
        bone.translateAwayFromPivotPoint(poseStack);
        for (int i = 0; i < cuboidBone.cubes.length; i++) {
            GeoCube cube = cuboidBone.cubes[i];
            poseStack.pushPose();
            cube.translateToPivotPoint(poseStack);
            cube.rotate(poseStack);
            cube.translateAwayFromPivotPoint(poseStack);

            PartBounds bounds = fromGeckoCube(poseStack.last().pose(), cube);
            if (bounds != null) {
                VisualPartId id = VisualPartId.gecko(entityId, bone.name(), i);
                boolean accepted = BonePartSelectionClient.record(id, bounds, animationState);
                if (!accepted) {
                    poseStack.popPose();
                    continue;
                }
                if (BonePartSelectionClient.shouldRenderOutline(id)) {
                    PartOutlineRenderer.submit(renderTasks, poseStack, bounds);
                }
                if (BonePartSelectionClient.isDamageFlashing(id)) {
                    PartOutlineRenderer.submitDamageFlash(renderTasks, poseStack, bounds);
                }
            }

            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static PartBounds fromGeckoCube(org.joml.Matrix4f pose, GeoCube cube) {
        PartBounds.BoundsBuilder builder = PartBounds.builder(pose);
        GeoQuad[] quads = cube.quads();
        if (quads == null) {
            return null;
        }

        for (GeoQuad quad : quads) {
            if (quad == null) {
                continue;
            }

            for (GeoVertex vertex : quad.vertices()) {
                builder.include(vertex.posX(), vertex.posY(), vertex.posZ());
            }
        }
        return builder.build();
    }

    private static GeoObbAnimationState animationState(RenderPassInfo renderPassInfo) {
        Object rawStates = renderPassInfo.getGeckolibData(DataTickets.ANIMATION_CONTROLLER_STATES);
        if (!(rawStates instanceof ControllerState[] states)) {
            return GeoObbAnimationState.NONE;
        }
        if (states == null || states.length == 0) {
            return GeoObbAnimationState.NONE;
        }

        Object rawManager = renderPassInfo.getGeckolibData(DataTickets.ANIMATABLE_MANAGER);
        AnimatableManager<? extends GeoAnimatable> manager = rawManager instanceof AnimatableManager<?> animatableManager
                ? (AnimatableManager<? extends GeoAnimatable>) animatableManager
                : null;
        for (ControllerState state : states) {
            if (state != null && state.animationPoint() != null) {
                AnimationController<?> controller = controllerForState(manager, state);
                String controllerName = controller == null ? "" : controller.getName();
                boolean triggered = controller != null && controller.isPlayingTriggeredAnimation();
                boolean transitioning = controller != null && controller.isTransitioning();
                boolean finished = controller != null && controller.hasAnimationFinished();
                double timelineTime = controller == null ? 0.0 : controller.getCurrentTimelineTime();
                return new GeoObbAnimationState(
                        controllerName,
                        state.animationPoint().animation().name(),
                        state.animationPoint().animTime(),
                        timelineTime,
                        triggered,
                        transitioning,
                        finished);
            }
        }
        return GeoObbAnimationState.NONE;
    }

    private static AnimationController<?> controllerForState(AnimatableManager<? extends GeoAnimatable> manager, ControllerState state) {
        if (manager == null) {
            return null;
        }

        for (AnimationController<?> controller : manager.getAnimationControllers().values()) {
            if (controller.getCurrentAnimationPoint() == state.animationPoint()) {
                return controller;
            }
        }
        return manager.getAnimationControllers().values().stream().findFirst().orElse(null);
    }

    private static Entity entityById(long entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? null : minecraft.level.getEntity((int) entityId);
    }
}
