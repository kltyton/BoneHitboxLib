package com.kltyton.bonehitboxlib.client;

import com.kltyton.bonehitboxlib.client.compat.geckolib.BoneHitboxLibForgeGeckoClient;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.client.config.BoneHitboxClientOptions;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient;
import com.kltyton.bonehitboxlib.compat.geckolib.GeckoLibCompat;
import com.kltyton.bonehitboxlib.network.protocol.BoneHitboxNetworking;

import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import com.kltyton.bonehitboxlib.network.BoneHitboxLibForgeNetworking;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * CN: Forge 客户端入口。
 * EN: Forge client bootstrap.
 */
public final class BoneHitboxLibForgeClient {

    private BoneHitboxLibForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        BoneHitboxNetworking.setClientSelectionSender(payload -> sendToServerWhenConnected(payload));
        BoneHitboxNetworking.setClientEntityPartsSender(payload -> sendToServerWhenConnected(payload));
        BoneHitboxNetworking.setClientContactReportSender(payload -> sendToServerWhenConnected(payload));
        BoneHitboxNetworking.setClientGeoKeyframeSkillSender(payload -> sendToServerWhenConnected(payload));
        modEventBus.addListener(BoneHitboxLibForgeClient::registerRenderers);
        modEventBus.addListener(BoneHitboxLibForgeClient::registerKeys);
        MinecraftForge.EVENT_BUS.addListener(BoneHitboxLibForgeClient::clientTick);
        MinecraftForge.EVENT_BUS.addListener(BoneHitboxLibForgeClient::attackEntity);
        MinecraftForge.EVENT_BUS.addListener(BoneHitboxLibForgeClient::interactEntity);
    }

    private static void sendToServerWhenConnected(Object payload) {
        if (Minecraft.getInstance().getConnection() != null) {
            BoneHitboxLibForgeNetworking.CHANNEL.sendToServer(payload);
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
            Class.forName("com.kltyton.bonehitboxlib.client.compat.geckolib.BoneHitboxLibForgeGeckoClient")
                    .getMethod("registerRenderers", EntityRenderersEvent.RegisterRenderers.class)
                    .invoke(null, event);
        } catch (ReflectiveOperationException exception) {
            Constants.LOG.warn("Failed to register optional GeckoLib Forge renderers.", exception);
        }
    }

    private static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { BonePartSelectionClient.clientTick(Minecraft.getInstance()); }
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
