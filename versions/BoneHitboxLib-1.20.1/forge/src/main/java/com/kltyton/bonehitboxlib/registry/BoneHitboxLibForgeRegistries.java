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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * CN: Forge 端注册桥。
 * EN: Forge-side registry bridge.
 */
public final class BoneHitboxLibForgeRegistries {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Constants.MOD_ID);

    public static final RegistryObject<EntityType<ObbZombieTestEntity>> OBB_ZOMBIE_TEST = ENTITY_TYPES.register("obb_zombie_test", () -> EntityType.Builder.of(ObbZombieTestEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(10).updateInterval(3).build(Constants.id("obb_zombie_test").toString()));

    public static final RegistryObject<EntityType<SoftObbRavagerTestEntity>> SOFT_OBB_RAVAGER_TEST = ENTITY_TYPES.register("soft_obb_ravager_test", () -> EntityType.Builder.of(SoftObbRavagerTestEntity::new, MobCategory.MONSTER).sized(1.95F, 2.2F).clientTrackingRange(10).updateInterval(3).build(Constants.id("soft_obb_ravager_test").toString()));

    public static final RegistryObject<EntityType<HardObbRavagerTestEntity>> HARD_OBB_RAVAGER_TEST = ENTITY_TYPES.register("hard_obb_ravager_test", () -> EntityType.Builder.of(HardObbRavagerTestEntity::new, MobCategory.MONSTER).sized(1.95F, 2.2F).clientTrackingRange(10).updateInterval(3).build(Constants.id("hard_obb_ravager_test").toString()));

    public static final RegistryObject<EntityType<GeckoObbTestEntity>> GECKO_OBB_TEST = registerGeckoObbTest();
    public static final RegistryObject<EntityType<GeckoSoftObbTestVehicleEntity>> GECKO_SOFT_OBB_TEST_VEHICLE = registerGeckoSoftObbTestVehicle();
    public static final RegistryObject<EntityType<GeckoHardObbTestVehicleEntity>> GECKO_HARD_OBB_TEST_VEHICLE = registerGeckoHardObbTestVehicle();

    private BoneHitboxLibForgeRegistries() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    private static RegistryObject<EntityType<GeckoObbTestEntity>> registerGeckoObbTest() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return ENTITY_TYPES.register("gecko_obb_test_entity", () -> EntityType.Builder.of(GeckoObbTestEntity::new, MobCategory.MISC).sized(1.4F, 3.1F).clientTrackingRange(10).updateInterval(3).build(Constants.id("gecko_obb_test_entity").toString()));
    }

    private static RegistryObject<EntityType<GeckoSoftObbTestVehicleEntity>> registerGeckoSoftObbTestVehicle() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return ENTITY_TYPES.register("gecko_soft_obb_test_vehicle", () -> EntityType.Builder.of(GeckoSoftObbTestVehicleEntity::new, MobCategory.MISC).sized(1.4F, 3.1F).clientTrackingRange(10).updateInterval(3).build(Constants.id("gecko_soft_obb_test_vehicle").toString()));
    }

    private static RegistryObject<EntityType<GeckoHardObbTestVehicleEntity>> registerGeckoHardObbTestVehicle() {
        if (!GeckoLibCompat.isLoaded()) {
            return null;
        }
        return ENTITY_TYPES.register("gecko_hard_obb_test_vehicle", () -> EntityType.Builder.of(GeckoHardObbTestVehicleEntity::new, MobCategory.MISC).sized(1.4F, 3.1F).clientTrackingRange(10).updateInterval(3).build(Constants.id("gecko_hard_obb_test_vehicle").toString()));
    }
}
