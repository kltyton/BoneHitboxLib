package com.kltyton.bonehitboxlib.client.config;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.config.common.BoneHitboxConfig;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

/**
 * CN: OBB 调试渲染的客户端开关和按键。
 * EN: Client switches and key binding for OBB debug rendering.
 */
public final class BoneHitboxClientOptions {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Constants.id("bonehitboxlib"));
    public static final KeyMapping HOLD_OBB_KEY = new KeyMapping(
            "key.bonehitboxlib.hold_obb",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY);

    private BoneHitboxClientOptions() {
    }

    public static boolean shouldRenderCrosshairObb(Minecraft minecraft) {
        return BoneHitboxConfig.showCrosshairObb()
                || HOLD_OBB_KEY.isDown();
    }

    public static boolean shouldRenderDebugObbs(Minecraft minecraft) {
        return minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES);
    }
}
