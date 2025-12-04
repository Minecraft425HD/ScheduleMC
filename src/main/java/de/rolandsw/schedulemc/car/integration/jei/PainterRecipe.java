package de.rolandsw.schedulemc.car.integration.jei;

import de.rolandsw.schedulemc.car.blocks.BlockPaint;
import net.minecraft.world.item.Item;

public class PainterRecipe {

    private Item input;
    private BlockPaint output;

    public PainterRecipe(Item input, BlockPaint output) {
        this.input = input;
        this.output = output;
    }

    public Item getInput() {
        return input;
    }

    public BlockPaint getOutput() {
        return output;
    }
}
