package de.rolandsw.schedulemc.secretdoors.blockentity;

import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity für den Füller-Block.
 * Speichert nur die absolute Position des Controller-Blocks.
 */
public class DoorFillerBlockEntity extends BlockEntity {

    private BlockPos controllerPos = null;

    public DoorFillerBlockEntity(BlockPos pos, BlockState state) {
        super(SecretDoors.DOOR_FILLER_BE.get(), pos, state);
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos pos) {
        this.controllerPos = pos;
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (controllerPos != null) {
            tag.putLong("controller_pos", controllerPos.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("controller_pos")) {
            controllerPos = BlockPos.of(tag.getLong("controller_pos"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
