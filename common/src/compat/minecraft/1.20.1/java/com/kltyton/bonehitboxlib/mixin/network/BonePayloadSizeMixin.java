package com.kltyton.bonehitboxlib.mixin.network;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.network.payload.selection.ObbPartSelectionPayload;
import com.kltyton.bonehitboxlib.network.payload.entity.ObbEntityPartsPayload;
import com.kltyton.bonehitboxlib.network.payload.contact.ObbContactReportPayload;
import com.kltyton.bonehitboxlib.network.payload.skill.GeoKeyframeSkillPayload;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class BonePayloadSizeMixin {
    @Shadow @Final private ResourceLocation identifier;

    @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", constant = @Constant(intValue = 32767))
    private int bonehitboxlib$boundedGeometryPayload(int vanillaLimit) {
        return Constants.id("main").equals(identifier) || ObbPartSelectionPayload.TYPE.equals(identifier) || ObbEntityPartsPayload.TYPE.equals(identifier)
                || ObbContactReportPayload.TYPE.equals(identifier) || GeoKeyframeSkillPayload.TYPE.equals(identifier)
                ? 1048576 : vanillaLimit;
    }
}
