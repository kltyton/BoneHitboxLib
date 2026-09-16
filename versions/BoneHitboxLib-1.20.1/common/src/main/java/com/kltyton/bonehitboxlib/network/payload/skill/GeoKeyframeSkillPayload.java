package com.kltyton.bonehitboxlib.network.payload.skill;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.api.geckolib.state.GeoObbAnimationState;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** CN: 客户端 GeckoLib 自定义关键帧到服务端技能分发器的通知包。EN: Client GeckoLib custom-keyframe notification for the server skill dispatcher. */
public record GeoKeyframeSkillPayload(
        int entityId,
        long clientSequence,
        long observedGameTime,
        String marker,
        GeoObbAnimationState animationState,
        double markerTimeSeconds,
        double animationSpeed) {

    public static final int MAX_FIELD_LENGTH = 128;
    public static final ResourceLocation TYPE = Constants.id("geo_keyframe_skill");

    public GeoKeyframeSkillPayload {
        marker = bounded(marker);
        animationState = animationState == null
                ? GeoObbAnimationState.NONE
                : new GeoObbAnimationState(
                        bounded(animationState.controllerName()),
                        bounded(animationState.animationName()),
                        animationState.animationTimeSeconds(),
                        animationState.timelineTimeSeconds(),
                        animationState.triggered(),
                        animationState.transitioning(),
                        animationState.finished());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeLong(clientSequence);
        buffer.writeLong(observedGameTime);
        buffer.writeUtf(marker, MAX_FIELD_LENGTH);
        buffer.writeUtf(animationState.controllerName(), MAX_FIELD_LENGTH);
        buffer.writeUtf(animationState.animationName(), MAX_FIELD_LENGTH);
        buffer.writeDouble(animationState.animationTimeSeconds());
        buffer.writeDouble(animationState.timelineTimeSeconds());
        buffer.writeBoolean(animationState.triggered());
        buffer.writeBoolean(animationState.transitioning());
        buffer.writeBoolean(animationState.finished());
        buffer.writeDouble(markerTimeSeconds);
        buffer.writeDouble(animationSpeed);
    }

    public static GeoKeyframeSkillPayload read(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        long sequence = buffer.readLong();
        long observedGameTime = buffer.readLong();
        String marker = buffer.readUtf(MAX_FIELD_LENGTH);
        GeoObbAnimationState animationState = new GeoObbAnimationState(
                buffer.readUtf(MAX_FIELD_LENGTH),
                buffer.readUtf(MAX_FIELD_LENGTH),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean());
        return new GeoKeyframeSkillPayload(
                entityId,
                sequence,
                observedGameTime,
                marker,
                animationState,
                buffer.readDouble(),
                buffer.readDouble());
    }

    private static String bounded(String value) {
        String normalized = value == null ? "" : value;
        return normalized.substring(0, Math.min(normalized.length(), MAX_FIELD_LENGTH));
    }

}
