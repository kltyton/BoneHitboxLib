package com.kltyton.bonehitboxlib.mixin.client.render;

import com.kltyton.bonehitboxlib.client.compat.geckolib.skill.GeoControllerClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "software.bernie.geckolib.animation.AnimationController", remap = false)
public abstract class GeoControllerClockMixin implements GeoControllerClock {
    @Shadow protected double tickOffset;
    @Unique private double bonehitboxlib$timelineTick;

    @ModifyVariable(method = "process", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private double bonehitboxlib$captureClock(double seekTime) {
        bonehitboxlib$timelineTick = seekTime;
        return seekTime;
    }

    @Override
    public double bonehitboxlib$getTimelineTick() { return bonehitboxlib$timelineTick; }

    @Override
    public double bonehitboxlib$getTickOffset() { return tickOffset; }
}
