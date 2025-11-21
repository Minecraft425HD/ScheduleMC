package de.rolandsw.schedulemc;

import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import de.rolandsw.schedulemc.economy.blocks.EconomyBlocks;
import de.rolandsw.schedulemc.npc.items.NPCItems;
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
                // NPC TOOLS
                // ═══════════════════════════════════════════════════════════
                output.accept(NPCItems.NPC_SPAWNER_TOOL.get());
                
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
                // TÖPFE
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoBlocks.TERRACOTTA_POT.get());
                output.accept(TobaccoBlocks.CERAMIC_POT.get());
                output.accept(TobaccoBlocks.IRON_POT.get());
                output.accept(TobaccoBlocks.GOLDEN_POT.get());
                
                // ═══════════════════════════════════════════════════════════
                // VERARBEITUNGS-BLÖCKE
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoBlocks.DRYING_RACK.get());
                output.accept(TobaccoBlocks.FERMENTATION_BARREL.get());
                output.accept(TobaccoBlocks.SINK.get());
                
                // ═══════════════════════════════════════════════════════════
                // ECONOMY BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(EconomyBlocks.CASH_BLOCK.get());
                output.accept(EconomyBlocks.ATM_BLOCK.get());
            })
            .build()
    );
    
    // ═══════════════════════════════════════════════════════════
    // OPTIONAL: Separate Tabs für Organisation
    // ═══════════════════════════════════════════════════════════
    
    /**
     * OPTIONAL: Separater Tab nur für Tabak-Items
     */
    public static final RegistryObject<CreativeModeTab> TOBACCO_TAB = CREATIVE_MODE_TABS.register("tobacco_tab",
        () -> CreativeModeTab.builder()
            .title(Component.literal("Tobacco Farming"))
            .icon(() -> new ItemStack(TobaccoItems.VIRGINIA_SEEDS.get()))
            .withTabsBefore(PLOTMOD_TAB.getId())
            .displayItems((parameters, output) -> {
                
                // Samen
                output.accept(TobaccoItems.VIRGINIA_SEEDS.get());
                output.accept(TobaccoItems.BURLEY_SEEDS.get());
                output.accept(TobaccoItems.ORIENTAL_SEEDS.get());
                output.accept(TobaccoItems.HAVANA_SEEDS.get());
                
                // Frische Blätter
                output.accept(TobaccoItems.FRESH_VIRGINIA_LEAF.get());
                output.accept(TobaccoItems.FRESH_BURLEY_LEAF.get());
                output.accept(TobaccoItems.FRESH_ORIENTAL_LEAF.get());
                output.accept(TobaccoItems.FRESH_HAVANA_LEAF.get());
                
                // Getrocknete Blätter
                output.accept(TobaccoItems.DRIED_VIRGINIA_LEAF.get());
                output.accept(TobaccoItems.DRIED_BURLEY_LEAF.get());
                output.accept(TobaccoItems.DRIED_ORIENTAL_LEAF.get());
                output.accept(TobaccoItems.DRIED_HAVANA_LEAF.get());
                
                // Fermentierte Blätter
                output.accept(TobaccoItems.FERMENTED_VIRGINIA_LEAF.get());
                output.accept(TobaccoItems.FERMENTED_BURLEY_LEAF.get());
                output.accept(TobaccoItems.FERMENTED_ORIENTAL_LEAF.get());
                output.accept(TobaccoItems.FERMENTED_HAVANA_LEAF.get());
                
                // Flaschen
                output.accept(TobaccoItems.FERTILIZER_BOTTLE.get());
                output.accept(TobaccoItems.GROWTH_BOOSTER_BOTTLE.get());
                output.accept(TobaccoItems.QUALITY_BOOSTER_BOTTLE.get());
                
                // Werkzeuge
                output.accept(TobaccoItems.WATERING_CAN.get());
                
                // Erdsäcke
                output.accept(TobaccoItems.SOIL_BAG_SMALL.get());
                output.accept(TobaccoItems.SOIL_BAG_MEDIUM.get());
                output.accept(TobaccoItems.SOIL_BAG_LARGE.get());
                
                // Töpfe
                output.accept(TobaccoBlocks.TERRACOTTA_POT.get());
                output.accept(TobaccoBlocks.CERAMIC_POT.get());
                output.accept(TobaccoBlocks.IRON_POT.get());
                output.accept(TobaccoBlocks.GOLDEN_POT.get());
                
                // Verarbeitung
                output.accept(TobaccoBlocks.DRYING_RACK.get());
                output.accept(TobaccoBlocks.FERMENTATION_BARREL.get());
                output.accept(TobaccoBlocks.SINK.get());

            })
            .build()
    );
}
