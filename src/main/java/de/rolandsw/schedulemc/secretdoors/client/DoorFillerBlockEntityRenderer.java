package de.rolandsw.schedulemc.secretdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Rendert einen Füller-Block mit der Tarnung seines Controllers.
 * Schaut den Camo-Block beim Controller-BlockEntity nach und zeichnet ihn
 * an der Füller-Position – damit das gesamte Türfeld einheitlich aussieht.
 */
@OnlyIn(Dist.CLIENT)
public class DoorFillerBlockEntityRenderer implements BlockEntityRenderer<DoorFillerBlockEntity> {

    public DoorFillerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(DoorFillerBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) return;

        BlockPos controllerPos = be.getControllerPos();
        if (controllerPos == null) return;

        BlockEntity controllerBE = level.getBlockEntity(controllerPos);
        if (!(controllerBE instanceof SecretDoorBlockEntity doorBE)) return;

        // Camo des Controllers übernehmen (Standard: Steinquader – identisch zum Controller-Renderer)
        Block camoBlock = doorBE.getCamoBlock();
        BlockState renderState = (camoBlock != null ? camoBlock : Blocks.STONE_BRICKS).defaultBlockState();

        Minecraft.getInstance().getBlockRenderer()
            .renderSingleBlock(renderState, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(DoorFillerBlockEntity be) {
        // Grosse Türen können über Chunk-Grenzen gehen – immer rendern
        return true;
    }
}
