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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;

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

    public synchronized void save(CompoundTag output) {
        output.putInt("version", SAVE_VERSION);
        ListTag boneOutputs = new ListTag();
        for (ObbBoneState bone : bones.values()) {
            CompoundTag boneOutput = new CompoundTag();
            boneOutput.putString("source", bone.key().source());
            boneOutput.putString("name", bone.key().name());
            boneOutput.putInt("cube", bone.key().cubeIndex());
            boneOutput.putInt("attributes", ObbBoneAttribute.toMask(bone.persistentAttributes()));
            boneOutput.putInt("disabled_attributes", ObbBoneAttribute.toMask(bone.disabledAttributes()));
            boneOutput.putInt("collision_mode_override", bone.persistentCollisionModeOverride().map(Enum::ordinal).orElse(-1));
            ListTag dataOutputs = new ListTag();
            Map<String, com.mojang.serialization.Dynamic<?>> retained = unreadData.getOrDefault(bone.key(), Map.of());
            retained.forEach((id, value) -> {
                CompoundTag rawOutput = new CompoundTag();
                rawOutput.putString("id", id);
                rawOutput.put("value", value.convert(NbtOps.INSTANCE).getValue().copy());
                dataOutputs.add(rawOutput);
            });
            for (Map.Entry<ObbDataKey<?>, Object> entry : bone.dataEntries().entrySet()) {
                if (retained.containsKey(entry.getKey().id().toString())) { continue; }
                CompoundTag dataOutput = new CompoundTag();
                dataOutput.putString("id", entry.getKey().id().toString());
                writeData(dataOutput, entry.getKey(), entry.getValue());
                dataOutputs.add(dataOutput);
            }
            if (!dataOutputs.isEmpty()) { boneOutput.put("data", dataOutputs); }
            boneOutputs.add(boneOutput);
        }
        if (!boneOutputs.isEmpty()) { output.put("bones", boneOutputs); }
        ListTag groupOutputs = new ListTag();
        for (Map.Entry<AttributeGroupKey, Set<String>> entry : persistentAttributeGroups.entrySet()) {
            CompoundTag groupOutput = new CompoundTag();
            groupOutput.putString("source", entry.getKey().source());
            groupOutput.putInt("attribute", entry.getKey().attribute().ordinal());
            ListTag names = new ListTag();
            entry.getValue().forEach(name -> names.add(StringTag.valueOf(name)));
            groupOutput.put("names", names);
            groupOutputs.add(groupOutput);
        }
        if (!groupOutputs.isEmpty()) { output.put("attribute_groups", groupOutputs); }
    }

    public synchronized void load(CompoundTag input) {
        int saveVersion = input.contains("version", Tag.TAG_ANY_NUMERIC) ? input.getInt("version") : 1;
        bones.clear();
        unreadData.clear();
        persistentAttributeGroups.clear();
        temporaryAttributeGroups.clear();
        temporaryCollisionModes.clear();
        for (Tag rawGroup : input.getList("attribute_groups", Tag.TAG_COMPOUND)) {
            CompoundTag groupInput = (CompoundTag) rawGroup;
            String source = groupInput.getString("source");
            int ordinal = groupInput.contains("attribute", Tag.TAG_ANY_NUMERIC) ? groupInput.getInt("attribute") : -1;
            if (ordinal < 0 || ordinal >= ObbBoneAttribute.values().length) { continue; }
            Set<String> names = new LinkedHashSet<>();
            groupInput.getList("names", Tag.TAG_STRING).forEach(name -> names.add(name.getAsString()));
            if (!names.isEmpty()) {
                persistentAttributeGroups.put(new AttributeGroupKey(source, ObbBoneAttribute.values()[ordinal]), names);
            }
        }
        for (Tag rawBone : input.getList("bones", Tag.TAG_COMPOUND)) {
            CompoundTag boneInput = (CompoundTag) rawBone;
            String source = boneInput.getString("source");
            String name = boneInput.getString("name");
            int cube = boneInput.contains("cube", Tag.TAG_ANY_NUMERIC) ? boneInput.getInt("cube") : -1;
            if (name.isBlank() || cube < 0) { continue; }
            ObbBoneKey key = new ObbBoneKey(source, name, cube);
            ObbBoneState bone = resolve(key).orElseGet(() -> {
                ObbBoneState restored = new ObbBoneState(key, Set.of(), ObbCollisionMode.NONE, Map.of());
                bones.put(key, restored);
                return restored;
            });
            int collisionOrdinal = saveVersion >= SAVE_VERSION && boneInput.contains("collision_mode_override", Tag.TAG_ANY_NUMERIC)
                    ? boneInput.getInt("collision_mode_override") : -1;
            ObbCollisionMode collisionMode = collisionOrdinal >= 0 && collisionOrdinal < ObbCollisionMode.values().length
                    ? ObbCollisionMode.values()[collisionOrdinal] : null;
            Set<ObbBoneAttribute> explicitAttributes = saveVersion >= SAVE_VERSION
                    ? ObbBoneAttribute.fromMask(boneInput.getInt("attributes")) : Set.of();
            bone.loadPersistentState(explicitAttributes,
                    ObbBoneAttribute.fromMask(boneInput.getInt("disabled_attributes")), collisionMode);
            for (Tag rawData : boneInput.getList("data", Tag.TAG_COMPOUND)) {
                CompoundTag dataInput = (CompoundTag) rawData;
                String rawId = dataInput.getString("id");
                ResourceLocation id = ResourceLocation.tryParse(rawId);
                Optional<ObbDataKey<?>> dataKey = id == null ? Optional.empty()
                        : id.equals(Constants.id("example_part_health")) ? Optional.of(ObbBuiltinDataKeys.PART_HEALTH)
                        : ObbDataRegistry.get(id);
                if (dataKey.isPresent() && readData(dataInput, bone, dataKey.get())) { continue; }
                Tag value = dataInput.get("value");
                if (value != null) {
                    unreadData.computeIfAbsent(key, ignored -> new LinkedHashMap<>()).put(rawId,
                            new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, value.copy()));
                }
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
    private static <T> void writeData(CompoundTag output, ObbDataKey<T> key, Object value) {
        key.codec().encodeStart(NbtOps.INSTANCE, (T) value)
                .resultOrPartial(error -> Constants.LOG.warn("Unable to encode OBB data {}: {}", key.id(), error))
                .ifPresent(encoded -> output.put("value", encoded));
    }

    private static <T> boolean readData(CompoundTag input, ObbBoneState state, ObbDataKey<T> key) {
        Tag encoded = input.get("value");
        if (encoded == null) { return false; }
        Optional<T> value = key.codec().parse(NbtOps.INSTANCE, encoded).result();
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
