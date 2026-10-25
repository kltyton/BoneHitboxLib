package com.kltyton.bonehitboxlib;

import com.kltyton.bonehitboxlib.client.BoneHitboxLibNeoForgeClient;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import com.kltyton.bonehitboxlib.config.common.BoneHitboxConfig;
import com.kltyton.bonehitboxlib.network.payload.entity.ObbEntityPartsPayload;
import com.kltyton.bonehitboxlib.network.payload.selection.ObbPartSelectionPayload;
import com.kltyton.bonehitboxlib.network.payload.contact.ObbContactReportPayload;
import com.kltyton.bonehitboxlib.network.payload.skill.GeoKeyframeSkillPayload;
import com.kltyton.bonehitboxlib.server.hook.vanilla.BoneHitboxServerHooks;
import com.kltyton.bonehitboxlib.server.network.BoneHitboxServerNetwork;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/**
 * CN: NeoForge loader 入口。
 * EN: NeoForge loader entrypoint.
 */
@Mod(Constants.MOD_ID)
public final class BoneHitboxLib {

    /**
     * CN: 创建 NeoForge 入口。
     * EN: Creates the NeoForge entrypoint.
     *
     * @param eventBus the mod event bus
     * @param modContainer the NeoForge mod container
     */
    public BoneHitboxLib(IEventBus eventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, BoneHitboxConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, BoneHitboxConfig.SERVER_SPEC);
        eventBus.addListener(BoneHitboxLib::registerPayloads);
        NeoForge.EVENT_BUS.addListener(BoneHitboxLib::interactEntity);
        NeoForge.EVENT_BUS.addListener(BoneHitboxLib::serverTick);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent event) ->
                com.kltyton.bonehitboxlib.server.sync.ObbServerLifecycle.clear());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerClient(eventBus);
        }
        eventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                event.enqueueWork(com.kltyton.bonehitboxlib.api.block.shape.VanillaBlockShapes::initializeModBlocks));
        BoneHitboxLibCommon.init();
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("3")
                .playToServer(ObbPartSelectionPayload.TYPE, ObbPartSelectionPayload.CODEC,
                        (payload, context) -> BoneHitboxServerNetwork.handlePartSelection(payload, (ServerPlayer) context.player()))
                .playToServer(ObbEntityPartsPayload.TYPE, ObbEntityPartsPayload.CODEC,
                        (payload, context) -> BoneHitboxServerNetwork.handleEntityParts(payload, (ServerPlayer) context.player()))
                .playToServer(ObbContactReportPayload.TYPE, ObbContactReportPayload.CODEC,
                        (payload, context) -> BoneHitboxServerNetwork.handleContactReport(payload, (ServerPlayer) context.player()))
                .playToServer(GeoKeyframeSkillPayload.TYPE, GeoKeyframeSkillPayload.CODEC,
                        (payload, context) -> BoneHitboxServerNetwork.handleGeoKeyframeSkill(payload, (ServerPlayer) context.player()));
    }

    private static void interactEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        InteractionResult result = BoneHitboxServerHooks.handlePlayerInteract(event.getEntity(), event.getHand(), event.getTarget());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private static void serverTick(ServerTickEvent.Post event) {
        com.kltyton.bonehitboxlib.server.sync.ObbServerLifecycle.tick(event.getServer());
    }

    private static void registerClient(IEventBus eventBus) {
        try {
            Class<?> clientClass = Class.forName("com.kltyton.bonehitboxlib.client.BoneHitboxLibNeoForgeClient");
            clientClass.getMethod("register", IEventBus.class).invoke(null, eventBus);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register BoneHitboxLib NeoForge client hooks", exception);
        }
    }
}
