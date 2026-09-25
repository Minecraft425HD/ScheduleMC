package de.rolandsw.schedulemc.npc.crime.poster;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

/**
 * Speichert die Fahndungsdaten (Zielspieler, Wanted-Level, Kopfgeld), die beim
 * Platzieren aus dem haltenden {@link WantedPosterItem} übernommen werden.
 */
public class WantedPosterBlockEntity extends BlockEntity {

    @Nullable
    private UUID targetUUID;
    private String targetName = "";
    private int wantedLevel;
    private double bountyAmount;

    public WantedPosterBlockEntity(BlockPos pos, BlockState state) {
        super(WantedPosterRegistry.WANTED_POSTER_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Übernimmt die im Item gespeicherten Fahndungsdaten beim Platzieren.
     */
    public void readFromItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        if (tag.hasUUID(WantedPosterItem.NBT_TARGET_UUID)) {
            targetUUID = tag.getUUID(WantedPosterItem.NBT_TARGET_UUID);
        }
        targetName = tag.getString(WantedPosterItem.NBT_TARGET_NAME);
        wantedLevel = tag.getInt(WantedPosterItem.NBT_WANTED_LEVEL);
        bountyAmount = tag.getDouble(WantedPosterItem.NBT_BOUNTY_AMOUNT);

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Zeigt die Fahndungsdaten dem rechtsklickenden Spieler an.
     */
    public void showInfo(Player player) {
        if (targetUUID == null) {
            if (player instanceof ServerPlayer) {
                player.sendSystemMessage(Component.translatable("message.wanted_poster.blank"));
            }
            return;
        }

        String stars = "★".repeat(Math.max(0, wantedLevel));
        player.sendSystemMessage(Component.translatable("message.wanted_poster.header"));
        player.sendSystemMessage(Component.translatable("message.wanted_poster.name", targetName));
        player.sendSystemMessage(Component.translatable("message.wanted_poster.level", stars));
        player.sendSystemMessage(Component.translatable("message.wanted_poster.bounty",
            String.format(Locale.ROOT, "%.2f", bountyAmount)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (targetUUID != null) {
            tag.putUUID(WantedPosterItem.NBT_TARGET_UUID, targetUUID);
        }
        tag.putString(WantedPosterItem.NBT_TARGET_NAME, targetName);
        tag.putInt(WantedPosterItem.NBT_WANTED_LEVEL, wantedLevel);
        tag.putDouble(WantedPosterItem.NBT_BOUNTY_AMOUNT, bountyAmount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID(WantedPosterItem.NBT_TARGET_UUID)) {
            targetUUID = tag.getUUID(WantedPosterItem.NBT_TARGET_UUID);
        }
        targetName = tag.getString(WantedPosterItem.NBT_TARGET_NAME);
        wantedLevel = tag.getInt(WantedPosterItem.NBT_WANTED_LEVEL);
        bountyAmount = tag.getDouble(WantedPosterItem.NBT_BOUNTY_AMOUNT);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nullable
    public UUID getTargetUUID() { return targetUUID; }
    public String getTargetName() { return targetName; }
    public int getWantedLevel() { return wantedLevel; }
    public double getBountyAmount() { return bountyAmount; }
}
