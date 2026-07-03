package com.kltyton.bonehitboxlib;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge loader entrypoint.
 */
@Mod(Constants.MOD_ID)
public final class BoneHitboxLib {

    /**
     * Creates the NeoForge entrypoint.
     *
     * @param eventBus the mod event bus
     */
    public BoneHitboxLib(IEventBus eventBus) {
        BoneHitboxLibCommon.init();
    }
}
