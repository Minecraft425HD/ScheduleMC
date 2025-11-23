package de.rolandsw.schedulemc.economy.blocks;

import de.rolandsw.schedulemc.economy.blockentity.CashBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Bargeld-Block - IMMER halber Block (8 Pixel)
 * NUR mit Geldbörse (in Slot 9) abbaubar!
 */
public class CashBlock extends Block implements EntityBlock {
    
    public static final IntegerProperty MONEY_LEVEL = IntegerProperty.create("money_level", 0, 10);
    private static final double MAX_MONEY = 1000.0;
    private static final int WALLET_SLOT = 8; // Slot 9 = Index 8
    
    // ALLE Shapes = 8 Pixel hoch (halber Block)
    private static final VoxelShape SLAB_SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    
    public CashBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0f, 3600000.0f) // UNZERSTÖRBAR wie Bedrock!
                .noOcclusion());
        registerDefaultState(getStateDefinition().any().setValue(MONEY_LEVEL, 0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MONEY_LEVEL);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SLAB_SHAPE; // Immer halber Block
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CashBlockEntity(pos, state);
    }
    
    /**
     * Rechtsklick: NICHTS (wird in CashItem.useOn() behandelt)
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, 
                                 InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.SUCCESS; // Silent
    }
    
    /**
     * Linksklick: NUR mit Geldbörse in Slot 9 - GESAMTEN Restwert abbauen
     */
    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        
        // Prüfe ob Spieler Geldbörse in SLOT 9 hat
        ItemStack wallet = player.getInventory().getItem(WALLET_SLOT); // Slot 9 = Index 8
        if (!(wallet.getItem() instanceof de.rolandsw.schedulemc.economy.items.CashItem)) {
            // KEINE Geldbörse in Slot 9 - Block ist UNZERSTÖRBAR
            return;
        }
        
        double blockValue = getValue(level, pos);
        
        if (blockValue <= 0) {
            player.displayClientMessage(Component.literal(
                "§c✗ Block ist leer!"
            ), true);
            return;
        }
        
        // Füge GESAMTEN Wert zur Geldbörse in Slot 9 hinzu
        de.rolandsw.schedulemc.economy.items.CashItem.addValue(wallet, blockValue);
        
        // Entferne Block komplett
        level.removeBlock(pos, false);
        
        player.displayClientMessage(Component.literal(
            "§a✓ " + String.format("%.0f€", blockValue) + " aufgesammelt"
        ), true);
        
        level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // STATIC HELPER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    public static BlockState createBlock() {
        return de.rolandsw.schedulemc.economy.blocks.EconomyBlocks.CASH_BLOCK.get().defaultBlockState();
    }
    
    public static void setValue(Level level, BlockPos pos, double value) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CashBlockEntity cashBE) {
            cashBE.setValue(value);
            updateBlockState(level, pos, value);
        }
    }
    
    public static void addValue(Level level, BlockPos pos, double amount) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CashBlockEntity cashBE) {
            cashBE.addValue(amount);
            updateBlockState(level, pos, cashBE.getValue());
        }
    }
    
    public static double getValue(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CashBlockEntity cashBE) {
            return cashBE.getValue();
        }
        return 0.0;
    }
    
    public static BlockState getCashBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CashBlock) {
            return state;
        }
        return null;
    }
    
    private static void updateBlockState(Level level, BlockPos pos, double value) {
        // Berechne money_level: 0-10 basierend auf Wert
        int moneyLevel = (int) Math.min(10, (value / MAX_MONEY) * 10);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CashBlock) {
            level.setBlock(pos, state.setValue(MONEY_LEVEL, moneyLevel), 3);
        }
    }
}
