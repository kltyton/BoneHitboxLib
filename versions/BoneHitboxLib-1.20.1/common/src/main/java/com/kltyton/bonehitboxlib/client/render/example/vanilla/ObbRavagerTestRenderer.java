package com.kltyton.bonehitboxlib.client.render.example.vanilla;

import com.kltyton.bonehitboxlib.example.entity.vanilla.ravager.ObbRavagerTestEntity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;

import net.minecraft.resources.ResourceLocation;

/**
 * CN: 项目内 Ravager OBB 测试实体 renderer，复用原版 ravager 模型和贴图。
 * EN: Renderer for the project-owned Ravager OBB test entity, reusing vanilla ravager model and texture.
 */
public final class ObbRavagerTestRenderer extends MobRenderer<Ravager, RavagerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/illager/ravager.png");

    public ObbRavagerTestRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(Ravager entity) {
        return TEXTURE;
    }

}
