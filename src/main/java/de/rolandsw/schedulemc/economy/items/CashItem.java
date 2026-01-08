package de.rolandsw.schedulemc.economy.items;

import de.rolandsw.schedulemc.economy.blocks.CashBlock;
import de.rolandsw.schedulemc.economy.blocks.EconomyBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Bargeld-Item (Geldbörse)
 * - UNLIMITED Speicher
 * - NUR in Slot 9 (Index 8)
 * - NICHT entfernbar
 * - UUID-basiert (überlebt Tod!)
 */
public class CashItem extends Item {
    
    private static final int PLACE_AMOUNT = 100; // 100€ pro Rechtsklick
    private static final double MAX_PER_BLOCK = 1000.0; // Max 1000€ pro Block
    private static final int WALLET_SLOT = 8; // Slot 9 = Index 8
    
    public CashItem() {
        super(new Properties()
                .stacksTo(1)); // Nur 1 Stack, Wert in NBT
    }
    
    /**
     * RECHTSKLICK: Platziere oder füge Geld hinzu
     * Verwendet IMMER Geldbörse aus Slot 9!
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        // Hole Geldbörse aus Slot 9 (NICHT aus Hand!)
        ItemStack wallet = context.getPlayer().getInventory().getItem(WALLET_SLOT);
        
        if (!(wallet.getItem() instanceof CashItem)) {
            context.getPlayer().displayClientMessage(Component.literal(
                "§c✗ Keine Geldbörse in Slot 9!"
            ), true);
            return InteractionResult.FAIL;
        }
        
        double currentValue = getValue(wallet);
        
        // Prüfe ob genug Geld vorhanden
        if (currentValue < PLACE_AMOUNT) {
            context.getPlayer().displayClientMessage(Component.literal(
                "§c✗ Nicht genug Bargeld!\n" +
                "§7Benötigt: §e100€\n" +
                "§7Vorhanden: §e" + String.format("%.0f€", currentValue)
            ), true);
            return InteractionResult.FAIL;
        }
        
        // Ist es bereits ein CashBlock?
        if (clickedState.getBlock() instanceof CashBlock) {
            // Block existiert bereits - füge Geld hinzu
            double blockValue = CashBlock.getValue(level, clickedPos);
            
            if (blockValue >= MAX_PER_BLOCK) {
                context.getPlayer().displayClientMessage(Component.literal(
                    "§c✗ Block ist voll! (1000€)\n" +
                    "§7Platziere an neuer Position"
                ), true);
                return InteractionResult.FAIL;
            }
            
            // Füge 100€ hinzu
            CashBlock.addValue(level, clickedPos, PLACE_AMOUNT);
            removeValue(wallet, PLACE_AMOUNT);
            
            double newValue = CashBlock.getValue(level, clickedPos);
            context.getPlayer().displayClientMessage(Component.literal(
                "§a✓ +100€\n" +
                "§7Block: §e" + String.format("%.0f€/1000€", newValue)
            ), true);
            
            level.playSound(null, clickedPos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);
            return InteractionResult.SUCCESS;
        }
        
        // Neuen Block platzieren - NUR auf Luft!
        BlockPos placePos = clickedPos.relative(context.getClickedFace());
        
        // Prüfe ob Position frei ist
        if (!level.getBlockState(placePos).isAir()) {
            // SILENT FAIL
            return InteractionResult.FAIL;
        }
        
        // Platziere neuen CashBlock
        BlockState newState = EconomyBlocks.CASH_BLOCK.get().defaultBlockState();
        level.setBlock(placePos, newState, 3);
        
        // Setze Wert auf 100€
        CashBlock.setValue(level, placePos, PLACE_AMOUNT);
        removeValue(wallet, PLACE_AMOUNT);
        
        context.getPlayer().displayClientMessage(Component.literal(
            "§a✓ 100€ platziert!"
        ), true);
        
        level.playSound(null, placePos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }
    
    /**
     * Verhindert Shift+Rechtsklick zum Platzieren
     */
    @Override
    public boolean canAttackBlock(net.minecraft.world.level.block.state.BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return false; // Kann keine Blöcke abbauen
    }
    
    /**
     * Erstellt Bargeld mit Wert
     */
    public static ItemStack create(double amount) {
        ItemStack stack = new ItemStack(de.rolandsw.schedulemc.items.ModItems.CASH.get());
        setValue(stack, amount);
        return stack;
    }
    
    /**
     * Setzt Wert (UNLIMITED!)
     */
    public static void setValue(ItemStack stack, double value) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble("CashValue", Math.max(0, value));
    }
    
    /**
     * Gibt Wert zurück
     */
    public static double getValue(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CashValue")) {
            return tag.getDouble("CashValue");
        }
        return 0.0;
    }
    
    /**
     * Fügt Wert hinzu (UNLIMITED!)
     */
    public static boolean addValue(ItemStack stack, double amount) {
        double current = getValue(stack);
        setValue(stack, current + amount);
        return true;
    }
    
    /**
     * Entfernt Wert
     */
    public static boolean removeValue(ItemStack stack, double amount) {
        double current = getValue(stack);
        if (current >= amount) {
            setValue(stack, current - amount);
            return true;
        }
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        double value = getValue(stack);
        
        tooltip.add(Component.literal("§7Guthaben: §a" + String.format("%.2f€", value)));
        tooltip.add(Component.translatable("tooltip.cash.capacity_unlimited"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§c§lGESPERRT IN SLOT 9!"));
        tooltip.add(Component.translatable("tooltip.cash.survives_death"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§7Rechtsklick: §e+100€ zum Block"));
        tooltip.add(Component.literal("§7Linksklick halten: §eGesamten Block"));
    }
    
    @Override
    public Component getName(ItemStack stack) {
        double value = getValue(stack);
        if (value <= 0) {
            return Component.translatable("tooltip.cash.wallet_empty");
        } else {
            return Component.translatable("tooltip.cash.wallet_filled", String.format("%.0f€", value));
        }
    }
    
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false; // Kein Bar da unlimited
    }
}
