package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.PotType;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPotData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
                int oldStage = potData.getPlant().getGrowthStage();

                // Pflanze wachsen lassen (alle 4. Mal = 1x pro Sekunde)
                if (plantGrowthCounter >= 4) {
                    plantGrowthCounter = 0;
                    potData.getPlant().tick();
                }

                int newStage = potData.getPlant().getGrowthStage();

                // Ressourcen BEIM WACHSTUM verbrauchen (bei jedem Wachstumsschritt)
                if (oldStage != newStage) {
                    // 7 Wachstumsschritte (0→1, 1→2, ... 6→7)
                    // Verbrauche 1/7 der Topfkapazität pro Schritt
                    // Damit ist der Topf bei Erntereife IMMER leer (unabhängig von Topf-Typ)
                    double waterPerStep = potData.getMaxWater() / 7.0;
                    double soilPerStep = 15.0 / 7.0;

                    potData.consumeWater(waterPerStep);
                    potData.consumeSoil(soilPerStep);

                    // Update Pflanzen-Block
                    de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock.growToStage(
                        level, worldPosition, newStage, potData.getPlant().getType()
                    );

                    setChanged();
                    // Client-Update senden (nur bei tatsächlichem Wachstum)
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        
        tag.putString("PotType", potData.getPotType().name());
        tag.putDouble("WaterLevel", potData.getWaterLevelExact()); // Speichere exakte Werte
        tag.putDouble("SoilLevel", potData.getSoilLevelExact()); // Speichere exakte Werte
        tag.putBoolean("HasSoil", potData.hasSoil());
        
        if (potData.hasPlant()) {
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
            potData.setWaterLevel(tag.getDouble("WaterLevel")); // Lade als double
        }
        if (tag.contains("SoilLevel")) {
            potData.setSoilLevel(tag.getDouble("SoilLevel")); // Lade als double
        }

        // Pflanzen-Daten laden (oder Pflanze entfernen wenn nicht im Tag)
        if (tag.contains("Plant")) {
            CompoundTag plantTag = tag.getCompound("Plant");

            TobaccoType type = TobaccoType.valueOf(plantTag.getString("Type"));

            // Nur Samen pflanzen wenn noch keine Pflanze da ist
            if (!potData.hasPlant()) {
                potData.plantSeed(type);
            }

            var plant = potData.getPlant();
            if (plant != null) {
                plant.setQuality(de.rolandsw.schedulemc.tobacco.TobaccoQuality.valueOf(plantTag.getString("Quality")));
                plant.setGrowthStage(plantTag.getInt("GrowthStage"));

                // Ticks rekonstruieren
                int ticksGrown = plantTag.getInt("TicksGrown");
                while (plant.getTicksGrown() < ticksGrown) {
                    plant.incrementTicks();
                }

                if (plantTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
                if (plantTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
                if (plantTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
            }
        } else {
            // Kein Plant-Tag -> Pflanze entfernen falls vorhanden
            if (potData.hasPlant()) {
                potData.clearPlant();
            }
        }
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
