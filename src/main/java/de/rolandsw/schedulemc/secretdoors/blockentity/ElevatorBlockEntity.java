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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * BlockEntity für den Aufzug-Block.
 * Speichert: Filler-Offsets (horizontale Plattform), verknüpfte Stationen,
 * Tarnung, Besitzer und den Verknüpfungs-Modus.
 */
public class ElevatorBlockEntity extends BlockEntity {

    public static final int MAX_STATIONS = 32;
    public static final int MAX_DISTANCE = 128; // Max. Y-Abstand zwischen zwei Stationen

    private final List<int[]> fillerOffsets = new ArrayList<>();
    private final List<long[]> linkedStations = new ArrayList<>();
    private String camoBlockId = null;
    private UUID ownerId = null;
    private String ownerName = "";
    private boolean linkingMode = false;

    public ElevatorBlockEntity(BlockPos pos, BlockState state) {
        super(SecretDoors.ELEVATOR_BE.get(), pos, state);
    }

    // ─────────────────────────────────────────────────────────────────
    // Stationen
    // ─────────────────────────────────────────────────────────────────

    /**
     * Verknüpft eine Station (bidirektional wird vom Aufrufer sichergestellt).
     * @return true = verknüpft, false = bereits vorhanden oder Limit / Abstand überschritten
     */
    public boolean addLinkedStation(BlockPos pos) {
        long encoded = pos.asLong();
        for (long[] entry : linkedStations) {
            if (entry[0] == encoded) return false; // bereits vorhanden
        }
        if (linkedStations.size() >= MAX_STATIONS) return false;
        if (Math.abs(pos.getY() - worldPosition.getY()) > MAX_DISTANCE) return false;
        linkedStations.add(new long[]{encoded});
        setChanged();
        return true;
    }

    public void removeLinkedStation(BlockPos pos) {
        long encoded = pos.asLong();
        linkedStations.removeIf(e -> e[0] == encoded);
        setChanged();
    }

    public List<BlockPos> getLinkedStationsSortedByY() {
        List<BlockPos> list = new ArrayList<>();
        for (long[] e : linkedStations) list.add(BlockPos.of(e[0]));
        list.sort(Comparator.comparingInt(BlockPos::getY));
        return list;
    }

    /**
     * Nächste Station oberhalb von {@code current} (zyklisch – springt zur untersten wenn oben).
     */
    public BlockPos getNextStationAbove(BlockPos current) {
        List<BlockPos> sorted = getLinkedStationsSortedByY();
        if (sorted.isEmpty()) return null;
        // Stationen mit höherem Y als current
        for (BlockPos s : sorted) {
            if (s.getY() > current.getY()) return s;
        }
        // Wrap: zurück zur untersten Station
        return sorted.get(0);
    }

    // ─────────────────────────────────────────────────────────────────
    // Teleportation
    // ─────────────────────────────────────────────────────────────────

    /**
     * Teleportiert den Spieler eine Ebene höher (oder zur untersten wenn bereits oben).
     * Gibt false zurück wenn keine verknüpften Stationen vorhanden sind.
     */
    public boolean teleportPlayerUp(ServerPlayer player) {
        BlockPos target = getNextStationAbove(worldPosition);
        if (target == null) return false;
        // Zielkoordinaten: Spieler landet 1 Block über dem Aufzugsboden der Zielstation
        double tx = target.getX() + 0.5;
        double ty = target.getY() + 1.0;
        double tz = target.getZ() + 0.5;
        player.teleportTo(tx, ty, tz);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────
    // Filler-Offsets
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
    // Tarnung
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

    public Block getCamoBlock() {
        if (camoBlockId == null) return null;
        ResourceLocation rl = new ResourceLocation(camoBlockId);
        return ForgeRegistries.BLOCKS.containsKey(rl) ? ForgeRegistries.BLOCKS.getValue(rl) : null;
    }

    public String getCamoBlockId() {
        return camoBlockId;
    }

    // ─────────────────────────────────────────────────────────────────
    // Besitzer & Berechtigung
    // ─────────────────────────────────────────────────────────────────

    public void setOwner(Player player) {
        this.ownerId = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    public boolean canUse(Player player) {
        if (player instanceof ServerPlayer sp && sp.hasPermissions(2)) return true;
        if (ownerId == null) return true;
        return ownerId.equals(player.getUUID());
    }

    public String getOwnerName() {
        return ownerName;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    // ─────────────────────────────────────────────────────────────────
    // Verknüpfungs-Modus
    // ─────────────────────────────────────────────────────────────────

    public void setLinkingMode(boolean mode) {
        this.linkingMode = mode;
        setChanged();
    }

    public boolean isLinkingMode() {
        return linkingMode;
    }

    // ─────────────────────────────────────────────────────────────────
    // NBT
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (camoBlockId != null) tag.putString("camo_block", camoBlockId);
        if (ownerId != null) {
            tag.putUUID("owner_id", ownerId);
            tag.putString("owner_name", ownerName);
        }
        tag.putBoolean("linking_mode", linkingMode);

        ListTag fillerList = new ListTag();
        for (int[] o : fillerOffsets) {
            CompoundTag t = new CompoundTag();
            t.putInt("dx", o[0]); t.putInt("dy", o[1]); t.putInt("dz", o[2]);
            fillerList.add(t);
        }
        tag.put("elevator_filler_offsets", fillerList);

        ListTag stationList = new ListTag();
        for (long[] e : linkedStations) {
            CompoundTag t = new CompoundTag();
            t.putLong("pos", e[0]);
            stationList.add(t);
        }
        tag.put("linked_stations", stationList);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        camoBlockId = tag.contains("camo_block") ? tag.getString("camo_block") : null;
        if (tag.hasUUID("owner_id")) {
            ownerId = tag.getUUID("owner_id");
            ownerName = tag.getString("owner_name");
        }
        linkingMode = tag.getBoolean("linking_mode");

        fillerOffsets.clear();
        ListTag fl = tag.getList("elevator_filler_offsets", Tag.TAG_COMPOUND);
        for (int i = 0; i < fl.size(); i++) {
            CompoundTag t = fl.getCompound(i);
            fillerOffsets.add(new int[]{t.getInt("dx"), t.getInt("dy"), t.getInt("dz")});
        }

        linkedStations.clear();
        ListTag sl = tag.getList("linked_stations", Tag.TAG_COMPOUND);
        for (int i = 0; i < sl.size(); i++) {
            linkedStations.add(new long[]{sl.getCompound(i).getLong("pos")});
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
