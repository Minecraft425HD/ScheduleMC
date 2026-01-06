package de.rolandsw.schedulemc.economy.blocks;

import de.rolandsw.schedulemc.economy.blockentity.ATMBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class ATMBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF =
            EnumProperty.create("half", DoubleBlockHalf.class);

    public ATMBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, net.minecraft.core.Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    // --- BlockEntity Support ---
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // NUR unterer Block hat BlockEntity
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return new ATMBlockEntity(pos, state);
        }
        return null;
    }

    // --- Platzierungslogik (setzt LOWER + UPPER Block) ---
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();

        // Prüfen ob oben Platz ist
        if (!level.getBlockState(pos.above()).canBeReplaced(ctx)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            BlockState upper = this.defaultBlockState()
                    .setValue(FACING, state.getValue(FACING))
                    .setValue(HALF, DoubleBlockHalf.UPPER);

            level.setBlock(pos.above(), upper, 3);
        }
    }

    // --- RECHTSKLICK → GUI ÖFFNEN ---
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, 
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Wenn oberer Block geklickt → verwende unteren Block (dort ist die BlockEntity)
        final BlockPos targetPos; // FINAL für Lambda!
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            targetPos = pos.below();
        } else {
            targetPos = pos;
        }

        // Hole BlockEntity vom UNTEREN Block
        BlockEntity be = level.getBlockEntity(targetPos);
        if (!(be instanceof ATMBlockEntity atmBE)) {
            return InteractionResult.FAIL;
        }

        // Öffne GUI
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("ATM");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
                    return new de.rolandsw.schedulemc.economy.menu.ATMMenu(
                        id, 
                        playerInventory, 
                        atmBE, 
                        targetPos
                    );
                }
            }, targetPos);
        }

        return InteractionResult.CONSUME;
    }

    // --- Abbau-Logik: baut immer beide Teile ab ---
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);

            if (half == DoubleBlockHalf.LOWER) {
                BlockPos upperPos = pos.above();
                BlockState upperState = level.getBlockState(upperPos);
                if (upperState.getBlock() == this && upperState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                    level.setBlock(upperPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, upperPos, Block.getId(upperState));
                }
            } else {
                BlockPos lowerPos = pos.below();
                BlockState lowerState = level.getBlockState(lowerPos);
                if (lowerState.getBlock() == this && lowerState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    BlockState newState = lowerState.getFluidState().is(net.minecraft.tags.FluidTags.WATER) 
                        ? net.minecraft.world.level.block.Blocks.WATER.defaultBlockState() 
                        : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                    level.setBlock(lowerPos, newState, 35);
                    level.levelEvent(player, 2001, lowerPos, Block.getId(lowerState));
                }
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    // --- Kollision / Hitbox ---
    private static final VoxelShape FULL_BLOCK =
            Block.box(0, 0, 0, 16, 16, 16);

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter getter,
                               BlockPos pos, CollisionContext ctx) {
        return FULL_BLOCK;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter getter,
                                        BlockPos pos, CollisionContext ctx) {
        return FULL_BLOCK;
    }

    // --- Rotation / Spiegeln ---
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    // --- Damit Kolben den Block NICHT verschieben ---
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    // Enum für die zwei Hälften
    public enum DoubleBlockHalf implements net.minecraft.util.StringRepresentable {
        LOWER("lower"),
        UPPER("upper");

        private final String name;

        DoubleBlockHalf(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
