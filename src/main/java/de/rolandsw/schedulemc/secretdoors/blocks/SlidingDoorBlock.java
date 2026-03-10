package de.rolandsw.schedulemc.secretdoors.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Geheime Schiebetür (Sliding Door).
 * Öffnet sich, indem sie seitlich in die Wand gleitet.
 * Unterstützt dynamische Größen von 1×1 bis 10×10.
 */
public class SlidingDoorBlock extends AbstractSecretDoorBlock {

    public SlidingDoorBlock(BlockBehaviour.Properties props, DoorMaterial material) {
        super(props, material, DoorType.SLIDE);
    }
}
