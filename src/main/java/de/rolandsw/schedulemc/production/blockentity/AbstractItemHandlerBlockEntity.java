package de.rolandsw.schedulemc.production.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Gemeinsame Basis für BlockEntities mit einem einzelnen {@link ItemStackHandler}.
 *
 * <p>Kapselt das Forge-Capability-Boilerplate
 * ({@link #onLoad()} / {@link #invalidateCaps()} / {@link #getCapability})
 * sowie die Client-Server-Synchronisation
 * ({@link #getUpdatePacket()} / {@link #getUpdateTag()}),
 * das in nahezu allen Produktions-BlockEntities wörtlich wiederholt wird.
 *
 * <p><b>Nutzung:</b> Subklasse erstellt {@link #itemHandler} im Konstruktor und weist ihn
 * dem geerbten Feld zu. Die {@link LazyOptional}-Registrierung übernimmt diese Klasse in
 * {@link #onLoad()}, das Forge nach dem Laden aufruft.
 */
public abstract class AbstractItemHandlerBlockEntity extends BlockEntity {

    /** Gemeinsamer ItemHandler – muss vom Subklassen-Konstruktor gesetzt werden. */
    protected ItemStackHandler itemHandler;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected AbstractItemHandlerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ── Forge Capabilities ────────────────────────────────────────────────────

    @Override
    public void onLoad() {
        super.onLoad();
        if (itemHandler != null) {
            lazyItemHandler = LazyOptional.of(() -> itemHandler);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public @NotNull <C> LazyOptional<C> getCapability(@NotNull Capability<C> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    // ── Client-Server-Sync ────────────────────────────────────────────────────

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

    // ── Getter ────────────────────────────────────────────────────────────────

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}
