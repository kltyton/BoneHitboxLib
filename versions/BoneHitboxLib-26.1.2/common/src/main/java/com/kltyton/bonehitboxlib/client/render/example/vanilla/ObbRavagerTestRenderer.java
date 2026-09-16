package com.kltyton.bonehitboxlib.client.render.example.vanilla;

import com.kltyton.bonehitboxlib.example.entity.vanilla.ravager.ObbRavagerTestEntity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.ravager.RavagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.Identifier;

/**
 * CN: 项目内 Ravager OBB 测试实体 renderer，复用原版 ravager 模型和贴图。
 * EN: Renderer for the project-owned Ravager OBB test entity, reusing vanilla ravager model and texture.
 */
public final class ObbRavagerTestRenderer<T extends ObbRavagerTestEntity> extends MobRenderer<T, RavagerRenderState, RavagerModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/illager/ravager.png");

    public ObbRavagerTestRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.1F);
    }

    @Override
    public Identifier getTextureLocation(RavagerRenderState state) {
        return TEXTURE;
    }

    @Override
    public RavagerRenderState createRenderState() {
        return new RavagerRenderState();
    }

    @Override
    public void extractRenderState(T entity, RavagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.stunnedTicksRemaining = entity.getStunnedTick() > 0.0F ? entity.getStunnedTick() - partialTicks : 0.0F;
        state.attackTicksRemaining = entity.getAttackTick() > 0.0F ? entity.getAttackTick() - partialTicks : 0.0F;
        state.roarAnimation = entity.getRoarTick() > 0 ? (20 - entity.getRoarTick() + partialTicks) / 20.0F : 0.0F;
    }
}
