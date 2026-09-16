package com.kltyton.bonehitboxlib.client.compat.geckolib.model;

import software.bernie.geckolib.model.GeoModel;
import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.example.entity.geckolib.GeckoObbTestEntity;

import net.minecraft.resources.ResourceLocation;

/**
 * CN: 使用仓库内置 hulkbuster GeckoLib 资产的测试模型。
 * EN: Test model backed by the bundled hulkbuster GeckoLib assets.
 */
public final class GeckoObbTestModel<T extends GeckoObbTestEntity> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return Constants.id("geo/entity/hulkbuster.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return Constants.id("textures/entity/hulkbuster/hulkbuster.png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return Constants.id("animations/entity/hulkbuster.animation.json");
    }
}
