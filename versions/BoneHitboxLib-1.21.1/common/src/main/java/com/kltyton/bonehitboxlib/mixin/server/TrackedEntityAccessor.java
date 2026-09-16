package com.kltyton.bonehitboxlib.mixin.server;

import java.util.Set;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface TrackedEntityAccessor {
    @Accessor("entity") Entity bonehitboxlib$entity();
    @Accessor("seenBy") Set<ServerPlayerConnection> bonehitboxlib$seenBy();
}
