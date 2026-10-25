package com.kltyton.bonehitboxlib.network;

import com.kltyton.bonehitboxlib.network.payload.selection.ObbPartSelectionPayload;
import com.kltyton.bonehitboxlib.network.payload.entity.ObbEntityPartsPayload;
import com.kltyton.bonehitboxlib.network.payload.contact.ObbContactReportPayload;
import com.kltyton.bonehitboxlib.network.payload.skill.GeoKeyframeSkillPayload;
import com.kltyton.bonehitboxlib.server.network.BoneHitboxServerNetwork;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * CN: Fabric 网络包注册。
 * EN: Fabric networking registration.
 */
public final class BoneHitboxLibFabricNetworking {
    private BoneHitboxLibFabricNetworking() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ObbPartSelectionPayload.TYPE, (server, player, listener, buffer, response) -> {
            ObbPartSelectionPayload payload = ObbPartSelectionPayload.read(buffer);
            server.execute(() -> BoneHitboxServerNetwork.handlePartSelection(payload, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(ObbEntityPartsPayload.TYPE, (server, player, listener, buffer, response) -> {
            ObbEntityPartsPayload payload = ObbEntityPartsPayload.read(buffer);
            server.execute(() -> BoneHitboxServerNetwork.handleEntityParts(payload, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(ObbContactReportPayload.TYPE, (server, player, listener, buffer, response) -> {
            ObbContactReportPayload payload = ObbContactReportPayload.read(buffer);
            server.execute(() -> BoneHitboxServerNetwork.handleContactReport(payload, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(GeoKeyframeSkillPayload.TYPE, (server, player, listener, buffer, response) -> {
            GeoKeyframeSkillPayload payload = GeoKeyframeSkillPayload.read(buffer);
            server.execute(() -> BoneHitboxServerNetwork.handleGeoKeyframeSkill(payload, player));
        });
    }
}
