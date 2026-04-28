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
    private long lastGameTime = -1L;
    private double plantGrowthAccumulator = 0.0;

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

        if (tickCounter >= 20) {
            tickCounter = 0;

            long worldNow = level.getDayTime();
            if (lastGameTime < 0) lastGameTime = worldNow;
            long worldTicksPassed = Math.max(0L, worldNow - lastGameTime);
            lastGameTime = worldNow;

            if (worldTicksPassed > 0 && potData.hasPlant() && potData.canGrow()) {
                PlantGrowthHandler handler = PlantGrowthHandlerFactory.getHandler(potData);

                if (handler != null) {
                    if (!handler.canGrow(level, worldPosition, potData)) {  // NOPMD
                        // Licht nicht ausreichend
                    } else {
                        double lightSpeedMultiplier = getLightSpeedMultiplier();
                        if (lightSpeedMultiplier <= 0) {
                            // Kein Licht (Nacht oder innen ohne Growlight): kein Wachstum
                        } else {
                            plantGrowthAccumulator += worldTicksPassed * lightSpeedMultiplier / 80.0;

                            int callCount = (int) Math.min(plantGrowthAccumulator, 10000);
                            if (callCount > 0) {
                                plantGrowthAccumulator -= callCount;

                                int oldStage = handler.getCurrentStage(potData);
                                int stageAdvances = 0;
                                int prevStage = oldStage;
                                for (int i = 0; i < callCount; i++) {
                                    handler.tick(potData);
                                    int cur = handler.getCurrentStage(potData);
                                    if (cur != prevStage) {
                                        stageAdvances++;
                                        prevStage = cur;
                                    }
                                }

                                int newStage = handler.getCurrentStage(potData);
                                if (oldStage != newStage) {
                                    for (int s = 0; s < stageAdvances; s++) {
                                        if (potData.hasMushroomPlant()) {
                                            var mushroom = potData.getMushroomPlant();
                                            if (mushroom.needsWater()) {
                                                consumeResourcesForGrowth(newStage);
                                            } else {
                                                potData.consumeSoil(potData.getSoilConsumptionPerStage());
                                            }
                                        } else {
                                            consumeResourcesForGrowth(newStage);
                                        }
                                    }
                                    handler.updateBlockState(level, worldPosition, newStage, potData);
                                    setChanged();
                                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                                }
                            }
                        }
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
     *
     * NEUES SYSTEM:
     * - WASSER: 1/7 der Wasserkapazität pro Stufe (wird hier verbraucht)
     * - ERDE: Wird NICHT mehr während des Wachstums verbraucht!
     *   → Erde wird nur noch bei der Ernte pauschal abgezogen (-33)
     *   → Im HUD wird visuell eine Reduzierung angezeigt (rein optisch!)
     */
    private void consumeResourcesForGrowth(int _newStage) {
        // Wasser: 1/7 der Kapazität pro Stufe
        double waterToConsume = potData.getMaxWater() / 7.0;
        potData.consumeWater(waterToConsume);

        // ERDE: Wird NICHT mehr hier verbraucht!
        // → soilLevel bleibt konstant während des Wachstums
        // → Bei Ernte: -33 Erde (siehe PlantBlock.verifyAndCorrectResources())
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putString("PotType", potData.getPotType().name());
        tag.putLong("PlantLastGameTime", lastGameTime);
        tag.putDouble("PlantGrowthAccumulator", plantGrowthAccumulator);
        tag.putDouble("WaterLevel", potData.getWaterLevelExact());
        tag.putDouble("SoilLevel", potData.getSoilLevelExact());
        tag.putDouble("SoilLevelAtPlanting", potData.getSoilLevelAtPlanting());
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
            PotType type;
            try { type = PotType.valueOf(tag.getString("PotType")); }
            catch (IllegalArgumentException e) { type = PotType.values()[0]; }
            if (potData == null || potData.getPotType() != type) {
                this.potData = new PlantPotData(type);
            }
        }

        if (tag.contains("PlantLastGameTime")) lastGameTime = tag.getLong("PlantLastGameTime");
        if (tag.contains("PlantGrowthAccumulator")) plantGrowthAccumulator = tag.getDouble("PlantGrowthAccumulator");

        // WICHTIG: Werte direkt setzen, nicht addieren!
        if (tag.contains("WaterLevel")) {
            potData.setWaterLevel(tag.getDouble("WaterLevel"));
        }
        if (tag.contains("SoilLevel")) {
            potData.setSoilLevel(tag.getDouble("SoilLevel"));
        }
        if (tag.contains("SoilLevelAtPlanting")) {
            potData.setSoilLevelAtPlanting(tag.getDouble("SoilLevelAtPlanting"));
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
            !tag.contains("PoppyPlant") && !tag.contains("MushroomPlant") &&
            !tag.contains("GrapePlant") && !tag.contains("CoffeePlant")) {
            if (potData.hasPlant()) {
                potData.clearPlant();
            }
        }
    }


    /**
     * Berechnet Wachstumsgeschwindigkeits-Multiplikator basierend auf Licht
     * Prüft 2-3 Blöcke über dem Topf (wo das Grow Light platziert werden kann)
     * Wenn kein Grow Light vorhanden ist, wird Sonnenlicht genutzt (50% Geschwindigkeit)
     */
    private double getLightSpeedMultiplier() {
        if (level == null) return 0.5; // Fallback: Sonnenlicht = 50%

        // Prüfe 2-3 Blöcke über dem Topf (Grow Light Position)
        for (int yOffset = 2; yOffset <= 3; yOffset++) {
            BlockPos growLightPos = worldPosition.above(yOffset);
            BlockState checkState = level.getBlockState(growLightPos);
            Block checkBlock = checkState.getBlock();

            if (checkBlock instanceof GrowLightSlabBlock growLight) {
                // Grow Light gefunden! Nutze dessen Geschwindigkeits-Multiplikator
                return growLight.getTier().getGrowthSpeedMultiplier();
            }
        }

        // Kein Grow Light → prüfe Tageslicht (außen, tageszeitabhängig)
        BlockPos checkPos = worldPosition.above(2);
        int rawSkyLight = level.getBrightness(LightLayer.SKY, checkPos);
        if (rawSkyLight > 0) {
            // Außen: nur tagsüber wachsen (Effektivlicht >= 9 → 50% Geschwindigkeit)
            return level.getRawBrightness(checkPos, 0) >= 9 ? 0.5 : 0.0;
        }

        // Innen ohne Grow Light: kein aktives Wachstum
        return 0.0;
    }

    /**
     * Holt Qualitätsbonus von Grow Light (nur Premium)
     * Prüft 2-3 Blöcke über dem Topf (wo das Grow Light platziert werden kann)
     */
    public double getGrowLightQualityBonus() {
        if (level == null) return 0.0;

        // Prüfe 2-3 Blöcke über dem Topf
        for (int yOffset = 2; yOffset <= 3; yOffset++) {
            BlockPos growLightPos = worldPosition.above(yOffset);
            BlockState checkState = level.getBlockState(growLightPos);
            Block checkBlock = checkState.getBlock();

            if (checkBlock instanceof GrowLightSlabBlock growLight) {
                return growLight.getTier().getQualityBonus();
            }
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
