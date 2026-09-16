package com.kltyton.bonehitboxlib.network.payload.contact;

import java.util.ArrayList;
import java.util.List;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.api.bone.key.ObbBoneKey;
import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.context.collision.ObbCollisionPhase;
import com.kltyton.bonehitboxlib.api.context.collision.ObbContactKind;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * CN: 客户端完成 SAT 后发送的批量接触摘要；服务端负责解析、去重、事件派发及可选 OBB 物理响应。
 * EN: Batched contact summary sent after client SAT; the server resolves, deduplicates, dispatches events, and applies optional OBB physics.
 */
public record ObbContactReportPayload(long observedGameTime, Vec3 reporterMovement, List<Contact> contacts)
        {
    public static final int MAX_CONTACTS = 512;
    private static final int MAX_FIELD_LENGTH = 128;
    public static final ResourceLocation TYPE = Constants.id("contact_report");

    public ObbContactReportPayload {
        reporterMovement = finiteMovement(reporterMovement);
        contacts = List.copyOf(contacts.size() > MAX_CONTACTS ? contacts.subList(0, MAX_CONTACTS) : contacts);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarLong(observedGameTime);
        buffer.writeFloat((float) reporterMovement.x());
        buffer.writeFloat((float) reporterMovement.y());
        buffer.writeFloat((float) reporterMovement.z());
        buffer.writeVarInt(contacts.size());
        contacts.forEach(contact -> contact.write(buffer));
    }

    public static ObbContactReportPayload read(FriendlyByteBuf buffer) {
        long observedGameTime = buffer.readVarLong();
        Vec3 reporterMovement = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        int declaredCount = buffer.readVarInt();
        if (declaredCount < 0 || declaredCount > MAX_CONTACTS) {
            throw new IllegalArgumentException("Invalid OBB contact count: " + declaredCount);
        }
        List<Contact> contacts = new ArrayList<>(declaredCount);
        for (int i = 0; i < declaredCount; i++) {
            contacts.add(Contact.read(buffer));
        }
        return new ObbContactReportPayload(observedGameTime, reporterMovement, contacts);
    }

    private static Vec3 finiteMovement(Vec3 movement) {
        if (movement == null
                || !Double.isFinite(movement.x())
                || !Double.isFinite(movement.y())
                || !Double.isFinite(movement.z())) {
            return Vec3.ZERO;
        }
        double length = movement.length();
        return length > 4.0 ? movement.scale(4.0 / length) : movement;
    }

    public record Contact(
            ObbContactKind kind,
            ObbCollisionPhase phase,
            int firstEntityId,
            ObbBoneKey firstBone,
            int secondEntityId,
            ObbBoneKey secondBone,
            ObbCollisionMode collisionMode,
            Vec3 correction) {

        private void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(kind.ordinal());
            buffer.writeVarInt(phase.ordinal());
            buffer.writeVarInt(firstEntityId);
            writeBone(buffer, firstBone);
            buffer.writeVarInt(secondEntityId);
            writeBone(buffer, secondBone);
            buffer.writeVarInt(collisionMode.ordinal());
            buffer.writeFloat((float) correction.x());
            buffer.writeFloat((float) correction.y());
            buffer.writeFloat((float) correction.z());
        }

        private static Contact read(FriendlyByteBuf buffer) {
            ObbContactKind kind = enumValue(ObbContactKind.values(), buffer.readVarInt(), ObbContactKind.COLLISION);
            ObbCollisionPhase phase = enumValue(ObbCollisionPhase.values(), buffer.readVarInt(), ObbCollisionPhase.BEGIN);
            int firstEntityId = buffer.readVarInt();
            ObbBoneKey firstBone = readBone(buffer);
            int secondEntityId = buffer.readVarInt();
            ObbBoneKey secondBone = readBone(buffer);
            ObbCollisionMode collisionMode = enumValue(ObbCollisionMode.values(), buffer.readVarInt(), ObbCollisionMode.NONE);
            Vec3 correction = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
            return new Contact(kind, phase, firstEntityId, firstBone, secondEntityId, secondBone, collisionMode, correction);
        }
    }

    private static void writeBone(FriendlyByteBuf buffer, ObbBoneKey key) {
        buffer.writeUtf(key.source(), MAX_FIELD_LENGTH);
        buffer.writeUtf(key.name(), MAX_FIELD_LENGTH);
        buffer.writeVarInt(key.cubeIndex());
    }

    private static ObbBoneKey readBone(FriendlyByteBuf buffer) {
        return new ObbBoneKey(
                buffer.readUtf(MAX_FIELD_LENGTH),
                buffer.readUtf(MAX_FIELD_LENGTH),
                Math.max(0, buffer.readVarInt()));
    }

    private static <T> T enumValue(T[] values, int ordinal, T fallback) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }
}
