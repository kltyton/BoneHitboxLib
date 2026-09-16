package com.kltyton.bonehitboxlib.registry;

import com.kltyton.bonehitboxlib.Constants;

import com.kltyton.bonehitboxlib.example.entity.vanilla.ravager.HardObbRavagerTestEntity;
import com.kltyton.bonehitboxlib.example.entity.vanilla.ravager.ObbRavagerTestEntity;
import com.kltyton.bonehitboxlib.example.entity.vanilla.zombie.ObbZombieTestEntity;
import com.kltyton.bonehitboxlib.example.entity.vanilla.ravager.SoftObbRavagerTestEntity;
import com.kltyton.bonehitboxlib.compat.geckolib.GeckoLibCompat;
import com.kltyton.bonehitboxlib.example.entity.geckolib.GeckoHardObbTestVehicleEntity;
import com.kltyton.bonehitboxlib.example.entity.geckolib.GeckoObbTestEntity;
import com.kltyton.bonehitboxlib.example.entity.geckolib.GeckoSoftObbTestVehicleEntity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * CN: Fabric 端注册桥。
 * EN: Fabric-side registry bridge.
 */
public final class BoneHitboxLibFabricRegistries {
    public static final EntityType<ObbZombieTestEntity> OBB_ZOMBIE_TEST = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Constants.id("obb_zombie_test"),
            EntityType.Builder.of(ObbZombieTestEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build(Constants.id("obb_zombie_test").toString()));

    public static final EntityType<SoftObbRavagerTestEntity> SOFT_OBB_RAVAGER_TEST = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Constants.id("soft_obb_ravager_test"),
            EntityType.Builder.of(SoftObbRavagerTestEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.2F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build(Constants.id("soft_obb_ravager_test").toString()));

    public static final EntityType<HardObbRavagerTestEntity> HARD_OBB_RAVAGER_TEST = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Constants.id("hard_obb_ravager_test"),
            EntityType.Builder.of(HardObbRavagerTestEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 2.2F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build(Constants.id("hard_obb_ravager_test").toString()));

    public static final EntityType<GeckoObbTestEntity> GECKO_OBB_TEST = registerGeckoObbTest();
    public static final EntityType<GeckoSoftObbTestVehicleEntity> GECKO_SOFT_OBB_TEST_VEHICLE = registerGeckoSoftObbTestVehicle();
    public static final EntityType<GeckoHardObbTestVehicleEntity> GECKO_HARD_OBB_TEST_VEHICLE = registerGeckoHardObbTestVehicle();

    private BoneHitboxLibFabricRegistries() {
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(OBB_ZOMBIE_TEST, ObbZombieTestEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOFT_OBB_RAVAGER_TEST, ObbRavagerTestEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HARD_OBB_RAVAGER_TEST, ObbRavagerTestEntity.createAttributes());
        if (GECKO_OBB_TEST != null) {
            FabricDefaultAttributeRegistry.register(GECKO_OBB_TEST, GeckoObbTestEntity.createAttributes());
        }
        if (GECKO_SOFT_OBB_TEST_VEHICLE != null) {
            FabricDefaultAttributeRegistry.register(GECKO_SOFT_OBB_TEST_VEHICLE, GeckoObbTestEntity.createAttributes());
        }
        if (GECKO_HARD_OBB_TEST_VEHICLE != null) {
            FabricDefaultAttributeRegistry.register(GECKO_HARD_OBB_TEST_VEHICLE, GeckoObbTestEntity.createAttributes());
        }
    }

    private static EntityType<GeckoObbTestEntity> registerGeckoObbTest() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Constants.id("gecko_obb_test_entity"),
                EntityType.Builder.of(GeckoObbTestEntity::new, MobCategory.MISC)
                        .sized(1.4F, 3.1F)
                        .clientTrackingRange(10)
                        .updateInterval(3)
                        .build(Constants.id("gecko_obb_test_entity").toString()));
    }

    private static EntityType<GeckoSoftObbTestVehicleEntity> registerGeckoSoftObbTestVehicle() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Constants.id("gecko_soft_obb_test_vehicle"),
                EntityType.Builder.of(GeckoSoftObbTestVehicleEntity::new, MobCategory.MISC)
                        .sized(1.4F, 3.1F)
                        .clientTrackingRange(10)
                        .updateInterval(3)
                        .build(Constants.id("gecko_soft_obb_test_vehicle").toString()));
    }

    private static EntityType<GeckoHardObbTestVehicleEntity> registerGeckoHardObbTestVehicle() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Constants.id("gecko_hard_obb_test_vehicle"),
                EntityType.Builder.of(GeckoHardObbTestVehicleEntity::new, MobCategory.MISC)
                        .sized(1.4F, 3.1F)
                        .clientTrackingRange(10)
                        .updateInterval(3)
                        .build(Constants.id("gecko_hard_obb_test_vehicle").toString()));
    }
}
