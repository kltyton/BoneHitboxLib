package com.kltyton.bonehitboxlib.api.entity;

import java.util.Optional;

import com.kltyton.bonehitboxlib.api.bone.attribute.ObbBoneAttribute;
import com.kltyton.bonehitboxlib.api.bone.key.ObbBoneKey;
import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.context.attack.ObbAttackContext;
import com.kltyton.bonehitboxlib.api.context.collision.ObbCollisionContext;
import com.kltyton.bonehitboxlib.api.context.interaction.ObbInteractionContext;
import com.kltyton.bonehitboxlib.api.registration.registrar.ObbBoneRegistrar;
import com.kltyton.bonehitboxlib.api.state.runtime.ObbEntityState;
import com.kltyton.bonehitboxlib.api.state.runtime.ObbEntityStates;
import com.kltyton.bonehitboxlib.api.state.runtime.ObbBoneState;

import net.minecraft.world.InteractionResult;

/**
 * CN: 实体显式 opt-in 接口。实现者必须注册哪些视觉模型骨骼/cube 应成为 OBB。
 * EN: Explicit entity opt-in interface. Implementors must register which visual-model bones/cubes become OBBs.
 */
public interface BoneHitboxEntity {
    /**
     * CN: 注册该实体的 OBB 骨骼。可使用 ObbBoneRegistrar.ALL、BASE 或精确名称。
     * EN: Registers this entity's OBB bones using ObbBoneRegistrar.ALL, BASE, or exact names.
     */
    void bonehitboxlib$registerObbBones(ObbBoneRegistrar registrar);

    /** Restricts tracked clients allowed to supply model poses; override for an authoritative owner. */
    default boolean bonehitboxlib$acceptObbReporter(net.minecraft.server.level.ServerPlayer reporter) {
        return true;
    }

    /** Maximum model distance from the entity root, in blocks. Large models may explicitly raise this limit. */
    default double bonehitboxlib$maxSnapshotRadius() {
        return 64.0;
    }

    /** CN: 返回该实体的持久/运行时 OBB 状态。EN: Returns this entity's persistent/runtime OBB state. */
    default ObbEntityState bonehitboxlib$obbState() {
        return ObbEntityStates.get(this);
    }

    /** CN: 获取一个已注册/已发现的 OBB 骨骼。EN: Resolves one registered and discovered OBB bone. */
    default Optional<ObbBoneState> bonehitboxlib$obbBone(ObbBoneKey key) {
        return bonehitboxlib$obbState().resolve(key);
    }

    default boolean bonehitboxlib$hasObbAttribute(ObbBoneKey key, ObbBoneAttribute attribute) {
        return bonehitboxlib$obbBone(key).map(bone -> bone.hasAttribute(attribute)).orElse(false);
    }

    /** CN: 永久开关一个骨骼属性，包括注册时设置的属性。EN: Permanently toggles a bone attribute, including a registered default. */
    default boolean bonehitboxlib$setObbAttribute(ObbBoneKey key, ObbBoneAttribute attribute, boolean enabled) {
        Optional<ObbBoneState> bone = bonehitboxlib$obbBone(key);
        bone.ifPresent(value -> value.setAttribute(attribute, enabled));
        return bone.isPresent();
    }

    /** CN: 在作用域中临时开关属性，清除作用域后自动恢复。EN: Temporarily toggles an attribute until its scope is cleared. */
    default boolean bonehitboxlib$setTemporaryObbAttribute(String scope, ObbBoneKey key,
            ObbBoneAttribute attribute, boolean enabled) {
        Optional<ObbBoneState> bone = bonehitboxlib$obbBone(key);
        bone.ifPresent(value -> value.setTemporaryAttribute(scope, attribute, enabled));
        return bone.isPresent();
    }

    default void bonehitboxlib$clearTemporaryObbAttributes(String scope) {
        bonehitboxlib$obbState().clearTemporaryAttributeScope(scope);
    }

    default boolean bonehitboxlib$isAttackBox(ObbBoneKey key) {
        return bonehitboxlib$obbBone(key).map(ObbBoneState::isAttackBox).orElse(false);
    }

    default boolean bonehitboxlib$setAttackBox(ObbBoneKey key, boolean enabled) {
        return bonehitboxlib$setObbAttribute(key, ObbBoneAttribute.ATTACK, enabled);
    }

    default boolean bonehitboxlib$isHurtBox(ObbBoneKey key) {
        return bonehitboxlib$obbBone(key).map(ObbBoneState::isHurtBox).orElse(false);
    }

    default boolean bonehitboxlib$setHurtBox(ObbBoneKey key, boolean enabled) {
        return bonehitboxlib$setObbAttribute(key, ObbBoneAttribute.HURT, enabled);
    }

    default boolean bonehitboxlib$isCollisionBox(ObbBoneKey key) {
        return bonehitboxlib$obbBone(key).map(ObbBoneState::isCollisionBox).orElse(false);
    }

    default boolean bonehitboxlib$setCollisionBox(ObbBoneKey key, ObbCollisionMode mode) {
        Optional<ObbBoneState> bone = bonehitboxlib$obbBone(key);
        bone.ifPresent(value -> value.setCollisionMode(mode));
        return bone.isPresent();
    }

    default boolean bonehitboxlib$setTemporaryAttackBox(String scope, ObbBoneKey key, boolean enabled) {
        return bonehitboxlib$setTemporaryObbAttribute(scope, key, ObbBoneAttribute.ATTACK, enabled);
    }

    default boolean bonehitboxlib$setTemporaryHurtBox(String scope, ObbBoneKey key, boolean enabled) {
        return bonehitboxlib$setTemporaryObbAttribute(scope, key, ObbBoneAttribute.HURT, enabled);
    }

    default boolean bonehitboxlib$setTemporaryCollisionBox(String scope, ObbBoneKey key, ObbCollisionMode mode) {
        Optional<ObbBoneState> bone = bonehitboxlib$obbBone(key);
        bone.ifPresent(value -> value.setTemporaryCollision(scope, mode));
        return bone.isPresent();
    }

    /**
     * CN: 是否用命中部位闪红替代全身受击闪红。
     * EN: Whether the full-body hurt flash should be replaced by the hit-part flash.
     */
    default boolean bonehitboxlib$onlyHitPartTurnsRed() {
        return true;
    }

    /**
     * CN: 原版攻击已发生、攻击骨骼回调派发前调用；返回 false 会停止 OBB 扩展回调，但不会撤销原版攻击。
     * EN: Called before attack-bone callbacks after the vanilla attack occurs; false stops OBB extension callbacks but does not undo vanilla damage.
     */
    default boolean bonehitboxlib$beforeAttackBoxAttack(ObbAttackContext context) {
        return true;
    }

    /** CN: 攻击骨骼主回调。EN: Main attack-bone callback. */
    default void bonehitboxlib$onAttackBoxAttack(ObbAttackContext context) {
    }

    /** CN: 攻击骨骼回调链结束后调用。EN: Called after the attack-bone callback chain. */
    default void bonehitboxlib$afterAttackBoxAttack(ObbAttackContext context) {
    }

    /**
     * CN: 受击骨骼主回调前调用；返回 false 只停止 OBB 受击扩展回调。
     * EN: Called before hurt-bone callbacks; false only stops OBB hurt extension callbacks.
     */
    default boolean bonehitboxlib$beforeHurtBoxHurt(ObbAttackContext context) {
        return true;
    }

    /** CN: 受击骨骼主回调。EN: Main hurt-bone callback. */
    default void bonehitboxlib$onHurtBoxHurt(ObbAttackContext context) {
    }

    /** CN: 受击骨骼回调链结束后调用。EN: Called after the hurt-bone callback chain. */
    default void bonehitboxlib$afterHurtBoxHurt(ObbAttackContext context) {
    }

    /**
     * CN: 骨骼交互前调用；非 PASS 结果会跳过主交互回调并直接作为 OBB 交互结果。
     * EN: Called before bone interaction; a non-PASS result skips the main interaction callback and becomes the OBB interaction result.
     */
    default InteractionResult bonehitboxlib$beforeObbInteract(ObbInteractionContext context) {
        return InteractionResult.PASS;
    }

    /** CN: 骨骼交互主回调。EN: Main bone-interaction callback. */
    default InteractionResult bonehitboxlib$onObbInteract(ObbInteractionContext context) {
        return InteractionResult.PASS;
    }

    /** CN: 骨骼交互结果确定后调用。EN: Called after the bone-interaction result is determined. */
    default void bonehitboxlib$afterObbInteract(ObbInteractionContext context, InteractionResult result) {
    }

    /**
     * CN: 规范化碰撞的 BEGIN 或 END 阶段回调；同一连续骨骼对接触不会因多客户端上报而重复 BEGIN。
     * EN: Called for BEGIN or END of a canonical contact; one continuous bone-pair contact does not duplicate BEGIN across reporters.
     */
    default void bonehitboxlib$onObbCollision(ObbCollisionContext context) {
    }

    /**
     * CN: 纯 OBB 接触攻击事件前置 hook；它不代表原版攻击或伤害已经发生。
     * EN: Pre-hook for an OBB-only contact attack; it does not imply vanilla attack or damage occurred.
     */
    default boolean bonehitboxlib$beforeObbCollisionAttack(ObbAttackContext context) {
        return true;
    }

    default void bonehitboxlib$onObbCollisionAttack(ObbAttackContext context) {
    }

    default void bonehitboxlib$afterObbCollisionAttack(ObbAttackContext context) {
    }

    /** CN: 纯 OBB 接触受击事件前置 hook。EN: Pre-hook for an OBB-only contact hurt event. */
    default boolean bonehitboxlib$beforeObbCollisionHurt(ObbAttackContext context) {
        return true;
    }

    default void bonehitboxlib$onObbCollisionHurt(ObbAttackContext context) {
    }

    default void bonehitboxlib$afterObbCollisionHurt(ObbAttackContext context) {
    }
}
