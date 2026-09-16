package com.kltyton.bonehitboxlib.api.geckolib.entity;

import com.kltyton.bonehitboxlib.api.geckolib.entity.GeoBoneHitboxState;
import com.kltyton.bonehitboxlib.api.geckolib.state.GeoObbAnimationState;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.kltyton.bonehitboxlib.api.bone.attribute.ObbBoneAttribute;
import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;

/**
 * CN: GeckoLib/Geo 渲染实体专用 OBB 扩展接口。
 * EN: OBB extension interface for GeckoLib/Geo-rendered entities.
 */
public interface GeoBoneHitboxEntity extends BoneHitboxEntity {
    String BONEHITBOXLIB_GEO_SOURCE = "gecko";
    String BONEHITBOXLIB_ANIMATION_SCOPE = "gecko_animation";

    /** CN: 读取永久骨骼属性组。EN: Returns a permanent bone-attribute group. */
    default Set<String> bonehitboxlib$geoPermanentGroups(ObbBoneAttribute attribute) {
        return bonehitboxlib$obbState().persistentAttributeBones(BONEHITBOXLIB_GEO_SOURCE, attribute);
    }

    /** CN: 覆盖一个永久骨骼属性组。EN: Replaces one permanent bone-attribute group. */
    default void bonehitboxlib$geoSetPermanentGroups(ObbBoneAttribute attribute, Collection<String> groupNames) {
        bonehitboxlib$obbState().setPersistentAttributeBones(BONEHITBOXLIB_GEO_SOURCE, attribute, groupNames);
    }

    /** CN: 向一个永久骨骼属性组追加名称。EN: Adds names to one permanent bone-attribute group. */
    default void bonehitboxlib$geoAddPermanentGroups(ObbBoneAttribute attribute, Collection<String> groupNames) {
        Set<String> merged = new LinkedHashSet<>(bonehitboxlib$geoPermanentGroups(attribute));
        if (groupNames != null) {
            merged.addAll(groupNames);
        }
        bonehitboxlib$geoSetPermanentGroups(attribute, merged);
    }

    default void bonehitboxlib$geoClearPermanentGroups(ObbBoneAttribute attribute) {
        bonehitboxlib$geoSetPermanentGroups(attribute, Set.of());
    }

    /**
     * CN: 在当前动画作用域中临时设置骨骼属性；动画结束或切换后自动恢复。
     * EN: Temporarily sets a bone attribute in the current animation scope and restores it after the animation ends or changes.
     */
    default void bonehitboxlib$geoSetTemporaryGroups(ObbBoneAttribute attribute, Collection<String> groupNames) {
        bonehitboxlib$geoSetTemporaryGroups(bonehitboxlib$geoAnimationState(), attribute, groupNames);
    }

    default void bonehitboxlib$geoSetTemporaryGroups(GeoObbAnimationState animationState,
            ObbBoneAttribute attribute, Collection<String> groupNames) {
        bonehitboxlib$obbState().setTemporaryAttributeBones(
                BONEHITBOXLIB_ANIMATION_SCOPE,
                BONEHITBOXLIB_GEO_SOURCE,
                attribute,
                groupNames);
        GeoBoneHitboxState.bindTemporaryWindow(this, animationState);
    }

    default Set<String> bonehitboxlib$geoTemporaryGroups(ObbBoneAttribute attribute) {
        return bonehitboxlib$obbState().temporaryAttributeBones(
                BONEHITBOXLIB_ANIMATION_SCOPE,
                BONEHITBOXLIB_GEO_SOURCE,
                attribute);
    }

    /** CN: 临时设置软/硬碰撞骨骼组。EN: Temporarily sets a soft/hard collision-bone group. */
    default void bonehitboxlib$geoSetTemporaryCollisionGroups(ObbCollisionMode mode, Collection<String> groupNames) {
        bonehitboxlib$geoSetTemporaryCollisionGroups(bonehitboxlib$geoAnimationState(), mode, groupNames);
    }

    default void bonehitboxlib$geoSetTemporaryCollisionGroups(GeoObbAnimationState animationState,
            ObbCollisionMode mode, Collection<String> groupNames) {
        bonehitboxlib$obbState().setTemporaryCollisionBones(
                BONEHITBOXLIB_ANIMATION_SCOPE,
                BONEHITBOXLIB_GEO_SOURCE,
                mode,
                groupNames);
        GeoBoneHitboxState.bindTemporaryWindow(this, animationState);
    }

    /** CN: 清除本次动画的全部临时属性。EN: Clears all temporary attributes for the current animation. */
    default void bonehitboxlib$geoClearTemporaryGroups() {
        bonehitboxlib$obbState().clearTemporaryAttributeScope(BONEHITBOXLIB_ANIMATION_SCOPE);
        GeoBoneHitboxState.clearTemporaryWindow(this);
    }

    default Set<String> bonehitboxlib$geoPermanentAttackGroups() {
        return bonehitboxlib$geoPermanentGroups(ObbBoneAttribute.ATTACK);
    }

    default void bonehitboxlib$geoSetPermanentAttackGroups(Collection<String> groupNames) {
        bonehitboxlib$geoSetPermanentGroups(ObbBoneAttribute.ATTACK, groupNames);
    }

    default void bonehitboxlib$geoAddPermanentAttackGroups(Collection<String> groupNames) {
        bonehitboxlib$geoAddPermanentGroups(ObbBoneAttribute.ATTACK, groupNames);
    }

    default void bonehitboxlib$geoClearPermanentAttackGroups() {
        bonehitboxlib$geoClearPermanentGroups(ObbBoneAttribute.ATTACK);
    }

    default Set<String> bonehitboxlib$geoTemporaryAttackGroups() {
        return bonehitboxlib$geoTemporaryGroups(ObbBoneAttribute.ATTACK);
    }

    default void bonehitboxlib$geoSetTemporaryAttackGroups(Collection<String> groupNames) {
        bonehitboxlib$geoSetTemporaryGroups(ObbBoneAttribute.ATTACK, groupNames);
    }

    default void bonehitboxlib$geoSetTemporaryAttackGroups(GeoObbAnimationState animationState,
            Collection<String> groupNames) {
        bonehitboxlib$geoSetTemporaryGroups(animationState, ObbBoneAttribute.ATTACK, groupNames);
    }

    default void bonehitboxlib$geoClearTemporaryAttackGroups() {
        bonehitboxlib$geoSetTemporaryGroups(ObbBoneAttribute.ATTACK, Set.of());
    }

    default void bonehitboxlib$geoSetTemporaryHurtGroups(Collection<String> groupNames) {
        bonehitboxlib$geoSetTemporaryGroups(ObbBoneAttribute.HURT, groupNames);
    }

    default Set<String> bonehitboxlib$geoTemporaryHurtGroups() {
        return bonehitboxlib$geoTemporaryGroups(ObbBoneAttribute.HURT);
    }

    default void bonehitboxlib$geoClearTemporaryHurtGroups() {
        bonehitboxlib$geoSetTemporaryGroups(ObbBoneAttribute.HURT, Set.of());
    }

    /** CN: 当前服务端/客户端已知的 Geo 动画状态。EN: Current Geo animation state known to this side. */
    default GeoObbAnimationState bonehitboxlib$geoAnimationState() {
        return GeoBoneHitboxState.animationState(this);
    }

    /**
     * CN: 由库在收到模型状态或渲染状态时调用，用户通常不需要手动调用。
     * EN: Called by the library when model/render state is received; users normally do not call this manually.
     */
    default void bonehitboxlib$geoUpdateAnimationStates(java.util.List<GeoObbAnimationState> states) {
        if (GeoBoneHitboxState.updateAnimationStates(this, states)) {
            bonehitboxlib$obbState().clearTemporaryAttributeScope(BONEHITBOXLIB_ANIMATION_SCOPE);
        }
    }

    default void bonehitboxlib$geoUpdateAnimationState(GeoObbAnimationState animationState) {
        if (GeoBoneHitboxState.updateAnimationState(this, animationState)) {
            bonehitboxlib$obbState().clearTemporaryAttributeScope(BONEHITBOXLIB_ANIMATION_SCOPE);
        }
    }
}
