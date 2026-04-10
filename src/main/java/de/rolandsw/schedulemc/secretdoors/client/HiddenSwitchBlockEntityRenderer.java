package de.rolandsw.schedulemc.secretdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.rolandsw.schedulemc.secretdoors.SecretDoors;
import de.rolandsw.schedulemc.secretdoors.blockentity.HiddenSwitchBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Rendert den versteckten Schalter mit der gewählten Tarnung (Textur des gewählten Blocks).
 * Standard-Textur: Steinquader.
 */
@OnlyIn(Dist.CLIENT)
public class HiddenSwitchBlockEntityRenderer implements BlockEntityRenderer<HiddenSwitchBlockEntity> {

    public HiddenSwitchBlockEntityRenderer(BlockEntityRendererProvider.Context _ctx) {}

    @Override
    public void render(HiddenSwitchBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState renderState = be.getCamoBlock().defaultBlockState();

        // Niemals den Hidden-Switch selbst (oder andere nicht-modellbasierte Blöcke)
        // als Tarn-Renderstate verwenden, sonst kann es zu schwarzem/fehlerhaftem
        // Rendering kommen.
        if (renderState.getRenderShape() != RenderShape.MODEL
            || renderState.is(SecretDoors.HIDDEN_SWITCH_STONE.get())
            || renderState.is(SecretDoors.HIDDEN_SWITCH_OAK.get())) {
            renderState = Blocks.STONE_BRICKS.defaultBlockState();
        }

        // packedLight vom Renderer verwenden (wie bei den anderen Secret-Door BERs),
        // damit keine fehlerhaften Dunkel-/Schwarzdarstellungen auftreten.
        Minecraft.getInstance().getBlockRenderer()
            .renderSingleBlock(renderState, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(HiddenSwitchBlockEntity be) {
        return false;
    }
}
