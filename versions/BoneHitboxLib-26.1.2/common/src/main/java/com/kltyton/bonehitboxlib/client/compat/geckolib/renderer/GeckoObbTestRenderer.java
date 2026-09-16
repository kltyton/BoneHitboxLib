package com.kltyton.bonehitboxlib.client.compat.geckolib.renderer;

import com.kltyton.bonehitboxlib.client.compat.geckolib.model.GeckoObbTestModel;

import com.geckolib.renderer.GeoEntityRenderer;
import com.kltyton.bonehitboxlib.example.entity.geckolib.GeckoObbTestEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * CN: 内置 GeckoLib OBB 测试实体 renderer。
 * EN: Renderer for the built-in GeckoLib OBB test entities.
 */
public final class GeckoObbTestRenderer<T extends GeckoObbTestEntity> extends GeoEntityRenderer<T, LivingEntityRenderState> {
    public GeckoObbTestRenderer(EntityRendererProvider.Context context) {
        this(context, 1.0F, 1.0F);
    }

    public GeckoObbTestRenderer(EntityRendererProvider.Context context, float widthScale, float heightScale) {
        super(context, new GeckoObbTestModel<>());
        withScale(widthScale, heightScale);
    }
}
