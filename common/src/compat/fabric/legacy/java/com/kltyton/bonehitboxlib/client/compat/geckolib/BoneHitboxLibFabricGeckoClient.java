package com.kltyton.bonehitboxlib.client.compat.geckolib;

import com.kltyton.bonehitboxlib.client.compat.geckolib.layer.GeckoBoneSelectionLayer;
import software.bernie.geckolib.event.GeoRenderEvent;

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
