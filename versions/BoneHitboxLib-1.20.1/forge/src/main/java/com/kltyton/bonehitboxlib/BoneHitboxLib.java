package com.kltyton.bonehitboxlib;

import com.kltyton.bonehitboxlib.client.BoneHitboxLibForgeClient;
import com.kltyton.bonehitboxlib.registry.BoneHitboxLibForgeRegistries;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import com.kltyton.bonehitboxlib.network.BoneHitboxLibForgeNetworking;

import com.kltyton.bonehitboxlib.example.entity.vanilla.ravager.ObbRavagerTestEntity;
import com.kltyton.bonehitboxlib.example.entity.vanilla.zombie.ObbZombieTestEntity;
import com.kltyton.bonehitboxlib.example.entity.geckolib.GeckoObbTestEntity;
import com.kltyton.bonehitboxlib.config.common.BoneHitboxConfig;
import com.kltyton.bonehitboxlib.network.payload.entity.ObbEntityPartsPayload;
import com.kltyton.bonehitboxlib.network.payload.selection.ObbPartSelectionPayload;
import com.kltyton.bonehitboxlib.network.payload.contact.ObbContactReportPayload;
import com.kltyton.bonehitboxlib.network.payload.skill.GeoKeyframeSkillPayload;
import com.kltyton.bonehitboxlib.server.skill.keyframe.GeoKeyframeSkillDispatcher;
import com.kltyton.bonehitboxlib.server.hook.vanilla.BoneHitboxServerHooks;
import com.kltyton.bonehitboxlib.server.network.BoneHitboxServerNetwork;
import com.kltyton.bonehitboxlib.server.sync.contact.ServerObbContactStore;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

/**
 * CN: Forge loader 入口。
 * EN: Forge loader entrypoint.
 */
@Mod(Constants.MOD_ID)
public final class BoneHitboxLib {

    public BoneHitboxLib() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BoneHitboxConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, BoneHitboxConfig.SERVER_SPEC);
        BoneHitboxLibForgeRegistries.register(eventBus);
        eventBus.addListener(BoneHitboxLib::registerAttributes);
        BoneHitboxLibForgeNetworking.register();
        MinecraftForge.EVENT_BUS.addListener(BoneHitboxLib::interactEntity);
        MinecraftForge.EVENT_BUS.addListener(BoneHitboxLib::serverTick);
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStoppingEvent event) ->
                com.kltyton.bonehitboxlib.server.sync.ObbServerLifecycle.clear());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerClient(eventBus);
        }
        eventBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                event.enqueueWork(com.kltyton.bonehitboxlib.api.block.shape.VanillaBlockShapes::initializeModBlocks));
        BoneHitboxLibCommon.init();
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BoneHitboxLibForgeRegistries.OBB_ZOMBIE_TEST.get(), ObbZombieTestEntity.createAttributes().build());
        event.put(BoneHitboxLibForgeRegistries.SOFT_OBB_RAVAGER_TEST.get(), ObbRavagerTestEntity.createAttributes().build());
        event.put(BoneHitboxLibForgeRegistries.HARD_OBB_RAVAGER_TEST.get(), ObbRavagerTestEntity.createAttributes().build());
        if (BoneHitboxLibForgeRegistries.GECKO_OBB_TEST != null) {
            event.put(BoneHitboxLibForgeRegistries.GECKO_OBB_TEST.get(), GeckoObbTestEntity.createAttributes().build());
        }
        if (BoneHitboxLibForgeRegistries.GECKO_SOFT_OBB_TEST_VEHICLE != null) {
            event.put(BoneHitboxLibForgeRegistries.GECKO_SOFT_OBB_TEST_VEHICLE.get(), GeckoObbTestEntity.createAttributes().build());
        }
        if (BoneHitboxLibForgeRegistries.GECKO_HARD_OBB_TEST_VEHICLE != null) {
            event.put(BoneHitboxLibForgeRegistries.GECKO_HARD_OBB_TEST_VEHICLE.get(), GeckoObbTestEntity.createAttributes().build());
        }
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

    private static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { com.kltyton.bonehitboxlib.server.sync.ObbServerLifecycle.tick(event.getServer()); }
    }

    private static void registerClient(IEventBus eventBus) {
        try {
            Class<?> clientClass = Class.forName("com.kltyton.bonehitboxlib.client.BoneHitboxLibForgeClient");
            clientClass.getMethod("register", IEventBus.class).invoke(null, eventBus);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register BoneHitboxLib Forge client hooks", exception);
        }
    }
}
