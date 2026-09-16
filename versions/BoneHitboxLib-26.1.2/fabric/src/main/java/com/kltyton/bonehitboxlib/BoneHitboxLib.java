package com.kltyton.bonehitboxlib;

import com.kltyton.bonehitboxlib.event.BoneHitboxLibFabricEvents;
import com.kltyton.bonehitboxlib.network.BoneHitboxLibFabricNetworking;
import com.kltyton.bonehitboxlib.registry.BoneHitboxLibFabricRegistries;

import com.kltyton.bonehitboxlib.config.common.BoneHitboxConfig;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;

/**
 * CN: Fabric loader 入口。
 * EN: Fabric loader entrypoint.
 */
public final class BoneHitboxLib implements ModInitializer {

    /**
     * CN: 创建 Fabric 入口。
     * EN: Creates the Fabric entrypoint.
     */
    public BoneHitboxLib() {
    }

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.CLIENT, BoneHitboxConfig.CLIENT_SPEC);
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.SERVER, BoneHitboxConfig.SERVER_SPEC);
        BoneHitboxLibFabricRegistries.init();
        BoneHitboxLibFabricNetworking.register();
        BoneHitboxLibFabricEvents.register();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(
                server -> com.kltyton.bonehitboxlib.api.block.shape.VanillaBlockShapes.initializeModBlocks());
        BoneHitboxLibCommon.init();
    }
}
