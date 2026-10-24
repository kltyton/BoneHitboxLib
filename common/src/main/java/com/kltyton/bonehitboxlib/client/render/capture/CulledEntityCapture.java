package com.kltyton.bonehitboxlib.client.render.capture;

import java.util.IdentityHashMap;
import java.util.Map;

import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.client.selection.model.VisualPartId;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Samples nearby culled models without sending their deferred draw tasks to the renderer. */
public final class CulledEntityCapture {
    private static final double CAPTURE_RANGE_SQR = 32.0 * 32.0;
    private static final Map<Entity, AABB> SAMPLED_BOUNDS = new IdentityHashMap<>();
    private static final ThreadLocal<Capture> ACTIVE = new ThreadLocal<>();
    private static ClientLevel lastLevel;
    private static long lastGameTime = Long.MIN_VALUE;

    private CulledEntityCapture() { }

    public static AABB extendCullingBounds(AABB original, Entity entity, Frustum frustum,
            double cameraX, double cameraY, double cameraZ) {
        if (!(entity instanceof BoneHitboxEntity)
                || entity.distanceToSqr(cameraX, cameraY, cameraZ) > CAPTURE_RANGE_SQR
                || original.hasNaN() || original.getSize() == 0.0 || frustum.isVisible(original)) {
            return original;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return original;
        }
        long gameTime = level.getGameTime();
        if (level != lastLevel || gameTime != lastGameTime) {
            SAMPLED_BOUNDS.clear();
            lastLevel = level;
            lastGameTime = gameTime;
        }
        if (!SAMPLED_BOUNDS.containsKey(entity)) {
            SAMPLED_BOUNDS.put(entity, sample(minecraft, entity, new Vec3(cameraX, cameraY, cameraZ)));
        }
        AABB modelBounds = SAMPLED_BOUNDS.get(entity);
        return modelBounds == null ? original : original.minmax(modelBounds.inflate(0.5));
    }

    public static void record(VisualPartId part, AABB renderBounds) {
        Capture capture = ACTIVE.get();
        if (capture != null && capture.owner.equals(part.ownerKey())) {
            capture.bounds = capture.bounds == null ? renderBounds : capture.bounds.minmax(renderBounds);
        }
    }

    public static void clear() {
        SAMPLED_BOUNDS.clear();
        ACTIVE.remove();
        lastLevel = null;
        lastGameTime = Long.MIN_VALUE;
    }

    private static AABB sample(Minecraft minecraft, Entity entity, Vec3 camera) {
        var dispatcher = minecraft.getEntityRenderDispatcher();
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var state = dispatcher.extractEntity(entity, partialTick);
        Capture previous = ACTIVE.get();
        Capture capture = new Capture(VisualPartId.ownerKey(entity));
        ACTIVE.set(capture);
        try {
            // Native collectors defer drawing; model/layer callbacks still produce the real collision geometry.
            dispatcher.submit(state, minecraft.gameRenderer.gameRenderState().levelRenderState.cameraRenderState,
                    state.x - camera.x, state.y - camera.y, state.z - camera.z,
                    new PoseStack(), new SubmitNodeStorage());
            return capture.bounds == null ? null : capture.bounds.move(camera);
        } finally {
            if (previous == null) { ACTIVE.remove(); } else { ACTIVE.set(previous); }
        }
    }

    private static final class Capture {
        private final String owner;
        private AABB bounds;

        private Capture(String owner) { this.owner = owner; }
    }
}
