package de.rolandsw.schedulemc.secretdoors.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Geheime Schwenktür (Pivot Door).
 * Öffnet sich wie eine normale Tür (dreht sich seitwärts).
 * Unterstützt dynamische Größen von 1×1 bis 20×20.
 */
public class SecretDoorBlock extends AbstractSecretDoorBlock {

    public SecretDoorBlock(BlockBehaviour.Properties props) {
        super(props, DoorType.PIVOT);
    }
}
