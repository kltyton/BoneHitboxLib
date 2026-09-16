package com.kltyton.bonehitboxlib.network;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.network.payload.selection.ObbPartSelectionPayload;
import com.kltyton.bonehitboxlib.network.payload.entity.ObbEntityPartsPayload;
import com.kltyton.bonehitboxlib.network.payload.contact.ObbContactReportPayload;
import com.kltyton.bonehitboxlib.network.payload.skill.GeoKeyframeSkillPayload;
import com.kltyton.bonehitboxlib.server.network.BoneHitboxServerNetwork;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BoneHitboxLibForgeNetworking {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Constants.id("main"),
            () -> "3", "3"::equals, "3"::equals);
    private BoneHitboxLibForgeNetworking() { }

    public static void register() {
        CHANNEL.messageBuilder(ObbPartSelectionPayload.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ObbPartSelectionPayload::write).decoder(ObbPartSelectionPayload::read)
                .consumerMainThread((payload, context) -> {
                    var player = context.get().getSender();
                    if (player != null) { BoneHitboxServerNetwork.handlePartSelection(payload, player); }
                }).add();
        CHANNEL.messageBuilder(ObbEntityPartsPayload.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ObbEntityPartsPayload::write).decoder(ObbEntityPartsPayload::read)
                .consumerMainThread((payload, context) -> {
                    var player = context.get().getSender();
                    if (player != null) { BoneHitboxServerNetwork.handleEntityParts(payload, player); }
                }).add();
        CHANNEL.messageBuilder(ObbContactReportPayload.class, 2, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ObbContactReportPayload::write).decoder(ObbContactReportPayload::read)
                .consumerMainThread((payload, context) -> {
                    var player = context.get().getSender();
                    if (player != null) { BoneHitboxServerNetwork.handleContactReport(payload, player); }
                }).add();
        CHANNEL.messageBuilder(GeoKeyframeSkillPayload.class, 3, NetworkDirection.PLAY_TO_SERVER)
                .encoder(GeoKeyframeSkillPayload::write).decoder(GeoKeyframeSkillPayload::read)
                .consumerMainThread((payload, context) -> {
                    var player = context.get().getSender();
                    if (player != null) { BoneHitboxServerNetwork.handleGeoKeyframeSkill(payload, player); }
                }).add();
    }
}
