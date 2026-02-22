package de.rolandsw.schedulemc.vehicle.gui;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.IContainerFactory;
import org.slf4j.Logger;

public class ContainerFactoryTileEntity<T extends AbstractContainerMenu, U extends BlockEntity> implements IContainerFactory<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ContainerCreator<T, U> containerCreator;

    public ContainerFactoryTileEntity(ContainerCreator<T, U> containerCreator) {
        this.containerCreator = containerCreator;
    }

    @Override
    public T create(int windowId, Inventory inv, FriendlyByteBuf data) {
        BlockEntity te = inv.player.level().getBlockEntity(data.readBlockPos());
        try {
            @SuppressWarnings("unchecked")
            T result = containerCreator.create(windowId, (U) te, inv);
            return result;
        } catch (ClassCastException e) {
            LOGGER.error("BlockEntity type mismatch: expected compatible type, got {}",
                te != null ? te.getClass().getSimpleName() : "null");
            return null;
        }
    }

    public interface ContainerCreator<T extends AbstractContainerMenu, U extends BlockEntity> {
        T create(int windowId, U tileEntity, Inventory inv);
    }
}
