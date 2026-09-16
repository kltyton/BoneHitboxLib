package com.kltyton.bonehitboxlib.client;

import com.kltyton.bonehitboxlib.client.compat.geckolib.BoneHitboxLibFabricGeckoClient;

import com.kltyton.bonehitboxlib.registry.BoneHitboxLibFabricRegistries;
import com.kltyton.bonehitboxlib.client.render.example.vanilla.ObbRavagerTestRenderer;
import com.kltyton.bonehitboxlib.client.render.example.vanilla.ObbZombieTestRenderer;
import com.kltyton.bonehitboxlib.client.config.BoneHitboxClientOptions;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient;
import com.kltyton.bonehitboxlib.compat.geckolib.GeckoLibCompat;
import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.network.protocol.BoneHitboxNetworking;
import com.kltyton.bonehitboxlib.network.payload.entity.ObbEntityPartsPayload;
import com.kltyton.bonehitboxlib.network.payload.selection.ObbPartSelectionPayload;
import com.kltyton.bonehitboxlib.network.payload.contact.ObbContactReportPayload;
import com.kltyton.bonehitboxlib.network.payload.skill.GeoKeyframeSkillPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;

/**
 * CN: Fabric 客户端入口。
 * EN: Fabric client bootstrap.
 */
public final class BoneHitboxLibFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED.register(
                client -> com.kltyton.bonehitboxlib.api.block.shape.VanillaBlockShapes.initializeModBlocks());
        BoneHitboxNetworking.setClientSelectionSender(payload -> sendIfSupported(ObbPartSelectionPayload.TYPE, payload::write));
        BoneHitboxNetworking.setClientEntityPartsSender(payload -> sendIfSupported(ObbEntityPartsPayload.TYPE, payload::write));
        BoneHitboxNetworking.setClientContactReportSender(payload -> sendIfSupported(ObbContactReportPayload.TYPE, payload::write));
        BoneHitboxNetworking.setClientGeoKeyframeSkillSender(payload -> sendIfSupported(GeoKeyframeSkillPayload.TYPE, payload::write));

        KeyBindingHelper.registerKeyBinding(BoneHitboxClientOptions.HOLD_OBB_KEY);
        EntityRendererRegistry.register(BoneHitboxLibFabricRegistries.OBB_ZOMBIE_TEST, ObbZombieTestRenderer::new);
        EntityRendererRegistry.register(BoneHitboxLibFabricRegistries.SOFT_OBB_RAVAGER_TEST, ObbRavagerTestRenderer::new);
        EntityRendererRegistry.register(BoneHitboxLibFabricRegistries.HARD_OBB_RAVAGER_TEST, ObbRavagerTestRenderer::new);
        registerOptionalGeckoRenderers();
        ClientTickEvents.END_CLIENT_TICK.register(BonePartSelectionClient::clientTick);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide()) {
                BonePartSelectionClient.recordAttack(entity);
            }
            return InteractionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide()) {
                BonePartSelectionClient.syncCurrentSelection();
            }
            return InteractionResult.PASS;
        });

    }

    private static void sendIfSupported(net.minecraft.resources.ResourceLocation channel,
            java.util.function.Consumer<net.minecraft.network.FriendlyByteBuf> encoder) {
        if (ClientPlayNetworking.canSend(channel)) {
            var buffer = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            encoder.accept(buffer);
            ClientPlayNetworking.send(channel, buffer);
        }
    }

    private static void registerOptionalGeckoRenderers() {
        if (!GeckoLibCompat.isLoaded()) {
            return;
        }

        try {
            Class.forName("com.kltyton.bonehitboxlib.client.compat.geckolib.BoneHitboxLibFabricGeckoClient")
                    .getMethod("registerRenderers")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            Constants.LOG.warn("Failed to register optional GeckoLib Fabric renderers.", exception);
        }
    }
}
