package de.rolandsw.schedulemc.cannabis.blockentity;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.items.DriedBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Trimm-Station – einfaches Klick-System.
 * Spieler klickt CLICKS_NEEDED Mal auf den Button.
 * Buds werden direkt aus dem Spieler-Inventar genommen.
 * Output: 1× TrimmedBud (gleiche Sorte/Qualität) + 2× Trim. Qualität bleibt gleich.
 */
public class TrimmStationBlockEntity extends BlockEntity {

    public static final int CLICKS_NEEDED = 5;

    private int clickCount = 0;
    private CannabisStrain  lastStrain  = CannabisStrain.HYBRID;
    private CannabisQuality lastQuality = CannabisQuality.GUT;

    public TrimmStationBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.TRIMM_STATION.get(), pos, state);
    }

    /**
     * Spieler klickt Trim-Button. Sucht ersten DriedBud im Inventar.
     * Nach CLICKS_NEEDED Klicks: 1 DriedBud entnehmen,
     * 1 TrimmedBud (gleiche Qualität) + 2 Trim ins Inventar geben.
     */
    public boolean doTrimClick(Player player) {
        ItemStack bud = findDriedBud(player);
        if (bud.isEmpty()) return false;

        lastStrain  = DriedBudItem.getStrain(bud);
        lastQuality = DriedBudItem.getQuality(bud);
        clickCount++;

        if (clickCount >= CLICKS_NEEDED) {
            bud.shrink(1);

            ItemStack tb = TrimmedBudItem.create(lastStrain, lastQuality, 1);
            if (!player.addItem(tb)) Block.popResource(level, worldPosition, tb);

            ItemStack tr = TrimItem.create(lastStrain, lastQuality, 2);
            while (!tr.isEmpty()) {
                ItemStack one = tr.split(1);
                if (!player.addItem(one)) Block.popResource(level, worldPosition, one);
            }

            clickCount = 0;
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    private ItemStack findDriedBud(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof DriedBudItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    // Getter
    public int getClickCount()           { return clickCount; }
    public CannabisStrain  getLastStrain()  { return lastStrain; }
    public CannabisQuality getLastQuality() { return lastQuality; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ClickCount",   clickCount);
        tag.putString("LastStrain",  lastStrain.name());
        tag.putString("LastQuality", lastQuality.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        clickCount = tag.getInt("ClickCount");
        try { lastStrain  = CannabisStrain.valueOf(tag.getString("LastStrain")); }
        catch (IllegalArgumentException e) { lastStrain = CannabisStrain.HYBRID; }
        try { lastQuality = CannabisQuality.valueOf(tag.getString("LastQuality")); }
        catch (IllegalArgumentException e) { lastQuality = CannabisQuality.GUT; }
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
