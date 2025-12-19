package de.rolandsw.schedulemc;

import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import de.rolandsw.schedulemc.economy.blocks.EconomyBlocks;
import de.rolandsw.schedulemc.npc.items.NPCItems;
import de.rolandsw.schedulemc.region.blocks.PlotBlocks;
import de.rolandsw.schedulemc.warehouse.WarehouseBlocks;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.lsd.blocks.LSDBlocks;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.mdma.blocks.MDMABlocks;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.cannabis.items.CannabisSeedItem;
import de.rolandsw.schedulemc.cannabis.blocks.CannabisBlocks;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.coca.blocks.CocaBlocks;
import de.rolandsw.schedulemc.poppy.items.PoppyItems;
import de.rolandsw.schedulemc.poppy.blocks.PoppyBlocks;
import de.rolandsw.schedulemc.mushroom.items.MushroomItems;
import de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks;
import de.rolandsw.schedulemc.meth.items.MethItems;
import de.rolandsw.schedulemc.meth.blocks.MethBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative Tabs für ScheduleMC
 */
public class ModCreativeTabs {
    
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ScheduleMC.MOD_ID);
    
    // ═══════════════════════════════════════════════════════════
    // HAUPT-TAB: Alle ScheduleMC Items
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<CreativeModeTab> PLOTMOD_TAB = CREATIVE_MODE_TABS.register("plotmod_tab",
        () -> CreativeModeTab.builder()
            .title(Component.literal("ScheduleMC"))
            .icon(() -> new ItemStack(ModItems.PLOT_SELECTION_TOOL.get()))
            .displayItems((parameters, output) -> {
                
                // ═══════════════════════════════════════════════════════════
                // PLOT ITEMS
                // ═══════════════════════════════════════════════════════════
                output.accept(ModItems.PLOT_SELECTION_TOOL.get());
                output.accept(ModItems.CASH.get());

                // ═══════════════════════════════════════════════════════════
                // PLOT BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(PlotBlocks.PLOT_INFO_BLOCK.get());

                // ═══════════════════════════════════════════════════════════
                // NPC TOOLS
                // ═══════════════════════════════════════════════════════════
                output.accept(NPCItems.NPC_SPAWNER_TOOL.get());
                output.accept(NPCItems.NPC_LOCATION_TOOL.get());
                output.accept(NPCItems.NPC_LEISURE_TOOL.get());
                output.accept(NPCItems.NPC_PATROL_TOOL.get());

                // ═══════════════════════════════════════════════════════════
                // TABAK SAMEN
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.VIRGINIA_SEEDS.get());
                output.accept(TobaccoItems.BURLEY_SEEDS.get());
                output.accept(TobaccoItems.ORIENTAL_SEEDS.get());
                output.accept(TobaccoItems.HAVANA_SEEDS.get());
                
                // ═══════════════════════════════════════════════════════════
                // TABAK BLÄTTER (Frisch)
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.FRESH_VIRGINIA_LEAF.get());
                output.accept(TobaccoItems.FRESH_BURLEY_LEAF.get());
                output.accept(TobaccoItems.FRESH_ORIENTAL_LEAF.get());
                output.accept(TobaccoItems.FRESH_HAVANA_LEAF.get());
                
                // ═══════════════════════════════════════════════════════════
                // TABAK BLÄTTER (Getrocknet)
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.DRIED_VIRGINIA_LEAF.get());
                output.accept(TobaccoItems.DRIED_BURLEY_LEAF.get());
                output.accept(TobaccoItems.DRIED_ORIENTAL_LEAF.get());
                output.accept(TobaccoItems.DRIED_HAVANA_LEAF.get());
                
                // ═══════════════════════════════════════════════════════════
                // TABAK BLÄTTER (Fermentiert)
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.FERMENTED_VIRGINIA_LEAF.get());
                output.accept(TobaccoItems.FERMENTED_BURLEY_LEAF.get());
                output.accept(TobaccoItems.FERMENTED_ORIENTAL_LEAF.get());
                output.accept(TobaccoItems.FERMENTED_HAVANA_LEAF.get());
                
                // ═══════════════════════════════════════════════════════════
                // FLASCHEN
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.FERTILIZER_BOTTLE.get());
                output.accept(TobaccoItems.GROWTH_BOOSTER_BOTTLE.get());
                output.accept(TobaccoItems.QUALITY_BOOSTER_BOTTLE.get());
                
                // ═══════════════════════════════════════════════════════════
                // WERKZEUGE
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.WATERING_CAN.get());
                
                // ═══════════════════════════════════════════════════════════
                // ERDSÄCKE
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.SOIL_BAG_SMALL.get());
                output.accept(TobaccoItems.SOIL_BAG_MEDIUM.get());
                output.accept(TobaccoItems.SOIL_BAG_LARGE.get());

                // ═══════════════════════════════════════════════════════════
                // VERPACKUNGSMATERIALIEN
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.PACKAGING_BAG.get());
                output.accept(TobaccoItems.PACKAGING_JAR.get());
                output.accept(TobaccoItems.PACKAGING_BOX.get());
                output.accept(TobaccoItems.PACKAGED_TOBACCO.get());

                // ═══════════════════════════════════════════════════════════
                // TÖPFE
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoBlocks.TERRACOTTA_POT.get());
                output.accept(TobaccoBlocks.CERAMIC_POT.get());
                output.accept(TobaccoBlocks.IRON_POT.get());
                output.accept(TobaccoBlocks.GOLDEN_POT.get());
                
                // ═══════════════════════════════════════════════════════════
                // VERARBEITUNGS-BLÖCKE
                // ═══════════════════════════════════════════════════════════
                // Trocknungsgestelle (3 Größen)
                output.accept(TobaccoBlocks.SMALL_DRYING_RACK.get());
                output.accept(TobaccoBlocks.MEDIUM_DRYING_RACK.get());
                output.accept(TobaccoBlocks.BIG_DRYING_RACK.get());

                // Fermentierungsfässer (3 Größen)
                output.accept(TobaccoBlocks.SMALL_FERMENTATION_BARREL.get());
                output.accept(TobaccoBlocks.MEDIUM_FERMENTATION_BARREL.get());
                output.accept(TobaccoBlocks.BIG_FERMENTATION_BARREL.get());

                // Weitere Verarbeitungsblöcke
                output.accept(TobaccoBlocks.SINK.get());

                // Packtische (3 Größen)
                output.accept(TobaccoBlocks.SMALL_PACKAGING_TABLE.get());
                output.accept(TobaccoBlocks.MEDIUM_PACKAGING_TABLE.get());
                output.accept(TobaccoBlocks.LARGE_PACKAGING_TABLE.get());

                // ═══════════════════════════════════════════════════════════
                // GROW LIGHTS
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoBlocks.BASIC_GROW_LIGHT_SLAB.get());
                output.accept(TobaccoBlocks.ADVANCED_GROW_LIGHT_SLAB.get());
                output.accept(TobaccoBlocks.PREMIUM_GROW_LIGHT_SLAB.get());

                // ═══════════════════════════════════════════════════════════
                // ECONOMY BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(EconomyBlocks.CASH_BLOCK.get());
                output.accept(EconomyBlocks.ATM_BLOCK.get());

                // ═══════════════════════════════════════════════════════════
                // WAREHOUSE BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(WarehouseBlocks.WAREHOUSE_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // LSD ITEMS (Präzisions-Labor)
                // ═══════════════════════════════════════════════════════════
                output.accept(LSDItems.MUTTERKORN.get());
                output.accept(LSDItems.ERGOT_KULTUR.get());
                output.accept(LSDItems.LYSERGSAEURE.get());
                output.accept(LSDItems.LSD_LOESUNG.get());
                output.accept(LSDItems.BLOTTER_PAPIER.get());
                output.accept(LSDItems.BLOTTER.get());

                // ═══════════════════════════════════════════════════════════
                // LSD BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(LSDBlocks.FERMENTATIONS_TANK.get());
                output.accept(LSDBlocks.DESTILLATIONS_APPARAT.get());
                output.accept(LSDBlocks.MIKRO_DOSIERER.get());
                output.accept(LSDBlocks.PERFORATIONS_PRESSE.get());

                // ═══════════════════════════════════════════════════════════
                // MDMA ITEMS (Arcade-Style Pillen-Presse)
                // ═══════════════════════════════════════════════════════════
                output.accept(MDMAItems.SAFROL.get());
                output.accept(MDMAItems.MDMA_BASE.get());
                output.accept(MDMAItems.MDMA_KRISTALL.get());
                output.accept(MDMAItems.BINDEMITTEL.get());
                output.accept(MDMAItems.FARBSTOFF.get());
                output.accept(MDMAItems.ECSTASY_PILL.get());

                // ═══════════════════════════════════════════════════════════
                // MDMA BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(MDMABlocks.REAKTIONS_KESSEL.get());
                output.accept(MDMABlocks.TROCKNUNGS_OFEN.get());
                output.accept(MDMABlocks.PILLEN_PRESSE.get());

                // ═══════════════════════════════════════════════════════════
                // CANNABIS ITEMS
                // ═══════════════════════════════════════════════════════════
                output.accept(CannabisSeedItem.create(CannabisStrain.INDICA, 1));
                output.accept(CannabisSeedItem.create(CannabisStrain.SATIVA, 1));
                output.accept(CannabisSeedItem.create(CannabisStrain.HYBRID, 1));
                output.accept(CannabisSeedItem.create(CannabisStrain.AUTOFLOWER, 1));
                output.accept(CannabisItems.FRESH_BUD.get());
                output.accept(CannabisItems.DRIED_BUD.get());
                output.accept(CannabisItems.TRIMMED_BUD.get());
                output.accept(CannabisItems.CURED_BUD.get());
                output.accept(CannabisItems.TRIM.get());
                output.accept(CannabisItems.HASH.get());
                output.accept(CannabisItems.CANNABIS_OIL.get());
                output.accept(CannabisItems.POLLEN_PRESS_MOLD.get());
                output.accept(CannabisItems.EXTRACTION_SOLVENT.get());

                // ═══════════════════════════════════════════════════════════
                // CANNABIS BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(CannabisBlocks.TROCKNUNGSNETZ_ITEM.get());
                output.accept(CannabisBlocks.TRIMM_STATION_ITEM.get());
                output.accept(CannabisBlocks.CURING_GLAS_ITEM.get());
                output.accept(CannabisBlocks.HASH_PRESSE_ITEM.get());
                output.accept(CannabisBlocks.OEL_EXTRAKTOR_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // COCA ITEMS
                // ═══════════════════════════════════════════════════════════
                output.accept(CocaItems.BOLIVIANISCH_SEEDS.get());
                output.accept(CocaItems.KOLUMBIANISCH_SEEDS.get());
                output.accept(CocaItems.FRESH_BOLIVIANISCH_LEAF.get());
                output.accept(CocaItems.FRESH_KOLUMBIANISCH_LEAF.get());
                output.accept(CocaItems.COCA_PASTE.get());
                output.accept(CocaItems.COCAINE.get());
                output.accept(CocaItems.PACKAGED_COCAINE.get());
                output.accept(CocaItems.DIESEL_CANISTER.get());
                output.accept(CocaItems.CRACK_ROCK.get());
                output.accept(CocaItems.BACKPULVER.get());

                // ═══════════════════════════════════════════════════════════
                // COCA BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(CocaBlocks.SMALL_EXTRACTION_VAT.get());
                output.accept(CocaBlocks.MEDIUM_EXTRACTION_VAT.get());
                output.accept(CocaBlocks.BIG_EXTRACTION_VAT.get());
                output.accept(CocaBlocks.SMALL_REFINERY.get());
                output.accept(CocaBlocks.MEDIUM_REFINERY.get());
                output.accept(CocaBlocks.BIG_REFINERY.get());
                output.accept(CocaBlocks.CRACK_KOCHER.get());

                // ═══════════════════════════════════════════════════════════
                // POPPY ITEMS
                // ═══════════════════════════════════════════════════════════
                output.accept(PoppyItems.AFGHANISCH_SEEDS.get());
                output.accept(PoppyItems.TUERKISCH_SEEDS.get());
                output.accept(PoppyItems.INDISCH_SEEDS.get());
                output.accept(PoppyItems.POPPY_POD.get());
                output.accept(PoppyItems.RAW_OPIUM.get());
                output.accept(PoppyItems.MORPHINE.get());
                output.accept(PoppyItems.HEROIN.get());
                output.accept(PoppyItems.PACKAGED_HEROIN_50G.get());
                output.accept(PoppyItems.PACKAGED_HEROIN_100G.get());
                output.accept(PoppyItems.PACKAGED_HEROIN_250G.get());
                output.accept(PoppyItems.PACKAGED_HEROIN_500G.get());
                output.accept(PoppyItems.SCORING_KNIFE.get());

                // ═══════════════════════════════════════════════════════════
                // POPPY BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(PoppyBlocks.RITZMASCHINE.get());
                output.accept(PoppyBlocks.OPIUM_PRESSE.get());
                output.accept(PoppyBlocks.KOCHSTATION.get());
                output.accept(PoppyBlocks.HEROIN_RAFFINERIE.get());

                // ═══════════════════════════════════════════════════════════
                // MUSHROOM ITEMS
                // ═══════════════════════════════════════════════════════════
                output.accept(MushroomItems.MIST_BAG_SMALL.get());
                output.accept(MushroomItems.MIST_BAG_MEDIUM.get());
                output.accept(MushroomItems.MIST_BAG_LARGE.get());
                output.accept(MushroomItems.SPORE_SYRINGE_CUBENSIS.get());
                output.accept(MushroomItems.SPORE_SYRINGE_AZURESCENS.get());
                output.accept(MushroomItems.SPORE_SYRINGE_MEXICANA.get());
                output.accept(MushroomItems.FRESH_CUBENSIS.get());
                output.accept(MushroomItems.FRESH_AZURESCENS.get());
                output.accept(MushroomItems.FRESH_MEXICANA.get());
                output.accept(MushroomItems.DRIED_CUBENSIS.get());
                output.accept(MushroomItems.DRIED_AZURESCENS.get());
                output.accept(MushroomItems.DRIED_MEXICANA.get());
                output.accept(MushroomItems.PACKAGED_1G.get());
                output.accept(MushroomItems.PACKAGED_3_5G.get());
                output.accept(MushroomItems.PACKAGED_7G.get());
                output.accept(MushroomItems.PACKAGED_14G.get());

                // ═══════════════════════════════════════════════════════════
                // MUSHROOM BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(MushroomBlocks.KLIMALAMPE_SMALL_ITEM.get());
                output.accept(MushroomBlocks.KLIMALAMPE_MEDIUM_ITEM.get());
                output.accept(MushroomBlocks.KLIMALAMPE_LARGE_ITEM.get());
                output.accept(MushroomBlocks.WASSERTANK_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // METH ITEMS
                // ═══════════════════════════════════════════════════════════
                output.accept(MethItems.EPHEDRIN.get());
                output.accept(MethItems.PSEUDOEPHEDRIN.get());
                output.accept(MethItems.ROTER_PHOSPHOR.get());
                output.accept(MethItems.JOD.get());
                output.accept(MethItems.METH_PASTE.get());
                output.accept(MethItems.ROH_METH.get());
                output.accept(MethItems.KRISTALL_METH.get());
                output.accept(MethItems.METH.get());
                output.accept(MethItems.PACKAGED_METH_1G.get());
                output.accept(MethItems.PACKAGED_METH_3_5G.get());
                output.accept(MethItems.PACKAGED_METH_7G.get());
                output.accept(MethItems.PACKAGED_METH_14G.get());
                output.accept(MethItems.PACKAGED_METH_28G.get());

                // ═══════════════════════════════════════════════════════════
                // METH BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(MethBlocks.CHEMIE_MIXER.get());
                output.accept(MethBlocks.REDUKTIONSKESSEL.get());
                output.accept(MethBlocks.KRISTALLISATOR.get());
                output.accept(MethBlocks.VAKUUM_TROCKNER.get());
            })
            .build()
    );
}
