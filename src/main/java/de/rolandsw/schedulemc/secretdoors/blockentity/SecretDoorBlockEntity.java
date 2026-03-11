package de.rolandsw.schedulemc.secretdoors.blockentity;

import de.rolandsw.schedulemc.secretdoors.SecretDoors;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BlockEntity für alle geheimen Türen.
 * Speichert: Größe (width x height), Offene/Geschlossene-Zustand,
 * Besitzer-UUID, Positionen der Füller-Blöcke und verknüpfte Schalter.
 */
public class SecretDoorBlockEntity extends BlockEntity {

    private int doorWidth = 1;
    private int doorHeight = 2;
    private boolean open = false;
    private UUID ownerId = null;
    private String ownerName = "";

    // Tarnung: Textur des angeklickten Blocks
    private String camoBlockId = null;

    // Relative Offsets der Füller-Blöcke (als [dx, dy, dz])
    private final List<int[]> fillerOffsets = new ArrayList<>();

    // Absolute Positionen verknüpfter Schalter
    private final List<long[]> linkedSwitches = new ArrayList<>();

    public SecretDoorBlockEntity(BlockPos pos, BlockState state) {
        super(SecretDoors.SECRET_DOOR_BE.get(), pos, state);
    }

    // ─────────────────────────────────────────────────────────────────
    // Kern-Logik
    // ─────────────────────────────────────────────────────────────────

    /**
     * Tür öffnen oder schließen.
     */
    public void toggle(Level level, Player player) {
        if (level.isClientSide) return;
        if (!canUse(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cDu hast keine Berechtigung, diese Tür zu bedienen."));
            return;
        }
        if (open) {
            close(level);
        } else {
            open(level);
        }
        setChanged();
        // Sync to clients
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /**
     * Tür öffnen: Alle Füller-Blöcke durch Luft ersetzen, OPEN=true setzen.
     */
    public void open(Level level) {
        if (level.isClientSide) return;
        open = true;
        // Füller-Blöcke entfernen
        for (int[] offset : fillerOffsets) {
            BlockPos fillerPos = worldPosition.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(fillerPos).is(SecretDoors.DOOR_FILLER.get())) {
                level.setBlockAndUpdate(fillerPos, Blocks.AIR.defaultBlockState());
            }
        }
        // Controller-Block auf OPEN=true setzen
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.OPEN)) {
            level.setBlockAndUpdate(worldPosition, state.setValue(BlockStateProperties.OPEN, true));
        }
        setChanged();
    }

    /**
     * Tür schließen: Füller-Blöcke wiederherstellen, OPEN=false setzen.
     */
    public void close(Level level) {
        if (level.isClientSide) return;
        open = false;
        // Füller-Blöcke wiederherstellen
        for (int[] offset : fillerOffsets) {
            BlockPos fillerPos = worldPosition.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(fillerPos).isAir()) {
                level.setBlockAndUpdate(fillerPos, SecretDoors.DOOR_FILLER.get().defaultBlockState());
                // FillerBlockEntity mit Controller-Position initialisieren
                if (level.getBlockEntity(fillerPos) instanceof DoorFillerBlockEntity fbe) {
                    fbe.setControllerPos(worldPosition);
                    fbe.setChanged();
                }
            }
        }
        // Controller-Block auf OPEN=false setzen
        BlockState state = level.getBlockState(worldPosition);
        if (state.hasProperty(BlockStateProperties.OPEN)) {
            level.setBlockAndUpdate(worldPosition, state.setValue(BlockStateProperties.OPEN, false));
        }
        setChanged();
    }

    /**
     * Größe der Tür ändern (entfernt alte Füller, platziert neue).
     */
    public void setSize(int w, int h, Level level) {
        if (level.isClientSide) return;
        // Alte Füller entfernen
        for (int[] offset : fillerOffsets) {
            BlockPos fillerPos = worldPosition.offset(offset[0], offset[1], offset[2]);
            if (level.getBlockState(fillerPos).is(SecretDoors.DOOR_FILLER.get())) {
                level.setBlockAndUpdate(fillerPos, Blocks.AIR.defaultBlockState());
            }
        }
        fillerOffsets.clear();
        doorWidth = Math.max(1, Math.min(20, w));
        doorHeight = Math.max(1, Math.min(20, h));
        setChanged();
    }

    // ─────────────────────────────────────────────────────────────────
    // Berechtigung
    // ─────────────────────────────────────────────────────────────────

    public boolean canUse(Player player) {
        // OP-Level 2+ oder Besitzer
        if (player instanceof ServerPlayer sp && sp.hasPermissions(2)) return true;
        if (ownerId == null) return true;
        return ownerId.equals(player.getUUID());
    }

    public void setOwner(Player player) {
        this.ownerId = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    // ─────────────────────────────────────────────────────────────────
    // Füller-Offsets verwalten
    // ─────────────────────────────────────────────────────────────────

    public void addFillerOffset(int dx, int dy, int dz) {
        fillerOffsets.add(new int[]{dx, dy, dz});
    }

    public void clearFillerOffsets() {
        fillerOffsets.clear();
    }

    public List<int[]> getFillerOffsets() {
        return fillerOffsets;
    }

    // ─────────────────────────────────────────────────────────────────
    // Verknüpfte Schalter
    // ─────────────────────────────────────────────────────────────────

    public void addLinkedSwitch(BlockPos pos) {
        long encoded = pos.asLong();
        for (long[] entry : linkedSwitches) {
            if (entry[0] == encoded) return; // Bereits verknüpft
        }
        linkedSwitches.add(new long[]{encoded});
        setChanged();
    }

    public void removeLinkedSwitch(BlockPos pos) {
        long encoded = pos.asLong();
        linkedSwitches.removeIf(entry -> entry[0] == encoded);
        setChanged();
    }

    public List<BlockPos> getLinkedSwitches() {
        List<BlockPos> result = new ArrayList<>();
        for (long[] entry : linkedSwitches) {
            result.add(BlockPos.of(entry[0]));
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────
    // Getter / Setter
    // ─────────────────────────────────────────────────────────────────

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public int getDoorWidth() { return doorWidth; }
    public int getDoorHeight() { return doorHeight; }

    public String getOwnerName() { return ownerName; }
    public UUID getOwnerId() { return ownerId; }

    public void setCamoBlock(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        this.camoBlockId = key != null ? key.toString() : null;
        setChanged();
    }

    public void clearCamoBlock() {
        this.camoBlockId = null;
        setChanged();
    }

    /** Gibt den Block zurück, dessen Textur als Tarnung genutzt wird (oder null = Standard). */
    public Block getCamoBlock() {
        if (camoBlockId == null) return null;
        ResourceLocation rl = new ResourceLocation(camoBlockId);
        return ForgeRegistries.BLOCKS.containsKey(rl) ? ForgeRegistries.BLOCKS.getValue(rl) : null;
    }

    public String getCamoBlockId() { return camoBlockId; }

    // ─────────────────────────────────────────────────────────────────
    // NBT
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("door_width", doorWidth);
        tag.putInt("door_height", doorHeight);
        tag.putBoolean("open", open);
        if (ownerId != null) {
            tag.putUUID("owner_id", ownerId);
            tag.putString("owner_name", ownerName);
        }
        if (camoBlockId != null) {
            tag.putString("camo_block", camoBlockId);
        }
        // Füller-Offsets speichern
        ListTag fillerList = new ListTag();
        for (int[] offset : fillerOffsets) {
            CompoundTag offsetTag = new CompoundTag();
            offsetTag.putInt("dx", offset[0]);
            offsetTag.putInt("dy", offset[1]);
            offsetTag.putInt("dz", offset[2]);
            fillerList.add(offsetTag);
        }
        tag.put("filler_offsets", fillerList);
        // Verknüpfte Schalter speichern
        ListTag switchList = new ListTag();
        for (long[] entry : linkedSwitches) {
            CompoundTag switchTag = new CompoundTag();
            switchTag.putLong("pos", entry[0]);
            switchList.add(switchTag);
        }
        tag.put("linked_switches", switchList);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        doorWidth = tag.getInt("door_width");
        doorHeight = tag.getInt("door_height");
        if (doorWidth < 1) doorWidth = 1;
        if (doorHeight < 1) doorHeight = 2;
        open = tag.getBoolean("open");
        if (tag.hasUUID("owner_id")) {
            ownerId = tag.getUUID("owner_id");
            ownerName = tag.getString("owner_name");
        }
        camoBlockId = tag.contains("camo_block") ? tag.getString("camo_block") : null;
        // Füller-Offsets laden
        fillerOffsets.clear();
        ListTag fillerList = tag.getList("filler_offsets", Tag.TAG_COMPOUND);
        for (int i = 0; i < fillerList.size(); i++) {
            CompoundTag offsetTag = fillerList.getCompound(i);
            fillerOffsets.add(new int[]{
                offsetTag.getInt("dx"),
                offsetTag.getInt("dy"),
                offsetTag.getInt("dz")
            });
        }
        // Verknüpfte Schalter laden
        linkedSwitches.clear();
        ListTag switchList = tag.getList("linked_switches", Tag.TAG_COMPOUND);
        for (int i = 0; i < switchList.size(); i++) {
            CompoundTag switchTag = switchList.getCompound(i);
            linkedSwitches.add(new long[]{switchTag.getLong("pos")});
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
