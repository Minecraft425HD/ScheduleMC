package de.rolandsw.schedulemc.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * BakedModel-Wrapper, der den Qualitätsrahmen (layer1, tintIndex == 1) versteckt,
 * wenn das Item in der Hand gehalten wird (isRenderingHeld == true).
 *
 * Im Inventar und in der Hotbar bleibt der Rahmen weiterhin sichtbar.
 */
public class QualityFrameHidingModel implements BakedModel {

    private final BakedModel wrapped;

    public QualityFrameHidingModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Forge-Variante (5 Parameter) – wird vom Item-Renderer verwendet.
     * Filtert layer1-Quads (tintIndex == 1) heraus, wenn der Rahmen versteckt werden soll.
     */
    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    @NotNull RandomSource rand,
                                    @NotNull ModelData data, @Nullable RenderType renderType) {
        List<BakedQuad> quads = wrapped.getQuads(state, side, rand, data, renderType);
        if (QualityItemColors.isRenderingHeld) {
            List<BakedQuad> filtered = new ArrayList<>();
            for (BakedQuad quad : quads) {
                if (quad.getTintIndex() != 1) {
                    filtered.add(quad);
                }
            }
            return filtered;
        }
        return quads;
    }

    /**
     * Vanilla-Variante (3 Parameter) – Fallback, falls ohne Forge-Kontext aufgerufen.
     */
    @Override
    @Deprecated
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    @NotNull RandomSource rand) {
        List<BakedQuad> quads = wrapped.getQuads(state, side, rand);
        if (QualityItemColors.isRenderingHeld) {
            List<BakedQuad> filtered = new ArrayList<>();
            for (BakedQuad quad : quads) {
                if (quad.getTintIndex() != 1) {
                    filtered.add(quad);
                }
            }
            return filtered;
        }
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return wrapped.isCustomRenderer();
    }

    @Override
    @NotNull
    public TextureAtlasSprite getParticleIcon() {
        return wrapped.getParticleIcon();
    }

    @Override
    @NotNull
    public ItemOverrides getOverrides() {
        return wrapped.getOverrides();
    }

    @Override
    @NotNull
    public ItemTransforms getTransforms() {
        return wrapped.getTransforms();
    }
}
