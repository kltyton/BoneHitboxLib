package com.kltyton.bonehitboxlib.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public interface HeldItemPoseCapture {
    void bonehitboxlib$captureHeldItems(LivingEntityRenderState state, PoseStack pose);
}
