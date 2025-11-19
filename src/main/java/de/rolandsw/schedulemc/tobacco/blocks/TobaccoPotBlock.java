package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.PotType;
import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoPotBlockEntity;
import de.rolandsw.schedulemc.tobacco.items.SoilBagItem;
import de.rolandsw.schedulemc.tobacco.items.TobaccoSeedItem;
import de.rolandsw.schedulemc.tobacco.items.WateringCanItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Tabak-Topf Block (COMPLETE FIX!)
 */
public class TobaccoPotBlock extends Block implements EntityBlock {
    
    private final PotType potType;
    
    public TobaccoPotBlock(PotType potType, Properties properties) {
        super(properties);
        this.potType = potType;
    }
    
    public PotType getPotType() {
        return potType;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        TobaccoPotBlockEntity be = new TobaccoPotBlockEntity(pos, state);
        be.setPotType(potType); // Setze PotType nach Erstellung
        return be;
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof TobaccoPotBlockEntity potBE) {
                potBE.tick();
            }
        };
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, 
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TobaccoPotBlockEntity potBE)) return InteractionResult.PASS;
        
        ItemStack handStack = player.getItemInHand(hand);
        var potData = potBE.getPotData();
        
        // ═══════════════════════════════════════════════════════════
        // 1. ERDE BEFÜLLEN (FIXED!)
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof SoilBagItem) {
            if (potData.hasSoil()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Topf hat bereits Erde!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            // Verbrauche 1 Einheit Erde
            if (SoilBagItem.consumeUnits(handStack, 1)) {
                potData.setSoil(true); // Diese Methode setzt auch soilLevel automatisch!
                potBE.setChanged();
                
                player.displayClientMessage(Component.literal(
                    "§a✓ Erde eingefüllt!\n" +
                    "§7Erde: §6" + potData.getSoilLevel() + "/" + potData.getMaxSoil()
                ), true);
                
                player.playSound(net.minecraft.sounds.SoundEvents.GRAVEL_PLACE, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal(
                    "§c✗ Erdsack ist leer!"
                ), true);
                return InteractionResult.FAIL;
            }
        }
        
        // ═══════════════════════════════════════════════════════════
        // 2. GIEßEN (FIXED!)
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof WateringCanItem) {
            if (!potData.hasSoil()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Topf braucht zuerst Erde!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            int waterLevel = WateringCanItem.getWaterLevel(handStack); // RICHTIGE Methode!
            if (waterLevel <= 0) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Gießkanne ist leer!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            int maxWater = potData.getMaxWater();
            int currentWater = potData.getWaterLevel();
            
            if (currentWater >= maxWater) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Topf ist bereits voll mit Wasser!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            // Gieße
            int toAdd = Math.min(waterLevel, maxWater - currentWater);
            potData.addWater(toAdd);
            
            // Verbrauche Wasser aus Gießkanne
            WateringCanItem.setWaterLevel(handStack, waterLevel - toAdd); // RICHTIGE Methode!
            potBE.setChanged();
            
            player.displayClientMessage(Component.literal(
                "§b✓ Gegossen!\n" +
                "§7Wasser: §b" + potData.getWaterLevel() + "/" + maxWater
            ), true);
            
            player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        
        // ═══════════════════════════════════════════════════════════
        // 3. PFLANZEN
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof TobaccoSeedItem seedItem) {
            if (!potData.hasSoil()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Topf braucht zuerst Erde!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            if (potData.hasPlant()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Topf hat bereits eine Pflanze!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            if (potData.getWaterLevel() < 10) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Zu wenig Wasser zum Pflanzen!"
                ), true);
                return InteractionResult.FAIL;
            }
            
            // Pflanze Samen
            potData.plantSeed(seedItem.getTobaccoType());
            potBE.setChanged();
            handStack.shrink(1);

            // Platziere Pflanzen-Block oberhalb des Topfes
            TobaccoPlantBlock.growToStage(level, pos, 0, seedItem.getTobaccoType());

            player.displayClientMessage(Component.literal(
                "§a✓ Samen gepflanzt!\n" +
                "§7Sorte: " + seedItem.getTobaccoType().getColoredName()
            ), true);

            player.playSound(net.minecraft.sounds.SoundEvents.CROP_PLANTED, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        
        // ═══════════════════════════════════════════════════════════
        // 4. ERNTEN
        // ═══════════════════════════════════════════════════════════
        if (handStack.isEmpty() && player.isShiftKeyDown() && potData.hasPlant()) {
            var plant = potData.getPlant();
            
            if (!plant.isFullyGrown()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Pflanze ist noch nicht ausgewachsen!\n" +
                    "§7Wachstum: §e" + (plant.getGrowthStage() * 100 / 7) + "%"
                ), true);
                return InteractionResult.FAIL;
            }
            
            // Ernte
            var harvested = potData.harvest();
            if (harvested != null) {
                ItemStack leaves = de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem.create(
                    harvested.getType(),
                    harvested.getQuality(),
                    harvested.getHarvestYield()
                );

                player.getInventory().add(leaves);
                potBE.setChanged();

                // Entferne Pflanzen-Block
                TobaccoPlantBlock.removePlant(level, pos);

                player.displayClientMessage(Component.literal(
                    "§a✓ Geerntet!\n" +
                    "§7Ertrag: §e" + harvested.getHarvestYield() + " Blätter\n" +
                    "§7Qualität: " + harvested.getQuality().getColoredName()
                ), true);

                player.playSound(net.minecraft.sounds.SoundEvents.CROP_BREAK, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
