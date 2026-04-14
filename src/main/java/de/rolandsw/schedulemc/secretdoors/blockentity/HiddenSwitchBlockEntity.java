package de.rolandsw.schedulemc.secretdoors.blockentity;

import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.mission.SecretBlockRegistry;
import de.rolandsw.schedulemc.secretdoors.blocks.AbstractSecretDoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BlockEntity für versteckte Schalter.
 * Speichert eine Liste verknüpfter Türen (absolute BlockPos).
 * Max. 8 verknüpfte Türen.
 */
public class HiddenSwitchBlockEntity extends BlockEntity {

    public static final int MAX_LINKED_DOORS = 8;

    private final List<long[]> linkedDoors = new ArrayList<>();
    private UUID ownerId = null;
    private String ownerName = "";
    private boolean linkingMode = false;
    private String camoBlockId = null; // Block-ID für Tarnung (null = Standard Steinquader)

    public HiddenSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(SecretDoors.HIDDEN_SWITCH_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            SecretBlockRegistry.register(serverLevel, worldPosition, "HIDDEN_SWITCH");
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            SecretBlockRegistry.unregister(serverLevel, worldPosition);
        }
        super.setRemoved();
    }

    // ─────────────────────────────────────────────────────────────────
    // Toggle alle verknüpften Türen
    // ─────────────────────────────────────────────────────────────────

    public void toggleLinkedDoors(Level level, Player player) {
        if (level.isClientSide) return;
        List<long[]> toRemove = new ArrayList<>();
        for (long[] entry : linkedDoors) {
            BlockPos doorPos = BlockPos.of(entry[0]);
            BlockState doorState = level.getBlockState(doorPos);
            if (doorState.getBlock() instanceof AbstractSecretDoorBlock) {
                if (level.getBlockEntity(doorPos) instanceof SecretDoorBlockEntity doorBe) {
                    doorBe.toggle(level, player);
                }
            } else {
                // Verknüpfung entfernen wenn Block nicht mehr existiert
                toRemove.add(entry);
            }
        }
        linkedDoors.removeAll(toRemove);
        if (!toRemove.isEmpty()) setChanged();
    }

    // ─────────────────────────────────────────────────────────────────
    // Verknüpfungen verwalten
    // ─────────────────────────────────────────────────────────────────

    public boolean linkDoor(BlockPos doorPos, Player player) {
        if (!canEdit(player)) return false;
        long encoded = doorPos.asLong();
        // Prüfen ob bereits verknüpft → dann trennen
        for (int i = 0; i < linkedDoors.size(); i++) {
            if (linkedDoors.get(i)[0] == encoded) {
                linkedDoors.remove(i);
                setChanged();
                return false; // false = getrennt
            }
        }
        if (linkedDoors.size() >= MAX_LINKED_DOORS) return false;
        linkedDoors.add(new long[]{encoded});
        setChanged();
        return true; // true = verknüpft
    }

    public void unlinkDoor(BlockPos doorPos) {
        long encoded = doorPos.asLong();
        linkedDoors.removeIf(entry -> entry[0] == encoded);
        setChanged();
    }

    public List<BlockPos> getLinkedDoors() {
        List<BlockPos> result = new ArrayList<>();
        for (long[] entry : linkedDoors) {
            result.add(BlockPos.of(entry[0]));
        }
        return result;
    }

    public int getLinkedDoorCount() {
        return linkedDoors.size();
    }

    // ─────────────────────────────────────────────────────────────────
    // Tarnung (Camo)
    // ─────────────────────────────────────────────────────────────────

    public void setCamoBlock(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        this.camoBlockId = key != null ? key.toString() : null;
        setChanged();
    }

    public void clearCamoBlock() {
        this.camoBlockId = null;
        setChanged();
    }

    /** Gibt den Block zurück, dessen Textur als Tarnung genutzt wird (oder Steinquader als Standard). */
    public Block getCamoBlock() {
        if (camoBlockId == null) return Blocks.STONE_BRICKS;
        ResourceLocation rl = new ResourceLocation(camoBlockId);
        Block block = ForgeRegistries.BLOCKS.containsKey(rl) ? ForgeRegistries.BLOCKS.getValue(rl) : null;
        return block != null ? block : Blocks.STONE_BRICKS;
    }

    public String getCamoBlockId() { return camoBlockId; }

    // ─────────────────────────────────────────────────────────────────
    // Berechtigung / Besitzer
    // ─────────────────────────────────────────────────────────────────

    public boolean canEdit(Player player) {
        return (player instanceof ServerPlayer sp && sp.hasPermissions(2)) || ownerId == null || ownerId.equals(player.getUUID());
    }

    public void setOwner(Player player) {
        this.ownerId = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    public boolean isLinkingMode() { return linkingMode; }

    public void setLinkingMode(boolean linkingMode) {
        this.linkingMode = linkingMode;
        setChanged();
    }

    public String getOwnerName() { return ownerName; }

    // ─────────────────────────────────────────────────────────────────
    // NBT
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag doorList = new ListTag();
        for (long[] entry : linkedDoors) {
            CompoundTag doorTag = new CompoundTag();
            doorTag.putLong("pos", entry[0]);
            doorList.add(doorTag);
        }
        tag.put("linked_doors", doorList);
        if (ownerId != null) {
            tag.putUUID("owner_id", ownerId);
            tag.putString("owner_name", ownerName);
        }
        tag.putBoolean("linking_mode", linkingMode);
        if (camoBlockId != null) {
            tag.putString("camo_block", camoBlockId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        linkedDoors.clear();
        ListTag doorList = tag.getList("linked_doors", Tag.TAG_COMPOUND);
        for (int i = 0; i < doorList.size(); i++) {
            CompoundTag doorTag = doorList.getCompound(i);
            linkedDoors.add(new long[]{doorTag.getLong("pos")});
        }
        if (tag.hasUUID("owner_id")) {
            ownerId = tag.getUUID("owner_id");
            ownerName = tag.getString("owner_name");
        }
        linkingMode = tag.getBoolean("linking_mode");
        camoBlockId = tag.contains("camo_block") ? tag.getString("camo_block") : null;
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
