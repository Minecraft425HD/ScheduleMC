package de.rolandsw.schedulemc.production.blockentity;

import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.blocks.CannabisPlantBlock;
import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.blocks.CocaPlantBlock;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.blocks.PoppyPlantBlock;
import de.rolandsw.schedulemc.production.core.PotType;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import de.rolandsw.schedulemc.production.growth.PlantGrowthHandler;
import de.rolandsw.schedulemc.production.growth.PlantGrowthHandlerFactory;
import de.rolandsw.schedulemc.production.nbt.PlantSerializer;
import de.rolandsw.schedulemc.production.nbt.PlantSerializerFactory;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoBlockEntities;
import de.rolandsw.schedulemc.tobacco.blocks.GrowLightSlabBlock;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
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
 * Universal Plant Pot BlockEntity
 * Supports all plant types: Tobacco, Cannabis, Coca, Poppy, Mushrooms
 */
public class PlantPotBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    private PlantPotData potData;
    private int tickCounter = 0;
    private int plantGrowthCounter = 0;

    public PlantPotBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.TOBACCO_POT.get(), pos, state);
        this.potData = new PlantPotData(PotType.TERRACOTTA);
    }

    public void setPotType(PotType type) {
        this.potData = new PlantPotData(type);
        setChanged();
    }

    public PlantPotData getPotData() {
        return potData;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        // Pflanzen-Wachstum prüfen (alle 20 Ticks = 1x pro Sekunde)
        // Performance-Optimierung: Reduziert von 4x/Sekunde auf 1x/Sekunde
        if (tickCounter >= 20) {
            tickCounter = 0;
            plantGrowthCounter++;

            if (potData.hasPlant() && potData.canGrow()) {
                // Hole den passenden Handler für diese Pflanze
                PlantGrowthHandler handler = PlantGrowthHandlerFactory.getHandler(potData);

                if (handler != null) {
                    // Prüfe ob Pflanze wachsen kann (Licht, spezielle Bedingungen)
                    if (!handler.canGrow(level, worldPosition, potData)) {
                        return;  // Bedingungen nicht erfüllt
                    }

                    // Wachstumsgeschwindigkeit basierend auf Licht
                    double lightSpeedMultiplier = getLightSpeedMultiplier();
                    int ticksNeeded = (int) Math.max(1, 4 / lightSpeedMultiplier);

                    // Hole aktuelles Stadium
                    int oldStage = handler.getCurrentStage(potData);

                    // Führe Wachstums-Tick aus
                    if (plantGrowthCounter >= ticksNeeded) {
                        plantGrowthCounter = 0;
                        handler.tick(potData);
                    }

                    // Hole neues Stadium
                    int newStage = handler.getCurrentStage(potData);

                    // Wenn gewachsen → Ressourcen verbrauchen & Block aktualisieren
                    if (oldStage != newStage) {
                        // Spezielle Logik für Pilze (verbrauchen nur bei Fruchtung Wasser)
                        if (potData.hasMushroomPlant()) {
                            var mushroom = potData.getMushroomPlant();
                            if (mushroom.needsWater()) {
                                consumeResourcesForGrowth(newStage);
                            } else {
                                // Nur Substrat verbrauchen
                                potData.consumeSoil(15.0 / 7.0);
                            }
                        } else {
                            // Standard-Ressourcen-Verbrauch für alle anderen Pflanzen
                            consumeResourcesForGrowth(newStage);
                        }

                        // Block-State aktualisieren
                        handler.updateBlockState(level, worldPosition, newStage, potData);

                        setChanged();
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    }
                }
            }
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        // Aktiv wenn eine Pflanze vorhanden ist und wachsen kann
        return potData.hasPlant() && potData.canGrow();
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

        // Pflanzen-Daten speichern (Strategy Pattern - eliminiert ~80 Zeilen Duplikation)
        for (PlantSerializer serializer : PlantSerializerFactory.getAllSerializers()) {
            serializer.savePlant(potData, tag);
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
                this.potData = new PlantPotData(type);
            }
        }

        // WICHTIG: Werte direkt setzen, nicht addieren!
        if (tag.contains("WaterLevel")) {
            potData.setWaterLevel(tag.getDouble("WaterLevel"));
        }
        if (tag.contains("SoilLevel")) {
            potData.setSoilLevel(tag.getDouble("SoilLevel"));
        }

        // Pflanzen-Daten laden (Strategy Pattern - eliminiert ~130 Zeilen Duplikation)
        for (PlantSerializer serializer : PlantSerializerFactory.getAllSerializers()) {
            serializer.loadPlant(potData, tag);
        }

        // Mist-Status laden (falls nicht durch Pilz gesetzt)
        if (tag.contains("HasMist") && !potData.hasMushroomPlant()) {
            potData.setMist(tag.getBoolean("HasMist"));
        }

        // Keine Pflanzen-Tags -> Pflanze entfernen falls vorhanden
        if (!tag.contains("Plant") && !tag.contains("CannabisPlant") && !tag.contains("CocaPlant") &&
            !tag.contains("PoppyPlant") && !tag.contains("MushroomPlant")) {
            if (potData.hasPlant()) {
                potData.clearPlant();
            }
        }
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
