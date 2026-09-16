package com.kltyton.bonehitboxlib.client.compat.geckolib;

import com.kltyton.bonehitboxlib.client.compat.geckolib.layer.GeckoBoneSelectionLayer;
import software.bernie.geckolib.event.GeoRenderEvent;

import com.kltyton.bonehitboxlib.registry.BoneHitboxLibForgeRegistries;
import com.kltyton.bonehitboxlib.client.compat.geckolib.renderer.GeckoObbTestRenderer;
import com.kltyton.bonehitboxlib.client.selection.service.BonePartSelectionClient;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * CN: Forge GeckoLib 可选 renderer 注册；只在 GeckoLib 存在时通过反射加载。
 * EN: Optional Forge GeckoLib renderer registration; loaded reflectively only when GeckoLib is present.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class BoneHitboxLibForgeGeckoClient {
    private static boolean eventsRegistered;

    private BoneHitboxLibForgeGeckoClient() {
    }

    public static void registerEvents() {
        if (eventsRegistered) {
            return;
        }

        eventsRegistered = true;
        MinecraftForge.EVENT_BUS.addListener(BoneHitboxLibForgeGeckoClient::addEntityLayers);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerEvents();
        if (BoneHitboxLibForgeRegistries.GECKO_OBB_TEST != null) {
            event.registerEntityRenderer(BoneHitboxLibForgeRegistries.GECKO_OBB_TEST.get(), GeckoObbTestRenderer::new);
        }
        if (BoneHitboxLibForgeRegistries.GECKO_SOFT_OBB_TEST_VEHICLE != null) {
            event.registerEntityRenderer(BoneHitboxLibForgeRegistries.GECKO_SOFT_OBB_TEST_VEHICLE.get(),
                    context -> new GeckoObbTestRenderer<>(context, 2.5F, 0.65F));
        }
        if (BoneHitboxLibForgeRegistries.GECKO_HARD_OBB_TEST_VEHICLE != null) {
            event.registerEntityRenderer(BoneHitboxLibForgeRegistries.GECKO_HARD_OBB_TEST_VEHICLE.get(),
                    context -> new GeckoObbTestRenderer<>(context, 2.5F, 0.65F));
        }
    }

    private static void addEntityLayers(GeoRenderEvent.Entity.CompileRenderLayers event) {
        event.addLayer(new GeckoBoneSelectionLayer(event.getRenderer()));
    }

}
