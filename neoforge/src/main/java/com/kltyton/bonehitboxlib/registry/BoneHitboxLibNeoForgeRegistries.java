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

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * CN: NeoForge 端注册桥。
 * EN: NeoForge-side registry bridge.
 */
public final class BoneHitboxLibNeoForgeRegistries {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(Constants.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ObbZombieTestEntity>> OBB_ZOMBIE_TEST = ENTITY_TYPES.registerEntityType(
            "obb_zombie_test",
            ObbZombieTestEntity::new,
            MobCategory.MONSTER,
            builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(10).updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<SoftObbRavagerTestEntity>> SOFT_OBB_RAVAGER_TEST = ENTITY_TYPES.registerEntityType(
            "soft_obb_ravager_test",
            SoftObbRavagerTestEntity::new,
            MobCategory.MONSTER,
            builder -> builder.sized(1.95F, 2.2F).clientTrackingRange(10).updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<HardObbRavagerTestEntity>> HARD_OBB_RAVAGER_TEST = ENTITY_TYPES.registerEntityType(
            "hard_obb_ravager_test",
            HardObbRavagerTestEntity::new,
            MobCategory.MONSTER,
            builder -> builder.sized(1.95F, 2.2F).clientTrackingRange(10).updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<GeckoObbTestEntity>> GECKO_OBB_TEST = registerGeckoObbTest();
    public static final DeferredHolder<EntityType<?>, EntityType<GeckoSoftObbTestVehicleEntity>> GECKO_SOFT_OBB_TEST_VEHICLE = registerGeckoSoftObbTestVehicle();
    public static final DeferredHolder<EntityType<?>, EntityType<GeckoHardObbTestVehicleEntity>> GECKO_HARD_OBB_TEST_VEHICLE = registerGeckoHardObbTestVehicle();

    private BoneHitboxLibNeoForgeRegistries() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    private static DeferredHolder<EntityType<?>, EntityType<GeckoObbTestEntity>> registerGeckoObbTest() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return ENTITY_TYPES.registerEntityType(
                "gecko_obb_test_entity",
                GeckoObbTestEntity::new,
                MobCategory.MISC,
                builder -> builder.sized(1.4F, 3.1F).clientTrackingRange(10).updateInterval(3));
    }

    private static DeferredHolder<EntityType<?>, EntityType<GeckoSoftObbTestVehicleEntity>> registerGeckoSoftObbTestVehicle() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return ENTITY_TYPES.registerEntityType(
                "gecko_soft_obb_test_vehicle",
                GeckoSoftObbTestVehicleEntity::new,
                MobCategory.MISC,
                builder -> builder.sized(1.4F, 3.1F).clientTrackingRange(10).updateInterval(3));
    }

    private static DeferredHolder<EntityType<?>, EntityType<GeckoHardObbTestVehicleEntity>> registerGeckoHardObbTestVehicle() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return ENTITY_TYPES.registerEntityType(
                "gecko_hard_obb_test_vehicle",
                GeckoHardObbTestVehicleEntity::new,
                MobCategory.MISC,
                builder -> builder.sized(1.4F, 3.1F).clientTrackingRange(10).updateInterval(3));
    }
}
