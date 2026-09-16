package com.kltyton.bonehitboxlib.diagnostic.physics;

import com.kltyton.bonehitboxlib.Constants;

import net.minecraft.SharedConstants;

/**
 * CN: 通过 {@code -Dbonehitboxlib.debugContacts=true} 启用的 OBB 物理诊断日志入口。
 * EN: OBB physics diagnostic log entrypoint enabled with {@code -Dbonehitboxlib.debugContacts=true}.
 */
public final class ObbPhysicsDebug {
    private static final boolean ENABLED = SharedConstants.IS_RUNNING_IN_IDE
            || Boolean.getBoolean("bonehitboxlib.debugContacts");

    private ObbPhysicsDebug() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    public static void log(String message, Object... arguments) {
        if (ENABLED) {
            Constants.LOG.info("[OBB-PHYS] " + message, arguments);
        }
    }
}
