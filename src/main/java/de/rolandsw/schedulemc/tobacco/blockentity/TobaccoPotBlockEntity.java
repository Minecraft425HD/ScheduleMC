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

        // Ressourcen-Verbrauch (alle 5 Ticks = 4x pro Sekunde für flüssige Anzeige)
        if (tickCounter >= 5) {
            tickCounter = 0;
            plantGrowthCounter++;

            if (potData.hasPlant() && potData.canGrow()) {
                int oldStage = potData.getPlant().getGrowthStage();
                int oldWater = potData.getWaterLevel();
                int oldSoil = potData.getSoilLevel();

                // Ressourcen verbrauchen (JEDES Mal)
                potData.consumeWater(potData.getPlant().getType().getWaterConsumption() * 0.0375);
                potData.consumeSoil(0.075);

                // Pflanze wachsen lassen (NUR alle 4. Mal = 1x pro Sekunde wie früher)
                if (plantGrowthCounter >= 4) {
                    plantGrowthCounter = 0;
                    potData.getPlant().tick();
                }

                int newStage = potData.getPlant().getGrowthStage();

                // Update Pflanzen-Block wenn Wachstumsstufe sich geändert hat
                if (oldStage != newStage) {
                    de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock.growToStage(
                        level, worldPosition, newStage, potData.getPlant().getType()
                    );
                }

                setChanged();

                // SOFORT Client-Update senden (IMMER bei Wachstum)
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        
        tag.putString("PotType", potData.getPotType().name());
        tag.putInt("WaterLevel", potData.getWaterLevel());
        tag.putInt("SoilLevel", potData.getSoilLevel());
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

        if (tag.contains("PotType")) {
            PotType type = PotType.valueOf(tag.getString("PotType"));
            this.potData = new TobaccoPotData(type);
        }

        // WICHTIG: Gespeicherte Werte direkt setzen, nicht addieren!
        if (tag.contains("WaterLevel")) {
            potData.setWaterLevel(tag.getInt("WaterLevel"));
        }
        if (tag.contains("SoilLevel")) {
            potData.setSoilLevel(tag.getInt("SoilLevel"));
        }
        if (tag.contains("HasSoil")) {
            // HasSoil wird jetzt automatisch in setSoilLevel gesetzt
        }

        if (tag.contains("Plant")) {
            CompoundTag plantTag = tag.getCompound("Plant");

            TobaccoType type = TobaccoType.valueOf(plantTag.getString("Type"));
            potData.plantSeed(type);

            var plant = potData.getPlant();
            if (plant != null) {
                plant.setQuality(de.rolandsw.schedulemc.tobacco.TobaccoQuality.valueOf(plantTag.getString("Quality")));
                plant.setGrowthStage(plantTag.getInt("GrowthStage"));

                for (int i = 0; i < plantTag.getInt("TicksGrown"); i++) {
                    plant.incrementTicks();
                }

                if (plantTag.getBoolean("HasFertilizer")) plant.applyFertilizer();
                if (plantTag.getBoolean("HasGrowthBooster")) plant.applyGrowthBooster();
                if (plantTag.getBoolean("HasQualityBooster")) plant.applyQualityBooster();
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
