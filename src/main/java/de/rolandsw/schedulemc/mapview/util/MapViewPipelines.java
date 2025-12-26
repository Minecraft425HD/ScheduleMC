package de.rolandsw.schedulemc.mapview.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;

// 1.20.1 Port - Replaced RenderPipeline with RenderType
// These are now simple references to MapViewRenderTypes or inline RenderType definitions
public class MapViewPipelines extends RenderStateShard {
    private MapViewPipelines(String p_173178_, Runnable p_173179_, Runnable p_173180_) {
        super(p_173178_, p_173179_, p_173180_);
    }

    // GUI textured with no depth test - standard GUI rendering
    public static final Function<ResourceLocation, RenderType> GUI_TEXTURED_ANY_DEPTH_PIPELINE =
            MapViewRenderTypes.GUI_TEXTURED;

    // GUI textured with DST_ALPHA blending
    public static final Function<ResourceLocation, RenderType> GUI_TEXTURED_ANY_DEPTH_DST_ALPHA_PIPELINE =
            net.minecraft.Util.memoize((texture) -> RenderType.create(
                    "lightmap_gui_textured_dst_alpha",
                    DefaultVertexFormat.POSITION_COLOR_TEX,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_TEXT_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setTransparencyState(new TransparencyStateShard("dst_alpha_transparency",
                                    () -> {
                                        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                                        com.mojang.blaze3d.systems.RenderSystem.blendFunc(
                                                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.DST_ALPHA,
                                                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_DST_ALPHA
                                        );
                                    },
                                    () -> {
                                        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                                        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                                    }))
                            .setDepthTestState(NO_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE)
                            .setCullState(NO_CULL)
                            .createCompositeState(false)
            ));

    // GUI textured with LEQUAL depth test
    public static final Function<ResourceLocation, RenderType> GUI_TEXTURED_LESS_OR_EQUAL_DEPTH_PIPELINE =
            net.minecraft.Util.memoize((texture) -> RenderType.create(
                    "lightmap_gui_textured_lequal_depth",
                    DefaultVertexFormat.POSITION_COLOR_TEX,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_TEXT_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(COLOR_DEPTH_WRITE)
                            .setCullState(NO_CULL)
                            .createCompositeState(false)
            ));

    // General GUI and GUI_TEXTURED - reference MapViewRenderTypes
    public static final RenderType GUI = MapViewRenderTypes.GUI;
    public static final Function<ResourceLocation, RenderType> GUI_TEXTURED = MapViewRenderTypes.GUI_TEXTURED;
    public static final Function<ResourceLocation, RenderType> GUI_TEXTURED_UNFILTERED = MapViewRenderTypes.GUI_TEXTURED_UNFILTERED;
}
