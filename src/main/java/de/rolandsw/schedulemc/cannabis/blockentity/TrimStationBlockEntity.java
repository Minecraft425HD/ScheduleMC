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
 * Spieler legt DriedBud per Rechtsklick ein (inputItem).
 * 5× Button-Klick → 1 TrimmedBud + 2 Trim im Ausgabefach (outputTrimmedBud, outputTrim).
 * Shift+Rechtsklick → Ausgabe (oder Eingabe falls leer) entnehmen.
 */
public class TrimStationBlockEntity extends BlockEntity {

    public static final int CLICKS_NEEDED = 5;

    private int clickCount = 0;
    private CannabisStrain  lastStrain  = CannabisStrain.HYBRID;
    private CannabisQuality lastQuality = CannabisQuality.GUT;

    private ItemStack inputItem       = ItemStack.EMPTY;
    private ItemStack outputTrimmedBud = ItemStack.EMPTY;
    private ItemStack outputTrim       = ItemStack.EMPTY;

    public TrimStationBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.TRIM_STATION.get(), pos, state);
    }

    // ─── Input ───────────────────────────────────────────────────────────────

    public static final int INPUT_MAX_COUNT = 16;

    public boolean addDriedBud(ItemStack stack) {
        if (!(stack.getItem() instanceof DriedBudItem)) return false;
        if (inputItem.isEmpty()) {
            inputItem = stack.copy();
            inputItem.setCount(1);
            setChanged();
            sync();
            return true;
        }
        if (ItemStack.isSameItemSameTags(inputItem, stack) && inputItem.getCount() < INPUT_MAX_COUNT) {
            inputItem.grow(1);
            setChanged();
            sync();
            return true;
        }
        return false;
    }

    // ─── Trim-Klick ──────────────────────────────────────────────────────────

    /**
     * Spieler klickt Trim-Button.
     * Sucht DriedBud zuerst im Eingabefach, dann im Spieler-Inventar.
     * Nach CLICKS_NEEDED Klicks: 1 DriedBud verbrauchen → output befüllen.
     */
    public boolean doTrimClick(Player player) {
        if (inputItem.isEmpty() || !(inputItem.getItem() instanceof DriedBudItem)) return false;

        lastStrain  = DriedBudItem.getStrain(inputItem);
        lastQuality = DriedBudItem.getQuality(inputItem);
        clickCount++;

        if (clickCount >= CLICKS_NEEDED) {
            inputItem.shrink(1);
            if (inputItem.isEmpty()) inputItem = ItemStack.EMPTY;

            // TrimmedBud ins Ausgabefach
            if (outputTrimmedBud.isEmpty()) {
                outputTrimmedBud = TrimmedBudItem.create(lastStrain, lastQuality, 1);
            } else {
                outputTrimmedBud.grow(1);
            }

            // 2× Trim ins Ausgabefach
            if (outputTrim.isEmpty()) {
                outputTrim = TrimItem.create(lastStrain, lastQuality, 2);
            } else {
                outputTrim.grow(2);
            }

            clickCount = 0;
        }

        setChanged();
        sync();
        return true;
    }

    // ─── Output / Extract ────────────────────────────────────────────────────

    public boolean hasOutput() {
        return !outputTrimmedBud.isEmpty() || !outputTrim.isEmpty();
    }

    /**
     * Entnimmt zuerst die Ausgabe; falls keine vorhanden, die Eingabe zurück.
     * @return true wenn etwas entnommen wurde
     */
    public boolean extractSomething(Player player) {
        boolean changed = false;

        if (!outputTrimmedBud.isEmpty()) {
            give(player, outputTrimmedBud);
            outputTrimmedBud = ItemStack.EMPTY;
            changed = true;
        }
        if (!outputTrim.isEmpty()) {
            ItemStack trim = outputTrim.copy();
            while (!trim.isEmpty()) {
                ItemStack one = trim.split(1);
                give(player, one);
            }
            outputTrim = ItemStack.EMPTY;
            changed = true;
        }
        if (!changed && !inputItem.isEmpty()) {
            give(player, inputItem);
            inputItem = ItemStack.EMPTY;
            changed = true;
        }

        if (changed) { setChanged(); sync(); }
        return changed;
    }

    private void give(Player player, ItemStack stack) {
        if (!player.addItem(stack.copy())) {
            Block.popResource(level, worldPosition, stack.copy());
        }
    }

    // ─── Getter ──────────────────────────────────────────────────────────────

    public int getClickCount()            { return clickCount; }
    public CannabisStrain  getLastStrain()  { return lastStrain; }
    public CannabisQuality getLastQuality() { return lastQuality; }
    public ItemStack getInputItem()       { return inputItem; }
    public ItemStack getOutputTrimmedBud() { return outputTrimmedBud; }
    public ItemStack getOutputTrim()      { return outputTrim; }

    public boolean hasInput() { return !inputItem.isEmpty(); }

    public ItemStack extractInputItem() {
        ItemStack result = inputItem.copy();
        inputItem = ItemStack.EMPTY;
        if (!result.isEmpty()) { setChanged(); sync(); }
        return result;
    }
    public ItemStack extractOutputTrimmedBud() {
        ItemStack result = outputTrimmedBud.copy();
        outputTrimmedBud = ItemStack.EMPTY;
        if (!result.isEmpty()) { setChanged(); sync(); }
        return result;
    }
    public ItemStack extractOutputTrim() {
        ItemStack result = outputTrim.copy();
        outputTrim = ItemStack.EMPTY;
        if (!result.isEmpty()) { setChanged(); sync(); }
        return result;
    }

    // ─── Sync / NBT ──────────────────────────────────────────────────────────

    private void sync() {
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ClickCount",   clickCount);
        tag.putString("LastStrain",  lastStrain.name());
        tag.putString("LastQuality", lastQuality.name());
        if (!inputItem.isEmpty()) {
            tag.put("InputItem", inputItem.save(new CompoundTag()));
        }
        if (!outputTrimmedBud.isEmpty()) {
            tag.put("OutputTrimmedBud", outputTrimmedBud.save(new CompoundTag()));
        }
        if (!outputTrim.isEmpty()) {
            tag.put("OutputTrim", outputTrim.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        clickCount = tag.getInt("ClickCount");
        try { lastStrain  = CannabisStrain.valueOf(tag.getString("LastStrain")); }
        catch (IllegalArgumentException e) { lastStrain = CannabisStrain.HYBRID; }
        try { lastQuality = CannabisQuality.valueOf(tag.getString("LastQuality")); }
        catch (IllegalArgumentException e) { lastQuality = CannabisQuality.GUT; }
        inputItem        = tag.contains("InputItem")        ? ItemStack.of(tag.getCompound("InputItem"))        : ItemStack.EMPTY;
        outputTrimmedBud = tag.contains("OutputTrimmedBud") ? ItemStack.of(tag.getCompound("OutputTrimmedBud")) : ItemStack.EMPTY;
        outputTrim       = tag.contains("OutputTrim")       ? ItemStack.of(tag.getCompound("OutputTrim"))       : ItemStack.EMPTY;
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
