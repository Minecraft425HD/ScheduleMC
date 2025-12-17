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
            })
            .build()
    );
}
