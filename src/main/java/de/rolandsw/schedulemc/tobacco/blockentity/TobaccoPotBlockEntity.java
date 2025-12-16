package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.blocks.CocaPlantBlock;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.blocks.PoppyPlantBlock;
import de.rolandsw.schedulemc.tobacco.PotType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.blocks.GrowLightSlabBlock;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * TileEntity für Tabak-Töpfe
 * Speichert: Wasser, Erde, Pflanzendaten
 */
public class TobaccoPotBlockEntity extends BlockEntity {
    
    private TobaccoPotData potData;
    private int tickCounter = 0;
    private int plantGrowthCounter = 0;

    public TobaccoPotBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.TOBACCO_POT.get(), pos, state);
        this.potData = new TobaccoPotData(PotType.TERRACOTTA);
    }

    public void setPotType(PotType type) {
        this.potData = new TobaccoPotData(type);
        setChanged();
    }

    public TobaccoPotData getPotData() {
        return potData;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        // Pflanzen-Wachstum prüfen (alle 5 Ticks = 4x pro Sekunde)
        if (tickCounter >= 5) {
            tickCounter = 0;
            plantGrowthCounter++;

            if (potData.hasPlant() && potData.canGrow()) {
                // Licht-Prüfung
                if (!hasEnoughLight()) {
                    return;  // Kein Wachstum ohne Licht
                }

                // Wachstumsgeschwindigkeit basierend auf Licht
                double lightSpeedMultiplier = getLightSpeedMultiplier();
                int ticksNeeded = (int) Math.max(1, 4 / lightSpeedMultiplier);

                // Tabak-Pflanze wachsen lassen
                if (potData.hasTobaccoPlant()) {
                    int oldStage = potData.getPlant().getGrowthStage();

                    if (plantGrowthCounter >= ticksNeeded) {
                        plantGrowthCounter = 0;
                        potData.getPlant().tick();
                    }

                    int newStage = potData.getPlant().getGrowthStage();

                    if (oldStage != newStage) {
                        consumeResourcesForGrowth(newStage);
                        de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock.growToStage(
                            level, worldPosition, newStage, potData.getPlant().getType()
                        );
                        setChanged();
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    }
                }

                // Koka-Pflanze wachsen lassen
                if (potData.hasCocaPlant()) {
                    int oldStage = potData.getCocaPlant().getGrowthStage();

                    if (plantGrowthCounter >= ticksNeeded) {
                        plantGrowthCounter = 0;
                        potData.getCocaPlant().tick();
                    }

                    int newStage = potData.getCocaPlant().getGrowthStage();

                    if (oldStage != newStage) {
                        consumeResourcesForGrowth(newStage);
                        CocaPlantBlock.growToStage(
                            level, worldPosition, newStage, potData.getCocaPlant().getType()
                        );
                        setChanged();
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    }
                }

                // Mohn-Pflanze wachsen lassen
                if (potData.hasPoppyPlant()) {
                    int oldStage = potData.getPoppyPlant().getGrowthStage();

                    if (plantGrowthCounter >= ticksNeeded) {
                        plantGrowthCounter = 0;
                        potData.getPoppyPlant().tick();
                    }

                    int newStage = potData.getPoppyPlant().getGrowthStage();

                    if (oldStage != newStage) {
                        consumeResourcesForGrowth(newStage);
                        PoppyPlantBlock.growToStage(
                            level, worldPosition, newStage, potData.getPoppyPlant().getType()
                        );
                        setChanged();
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    }
                }

                // Pilzkultur wachsen lassen (braucht Dunkelheit!)
                if (potData.hasMushroomPlant()) {
                    var mushroom = potData.getMushroomPlant();

                    // Prüfe Lichtlevel (Pilze brauchen Dunkelheit)
                    if (!isLightLevelValidForMushroom()) {
                        return; // Zu hell!
                    }

                    int oldStage = mushroom.getGrowthStage();

                    if (plantGrowthCounter >= ticksNeeded) {
                        plantGrowthCounter = 0;
                        mushroom.tick();
                    }

                    int newStage = mushroom.getGrowthStage();

                    if (oldStage != newStage) {
                        // Pilze verbrauchen nur während Fruchtung Wasser
                        if (mushroom.needsWater()) {
                            consumeResourcesForGrowth(newStage);
                        } else {
                            // Nur Substrat verbrauchen
                            potData.consumeSoil(15.0 / 7.0);
                        }
                        setChanged();
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * Prüft ob das Lichtlevel für Pilzwachstum geeignet ist
     */
    private boolean isLightLevelValidForMushroom() {
        if (level == null || !potData.hasMushroomPlant()) return false;

        var mushroom = potData.getMushroomPlant();
        BlockPos checkPos = worldPosition.above();
        int lightLevel = level.getBrightness(LightLayer.BLOCK, checkPos);

        return mushroom.isLightLevelValid(lightLevel);
    }

    /**
     * Verbraucht Ressourcen beim Wachstum
     */
    private void consumeResourcesForGrowth(int newStage) {
        double waterToConsume, soilToConsume;

        if (newStage == 7) {
            // Letzter Schritt: Verbrauche alles was noch da ist
            waterToConsume = potData.getWaterLevelExact();
            soilToConsume = potData.getSoilLevelExact();
        } else {
            // Normale Schritte: 1/7 der Kapazität
            waterToConsume = potData.getMaxWater() / 7.0;
            soilToConsume = 15.0 / 7.0;
        }

        potData.consumeWater(waterToConsume);
        potData.consumeSoil(soilToConsume);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putString("PotType", potData.getPotType().name());
        tag.putDouble("WaterLevel", potData.getWaterLevelExact());
        tag.putDouble("SoilLevel", potData.getSoilLevelExact());
        tag.putBoolean("HasSoil", potData.hasSoil());

        // Tabak-Pflanze speichern
        if (potData.hasTobaccoPlant()) {
            CompoundTag plantTag = new CompoundTag();
            var plant = potData.getPlant();

            plantTag.putString("Type", plant.getType().name());
            plantTag.putString("Quality", plant.getQuality().name());
            plantTag.putInt("GrowthStage", plant.getGrowthStage());
            plantTag.putInt("TicksGrown", plant.getTicksGrown());
            plantTag.putBoolean("HasFertilizer", plant.hasFertilizer());
            plantTag.putBoolean("HasGrowthBooster", plant.hasGrowthBooster());
            plantTag.putBoolean("HasQualityBooster", plant.hasQualityBooster());

            tag.put("Plant", plantTag);
        }

        // Koka-Pflanze speichern
        if (potData.hasCocaPlant()) {
            CompoundTag cocaTag = new CompoundTag();
            var cocaPlant = potData.getCocaPlant();

            cocaTag.putString("Type", cocaPlant.getType().name());
            cocaTag.putString("Quality", cocaPlant.getQuality().name());
            cocaTag.putInt("GrowthStage", cocaPlant.getGrowthStage());
            cocaTag.putInt("TicksGrown", cocaPlant.getTicksGrown());
            cocaTag.putBoolean("HasFertilizer", cocaPlant.hasFertilizer());
            cocaTag.putBoolean("HasGrowthBooster", cocaPlant.hasGrowthBooster());
            cocaTag.putBoolean("HasQualityBooster", cocaPlant.hasQualityBooster());

            tag.put("CocaPlant", cocaTag);
        }

        // Mohn-Pflanze speichern
        if (potData.hasPoppyPlant()) {
            CompoundTag poppyTag = new CompoundTag();
            var poppyPlant = potData.getPoppyPlant();

            poppyTag.putString("Type", poppyPlant.getType().name());
            poppyTag.putString("Quality", poppyPlant.getQuality().name());
            poppyTag.putInt("GrowthStage", poppyPlant.getGrowthStage());
            poppyTag.putInt("TicksGrown", poppyPlant.getTicksGrown());
            poppyTag.putBoolean("HasFertilizer", poppyPlant.hasFertilizer());
            poppyTag.putBoolean("HasGrowthBooster", poppyPlant.hasGrowthBooster());
            poppyTag.putBoolean("HasQualityBooster", poppyPlant.hasQualityBooster());

            tag.put("PoppyPlant", poppyTag);
        }

        // Pilzkultur speichern
        if (potData.hasMushroomPlant()) {
            CompoundTag mushroomTag = new CompoundTag();
            var mushroom = potData.getMushroomPlant();

            mushroomTag.putString("Type", mushroom.getType().name());
            mushroomTag.putString("Quality", mushroom.getQuality().name());
            mushroomTag.putInt("GrowthStage", mushroom.getGrowthStage());
            mushroomTag.putInt("TicksGrown", mushroom.getTicksGrown());
            mushroomTag.putInt("CurrentFlush", mushroom.getCurrentFlush());
            mushroomTag.putBoolean("HasFertilizer", mushroom.hasFertilizer());
            mushroomTag.putBoolean("HasGrowthBooster", mushroom.hasGrowthBooster());
            mushroomTag.putBoolean("HasQualityBooster", mushroom.hasQualityBooster());

            tag.put("MushroomPlant", mushroomTag);
        }

        // Mist-Status speichern
        tag.putBoolean("HasMist", potData.hasMist());
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // NUR neues Objekt erstellen wenn PotType sich ändert oder noch nicht existiert
        if (tag.contains("PotType")) {
            PotType type = PotType.valueOf(tag.getString("PotType"));
            if (potData == null || potData.getPotType() != type) {
                this.potData = new TobaccoPotData(type);
            }
        }

        // WICHTIG: Werte direkt setzen, nicht addieren!
        if (tag.contains("WaterLevel")) {
            potData.setWaterLevel(tag.getDouble("WaterLevel"));
        }
        if (tag.contains("SoilLevel")) {
            potData.setSoilLevel(tag.getDouble("SoilLevel"));
        }

        // Tabak-Pflanzen-Daten laden
        if (tag.contains("Plant")) {
            CompoundTag plantTag = tag.getCompound("Plant");

            TobaccoType type = TobaccoType.valueOf(plantTag.getString("Type"));

            if (!potData.hasTobaccoPlant()) {
                potData.plantSeed(type);
            }

            var plant = potData.getPlant();
            if (plant != null) {
                plant.setQuality(TobaccoQuality.valueOf(plantTag.getString("Quality")));
                plant.setGrowthStage(plantTag.getInt("GrowthStage"));

                int ticksGrown = plantTag.getInt("TicksGrown");
                while (plant.getTicksGrown() < ticksGrown) {
                    plant.incrementTicks();
                }

                if (plantTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
                if (plantTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
                if (plantTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
            }
        }

        // Koka-Pflanzen-Daten laden
        if (tag.contains("CocaPlant")) {
            CompoundTag cocaTag = tag.getCompound("CocaPlant");

            CocaType type = CocaType.valueOf(cocaTag.getString("Type"));

            if (!potData.hasCocaPlant()) {
                potData.plantCocaSeed(type);
            }

            var cocaPlant = potData.getCocaPlant();
            if (cocaPlant != null) {
                cocaPlant.setQuality(TobaccoQuality.valueOf(cocaTag.getString("Quality")));
                cocaPlant.setGrowthStage(cocaTag.getInt("GrowthStage"));

                int ticksGrown = cocaTag.getInt("TicksGrown");
                while (cocaPlant.getTicksGrown() < ticksGrown) {
                    cocaPlant.incrementTicks();
                }

                if (cocaTag.getBoolean("HasFertilizer")) cocaPlant.applyFertilizer();
                if (cocaTag.getBoolean("HasGrowthBooster")) cocaPlant.applyGrowthBooster();
                if (cocaTag.getBoolean("HasQualityBooster")) cocaPlant.applyQualityBooster();
            }
        }

        // Mohn-Pflanzen-Daten laden
        if (tag.contains("PoppyPlant")) {
            CompoundTag poppyTag = tag.getCompound("PoppyPlant");

            PoppyType type = PoppyType.valueOf(poppyTag.getString("Type"));

            if (!potData.hasPoppyPlant()) {
                potData.plantPoppySeed(type);
            }

            var poppyPlant = potData.getPoppyPlant();
            if (poppyPlant != null) {
                poppyPlant.setQuality(TobaccoQuality.valueOf(poppyTag.getString("Quality")));
                poppyPlant.setGrowthStage(poppyTag.getInt("GrowthStage"));

                int ticksGrown = poppyTag.getInt("TicksGrown");
                while (poppyPlant.getTicksGrown() < ticksGrown) {
                    poppyPlant.incrementTicks();
                }

                if (poppyTag.getBoolean("HasFertilizer")) poppyPlant.applyFertilizer();
                if (poppyTag.getBoolean("HasGrowthBooster")) poppyPlant.applyGrowthBooster();
                if (poppyTag.getBoolean("HasQualityBooster")) poppyPlant.applyQualityBooster();
            }
        }

        // Pilzkultur-Daten laden
        if (tag.contains("MushroomPlant")) {
            CompoundTag mushroomTag = tag.getCompound("MushroomPlant");

            MushroomType type = MushroomType.valueOf(mushroomTag.getString("Type"));

            // Setze Mist-Status vor dem Pflanzen
            potData.setMist(true);

            if (!potData.hasMushroomPlant()) {
                potData.plantMushroomSpore(type);
            }

            var mushroom = potData.getMushroomPlant();
            if (mushroom != null) {
                mushroom.setQuality(TobaccoQuality.valueOf(mushroomTag.getString("Quality")));
                mushroom.setGrowthStage(mushroomTag.getInt("GrowthStage"));
                mushroom.setCurrentFlush(mushroomTag.getInt("CurrentFlush"));

                int ticksGrown = mushroomTag.getInt("TicksGrown");
                while (mushroom.getTicksGrown() < ticksGrown) {
                    mushroom.incrementTicks();
                }

                if (mushroomTag.getBoolean("HasFertilizer")) mushroom.applyFertilizer();
                if (mushroomTag.getBoolean("HasGrowthBooster")) mushroom.applyGrowthBooster();
                if (mushroomTag.getBoolean("HasQualityBooster")) mushroom.applyQualityBooster();
            }
        }

        // Mist-Status laden (falls nicht durch Pilz gesetzt)
        if (tag.contains("HasMist") && !potData.hasMushroomPlant()) {
            potData.setMist(tag.getBoolean("HasMist"));
        }

        // Keine Pflanzen-Tags -> Pflanze entfernen falls vorhanden
        if (!tag.contains("Plant") && !tag.contains("CocaPlant") && !tag.contains("PoppyPlant") && !tag.contains("MushroomPlant")) {
            if (potData.hasPlant()) {
                potData.clearPlant();
            }
        }
    }

    /**
     * Prüft ob genug Licht für Wachstum vorhanden ist
     */
    private boolean hasEnoughLight() {
        if (level == null) return false;

        // Config: Ist Licht-Anforderung aktiviert?
        if (!ModConfigHandler.TOBACCO.REQUIRE_LIGHT_FOR_GROWTH.get()) {
            return true;  // Kein Licht benötigt
        }

        // Prüfe Lichtlevel über der Pflanze (2 Blöcke über Topf)
        BlockPos plantPos = worldPosition.above(2);
        int lightLevel = level.getBrightness(LightLayer.BLOCK, plantPos);

        // Minimales Lichtlevel aus Config
        int minLight = ModConfigHandler.TOBACCO.MIN_LIGHT_LEVEL.get();

        return lightLevel >= minLight;
    }

    /**
     * Berechnet Wachstumsgeschwindigkeits-Multiplikator basierend auf Licht
     */
    private double getLightSpeedMultiplier() {
        if (level == null) return 1.0;

        // Prüfe ob Grow Light direkt über Topf ist (1 Block)
        BlockPos abovePos = worldPosition.above();
        BlockState aboveState = level.getBlockState(abovePos);
        Block aboveBlock = aboveState.getBlock();

        if (aboveBlock instanceof GrowLightSlabBlock growLight) {
            // Grow Light gefunden! Nutze dessen Multiplikator
            return growLight.getTier().getGrowthSpeedMultiplier();
        }

        // Keine Grow Light → normale Geschwindigkeit
        return 1.0;
    }

    /**
     * Holt Qualitätsbonus von Grow Light (nur Premium)
     */
    public double getGrowLightQualityBonus() {
        if (level == null) return 0.0;

        BlockPos abovePos = worldPosition.above();
        BlockState aboveState = level.getBlockState(abovePos);
        Block aboveBlock = aboveState.getBlock();

        if (aboveBlock instanceof GrowLightSlabBlock growLight) {
            return growLight.getTier().getQualityBonus();
        }

        return 0.0;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }
}
