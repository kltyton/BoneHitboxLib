package com.kltyton.bonehitboxlib.api.block.shape;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Automatic model-derived multi-cell block whose selection outline is local to
 * the exact cell targeted by the player.
 *
 * <p>Extend this class, pass the registered block id to the constructor, and
 * use the consuming loader to register the concrete block. The packaged
 * {@code assets/<namespace>/models/block/<path>.json} model supplies placement
 * footprint, collision, interaction and selection shapes.</p>
 */
public abstract class AutoPartShapeBlock extends HorizontalDirectionalBlock {
    public static final int MAX_PART_OFFSET = ModelShapeCache.MAX_PART_OFFSET;
    public static final IntegerProperty ORIGIN_X =
            IntegerProperty.create("origin_x", 0, MAX_PART_OFFSET * 2);
    public static final IntegerProperty ORIGIN_Y =
            IntegerProperty.create("origin_y", 0, MAX_PART_OFFSET * 2);
    public static final IntegerProperty ORIGIN_Z =
            IntegerProperty.create("origin_z", 0, MAX_PART_OFFSET * 2);

    private static final ThreadLocal<Boolean> REMOVING_STRUCTURE =
            ThreadLocal.withInitial(() -> false);

    private final ResourceLocation modelId;
    private final BlockShapeMode shapeMode;
    private final java.util.Map<Direction, ModelShapeCache.ShapeSet> obbShapes = new java.util.EnumMap<>(Direction.class);
    private final java.util.Map<Direction, ModelShapeCache.ShapeSet> aabbShapes = new java.util.EnumMap<>(Direction.class);

    protected AutoPartShapeBlock(ResourceLocation blockId, Properties properties) {
        this(blockId, properties, BlockShapeMode.OBB);
    }

    protected AutoPartShapeBlock(ResourceLocation blockId, Properties properties, BlockShapeMode mode) {
        super(properties.dynamicShape().noOcclusion().pushReaction(PushReaction.BLOCK));
        this.modelId = blockId.withPrefix("block/");
        this.shapeMode = java.util.Objects.requireNonNull(mode);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            ModelShapeCache.ShapeSet original = ModelShapeCache.get(modelId, facing);
            obbShapes.put(facing, original);
            java.util.Map<ModelShapeCache.Cell, VoxelShape> local = new java.util.HashMap<>();
            original.localShapes().forEach((cell, shape) -> local.put(cell, BlockShapeMode.AABB.apply(shape)));
            VoxelShape whole = Shapes.empty();
            for (var entry : local.entrySet()) {
                var cell = entry.getKey();
                whole = Shapes.or(whole, entry.getValue().move(cell.x(), cell.y(), cell.z()));
            }
            aabbShapes.put(facing, new ModelShapeCache.ShapeSet(java.util.Map.copyOf(local),
                    original.occupiedCells(), whole.optimize()));
        }
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ORIGIN_X, encodeOffset(0))
                .setValue(ORIGIN_Y, encodeOffset(0))
                .setValue(ORIGIN_Z, encodeOffset(0)));
    }

    @Override
    public final @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState originState = getOriginStateForPlacement(context);
        if (originState == null) {
            return null;
        }
        BlockState state = originState
                .setValue(ORIGIN_X, encodeOffset(0))
                .setValue(ORIGIN_Y, encodeOffset(0))
                .setValue(ORIGIN_Z, encodeOffset(0));
        return canPlaceAllParts(context, state) ? state : null;
    }

    /**
     * Origin-only placement-state hook.
     */
    protected @Nullable BlockState getOriginStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public final void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && isOriginPart(state)) {
            placeLinkedParts(level, pos, state);
            placedOrigin(level, pos, state, placer, stack);
        }
    }

    /**
     * Origin-only placement hook for concrete blocks.
     */
    protected void placedOrigin(
            Level level,
            BlockPos originPos,
            BlockState originState,
            @Nullable LivingEntity placer,
            ItemStack stack) {
    }

    @Override
    public final VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return localShape(level, state, pos);
    }

    @Override
    public final VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return localShape(level, state, pos);
    }

    @Override
    public final VoxelShape getInteractionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos) {
        return localShape(level, state, pos);
    }

    @Override
    public final RenderShape getRenderShape(BlockState state) {
        return isOriginPart(state) ? getOriginRenderShape(state) : RenderShape.INVISIBLE;
    }

    /**
     * Origin-only render-shape hook. Linked parts are always invisible.
     */
    protected RenderShape getOriginRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }

    @Override
    public final InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        Origin origin = resolveOrigin(level, state, pos);
        if (origin == null) { return InteractionResult.PASS; }
        ItemStack stack = player.getItemInHand(hand);
        return stack.isEmpty()
                ? useOriginWithoutItem(origin.state(), level, origin.pos(), player, moveHit(hitResult, origin.pos()))
                : useOriginItemOn(stack, origin.state(), level, origin.pos(), player, hand, moveHit(hitResult, origin.pos()));
    }

    protected InteractionResult useOriginWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        return super.use(state, level, pos, player, InteractionHand.MAIN_HAND, hitResult);
    }

    protected InteractionResult useOriginItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        return super.use(state, level, pos, player, hand, hitResult);
    }

    @Override
    public final @Nullable MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos pos) {
        Origin origin = resolveOrigin(level, state, pos);
        return origin == null
                ? null
                : getOriginMenuProvider(origin.state(), level, origin.pos());
    }

    /**
     * Origin-only menu-provider hook, including spectator interaction.
     */
    protected @Nullable MenuProvider getOriginMenuProvider(
            BlockState state,
            Level level,
            BlockPos pos) {
        return super.getMenuProvider(state, level, pos);
    }

    @Override
    public final boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (isOriginPart(state)) {
            return canOriginSurvive(state, level, pos);
        }
        return resolveOrigin(level, state, pos) != null;
    }

    /**
     * Origin-only survival hook.
     */
    protected boolean canOriginSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return super.canSurvive(state, level, pos);
    }

    @Override
    public final BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (!isOriginPart(state)) {
            return resolveOrigin(level, state, pos) == null ? Blocks.AIR.defaultBlockState() : state;
        }
        return updateOriginShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    protected BlockState updateOriginShape(BlockState state, Direction direction, BlockState neighbourState,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public final void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            if (!REMOVING_STRUCTURE.get()) {
                Origin origin = resolveOriginOrStored(level, state, pos);
                if (origin != null) { removeStructure(level, origin, !origin.pos().equals(pos)); }
            }
            removedPart(state, server, pos, movedByPiston);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected void removedPart(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) { }

    @Override
    public final void playerWillDestroy(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player) {
        if (level.isClientSide() || isOriginPart(state)) {
            super.playerWillDestroy(level, pos, state, player);
            return;
        }

        Origin origin = resolveOrigin(level, state, pos);
        if (origin == null) {
            super.playerWillDestroy(level, pos, state, player);
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(origin.pos());
        ItemStack tool = player.getMainHandItem().copy();
        BlockState adjustedState = origin.state();
        super.playerWillDestroy(level, origin.pos(), adjustedState, player);
        removeStructure(level, origin, true);
        destroy(level, origin.pos(), adjustedState);
        if (!player.isCreative() && player.hasCorrectToolForDrops(adjustedState)) {
            playerDestroyOrigin(
                    level,
                    player,
                    origin.pos(),
                    adjustedState,
                    blockEntity,
                    tool);
        }
    }

    @Override
    public final void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool) {
        if (isOriginPart(state)) {
            playerDestroyOrigin(level, player, pos, state, blockEntity, tool);
        }
    }

    /**
     * Origin-only player-destruction hook.
     */
    protected void playerDestroyOrigin(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public final void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        explodeOrigin(level, pos, explosion);
    }

    protected void explodeOrigin(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
    }

    @Override
    public final List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (isOriginPart(state)) {
            return getOriginDrops(state, params);
        }
        Vec3 lootOrigin = params.getOptionalParameter(LootContextParams.ORIGIN);
        if (lootOrigin == null) {
            return List.of();
        }
        Origin origin = resolveOrigin(
                params.getLevel(),
                state,
                BlockPos.containing(lootOrigin));
        if (origin == null) {
            return List.of();
        }
        params.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin.pos()))
                .withOptionalParameter(
                        LootContextParams.BLOCK_ENTITY,
                        params.getLevel().getBlockEntity(origin.pos()));
        return getOriginDrops(origin.state(), params);
    }

    /**
     * Origin-only loot hook.
     */
    protected List<ItemStack> getOriginDrops(BlockState state, LootParams.Builder params) {
        return super.getDrops(state, params);
    }

    @Override
    public final ItemStack getCloneItemStack(
            BlockGetter level,
            BlockPos pos,
            BlockState state) {
        Origin origin = resolveOrigin(level, state, pos);
        return origin == null
                ? ItemStack.EMPTY
                : getOriginCloneItemStack(
                        level,
                        origin.pos(),
                        origin.state());
    }

    /**
     * Origin-only clone-item hook.
     */
    protected ItemStack getOriginCloneItemStack(
            BlockGetter level,
            BlockPos pos,
            BlockState state) {
        return super.getCloneItemStack(level, pos, state);
    }

    /**
     * Returns the canonical origin position encoded in a linked part state.
     */
    public final BlockPos getOriginPosition(BlockState state, BlockPos partPos) {
        return partPos.offset(
                decodeOffset(state.getValue(ORIGIN_X)),
                decodeOffset(state.getValue(ORIGIN_Y)),
                decodeOffset(state.getValue(ORIGIN_Z)));
    }

    /**
     * Returns whether the supplied state is the canonical origin part.
     */
    public final boolean isOriginPart(BlockState state) {
        return decodeOffset(state.getValue(ORIGIN_X)) == 0
                && decodeOffset(state.getValue(ORIGIN_Y)) == 0
                && decodeOffset(state.getValue(ORIGIN_Z)) == 0;
    }

    /**
     * Returns whether this part still points to a valid origin of this block.
     */
    public final boolean isValidPart(BlockGetter level, BlockState state, BlockPos pos) {
        return resolveOrigin(level, state, pos) != null;
    }

    public final VoxelShape wholeShape(
            BlockGetter level,
            BlockState state,
            BlockPos pos) {
        Origin origin = resolveOrigin(level, state, pos);
        return origin == null
                ? Shapes.empty()
                : shapes(origin.state()).wholeShape();
    }

    public final BlockShapeMode shapeMode() { return shapeMode; }

    public final ResourceLocation modelId() {
        return modelId;
    }

    protected boolean canPlaceAllParts(BlockPlaceContext context, BlockState originState) {
        Level level = context.getLevel();
        BlockPos originPos = context.getClickedPos();
        ModelShapeCache.ShapeSet shapeSet = shapes(originState);
        for (ModelShapeCache.Cell cell : shapeSet.occupiedCells()) {
            BlockPos partPos = originPos.offset(cell.x(), cell.y(), cell.z());
            if (!level.hasChunkAt(partPos) || !level.isInWorldBounds(partPos)
                    || !level.getWorldBorder().isWithinBounds(partPos)
                    || !level.getBlockState(partPos).canBeReplaced(context)) {
                return false;
            }
            VoxelShape placedShape = shapeSet.localShape(cell).move(partPos.getX(), partPos.getY(), partPos.getZ());
            if (!level.isUnobstructed(null, placedShape)) {
                return false;
            }
        }
        return true;
    }

    private void placeLinkedParts(Level level, BlockPos originPos, BlockState originState) {
        for (ModelShapeCache.Cell cell : shapes(originState).occupiedCells()) {
            if (cell.equals(ModelShapeCache.Cell.ORIGIN)) {
                continue;
            }
            BlockPos partPos = originPos.offset(cell.x(), cell.y(), cell.z());
            level.setBlock(partPos, partState(originState, cell), Block.UPDATE_ALL);
        }
    }

    private void removeStructure(Level level, Origin origin, boolean includeOrigin) {
        REMOVING_STRUCTURE.set(true);
        try {
            for (ModelShapeCache.Cell cell : shapes(origin.state()).occupiedCells()) {
                if (!includeOrigin && cell.equals(ModelShapeCache.Cell.ORIGIN)) {
                    continue;
                }
                BlockPos partPos =
                        origin.pos().offset(cell.x(), cell.y(), cell.z());
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(this)
                        && getOriginPosition(partState, partPos).equals(origin.pos())) {
                    level.setBlock(
                            partPos,
                            Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                }
            }
        } finally {
            REMOVING_STRUCTURE.remove();
        }
    }

    private VoxelShape localShape(BlockGetter level, BlockState state, BlockPos pos) {
        Origin origin = resolveOrigin(level, state, pos);
        if (origin == null) {
            return Shapes.empty();
        }
        ModelShapeCache.Cell cell = new ModelShapeCache.Cell(
                pos.getX() - origin.pos().getX(),
                pos.getY() - origin.pos().getY(),
                pos.getZ() - origin.pos().getZ());
        return shapes(origin.state()).localShape(cell);
    }

    private Origin resolveOrigin(BlockGetter level, BlockState state, BlockPos pos) {
        if (!state.is(this)) {
            return null;
        }
        BlockPos originPos = getOriginPosition(state, pos);
        BlockState originState = level.getBlockState(originPos);
        return originState.is(this) && isOriginPart(originState)
                ? new Origin(originPos, originState)
                : null;
    }

    private Origin resolveOriginOrStored(BlockGetter level, BlockState state, BlockPos pos) {
        Origin origin = resolveOrigin(level, state, pos);
        if (origin != null || isOriginPart(state)) {
            return origin != null ? origin : new Origin(pos, state);
        }
        BlockPos originPos = getOriginPosition(state, pos);
        BlockState originState = level.getBlockState(originPos);
        return originState.is(this) && isOriginPart(originState)
                ? new Origin(originPos, originState)
                : null;
    }

    private ModelShapeCache.ShapeSet shapes(BlockState state) {
        boolean allowed = com.kltyton.bonehitboxlib.config.common.BoneHitboxConfig.allowsBlockModel(
                net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this));
        boolean obb = allowed && (shapeMode == BlockShapeMode.OBB
                || com.kltyton.bonehitboxlib.config.common.BoneHitboxConfig.forceAllBlockObb());
        return (obb ? obbShapes : aabbShapes).get(state.getValue(FACING));
    }

    private BlockState partState(BlockState originState, ModelShapeCache.Cell cell) {
        return originState
                .setValue(ORIGIN_X, encodeOffset(-cell.x()))
                .setValue(ORIGIN_Y, encodeOffset(-cell.y()))
                .setValue(ORIGIN_Z, encodeOffset(-cell.z()));
    }

    private static BlockHitResult moveHit(BlockHitResult hitResult, BlockPos blockPos) {
        return new BlockHitResult(
                hitResult.getLocation(),
                hitResult.getDirection(),
                blockPos,
                hitResult.isInside());
    }

    private static int encodeOffset(int offset) {
        if (offset < -MAX_PART_OFFSET || offset > MAX_PART_OFFSET) {
            throw new IllegalArgumentException(
                    "automatic block part offset %d is outside [%d, %d]"
                            .formatted(offset, -MAX_PART_OFFSET, MAX_PART_OFFSET));
        }
        return offset + MAX_PART_OFFSET;
    }

    private static int decodeOffset(int stored) {
        return stored - MAX_PART_OFFSET;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ORIGIN_X, ORIGIN_Y, ORIGIN_Z);
    }

    private record Origin(BlockPos pos, BlockState state) {
    }
}
