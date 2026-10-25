package com.kltyton.bonehitboxlib.mixin.client.render;

import com.kltyton.bonehitboxlib.client.render.item.HeldItemCapture;
import com.kltyton.bonehitboxlib.client.render.outline.PartOutlineRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class HeldItemOutlineMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void bonehitboxlib$beginOutlines(CallbackInfo callback) { PartOutlineRenderer.beginFrame(); }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V"))
    private void bonehitboxlib$heldItemOutlines(CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        HeldItemCapture.submitLocalPlayerOutlines(minecraft);
        PartOutlineRenderer.flush(minecraft.renderBuffers().bufferSource());
    }
}
