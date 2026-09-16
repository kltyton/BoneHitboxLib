package com.kltyton.bonehitboxlib.client.render.example.vanilla;

import com.kltyton.bonehitboxlib.example.entity.vanilla.zombie.ObbZombieTestEntity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.BabyZombieModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

/**
 * CN: 项目内 Zombie OBB 测试实体 renderer，复用原版 zombie 模型和贴图。
 * EN: Renderer for the project-owned Zombie OBB test entity, reusing vanilla zombie model and texture.
 */
public final class ObbZombieTestRenderer extends AbstractZombieRenderer<ObbZombieTestEntity, ZombieRenderState, ZombieModel<ZombieRenderState>> {
    public ObbZombieTestRenderer(EntityRendererProvider.Context context) {
        this(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_BABY_ARMOR);
    }

    private ObbZombieTestRenderer(
            EntityRendererProvider.Context context,
            ModelLayerLocation body,
            ModelLayerLocation babyBody,
            ArmorModelSet<ModelLayerLocation> armorSet,
            ArmorModelSet<ModelLayerLocation> babyArmorSet) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(body)),
                new BabyZombieModel<>(context.bakeLayer(babyBody)),
                ArmorModelSet.bake(armorSet, context.getModelSet(), ZombieModel::new),
                ArmorModelSet.bake(babyArmorSet, context.getModelSet(), BabyZombieModel::new));
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }
}
