package com.kltyton.bonehitboxlib.client.compat.geckolib.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.example.entity.geckolib.GeckoObbTestEntity;

import net.minecraft.resources.Identifier;

/**
 * CN: 使用仓库内置 hulkbuster GeckoLib 资产的测试模型。
 * EN: Test model backed by the bundled hulkbuster GeckoLib assets.
 */
public final class GeckoObbTestModel<T extends GeckoObbTestEntity> extends GeoModel<T> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Constants.id("entity/hulkbuster");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Constants.id("textures/entity/hulkbuster/hulkbuster.png");
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return Constants.id("entity/hulkbuster");
    }
}
