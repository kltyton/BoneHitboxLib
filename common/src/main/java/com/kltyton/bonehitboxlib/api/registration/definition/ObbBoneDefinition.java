package com.kltyton.bonehitboxlib.api.registration.definition;

import com.kltyton.bonehitboxlib.api.registration.selector.ObbBoneSelector;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.kltyton.bonehitboxlib.api.bone.attribute.ObbBoneAttribute;
import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.registration.data.ObbBoneDataDefinition;

/**
 * CN: 一条冻结后的实体骨骼注册定义。
 * EN: One frozen entity-bone registration definition.
 */
public record ObbBoneDefinition(
        ObbBoneSelector selector,
        Set<ObbBoneAttribute> attributes,
        ObbCollisionMode collisionMode,
        List<ObbBoneDataDefinition> dataDefinitions) {
    public ObbBoneDefinition {
        attributes = attributes.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(attributes));
        collisionMode = collisionMode == null ? ObbCollisionMode.NONE : collisionMode;
        dataDefinitions = List.copyOf(dataDefinitions);
    }
}
