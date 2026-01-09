package de.rolandsw.schedulemc.production.blocks;

import de.rolandsw.schedulemc.cannabis.blocks.CannabisPlantBlock;
import de.rolandsw.schedulemc.cannabis.items.CannabisSeedItem;
import de.rolandsw.schedulemc.coca.blocks.CocaPlantBlock;
import de.rolandsw.schedulemc.coca.items.CocaSeedItem;
import de.rolandsw.schedulemc.coca.items.FreshCocaLeafItem;
import de.rolandsw.schedulemc.mushroom.items.FreshMushroomItem;
import de.rolandsw.schedulemc.mushroom.items.MistBagItem;
import de.rolandsw.schedulemc.mushroom.items.SporeSyringeItem;
import de.rolandsw.schedulemc.poppy.blocks.PoppyPlantBlock;
import de.rolandsw.schedulemc.poppy.items.PoppyPodItem;
import de.rolandsw.schedulemc.poppy.items.PoppySeedItem;
import de.rolandsw.schedulemc.production.blockentity.PlantPotBlockEntity;
import de.rolandsw.schedulemc.production.core.PotType;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock;
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
 * Universal Plant Pot Block
 * Supports all plant types: Tobacco, Cannabis, Coca, Poppy, Mushrooms
 */
public class PlantPotBlock extends Block implements EntityBlock {

    private final PotType potType;

    public PlantPotBlock(PotType potType, Properties properties) {
        super(properties);
        this.potType = potType;
    }

    public PotType getPotType() {
        return potType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        PlantPotBlockEntity be = new PlantPotBlockEntity(pos, state);
        be.setPotType(potType);
        return be;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof PlantPotBlockEntity potBE) {
                potBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PlantPotBlockEntity potBE)) return InteractionResult.PASS;

        ItemStack handStack = player.getItemInHand(hand);
        var potData = potBE.getPotData();

        // ═══════════════════════════════════════════════════════════
        // 1. ERDE BEFÜLLEN (Berücksichtigt Erdsack-Typ!)
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof SoilBagItem soilBagItem) {
            // Erlaube Nachfüllen nur wenn keine Pflanze vorhanden ist
            if (potData.hasPlant()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.remove_plant_first"
                ), true);
                return InteractionResult.FAIL;
            }

            // Prüfe ob Topf bereits voll ist
            if (potData.getSoilLevel() >= potData.getMaxSoil()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.pot_full_soil"
                ), true);
                return InteractionResult.FAIL;
            }

            // Verbrauche 1 Einheit Erde
            if (SoilBagItem.consumeUnits(handStack, 1)) {
                // Füge Erde basierend auf Erdsack-Typ hinzu (1, 2 oder 3 Pflanzen)
                int plantsPerBag = soilBagItem.getPlantsPerBag();
                potData.addSoilForPlants(plantsPerBag);
                potBE.setChanged();
                level.sendBlockUpdated(pos, state, state, 3); // Client-Update!

                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.soil_added_full",
                    (int)potData.getSoilLevel(),
                    potData.getMaxSoil(),
                    plantsPerBag
                ), true);

                player.playSound(net.minecraft.sounds.SoundEvents.GRAVEL_PLACE, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.soil_bag_empty"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 1b. MIST BEFÜLLEN (für Pilze)
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof MistBagItem mistBagItem) {
            if (potData.hasPlant()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.remove_plant_first"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.hasSoil()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.pot_has_soil"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.getSoilLevel() >= potData.getMaxSoil()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.pot_full_substrate"
                ), true);
                return InteractionResult.FAIL;
            }

            if (MistBagItem.consumeUnits(handStack, 1)) {
                int plantsPerBag = mistBagItem.getPlantsPerBag();
                potData.addMistForPlants(plantsPerBag);
                potBE.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);

                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.mist_added_full",
                    (int)potData.getSoilLevel(),
                    potData.getMaxSoil(),
                    plantsPerBag
                ), true);

                player.playSound(net.minecraft.sounds.SoundEvents.GRAVEL_PLACE, 1.0f, 0.8f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.mist_bag_empty"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 2. GIEßEN (FIXED!)
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof WateringCanItem) {
            if (!potData.hasSoil()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.needs_soil_first"
                ), true);
                return InteractionResult.FAIL;
            }

            int waterLevel = WateringCanItem.getWaterLevel(handStack);
            if (waterLevel <= 0) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.watering_can_empty"
                ), true);
                return InteractionResult.FAIL;
            }

            int maxWater = potData.getMaxWater();
            int currentWater = potData.getWaterLevel();

            if (currentWater >= maxWater) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.pot_full_water"
                ), true);
                return InteractionResult.FAIL;
            }

            // Gieße
            int toAdd = Math.min(waterLevel, maxWater - currentWater);
            potData.addWater(toAdd);

            // Verbrauche Wasser aus Gießkanne
            WateringCanItem.setWaterLevel(handStack, waterLevel - toAdd);
            potBE.setChanged();
            level.sendBlockUpdated(pos, state, state, 3); // Client-Update!

            player.displayClientMessage(Component.translatable(
                "block.plant_pot.watered",
                potData.getWaterLevel(),
                maxWater
            ), true);

            player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // ═══════════════════════════════════════════════════════════
        // 3. PFLANZEN - TABAK
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof TobaccoSeedItem seedItem) {
            if (!potData.hasSoil()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.needs_soil_first"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.hasPlant()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.has_plant"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.getWaterLevel() < 10) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.too_little_water"
                ), true);
                return InteractionResult.FAIL;
            }

            // Pflanze Samen
            potData.plantSeed(seedItem.getTobaccoType());
            potBE.setChanged();
            level.sendBlockUpdated(pos, state, state, 3); // Client-Update!
            handStack.shrink(1);

            // Platziere Pflanzen-Block oberhalb des Topfes
            TobaccoPlantBlock.growToStage(level, pos, 0, seedItem.getTobaccoType());

            player.displayClientMessage(Component.translatable(
                "block.plant_pot.tobacco_planted",
                seedItem.getTobaccoType().getColoredName()
            ), true);

            player.playSound(net.minecraft.sounds.SoundEvents.CROP_PLANTED, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // ═══════════════════════════════════════════════════════════
        // 3b. PFLANZEN - CANNABIS
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof CannabisSeedItem cannabisSeedItem) {
            if (!potData.hasSoil()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.needs_soil_first"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.hasPlant()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.has_plant"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.getWaterLevel() < 10) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.too_little_water"
                ), true);
                return InteractionResult.FAIL;
            }

            // Pflanze Cannabis-Samen
            potData.plantCannabisSeed(CannabisSeedItem.getStrain(handStack));
            potBE.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            handStack.shrink(1);

            // Platziere Cannabis-Pflanzen-Block oberhalb des Topfes
            CannabisPlantBlock.growToStage(level, pos, 0, CannabisSeedItem.getStrain(handStack));

            player.displayClientMessage(Component.translatable(
                "block.plant_pot.cannabis_planted_strain",
                CannabisSeedItem.getStrain(handStack).getColoredName()
            ), true);

            player.playSound(net.minecraft.sounds.SoundEvents.CROP_PLANTED, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // ═══════════════════════════════════════════════════════════
        // 3c. PFLANZEN - KOKA
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof CocaSeedItem cocaSeedItem) {
            if (!potData.hasSoil()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.needs_soil_first"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.hasPlant()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.has_plant"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.getWaterLevel() < 10) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.too_little_water"
                ), true);
                return InteractionResult.FAIL;
            }

            // Pflanze Koka-Samen
            potData.plantCocaSeed(cocaSeedItem.getCocaType());
            potBE.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            handStack.shrink(1);

            // Platziere Koka-Pflanzen-Block oberhalb des Topfes
            CocaPlantBlock.growToStage(level, pos, 0, cocaSeedItem.getCocaType());

            player.displayClientMessage(Component.translatable(
                "block.plant_pot.coca_planted",
                cocaSeedItem.getCocaType().getColoredName()
            ), true);

            player.playSound(net.minecraft.sounds.SoundEvents.CROP_PLANTED, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // ═══════════════════════════════════════════════════════════
        // 3d. PFLANZEN - MOHN
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof PoppySeedItem poppySeedItem) {
            if (!potData.hasSoil()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.needs_soil_first"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.hasPlant()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.has_plant"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.getWaterLevel() < 10) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.too_little_water"
                ), true);
                return InteractionResult.FAIL;
            }

            // Pflanze Mohn-Samen
            potData.plantPoppySeed(poppySeedItem.getPoppyType());
            potBE.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            handStack.shrink(1);

            // Platziere Mohn-Pflanzen-Block oberhalb des Topfes
            PoppyPlantBlock.growToStage(level, pos, 0, poppySeedItem.getPoppyType());

            player.displayClientMessage(Component.translatable(
                "block.plant_pot.poppy_planted",
                poppySeedItem.getPoppyType().getColoredName()
            ), true);

            player.playSound(net.minecraft.sounds.SoundEvents.CROP_PLANTED, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // ═══════════════════════════════════════════════════════════
        // 3e. IMPFEN - PILZE (Sporen-Spritze)
        // ═══════════════════════════════════════════════════════════
        if (handStack.getItem() instanceof SporeSyringeItem syringeItem) {
            if (!potData.hasMist()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.mist_not_present"
                ), true);
                return InteractionResult.FAIL;
            }

            if (potData.hasPlant()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.culture_already_present"
                ), true);
                return InteractionResult.FAIL;
            }

            // Impfe mit Sporen
            potData.plantMushroomSpore(syringeItem.getMushroomType());
            potBE.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            handStack.shrink(1);

            player.displayClientMessage(Component.translatable(
                "block.plant_pot.mushroom_spores_inoculated",
                syringeItem.getMushroomType().getColoredName()
            ), true);

            player.playSound(net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, 1.0f, 1.5f);
            return InteractionResult.SUCCESS;
        }

        // ═══════════════════════════════════════════════════════════
        // 4. ERNTEN - TABAK
        // ═══════════════════════════════════════════════════════════
        if (handStack.isEmpty() && player.isShiftKeyDown() && potData.hasTobaccoPlant()) {
            var plant = potData.getPlant();

            if (!plant.isFullyGrown()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.tobacco_not_fully_grown",
                    (plant.getGrowthStage() * 100 / 7)
                ), true);
                return InteractionResult.FAIL;
            }

            // Ernte Tabak
            var harvested = potData.harvest();
            if (harvested != null) {
                ItemStack leaves = de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem.create(
                    harvested.getType(),
                    harvested.getQuality(),
                    harvested.getHarvestYield()
                );

                player.getInventory().add(leaves);
                potBE.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);

                // Entferne Pflanzen-Block
                TobaccoPlantBlock.removePlant(level, pos);

                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.tobacco_harvested",
                    harvested.getHarvestYield(),
                    harvested.getQuality().getColoredName()
                ), true);

                player.playSound(net.minecraft.sounds.SoundEvents.CROP_BREAK, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 4b. ERNTEN - CANNABIS
        // ═══════════════════════════════════════════════════════════
        if (handStack.isEmpty() && player.isShiftKeyDown() && potData.hasCannabisPlant()) {
            var cannabisPlant = potData.getCannabisPlant();

            if (!cannabisPlant.isFullyGrown()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.cannabis_not_fully_grown",
                    (cannabisPlant.getGrowthStage() * 100 / 7)
                ), true);
                return InteractionResult.FAIL;
            }

            // Ernte Cannabis
            var harvested = potData.harvestCannabis();
            if (harvested != null) {
                ItemStack buds = de.rolandsw.schedulemc.cannabis.items.FreshBudItem.create(
                    harvested.getStrain(),
                    harvested.getQuality(),
                    harvested.getHarvestYield()
                );

                player.getInventory().add(buds);
                potBE.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);

                // Entferne Cannabis-Pflanzen-Block
                CannabisPlantBlock.removePlant(level, pos);

                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.cannabis_harvested",
                    harvested.getHarvestYield(),
                    harvested.getQuality().getColoredName()
                ), true);

                player.playSound(net.minecraft.sounds.SoundEvents.CROP_BREAK, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 4c. ERNTEN - KOKA
        // ═══════════════════════════════════════════════════════════
        if (handStack.isEmpty() && player.isShiftKeyDown() && potData.hasCocaPlant()) {
            var cocaPlant = potData.getCocaPlant();

            if (!cocaPlant.isFullyGrown()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.coca_not_fully_grown",
                    (cocaPlant.getGrowthStage() * 100 / 7)
                ), true);
                return InteractionResult.FAIL;
            }

            // Ernte Koka
            var harvested = potData.harvestCoca();
            if (harvested != null) {
                ItemStack leaves = FreshCocaLeafItem.create(
                    harvested.getType(),
                    harvested.getQuality(),
                    harvested.getHarvestYield()
                );

                player.getInventory().add(leaves);
                potBE.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);

                // Entferne Koka-Pflanzen-Block
                CocaPlantBlock.removePlant(level, pos);

                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.coca_harvested",
                    harvested.getHarvestYield(),
                    harvested.getQuality().getColoredName()
                ), true);

                player.playSound(net.minecraft.sounds.SoundEvents.CROP_BREAK, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 4d. ERNTEN - MOHN
        // ═══════════════════════════════════════════════════════════
        if (handStack.isEmpty() && player.isShiftKeyDown() && potData.hasPoppyPlant()) {
            var poppyPlant = potData.getPoppyPlant();

            if (!poppyPlant.isFullyGrown()) {
                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.poppy_not_fully_grown",
                    (poppyPlant.getGrowthStage() * 100 / 7)
                ), true);
                return InteractionResult.FAIL;
            }

            // Ernte Mohn
            var harvested = potData.harvestPoppy();
            if (harvested != null) {
                ItemStack pods = PoppyPodItem.create(
                    harvested.getType(),
                    harvested.getQuality(),
                    harvested.getHarvestYield()
                );

                player.getInventory().add(pods);
                potBE.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);

                // Entferne Mohn-Pflanzen-Block
                PoppyPlantBlock.removePlant(level, pos);

                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.poppy_harvested",
                    harvested.getHarvestYield(),
                    harvested.getQuality().getColoredName()
                ), true);

                player.playSound(net.minecraft.sounds.SoundEvents.CROP_BREAK, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 4e. ERNTEN - PILZE
        // ═══════════════════════════════════════════════════════════
        if (handStack.isEmpty() && player.isShiftKeyDown() && potData.hasMushroomPlant()) {
            var mushroom = potData.getMushroomPlant();

            if (!mushroom.canHarvest()) {
                if (!mushroom.isFullyGrown()) {
                    String phase = mushroom.isIncubating() ? "Inkubation" : "Fruchtung";
                    player.displayClientMessage(Component.translatable(
                        "block.plant_pot.mushroom_not_ready",
                        phase,
                        (mushroom.getGrowthStage() * 100 / 7)
                    ), true);
                } else {
                    player.displayClientMessage(Component.translatable(
                        "block.plant_pot.mushroom_substrate_exhausted"
                    ), true);
                }
                return InteractionResult.FAIL;
            }

            // Ernte Pilze
            int yield = mushroom.getHarvestYield();
            int remainingFlushes = mushroom.getRemainingFlushes() - 1;
            var harvested = potData.harvestMushroom();

            if (harvested != null) {
                ItemStack freshMushrooms = FreshMushroomItem.create(
                    harvested.getType(),
                    harvested.getQuality(),
                    yield
                );

                player.getInventory().add(freshMushrooms);
                potBE.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);

                String flushInfo = remainingFlushes > 0 ?
                    Component.translatable("block.plant_pot.mushroom_remaining_flushes", remainingFlushes).getString() :
                    Component.translatable("block.plant_pot.mushroom_last_flush").getString();

                player.displayClientMessage(Component.translatable(
                    "block.plant_pot.mushroom_harvested",
                    yield,
                    harvested.getQuality().getColoredName(),
                    flushInfo
                ), true);

                player.playSound(net.minecraft.sounds.SoundEvents.FUNGUS_BREAK, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
