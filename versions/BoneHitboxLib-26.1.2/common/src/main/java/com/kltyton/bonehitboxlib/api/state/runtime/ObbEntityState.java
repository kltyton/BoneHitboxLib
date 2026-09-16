package com.kltyton.bonehitboxlib.api.state.runtime;

import com.kltyton.bonehitboxlib.api.entity.BoneHitboxEntity;
import com.kltyton.bonehitboxlib.api.state.runtime.ObbBoneState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.kltyton.bonehitboxlib.Constants;
import com.kltyton.bonehitboxlib.api.bone.attribute.ObbBoneAttribute;
import com.kltyton.bonehitboxlib.api.bone.key.ObbBoneKey;
import com.kltyton.bonehitboxlib.api.bone.collision.ObbCollisionMode;
import com.kltyton.bonehitboxlib.api.data.builtin.key.ObbBuiltinDataKeys;
import com.kltyton.bonehitboxlib.api.data.key.ObbDataKey;
import com.kltyton.bonehitboxlib.api.data.key.ObbDataRegistry;
import com.kltyton.bonehitboxlib.api.registration.definition.ObbBoneDefinition;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * CN: 一个 BoneHitboxEntity 的注册定义、已发现骨骼、属性覆盖和扩展数据容器。
 * EN: Container for one BoneHitboxEntity's registrations, discovered bones, attribute overlays, and extension data.
 */
public final class ObbEntityState {
    private static final int SAVE_VERSION = 2;
    private static final String LEGACY_TEMPORARY_SCOPE = "legacy";

    private final Map<ObbBoneKey, Map<String, com.mojang.serialization.Dynamic<?>>> unreadData = new LinkedHashMap<>();
    private final List<ObbBoneDefinition> definitions;
    private final Map<ObbBoneKey, ObbBoneState> bones = new LinkedHashMap<>();
    private final Map<AttributeGroupKey, Set<String>> persistentAttributeGroups = new LinkedHashMap<>();
    private final Map<ScopedAttributeGroupKey, Set<String>> temporaryAttributeGroups = new LinkedHashMap<>();
    private final Map<ScopeSourceKey, ObbCollisionMode> temporaryCollisionModes = new LinkedHashMap<>();

    ObbEntityState(List<ObbBoneDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
    }

    public synchronized boolean isRegistered(ObbBoneKey key) {
        return definitions.stream().anyMatch(definition -> definition.selector().matches(key));
    }

    public synchronized Optional<ObbBoneState> resolve(ObbBoneKey key) {
        ObbBoneState existing = bones.get(key);
        if (existing != null) {
            return Optional.of(existing);
        }

        if (bones.size() >= 4096) { return Optional.empty(); }
        EnumSet<ObbBoneAttribute> attributes = EnumSet.noneOf(ObbBoneAttribute.class);
        ObbCollisionMode collisionMode = ObbCollisionMode.NONE;
        Map<ObbDataKey<?>, Object> defaultData = new LinkedHashMap<>();
        boolean matched = false;
        for (ObbBoneDefinition definition : definitions) {
            if (!definition.selector().matches(key)) {
                continue;
            }
            matched = true;
            attributes.addAll(definition.attributes());
            collisionMode = ObbCollisionMode.strongest(collisionMode, definition.collisionMode());
            definition.dataDefinitions().stream()
                    .filter(dataDefinition -> dataDefinition.selector().matches(key))
                    .forEach(dataDefinition -> defaultData.put(
                            dataDefinition.key(),
                            dataDefinition.createValue()));
        }
        if (!matched) {
            return Optional.empty();
        }

        ObbBoneState state = new ObbBoneState(key, attributes, collisionMode, defaultData);
        bones.put(key, state);
        refreshGroupAttributes(state);
        return Optional.of(state);
    }

    /**
     * CN: 仅解析服务端已注册的骨骼；客户端声明的属性和碰撞模式不授予服务端权限。
     * EN: Resolves server-registered bones without granting client-declared attributes or collision modes.
     */
    public synchronized ObbBoneState acceptClientSnapshot(ObbBoneKey key, Set<ObbBoneAttribute> attributes,
            ObbCollisionMode collisionMode) {
        return resolve(key).orElseThrow(() -> new IllegalArgumentException("Unregistered OBB bone: " + key));
    }

    public synchronized Collection<ObbBoneState> bones() {
        return List.copyOf(bones.values());
    }

    /**
     * CN: 该实体是否已由 OBB 物理碰撞替代原版 AABB 实体推挤。永久注册物理碰撞后，即使运行时暂时关闭已发现骨骼，也不会隐式恢复 AABB 推挤。
     * EN: Whether OBB physics replaces vanilla AABB entity pushing. A permanent physical registration remains opted in even while discovered bones are temporarily disabled.
     */
    public synchronized boolean replacesVanillaEntityPush() {
        return definitions.stream().anyMatch(definition -> definition.collisionMode() != ObbCollisionMode.NONE)
                || temporaryCollisionModes.values().stream().anyMatch(mode -> mode != ObbCollisionMode.NONE)
                || bones.values().stream().anyMatch(bone -> bone.collisionMode() != ObbCollisionMode.NONE);
    }

    public synchronized void clearClientSnapshots() {
        bones.values().forEach(ObbBoneState::clearClientSnapshot);
    }

    public synchronized void setPersistentAttributeBones(String source, ObbBoneAttribute attribute, Collection<String> boneNames) {
        replaceGroup(persistentAttributeGroups, source, attribute, boneNames);
        refreshAllGroupAttributes();
    }

    public synchronized Set<String> persistentAttributeBones(String source, ObbBoneAttribute attribute) {
        return Set.copyOf(persistentAttributeGroups.getOrDefault(new AttributeGroupKey(source, attribute), Set.of()));
    }

    /**
     * CN: 在命名作用域内临时设置一组骨骼属性；清除作用域会恢复设置前的有效状态。
     * EN: Temporarily sets an attribute on a bone group in a named scope; clearing the scope restores prior effective state.
     */
    public synchronized void setTemporaryAttributeBones(String scope, String source, ObbBoneAttribute attribute,
            Collection<String> boneNames) {
        ScopedAttributeGroupKey key = new ScopedAttributeGroupKey(scope, source, attribute);
        replaceScopedGroup(temporaryAttributeGroups, key, boneNames);
        if (attribute == ObbBoneAttribute.COLLISION && !temporaryAttributeGroups.containsKey(key)) {
            temporaryCollisionModes.remove(new ScopeSourceKey(scope, source));
        }
        refreshTemporaryScope(key.scope());
    }

    public synchronized Set<String> temporaryAttributeBones(String scope, String source, ObbBoneAttribute attribute) {
        return Set.copyOf(temporaryAttributeGroups.getOrDefault(
                new ScopedAttributeGroupKey(scope, source, attribute),
                Set.of()));
    }

    /**
     * CN: 临时设置一组物理碰撞骨骼及其软/硬模式。
     * EN: Temporarily sets physical collision bones and their soft/hard mode.
     */
    public synchronized void setTemporaryCollisionBones(String scope, String source, ObbCollisionMode mode,
            Collection<String> boneNames) {
        ObbCollisionMode safeMode = mode == null ? ObbCollisionMode.NONE : mode;
        setTemporaryAttributeBones(
                scope,
                source,
                ObbBoneAttribute.COLLISION,
                safeMode == ObbCollisionMode.NONE ? Set.of() : boneNames);
        ScopeSourceKey modeKey = new ScopeSourceKey(scope, source);
        if (safeMode == ObbCollisionMode.NONE) {
            temporaryCollisionModes.remove(modeKey);
        } else {
            temporaryCollisionModes.put(modeKey, safeMode);
        }
        refreshTemporaryScope(modeKey.scope());
    }

    /** CN: 清除一个临时作用域中的全部属性。EN: Clears every temporary attribute in one scope. */
    public synchronized void clearTemporaryAttributeScope(String scope) {
        String safeScope = normalizeScope(scope);
        temporaryAttributeGroups.keySet().removeIf(key -> key.scope().equals(safeScope));
        temporaryCollisionModes.keySet().removeIf(key -> key.scope().equals(safeScope));
        bones.values().forEach(bone -> bone.clearTemporaryAttributes(safeScope));
    }

    /**
     * CN: 旧版单临时组兼容入口；新代码应使用带 scope 的方法。
     * EN: Compatibility entrypoint for the old single temporary group; new code should use scoped methods.
     */
    public synchronized void setTransientAttributeBones(String source, ObbBoneAttribute attribute, Collection<String> boneNames) {
        setTemporaryAttributeBones(LEGACY_TEMPORARY_SCOPE, source, attribute, boneNames);
    }

    public synchronized Set<String> transientAttributeBones(String source, ObbBoneAttribute attribute) {
        return temporaryAttributeBones(LEGACY_TEMPORARY_SCOPE, source, attribute);
    }

    public synchronized void save(ValueOutput output) {
        output.putInt("version", SAVE_VERSION);
        ValueOutput.ValueOutputList boneOutputs = output.childrenList("bones");
        for (ObbBoneState bone : bones.values()) {
            ValueOutput boneOutput = boneOutputs.addChild();
            boneOutput.putString("source", bone.key().source());
            boneOutput.putString("name", bone.key().name());
            boneOutput.putInt("cube", bone.key().cubeIndex());
            boneOutput.putInt("attributes", ObbBoneAttribute.toMask(bone.persistentAttributes()));
            boneOutput.putInt("disabled_attributes", ObbBoneAttribute.toMask(bone.disabledAttributes()));
            boneOutput.putInt("collision_mode_override",
                    bone.persistentCollisionModeOverride().map(Enum::ordinal).orElse(-1));

            ValueOutput.ValueOutputList dataOutputs = boneOutput.childrenList("data");
            Map<String, com.mojang.serialization.Dynamic<?>> retained = unreadData.getOrDefault(bone.key(), Map.of());
            retained.forEach((id, value) -> {
                ValueOutput rawOutput = dataOutputs.addChild();
                rawOutput.putString("id", id);
                rawOutput.store("value", com.mojang.serialization.Codec.PASSTHROUGH, value);
            });
            for (Map.Entry<ObbDataKey<?>, Object> entry : bone.dataEntries().entrySet()) {
                if (retained.containsKey(entry.getKey().id().toString())) { continue; }
                ValueOutput dataOutput = dataOutputs.addChild();
                dataOutput.putString("id", entry.getKey().id().toString());
                writeData(dataOutput, entry.getKey(), entry.getValue());
            }
            if (dataOutputs.isEmpty()) {
                boneOutput.discard("data");
            }
        }
        if (boneOutputs.isEmpty()) {
            output.discard("bones");
        }

        ValueOutput.ValueOutputList groupOutputs = output.childrenList("attribute_groups");
        for (Map.Entry<AttributeGroupKey, Set<String>> entry : persistentAttributeGroups.entrySet()) {
            ValueOutput groupOutput = groupOutputs.addChild();
            groupOutput.putString("source", entry.getKey().source());
            groupOutput.putInt("attribute", entry.getKey().attribute().ordinal());
            ValueOutput.TypedOutputList<String> names = groupOutput.list("names", com.mojang.serialization.Codec.STRING);
            entry.getValue().forEach(names::add);
        }
        if (groupOutputs.isEmpty()) {
            output.discard("attribute_groups");
        }
    }

    public synchronized void load(ValueInput input) {
        int saveVersion = input.getIntOr("version", 1);
        bones.clear();
        unreadData.clear();
        persistentAttributeGroups.clear();
        temporaryAttributeGroups.clear();
        temporaryCollisionModes.clear();
        for (ValueInput groupInput : input.childrenListOrEmpty("attribute_groups")) {
            String source = groupInput.getStringOr("source", "");
            int ordinal = groupInput.getIntOr("attribute", -1);
            if (ordinal < 0 || ordinal >= ObbBoneAttribute.values().length) {
                continue;
            }
            Set<String> names = new LinkedHashSet<>();
            groupInput.listOrEmpty("names", com.mojang.serialization.Codec.STRING).forEach(names::add);
            if (!names.isEmpty()) {
                persistentAttributeGroups.put(new AttributeGroupKey(source, ObbBoneAttribute.values()[ordinal]), names);
            }
        }

        for (ValueInput boneInput : input.childrenListOrEmpty("bones")) {
            String source = boneInput.getStringOr("source", "");
            String name = boneInput.getStringOr("name", "");
            int cube = boneInput.getIntOr("cube", -1);
            if (name.isBlank() || cube < 0) {
                continue;
            }
            ObbBoneKey key = new ObbBoneKey(source, name, cube);
            ObbBoneState bone = resolve(key).orElseGet(() -> {
                ObbBoneState restored = new ObbBoneState(key, Set.of(), ObbCollisionMode.NONE, Map.of());
                bones.put(key, restored);
                return restored;
            });
            int collisionOrdinal = saveVersion >= SAVE_VERSION
                    ? boneInput.getIntOr("collision_mode_override", -1)
                    : -1;
            ObbCollisionMode collisionMode = collisionOrdinal >= 0 && collisionOrdinal < ObbCollisionMode.values().length
                    ? ObbCollisionMode.values()[collisionOrdinal]
                    : null;
            Set<ObbBoneAttribute> explicitAttributes = saveVersion >= SAVE_VERSION
                    ? ObbBoneAttribute.fromMask(boneInput.getIntOr("attributes", 0))
                    : Set.of();
            bone.loadPersistentState(
                    explicitAttributes,
                    ObbBoneAttribute.fromMask(boneInput.getIntOr("disabled_attributes", 0)),
                    collisionMode);

            for (ValueInput dataInput : boneInput.childrenListOrEmpty("data")) {
                String rawId = dataInput.getStringOr("id", "");
                Identifier id = Identifier.tryParse(rawId);
                Optional<ObbDataKey<?>> dataKey = id == null ? Optional.empty()
                        : id.equals(Constants.id("example_part_health")) ? Optional.of(ObbBuiltinDataKeys.PART_HEALTH)
                        : ObbDataRegistry.get(id);
                if (dataKey.isPresent() && readData(dataInput, bone, dataKey.get())) { continue; }
                dataInput.read("value", com.mojang.serialization.Codec.PASSTHROUGH).ifPresent(value ->
                        unreadData.computeIfAbsent(key, ignored -> new LinkedHashMap<>()).put(rawId, value));
                Constants.LOG.warn("Preserving unread OBB bone data key {} on {}", rawId, key.displayName());
            }
        }
        refreshAllGroupAttributes();
    }

    private void refreshAllGroupAttributes() {
        bones.values().forEach(this::refreshGroupAttributes);
    }

    private void refreshGroupAttributes(ObbBoneState state) {
        for (ObbBoneAttribute attribute : ObbBoneAttribute.values()) {
            boolean persistent = groupContains(persistentAttributeGroups, state.key(), attribute);
            state.setPersistentGroupAttribute(attribute, persistent);
        }
        temporaryScopes().forEach(scope -> refreshTemporaryScope(state, scope));
    }

    private void refreshTemporaryScope(String scope) {
        bones.values().forEach(state -> refreshTemporaryScope(state, scope));
    }

    private void refreshTemporaryScope(ObbBoneState state, String scope) {
        for (ObbBoneAttribute attribute : ObbBoneAttribute.values()) {
            boolean enabled = temporaryGroupContains(scope, state.key(), attribute);
            state.setTemporaryAttribute(scope, attribute, enabled);
        }
        if (temporaryGroupContains(scope, state.key(), ObbBoneAttribute.COLLISION)) {
            ObbCollisionMode mode = temporaryCollisionModes.getOrDefault(
                    new ScopeSourceKey(scope, state.key().source()),
                    ObbCollisionMode.NONE);
            if (mode != ObbCollisionMode.NONE) {
                state.setTemporaryCollision(scope, mode);
            }
        }
    }

    private Set<String> temporaryScopes() {
        Set<String> scopes = new LinkedHashSet<>();
        temporaryAttributeGroups.keySet().forEach(key -> scopes.add(key.scope()));
        temporaryCollisionModes.keySet().forEach(key -> scopes.add(key.scope()));
        return scopes;
    }

    private static boolean groupContains(Map<AttributeGroupKey, Set<String>> groups, ObbBoneKey key, ObbBoneAttribute attribute) {
        Set<String> names = groups.get(new AttributeGroupKey(key.source(), attribute));
        return names != null && names.contains(key.name());
    }

    private boolean temporaryGroupContains(String scope, ObbBoneKey key, ObbBoneAttribute attribute) {
        Set<String> names = temporaryAttributeGroups.get(new ScopedAttributeGroupKey(scope, key.source(), attribute));
        return names != null && names.contains(key.name());
    }

    private static void replaceGroup(Map<AttributeGroupKey, Set<String>> groups, String source, ObbBoneAttribute attribute,
            Collection<String> boneNames) {
        AttributeGroupKey key = new AttributeGroupKey(source, attribute);
        Set<String> cleaned = new LinkedHashSet<>();
        if (boneNames != null) {
            for (String boneName : boneNames) {
                if (boneName != null && !boneName.isBlank()) {
                    cleaned.add(boneName);
                }
            }
        }
        if (cleaned.isEmpty()) {
            groups.remove(key);
        } else {
            groups.put(key, cleaned);
        }
    }

    private static void replaceScopedGroup(Map<ScopedAttributeGroupKey, Set<String>> groups, ScopedAttributeGroupKey key,
            Collection<String> boneNames) {
        Set<String> cleaned = cleanNames(boneNames);
        if (cleaned.isEmpty()) {
            groups.remove(key);
        } else {
            groups.put(key, cleaned);
        }
    }

    private static Set<String> cleanNames(Collection<String> boneNames) {
        Set<String> cleaned = new LinkedHashSet<>();
        if (boneNames != null) {
            for (String boneName : boneNames) {
                if (boneName != null && !boneName.isBlank()) {
                    cleaned.add(boneName);
                }
            }
        }
        return cleaned;
    }

    @SuppressWarnings("unchecked")
    private static <T> void writeData(ValueOutput output, ObbDataKey<T> key, Object value) {
        output.store("value", key.codec(), (T) value);
    }

    private static <T> boolean readData(ValueInput input, ObbBoneState state, ObbDataKey<T> key) {
        Optional<T> value = input.read("value", key.codec());
        value.ifPresent(decoded -> state.setData(key, decoded));
        return value.isPresent();
    }

    private record AttributeGroupKey(String source, ObbBoneAttribute attribute) {
        private AttributeGroupKey {
            source = source == null ? "" : source;
        }
    }

    private record ScopedAttributeGroupKey(String scope, String source, ObbBoneAttribute attribute) {
        private ScopedAttributeGroupKey {
            scope = normalizeScope(scope);
            source = source == null ? "" : source;
            Objects.requireNonNull(attribute, "attribute");
        }
    }

    private record ScopeSourceKey(String scope, String source) {
        private ScopeSourceKey {
            scope = normalizeScope(scope);
            source = source == null ? "" : source;
        }
    }

    private static String normalizeScope(String scope) {
        String safeScope = Objects.requireNonNull(scope, "scope").trim();
        if (safeScope.isEmpty()) {
            throw new IllegalArgumentException("Temporary OBB attribute scope must not be blank");
        }
        return safeScope;
    }
}
