package com.kltyton.bonehitboxlib.client.compat.geckolib;

import com.kltyton.bonehitboxlib.client.compat.geckolib.layer.GeckoBoneSelectionLayer;
import software.bernie.geckolib.event.GeoRenderEvent;

import com.kltyton.bonehitboxlib.registry.BoneHitboxLibNeoForgeRegistries;
import com.kltyton.bonehitboxlib.client.compat.geckolib.renderer.GeckoObbTestRenderer;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient;

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
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerEvents();
        if (BoneHitboxLibNeoForgeRegistries.GECKO_OBB_TEST != null) {
            event.registerEntityRenderer(BoneHitboxLibNeoForgeRegistries.GECKO_OBB_TEST.get(), GeckoObbTestRenderer::new);
        }
        if (BoneHitboxLibNeoForgeRegistries.GECKO_SOFT_OBB_TEST_VEHICLE != null) {
            event.registerEntityRenderer(BoneHitboxLibNeoForgeRegistries.GECKO_SOFT_OBB_TEST_VEHICLE.get(),
                    context -> new GeckoObbTestRenderer<>(context, 2.5F, 0.65F));
        }
        if (BoneHitboxLibNeoForgeRegistries.GECKO_HARD_OBB_TEST_VEHICLE != null) {
            event.registerEntityRenderer(BoneHitboxLibNeoForgeRegistries.GECKO_HARD_OBB_TEST_VEHICLE.get(),
                    context -> new GeckoObbTestRenderer<>(context, 2.5F, 0.65F));
        }
    }

    private static void addEntityLayers(GeoRenderEvent.Entity.CompileRenderLayers event) {
        event.addLayer(new GeckoBoneSelectionLayer(event.getRenderer()));
    }

}
