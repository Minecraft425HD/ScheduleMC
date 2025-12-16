package de.rolandsw.schedulemc.mushroom.blockentity;

import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Wassertank-BlockEntity - gibt automatisch Wasser an benachbarte Töpfe
 */
public class WassertankBlockEntity extends BlockEntity {

    private static final int MAX_WATER = 10000; // 10 Eimer
    private static final int WATER_PER_TICK = 1; // Wasser pro Tick an Topf

    private int waterLevel = 0;
    private int tickCounter = 0;

    public WassertankBlockEntity(BlockPos pos, BlockState state) {
        super(MushroomBlockEntities.WASSERTANK.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        // Nur alle 10 Ticks bewässern
        if (tickCounter < 10) return;
        tickCounter = 0;

        if (waterLevel <= 0) return;

        // Finde benachbarten Topf zum Bewässern
        waterNeighborPot();
    }

    /**
     * Bewässert einen benachbarten Topf
     */
    private void waterNeighborPot() {
        if (level == null || waterLevel <= 0) return;

        // Prüfe alle horizontalen Nachbarn
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = worldPosition.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);

            if (be instanceof TobaccoPotBlockEntity potBE) {
                var potData = potBE.getPotData();

                // Nur bewässern wenn Topf nicht voll ist
                if (potData.getWaterLevel() < potData.getMaxWater()) {
                    int toAdd = Math.min(WATER_PER_TICK, waterLevel);
                    potData.addWater(toAdd);
                    waterLevel -= toAdd;
                    potBE.setChanged();
                    setChanged();

                    // Nur einen Topf pro Tick bewässern
                    return;
                }
            }
        }
    }

    /**
     * Fügt Wasser hinzu
     */
    public int addWater(int amount) {
        int toAdd = Math.min(amount, MAX_WATER - waterLevel);
        waterLevel += toAdd;
        setChanged();
        return toAdd;
    }

    /**
     * Entfernt Wasser
     */
    public int removeWater(int amount) {
        int toRemove = Math.min(amount, waterLevel);
        waterLevel -= toRemove;
        setChanged();
        return toRemove;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public int getMaxWater() {
        return MAX_WATER;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("WaterLevel", waterLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        waterLevel = tag.getInt("WaterLevel");
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
