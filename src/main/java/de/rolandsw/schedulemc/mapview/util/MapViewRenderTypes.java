package de.rolandsw.schedulemc.mapview.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MapViewRenderTypes extends RenderStateShard {
    private MapViewRenderTypes(String p_173178_, Runnable p_173179_, Runnable p_173180_) {
        super(p_173178_, p_173179_, p_173180_);
    }

    // GUI textured render type - for map and UI elements
    public static final Function<ResourceLocation, RenderType> GUI_TEXTURED = Util.memoize(
            (texture) -> RenderType.create(
                    "lightmap_gui_textured",
                    DefaultVertexFormat.POSITION_COLOR_TEX,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_TEXT_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(NO_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE)
                            .setCullState(NO_CULL)
                            .createCompositeState(false)
            )
    );

    // GUI textured unfiltered - for pixel-perfect rendering
    public static final Function<ResourceLocation, RenderType> GUI_TEXTURED_UNFILTERED = Util.memoize(
            (texture) -> RenderType.create(
                    "lightmap_gui_textured_unfiltered",
                    DefaultVertexFormat.POSITION_COLOR_TEX,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_TEXT_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, true)) // mipmap = true for unfiltered
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(NO_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE)
                            .setCullState(NO_CULL)
                            .createCompositeState(false)
            )
    );

    // GUI solid color render type
    public static final RenderType GUI = RenderType.create(
            "lightmap_gui",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_GUI_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );
}
