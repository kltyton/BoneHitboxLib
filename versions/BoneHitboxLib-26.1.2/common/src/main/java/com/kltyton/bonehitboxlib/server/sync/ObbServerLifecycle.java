package com.kltyton.bonehitboxlib.server.sync;

import com.kltyton.bonehitboxlib.server.network.ObbReportValidation;
import com.kltyton.bonehitboxlib.server.selection.ServerPartSelectionStore;
import com.kltyton.bonehitboxlib.server.skill.keyframe.GeoKeyframeSkillDispatcher;
import com.kltyton.bonehitboxlib.server.sync.contact.ServerObbContactStore;
import com.kltyton.bonehitboxlib.server.sync.snapshot.ServerObbStore;
import net.minecraft.server.MinecraftServer;

public final class ObbServerLifecycle {
    private ObbServerLifecycle() { }

    public static void tick(MinecraftServer server) {
        ServerObbStore.tick(server);
        ServerObbContactStore.tick(server);
        GeoKeyframeSkillDispatcher.tick(server);
        ServerPartSelectionStore.tick(server);
        ObbReportValidation.tick(server);
    }

    public static void clear() {
        GeoKeyframeSkillDispatcher.clear();
        ServerObbContactStore.clear();
        ServerObbStore.clear();
        ServerPartSelectionStore.clear();
        ObbReportValidation.clear();
    }
}
