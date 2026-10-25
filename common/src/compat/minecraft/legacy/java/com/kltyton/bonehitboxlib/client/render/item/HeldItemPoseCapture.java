package com.kltyton.bonehitboxlib.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;

public interface HeldItemPoseCapture {
    void bonehitboxlib$captureHeldItems(LivingEntity entity, float partialTick, PoseStack pose);
}
