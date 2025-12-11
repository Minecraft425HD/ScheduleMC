package de.rolandsw.schedulemc;

import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoBlocks;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import de.rolandsw.schedulemc.economy.blocks.EconomyBlocks;
import de.rolandsw.schedulemc.npc.items.NPCItems;
import de.rolandsw.schedulemc.region.blocks.PlotBlocks;
import de.rolandsw.schedulemc.warehouse.WarehouseBlocks;
import de.rolandsw.schedulemc.vehicle.items.VehicleItems;
import de.rolandsw.schedulemc.vehicle.blocks.VehicleBlocks;
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
                // VEHICLE SYSTEM
                // ═══════════════════════════════════════════════════════════
                output.accept(VehicleItems.VEHICLE_SPAWN_MARKER.get());

                // ═══════════════════════════════════════════════════════════
                // VEHICLE COMPONENTS - ENGINES
                // ═══════════════════════════════════════════════════════════
                output.accept(VehicleItems.ENGINE_INLINE_THREE.get());
                output.accept(VehicleItems.ENGINE_INLINE_SIX.get());
                output.accept(VehicleItems.ENGINE_TRUCK_V8.get());

                // ═══════════════════════════════════════════════════════════
                // VEHICLE COMPONENTS - WHEELS
                // ═══════════════════════════════════════════════════════════
                output.accept(VehicleItems.WHEELS_STANDARD.get());
                output.accept(VehicleItems.WHEELS_SPORT.get());
                output.accept(VehicleItems.WHEELS_OFFROAD.get());
                output.accept(VehicleItems.WHEELS_TRUCK.get());

                // ═══════════════════════════════════════════════════════════
                // VEHICLE COMPONENTS - BODIES
                // ═══════════════════════════════════════════════════════════
                output.accept(VehicleItems.BODY_SEDAN.get());
                output.accept(VehicleItems.BODY_SPORT.get());
                output.accept(VehicleItems.BODY_SUV.get());
                output.accept(VehicleItems.BODY_TRUCK.get());
                output.accept(VehicleItems.BODY_TRANSPORTER.get());

                // ═══════════════════════════════════════════════════════════
                // VEHICLE COMPONENTS - FUEL TANKS
                // ═══════════════════════════════════════════════════════════
                output.accept(VehicleItems.TANK_SMALL.get());
                output.accept(VehicleItems.TANK_MEDIUM.get());
                output.accept(VehicleItems.TANK_LARGE.get());
                output.accept(VehicleItems.TANK_TRUCK.get());

                // ═══════════════════════════════════════════════════════════
                // VEHICLE UTILITY ITEMS
                // ═══════════════════════════════════════════════════════════
                output.accept(VehicleItems.VEHICLE_KEY.get());
                output.accept(VehicleItems.LICENSE_PLATE.get());
                output.accept(VehicleItems.REPAIR_KIT.get());
                output.accept(VehicleItems.BATTERY.get());
                output.accept(VehicleItems.WRENCH.get());

                // ═══════════════════════════════════════════════════════════
                // VEHICLE BLOCKS
                // ═══════════════════════════════════════════════════════════
                output.accept(VehicleBlocks.GAS_STATION.get());
            })
            .build()
    );
}
