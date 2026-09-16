package com.kltyton.bonehitboxlib.mixin.client.render;

import com.kltyton.bonehitboxlib.client.selection.model.VisualPartEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * CN: 在原版 render state 上保存源实体 id，保证视觉部位 id 稳定。
 * EN: Stores the source entity id on vanilla render states so visual part ids remain stable.
 */
@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements VisualPartEntityRenderState {
    @Unique
    private int bonehitboxlib$entityId = -1;

    @Override
    public int bonehitboxlib$getEntityId() {
        return bonehitboxlib$entityId;
    }

    @Override
    public void bonehitboxlib$setEntityId(int entityId) {
        bonehitboxlib$entityId = entityId;
    }
}
