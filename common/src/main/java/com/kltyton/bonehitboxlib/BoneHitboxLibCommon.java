package com.kltyton.bonehitboxlib;

/**
 * Common bootstrap shared by supported loaders.
 */
public final class BoneHitboxLibCommon {

    private BoneHitboxLibCommon() {
    }

    /**
     * Initializes common mod state.
     */
    public static void init() {
        Constants.LOG.info("{} initialized.", Constants.MOD_NAME);
    }
}
