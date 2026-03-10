package de.rolandsw.schedulemc.secretdoors.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Fallgitter / Tor (Portcullis).
 * Zieht sich beim Öffnen nach oben zurück.
 * Unterstützt dynamische Größen von 1×1 bis 10×10.
 */
public class PortcullisBlock extends AbstractSecretDoorBlock {

    public PortcullisBlock(BlockBehaviour.Properties props, DoorMaterial material) {
        super(props, material, DoorType.PORTCULLIS);
    }
}
