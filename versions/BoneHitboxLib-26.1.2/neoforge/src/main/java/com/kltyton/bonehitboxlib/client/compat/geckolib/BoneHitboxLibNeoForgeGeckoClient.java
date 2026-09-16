package com.kltyton.bonehitboxlib.client.compat.geckolib;

import com.kltyton.bonehitboxlib.client.compat.geckolib.layer.GeckoBoneSelectionLayer;

import com.geckolib.constant.DataTickets;
import com.geckolib.event.entity.CompileEntityRenderLayersEvent;
import com.geckolib.event.entity.CompileEntityRenderStateEvent;
import com.geckolib.renderer.base.GeoRenderer;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * CN: NeoForge GeckoLib 可选 renderer 注册；只在 GeckoLib 存在时通过反射加载。
 * EN: Optional NeoForge GeckoLib renderer registration; loaded reflectively only when GeckoLib is present.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class BoneHitboxLibNeoForgeGeckoClient {
    private static boolean eventsRegistered;

    private BoneHitboxLibNeoForgeGeckoClient() {
    }

    public static void registerEvents() {
        if (eventsRegistered) {
            return;
        }

        eventsRegistered = true;
        NeoForge.EVENT_BUS.addListener(BoneHitboxLibNeoForgeGeckoClient::addEntityLayers);
        NeoForge.EVENT_BUS.addListener(BoneHitboxLibNeoForgeGeckoClient::suppressFullRedOverlay);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerEvents();
    }

    private static void addEntityLayers(CompileEntityRenderLayersEvent event) {
        event.addLayer(renderer -> new GeckoBoneSelectionLayer((GeoRenderer) renderer));
    }

    private static void suppressFullRedOverlay(CompileEntityRenderStateEvent event) {
        if (event.getAnimatable() instanceof LivingEntity livingEntity
                && (livingEntity.hurtTime > 0 || livingEntity.deathTime > 0)
                && BonePartSelectionClient.shouldSuppressFullEntityRed(livingEntity)) {
            event.addData(DataTickets.PACKED_OVERLAY, OverlayTexture.NO_OVERLAY);
        }
    }
}
