package de.rolandsw.schedulemc.secretdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.secretdoors.blockentity.ElevatorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Rendert den Aufzug-Controller mit seiner Tarnung.
 * Wenn keine Tarnung gesetzt: Standard-Eisen-Block.
 */
@OnlyIn(Dist.CLIENT)
public class ElevatorBlockEntityRenderer implements BlockEntityRenderer<ElevatorBlockEntity> {

    public ElevatorBlockEntityRenderer(BlockEntityRendererProvider.Context _ctx) {}

    @Override
    public void render(ElevatorBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Block camoBlock = be.getCamoBlock();
        BlockState renderState = (camoBlock != null ? camoBlock : Blocks.IRON_BLOCK).defaultBlockState();

        Minecraft.getInstance().getBlockRenderer()
            .renderSingleBlock(renderState, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(ElevatorBlockEntity be) {
        return true;
    }
}
