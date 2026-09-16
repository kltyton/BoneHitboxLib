package com.kltyton.bonehitboxlib.client.render.example.vanilla;

import com.kltyton.bonehitboxlib.example.entity.vanilla.zombie.ObbZombieTestEntity;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class ObbZombieTestRenderer extends AbstractZombieRenderer<ObbZombieTestEntity, ZombieModel<ObbZombieTestEntity>> {
    public ObbZombieTestRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
    }
}
