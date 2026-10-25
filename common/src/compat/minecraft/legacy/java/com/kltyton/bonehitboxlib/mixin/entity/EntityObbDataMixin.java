package com.kltyton.bonehitboxlib.mixin.entity;

import com.kltyton.bonehitboxlib.api.state.runtime.ObbEntityStates;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CN: 在原版实体保存/加载入口附加 BoneHitboxLib 数据，避免要求每个实体重复覆盖保存方法。
 * EN: Adds BoneHitboxLib data at vanilla entity save/load entrypoints without requiring every entity to override persistence methods.
 */
@Mixin(Entity.class)
public abstract class EntityObbDataMixin implements com.kltyton.bonehitboxlib.api.state.runtime.ObbEntityStateAccess {
    @org.spongepowered.asm.mixin.Unique
    private com.kltyton.bonehitboxlib.api.state.runtime.ObbEntityState bonehitboxlib$attachedState;

    @Override
    public com.kltyton.bonehitboxlib.api.state.runtime.ObbEntityState bonehitboxlib$getAttachedState() {
        return bonehitboxlib$attachedState;
    }

    @Override
    public void bonehitboxlib$setAttachedState(com.kltyton.bonehitboxlib.api.state.runtime.ObbEntityState state) {
        bonehitboxlib$attachedState = state;
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void bonehitboxlib$saveObbData(CompoundTag output, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<CompoundTag> callbackInfo) {
        ObbEntityStates.save((Entity) (Object) this, output);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void bonehitboxlib$loadObbData(CompoundTag input, CallbackInfo callbackInfo) {
        ObbEntityStates.load((Entity) (Object) this, input);
    }
}
