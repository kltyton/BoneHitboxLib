package com.kltyton.bonehitboxlib.network.payload.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.api.bone.attribute.ObbBoneAttribute;
import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.geckolib.state.GeoObbAnimationState;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;

/**
 * CN: 客户端从真实模型渲染管线提取并同步给服务端的实体 OBB 状态。
 * EN: Entity OBB state extracted from the real client model pipeline and synchronized to the server.
 */
public record ObbEntityPartsPayload(int entityId, GeoObbAnimationState animationState, List<Part> parts,
        List<GeoObbAnimationState> controllerStates) {
    public static final int MAX_PARTS = 384;
    public static final int MAX_CONTROLLERS = 64;
    private static final int MAX_FIELD_LENGTH = 128;

    public static final ResourceLocation TYPE = Constants.id("entity_parts_v3");

    public ObbEntityPartsPayload(int entityId, List<Part> parts) {
        this(entityId, GeoObbAnimationState.NONE, parts);
    }

    public ObbEntityPartsPayload(int entityId, GeoObbAnimationState animationState, List<Part> parts) {
        this(entityId, animationState, parts, animationState != null && animationState.active() ? List.of(animationState) : List.of());
    }

    public ObbEntityPartsPayload {
        animationState = animationState == null ? GeoObbAnimationState.NONE : animationState;
        controllerStates = List.copyOf(controllerStates);
        if (controllerStates.size() > MAX_CONTROLLERS) { throw new IllegalArgumentException("Too many animation controllers"); }
        parts = List.copyOf(parts.size() > MAX_PARTS ? parts.subList(0, MAX_PARTS) : parts);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        writeAnimationState(buffer, animationState);
        buffer.writeVarInt(controllerStates.size());
        for (GeoObbAnimationState state : controllerStates) { writeAnimationState(buffer, state); }
        buffer.writeVarInt(parts.size());
        for (Part part : parts) {
            part.write(buffer);
        }
    }

    public static ObbEntityPartsPayload read(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        GeoObbAnimationState animationState = readAnimationState(buffer);
        int controllerCount = buffer.readVarInt();
        if (controllerCount < 0 || controllerCount > MAX_CONTROLLERS) { throw new IllegalArgumentException("Invalid controller count"); }
        List<GeoObbAnimationState> controllers = new ArrayList<>(controllerCount);
        for (int i = 0; i < controllerCount; i++) { controllers.add(readAnimationState(buffer)); }
        int declaredCount = buffer.readVarInt();
        if (declaredCount < 0 || declaredCount > MAX_PARTS) {
            throw new IllegalArgumentException("Invalid OBB part count: " + declaredCount);
        }
        if (declaredCount == 0) {
            return new ObbEntityPartsPayload(entityId, animationState, List.of(), controllers);
        }
        List<Part> parts = new ArrayList<>(declaredCount);
        for (int i = 0; i < declaredCount; i++) {
            parts.add(Part.read(buffer));
        }
        return new ObbEntityPartsPayload(entityId, animationState, parts, controllers);
    }

    /**
     * CN: 单个 cube/bone group 的服务端 OBB 输入数据。
     * EN: Server OBB input data for one cube or bone group.
     */
    public record Part(String source, String partName, int cubeIndex, AABB localBounds, AABB worldBounds,
            Matrix4f localToWorld, Matrix4f worldToLocal, Set<ObbBoneAttribute> attributes,
            ObbCollisionMode collisionMode) {
        public Part(String source, String partName, int cubeIndex, AABB localBounds, AABB worldBounds,
                Matrix4f localToWorld, Matrix4f worldToLocal) {
            this(source, partName, cubeIndex, localBounds, worldBounds, localToWorld, worldToLocal, Set.of(), ObbCollisionMode.NONE);
        }

        public Part {
            localToWorld = new Matrix4f(localToWorld);
            worldToLocal = new Matrix4f(worldToLocal);
            attributes = attributes == null ? Set.of() : Set.copyOf(attributes);
            collisionMode = collisionMode == null ? ObbCollisionMode.NONE : collisionMode;
        }

        @Override public Matrix4f localToWorld() { return new Matrix4f(localToWorld); }
        @Override public Matrix4f worldToLocal() { return new Matrix4f(worldToLocal); }

        private void write(FriendlyByteBuf buffer) {
            buffer.writeUtf(source, MAX_FIELD_LENGTH);
            buffer.writeUtf(partName, MAX_FIELD_LENGTH);
            buffer.writeVarInt(cubeIndex);
            writeAabb(buffer, localBounds);
            writeAabb(buffer, worldBounds);
            writeMatrix(buffer, localToWorld);
            writeMatrix(buffer, worldToLocal);
            buffer.writeVarInt(ObbBoneAttribute.toMask(attributes));
            buffer.writeVarInt(collisionMode.ordinal());
        }

        private static Part read(FriendlyByteBuf buffer) {
            return new Part(
                    buffer.readUtf(MAX_FIELD_LENGTH),
                    buffer.readUtf(MAX_FIELD_LENGTH),
                    buffer.readVarInt(),
                    readAabb(buffer),
                    readAabb(buffer),
                    readMatrix(buffer),
                    readMatrix(buffer),
                    ObbBoneAttribute.fromMask(buffer.readVarInt()),
                    ObbEntityPartsPayload.collisionMode(buffer.readVarInt()));
        }
    }

    private static void writeAnimationState(FriendlyByteBuf buffer, GeoObbAnimationState state) {
        buffer.writeUtf(state.controllerName(), MAX_FIELD_LENGTH);
        buffer.writeUtf(state.animationName(), MAX_FIELD_LENGTH);
        buffer.writeDouble(state.animationTimeSeconds());
        buffer.writeDouble(state.timelineTimeSeconds());
        buffer.writeBoolean(state.triggered());
        buffer.writeBoolean(state.transitioning());
        buffer.writeBoolean(state.finished());
    }

    private static GeoObbAnimationState readAnimationState(FriendlyByteBuf buffer) {
        return new GeoObbAnimationState(
                buffer.readUtf(MAX_FIELD_LENGTH),
                buffer.readUtf(MAX_FIELD_LENGTH),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean());
    }

    private static ObbCollisionMode collisionMode(int ordinal) {
        return ordinal >= 0 && ordinal < ObbCollisionMode.values().length
                ? ObbCollisionMode.values()[ordinal]
                : ObbCollisionMode.NONE;
    }

    private static void writeAabb(FriendlyByteBuf buffer, AABB box) {
        buffer.writeDouble(box.minX);
        buffer.writeDouble(box.minY);
        buffer.writeDouble(box.minZ);
        buffer.writeDouble(box.maxX);
        buffer.writeDouble(box.maxY);
        buffer.writeDouble(box.maxZ);
    }

    private static AABB readAabb(FriendlyByteBuf buffer) {
        return new AABB(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble());
    }

    private static void writeMatrix(FriendlyByteBuf buffer, Matrix4f matrix) {
        buffer.writeFloat(matrix.m00());
        buffer.writeFloat(matrix.m01());
        buffer.writeFloat(matrix.m02());
        buffer.writeFloat(matrix.m03());
        buffer.writeFloat(matrix.m10());
        buffer.writeFloat(matrix.m11());
        buffer.writeFloat(matrix.m12());
        buffer.writeFloat(matrix.m13());
        buffer.writeFloat(matrix.m20());
        buffer.writeFloat(matrix.m21());
        buffer.writeFloat(matrix.m22());
        buffer.writeFloat(matrix.m23());
        buffer.writeFloat(matrix.m30());
        buffer.writeFloat(matrix.m31());
        buffer.writeFloat(matrix.m32());
        buffer.writeFloat(matrix.m33());
    }

    private static Matrix4f readMatrix(FriendlyByteBuf buffer) {
        return new Matrix4f(
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }
}
