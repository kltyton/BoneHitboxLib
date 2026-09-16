package com.kltyton.bonehitboxlib.client;

import com.kltyton.bonehitboxlib.client.compat.geckolib.BoneHitboxLibNeoForgeGeckoClient;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.client.config.BoneHitboxClientOptions;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient;
import com.kltyton.bonehitboxlib.compat.geckolib.GeckoLibCompat;
import com.kltyton.bonehitboxlib.network.protocol.BoneHitboxNetworking;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * CN: NeoForge 客户端入口。
 * EN: NeoForge client bootstrap.
 */
public final class BoneHitboxLibNeoForgeClient {

    private BoneHitboxLibNeoForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        BoneHitboxNetworking.setClientSelectionSender(payload -> sendToServerWhenConnected(payload));
        BoneHitboxNetworking.setClientEntityPartsSender(payload -> sendToServerWhenConnected(payload));
        BoneHitboxNetworking.setClientContactReportSender(payload -> sendToServerWhenConnected(payload));
        BoneHitboxNetworking.setClientGeoKeyframeSkillSender(payload -> sendToServerWhenConnected(payload));
        modEventBus.addListener(BoneHitboxLibNeoForgeClient::registerRenderers);
        modEventBus.addListener(BoneHitboxLibNeoForgeClient::registerKeys);
        NeoForge.EVENT_BUS.addListener(BoneHitboxLibNeoForgeClient::clientTick);
        NeoForge.EVENT_BUS.addListener(BoneHitboxLibNeoForgeClient::attackEntity);
        NeoForge.EVENT_BUS.addListener(BoneHitboxLibNeoForgeClient::interactEntity);
    }

    private static void sendToServerWhenConnected(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(payload);
        }
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerOptionalGeckoRenderers(event);
    }

    private static void registerOptionalGeckoRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (!GeckoLibCompat.isLoaded()) {
            return;
        }

        try {
            Class.forName("com.kltyton.bonehitboxlib.client.compat.geckolib.BoneHitboxLibNeoForgeGeckoClient")
                    .getMethod("registerRenderers", EntityRenderersEvent.RegisterRenderers.class)
                    .invoke(null, event);
        } catch (ReflectiveOperationException exception) {
            Constants.LOG.warn("Failed to register optional GeckoLib NeoForge renderers.", exception);
        }
    }

    private static void clientTick(ClientTickEvent.Post event) {
        BonePartSelectionClient.clientTick(Minecraft.getInstance());
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(BoneHitboxClientOptions.HOLD_OBB_KEY);
    }

    private static void attackEntity(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) {
            BonePartSelectionClient.recordAttack(event.getTarget());
        }
    }

    private static void interactEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) {
            BonePartSelectionClient.syncCurrentSelection();
        }
    }

}
