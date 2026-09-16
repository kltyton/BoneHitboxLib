package com.kltyton.bonehitboxlib.client.compat.geckolib;

import com.kltyton.bonehitboxlib.client.compat.geckolib.layer.GeckoBoneSelectionLayer;
import software.bernie.geckolib.event.GeoRenderEvent;

import com.kltyton.bonehitboxlib.registry.BoneHitboxLibFabricRegistries;
import com.kltyton.bonehitboxlib.client.compat.geckolib.renderer.GeckoObbTestRenderer;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * CN: Fabric GeckoLib 可选 renderer 注册；只在 GeckoLib 存在时通过反射加载。
 * EN: Optional Fabric GeckoLib renderer registration; loaded reflectively only when GeckoLib is present.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class BoneHitboxLibFabricGeckoClient {
    private static boolean eventsRegistered;

    private BoneHitboxLibFabricGeckoClient() {
    }

    public static void registerRenderers() {
        registerEvents();
        if (BoneHitboxLibFabricRegistries.GECKO_OBB_TEST != null) {
            EntityRendererRegistry.register(BoneHitboxLibFabricRegistries.GECKO_OBB_TEST, GeckoObbTestRenderer::new);
        }
        if (BoneHitboxLibFabricRegistries.GECKO_SOFT_OBB_TEST_VEHICLE != null) {
            EntityRendererRegistry.register(BoneHitboxLibFabricRegistries.GECKO_SOFT_OBB_TEST_VEHICLE,
                    context -> new GeckoObbTestRenderer<>(context, 2.5F, 0.65F));
        }
        if (BoneHitboxLibFabricRegistries.GECKO_HARD_OBB_TEST_VEHICLE != null) {
            EntityRendererRegistry.register(BoneHitboxLibFabricRegistries.GECKO_HARD_OBB_TEST_VEHICLE,
                    context -> new GeckoObbTestRenderer<>(context, 2.5F, 0.65F));
        }
    }

    private static void registerEvents() {
        if (eventsRegistered) {
            return;
        }

        eventsRegistered = true;
        GeoRenderEvent.Entity.CompileRenderLayers.EVENT.register(BoneHitboxLibFabricGeckoClient::addEntityLayers);
    }

    private static void addEntityLayers(GeoRenderEvent.Entity.CompileRenderLayers event) {
        event.addLayer(new GeckoBoneSelectionLayer(event.getRenderer()));
    }

}
