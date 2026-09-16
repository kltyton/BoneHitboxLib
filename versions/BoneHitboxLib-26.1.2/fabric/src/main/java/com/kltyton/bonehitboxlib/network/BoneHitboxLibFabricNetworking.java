package com.kltyton.bonehitboxlib.network;

import com.kltyton.bonehitboxlib.network.payload.selection.ObbPartSelectionPayload;
import com.kltyton.bonehitboxlib.network.payload.entity.ObbEntityPartsPayload;
import com.kltyton.bonehitboxlib.network.payload.contact.ObbContactReportPayload;
import com.kltyton.bonehitboxlib.network.payload.skill.GeoKeyframeSkillPayload;
import com.kltyton.bonehitboxlib.server.network.BoneHitboxServerNetwork;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * CN: Fabric 网络包注册。
 * EN: Fabric networking registration.
 */
public final class BoneHitboxLibFabricNetworking {
    private BoneHitboxLibFabricNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ObbPartSelectionPayload.TYPE, ObbPartSelectionPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ObbEntityPartsPayload.TYPE, ObbEntityPartsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ObbContactReportPayload.TYPE, ObbContactReportPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GeoKeyframeSkillPayload.TYPE, GeoKeyframeSkillPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ObbPartSelectionPayload.TYPE,
                (payload, context) -> BoneHitboxServerNetwork.handlePartSelection(payload, context.player()));
        ServerPlayNetworking.registerGlobalReceiver(ObbEntityPartsPayload.TYPE,
                (payload, context) -> BoneHitboxServerNetwork.handleEntityParts(payload, context.player()));
        ServerPlayNetworking.registerGlobalReceiver(ObbContactReportPayload.TYPE,
                (payload, context) -> BoneHitboxServerNetwork.handleContactReport(payload, context.player()));
        ServerPlayNetworking.registerGlobalReceiver(GeoKeyframeSkillPayload.TYPE,
                (payload, context) -> BoneHitboxServerNetwork.handleGeoKeyframeSkill(payload, context.player()));
    }
}
