package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.coca.blocks.CocaBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Koka-BlockEntities
 */
public class CocaBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // EXTRAKTIONSWANNEN BlockEntities (3 Größen)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<BlockEntityType<SmallExtractionVatBlockEntity>> SMALL_EXTRACTION_VAT =
            BLOCK_ENTITIES.register("small_extraction_vat", () ->
                    BlockEntityType.Builder.of(SmallExtractionVatBlockEntity::new,
                            CocaBlocks.SMALL_EXTRACTION_VAT.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<MediumExtractionVatBlockEntity>> MEDIUM_EXTRACTION_VAT =
            BLOCK_ENTITIES.register("medium_extraction_vat", () ->
                    BlockEntityType.Builder.of(MediumExtractionVatBlockEntity::new,
                            CocaBlocks.MEDIUM_EXTRACTION_VAT.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<BigExtractionVatBlockEntity>> BIG_EXTRACTION_VAT =
            BLOCK_ENTITIES.register("big_extraction_vat", () ->
                    BlockEntityType.Builder.of(BigExtractionVatBlockEntity::new,
                            CocaBlocks.BIG_EXTRACTION_VAT.get()
                    ).build(null));

    // ═══════════════════════════════════════════════════════════
    // RAFFINERIEN BlockEntities (3 Größen)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<BlockEntityType<SmallRefineryBlockEntity>> SMALL_REFINERY =
            BLOCK_ENTITIES.register("small_refinery", () ->
                    BlockEntityType.Builder.of(SmallRefineryBlockEntity::new,
                            CocaBlocks.SMALL_REFINERY.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<MediumRefineryBlockEntity>> MEDIUM_REFINERY =
            BLOCK_ENTITIES.register("medium_refinery", () ->
                    BlockEntityType.Builder.of(MediumRefineryBlockEntity::new,
                            CocaBlocks.MEDIUM_REFINERY.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<BigRefineryBlockEntity>> BIG_REFINERY =
            BLOCK_ENTITIES.register("big_refinery", () ->
                    BlockEntityType.Builder.of(BigRefineryBlockEntity::new,
                            CocaBlocks.BIG_REFINERY.get()
                    ).build(null));
}
