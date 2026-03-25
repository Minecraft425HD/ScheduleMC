package de.rolandsw.schedulemc.secretdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blocks.AbstractSecretDoorBlock;
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
 * Rendert die Geheimtür mit der Tarnung (Textur des gewählten Blocks).
 * Wenn keine Tarnung gesetzt, wird Steinquader als Standard genutzt.
 */
@OnlyIn(Dist.CLIENT)
public class SecretDoorBlockEntityRenderer implements BlockEntityRenderer<SecretDoorBlockEntity> {

    public SecretDoorBlockEntityRenderer(BlockEntityRendererProvider.Context _ctx) {}

    @Override
    public void render(SecretDoorBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Wenn offen: nichts rendern
        if (be.getBlockState().getValue(AbstractSecretDoorBlock.OPEN)) return;

        // Tarnung bestimmen (Standard: Steinquader)
        Block camoBlock = be.getCamoBlock();
        BlockState renderState = (camoBlock != null ? camoBlock : Blocks.STONE_BRICKS).defaultBlockState();

        // Block rendern
        Minecraft.getInstance().getBlockRenderer()
            .renderSingleBlock(renderState, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(SecretDoorBlockEntity be) {
        return false;
    }
}
