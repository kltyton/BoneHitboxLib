package com.kltyton.bonehitboxlib;

import net.fabricmc.api.ModInitializer;

/**
 * Fabric loader entrypoint.
 */
public final class BoneHitboxLib implements ModInitializer {

    /**
     * Creates the Fabric entrypoint.
     */
    public BoneHitboxLib() {
    }

    @Override
    public void onInitialize() {
        BoneHitboxLibCommon.init();
    }
}
