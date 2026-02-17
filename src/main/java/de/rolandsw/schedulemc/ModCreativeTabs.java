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
import de.rolandsw.schedulemc.beer.items.BeerItems;
import de.rolandsw.schedulemc.beer.blocks.BeerBlocks;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
import de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.cheese.blocks.CheeseBlocks;
import de.rolandsw.schedulemc.coffee.items.CoffeeItems;
import de.rolandsw.schedulemc.coffee.blocks.CoffeeBlocks;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.honey.blocks.HoneyBlocks;
import de.rolandsw.schedulemc.wine.items.WineItems;
import de.rolandsw.schedulemc.wine.blocks.WineBlocks;
import de.rolandsw.schedulemc.vehicle.items.ItemBioDieselCanister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ScheduleMC.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PLOTMOD_TAB = CREATIVE_MODE_TABS.register("plotmod_tab",
        () -> CreativeModeTab.builder()
            .title(Component.literal("ScheduleMC"))
            .icon(() -> new ItemStack(ModItems.PLOT_SELECTION_TOOL.get()))
            .displayItems((parameters, output) -> {

                // ═══════════════════════════════════════════════════════════
                // CORE ITEMS & TOOLS
                // ═══════════════════════════════════════════════════════════
                output.accept(ModItems.PLOT_SELECTION_TOOL.get());
                output.accept(ModItems.CASH.get());
                output.accept(ModItems.PACKAGED_DRUG.get());

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
                output.accept(NPCItems.ENTITY_REMOVER.get());

                // ═══════════════════════════════════════════════════════════
                // SCHLOSS-SYSTEM (Locks, Keys, Lock Picks)
                // ═══════════════════════════════════════════════════════════
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.SIMPLE_LOCK.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.SECURITY_LOCK.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.HIGH_SECURITY_LOCK.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.COMBINATION_LOCK.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.DUAL_LOCK.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.KEY_BLANK_COPPER.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.KEY_BLANK_IRON.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.KEY_BLANK_NETHERITE.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.KEY_RING.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.LOCK_PICK.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.HACKING_CODE_CRACKER.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.HACKING_BYPASS.get());
                output.accept(de.rolandsw.schedulemc.lock.items.LockItems.HACKING_OMNI.get());

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
                // FAHRZEUGE & WERKSTATT
                // ═══════════════════════════════════════════════════════════
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.blocks.ModBlocks.FUEL_STATION.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.blocks.ModBlocks.WERKSTATT.get()));

                // Fahrzeuge
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.SPAWN_VEHICLE_OAK.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.SPAWN_VEHICLE_BIG_OAK.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.SPAWN_VEHICLE_WHITE_TRANSPORTER.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.SPAWN_VEHICLE_WHITE_SUV.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.SPAWN_VEHICLE_WHITE_SPORT.get()));

                // Fahrzeug-Items
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.CANISTER.get()));
                output.accept(ItemBioDieselCanister.createPreFilledStack());
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.REPAIR_KIT.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.KEY.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.BATTERY.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.LICENSE_PLATE.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.LICENSE_PLATE_HOLDER.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.VEHICLE_SPAWN_TOOL.get()));

                // Motoren
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.NORMAL_MOTOR.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.PERFORMANCE_MOTOR.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.PERFORMANCE_2_MOTOR.get()));

                // Reifen
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.STANDARD_TIRE.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.SPORT_TIRE.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.PREMIUM_TIRE.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.OFFROAD_TIRE.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.ALLTERRAIN_TIRE.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.HEAVY_DUTY_TIRE.get()));

                // Karosserien
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.LIMOUSINE_CHASSIS.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.VAN_CHASSIS.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.TRUCK_CHASSIS.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.OFFROAD_CHASSIS.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.LUXUS_CHASSIS.get()));

                // Tanks
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.TANK_15L.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.TANK_30L.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.TANK_50L.get()));

                // Fender
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.FENDER_BASIC.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.FENDER_CHROME.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.FENDER_SPORT.get()));

                // Container-Module
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.CARGO_MODULE.get()));
                output.accept(new ItemStack(de.rolandsw.schedulemc.vehicle.items.ModItems.FLUID_MODULE.get()));

                // ═══════════════════════════════════════════════════════════
                // TABAK - SAMEN
                // ═══════════════════════════════════════════════════════════
                output.accept(TobaccoItems.VIRGINIA_SEEDS.get());
                output.accept(TobaccoItems.BURLEY_SEEDS.get());
                output.accept(TobaccoItems.ORIENTAL_SEEDS.get());
                output.accept(TobaccoItems.HAVANA_SEEDS.get());

                // TABAK - BLÄTTER (Frisch) - mit Quality NBT
                output.accept(de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.VIRGINIA,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.BURLEY,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.ORIENTAL,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.HAVANA,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));

                // TABAK - BLÄTTER (Getrocknet) - mit Quality NBT
                output.accept(de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.VIRGINIA,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.BURLEY,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.ORIENTAL,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.HAVANA,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));

                // TABAK - BLÄTTER (Fermentiert) - mit Quality NBT
                output.accept(de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.VIRGINIA,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.BURLEY,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.ORIENTAL,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));
                output.accept(de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem.create(
                    de.rolandsw.schedulemc.tobacco.TobaccoType.HAVANA,
                    de.rolandsw.schedulemc.tobacco.TobaccoQuality.GUT, 1));

                // TABAK - FLASCHEN & WERKZEUGE
                output.accept(TobaccoItems.FERTILIZER_BOTTLE.get());
                output.accept(TobaccoItems.GROWTH_BOOSTER_BOTTLE.get());
                output.accept(TobaccoItems.QUALITY_BOOSTER_BOTTLE.get());
                output.accept(TobaccoItems.WATERING_CAN.get());

                // TABAK - ERDSÄCKE
                output.accept(TobaccoItems.SOIL_BAG_SMALL.get());
                output.accept(TobaccoItems.SOIL_BAG_MEDIUM.get());
                output.accept(TobaccoItems.SOIL_BAG_LARGE.get());

                // TABAK - VERPACKUNG
                output.accept(TobaccoItems.PACKAGING_BAG.get());
                output.accept(TobaccoItems.PACKAGING_JAR.get());
                output.accept(TobaccoItems.PACKAGING_BOX.get());

                // TABAK - BLÖCKE
                output.accept(TobaccoBlocks.TERRACOTTA_POT.get());
                output.accept(TobaccoBlocks.CERAMIC_POT.get());
                output.accept(TobaccoBlocks.IRON_POT.get());
                output.accept(TobaccoBlocks.GOLDEN_POT.get());
                output.accept(TobaccoBlocks.SMALL_DRYING_RACK.get());
                output.accept(TobaccoBlocks.MEDIUM_DRYING_RACK.get());
                output.accept(TobaccoBlocks.BIG_DRYING_RACK.get());
                output.accept(TobaccoBlocks.SMALL_FERMENTATION_BARREL.get());
                output.accept(TobaccoBlocks.MEDIUM_FERMENTATION_BARREL.get());
                output.accept(TobaccoBlocks.BIG_FERMENTATION_BARREL.get());
                output.accept(TobaccoBlocks.SINK.get());
                output.accept(TobaccoBlocks.SMALL_PACKAGING_TABLE.get());
                output.accept(TobaccoBlocks.MEDIUM_PACKAGING_TABLE.get());
                output.accept(TobaccoBlocks.LARGE_PACKAGING_TABLE.get());
                output.accept(TobaccoBlocks.BASIC_GROW_LIGHT_SLAB.get());
                output.accept(TobaccoBlocks.ADVANCED_GROW_LIGHT_SLAB.get());
                output.accept(TobaccoBlocks.PREMIUM_GROW_LIGHT_SLAB.get());

                // ═══════════════════════════════════════════════════════════
                // CANNABIS
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

                // CANNABIS - BLÖCKE
                output.accept(CannabisBlocks.TRIMM_STATION_ITEM.get());
                output.accept(CannabisBlocks.CURING_GLAS_ITEM.get());
                output.accept(CannabisBlocks.HASH_PRESSE_ITEM.get());
                output.accept(CannabisBlocks.OEL_EXTRAKTOR_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // COCA / KOKAIN
                // ═══════════════════════════════════════════════════════════
                output.accept(CocaItems.BOLIVIANISCH_SEEDS.get());
                output.accept(CocaItems.KOLUMBIANISCH_SEEDS.get());
                output.accept(CocaItems.PERUANISCH_SEEDS.get());
                output.accept(CocaItems.FRESH_BOLIVIANISCH_LEAF.get());
                output.accept(CocaItems.FRESH_KOLUMBIANISCH_LEAF.get());
                output.accept(CocaItems.FRESH_PERUANISCH_LEAF.get());
                output.accept(CocaItems.COCA_PASTE_BOLIVIANISCH.get());
                output.accept(CocaItems.COCA_PASTE_KOLUMBIANISCH.get());
                output.accept(CocaItems.COCA_PASTE_PERUANISCH.get());
                output.accept(CocaItems.COCAINE.get());
                output.accept(CocaItems.CRACK_ROCK.get());
                output.accept(CocaItems.BACKPULVER.get());

                // COCA - BLÖCKE
                output.accept(CocaBlocks.SMALL_EXTRACTION_VAT.get());
                output.accept(CocaBlocks.MEDIUM_EXTRACTION_VAT.get());
                output.accept(CocaBlocks.BIG_EXTRACTION_VAT.get());
                output.accept(CocaBlocks.SMALL_REFINERY.get());
                output.accept(CocaBlocks.MEDIUM_REFINERY.get());
                output.accept(CocaBlocks.BIG_REFINERY.get());
                output.accept(CocaBlocks.CRACK_KOCHER.get());

                // ═══════════════════════════════════════════════════════════
                // MOHN / HEROIN
                // ═══════════════════════════════════════════════════════════
                output.accept(PoppyItems.AFGHANISCH_SEEDS.get());
                output.accept(PoppyItems.TUERKISCH_SEEDS.get());
                output.accept(PoppyItems.INDISCH_SEEDS.get());
                output.accept(PoppyItems.POPPY_POD.get());
                output.accept(PoppyItems.RAW_OPIUM.get());
                output.accept(PoppyItems.MORPHINE.get());
                output.accept(PoppyItems.HEROIN.get());
                output.accept(PoppyItems.SCORING_KNIFE.get());

                // MOHN - BLÖCKE
                output.accept(PoppyBlocks.RITZMASCHINE.get());
                output.accept(PoppyBlocks.OPIUM_PRESSE.get());
                output.accept(PoppyBlocks.KOCHSTATION.get());
                output.accept(PoppyBlocks.HEROIN_RAFFINERIE.get());

                // ═══════════════════════════════════════════════════════════
                // PILZE
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

                // PILZE - BLÖCKE
                output.accept(MushroomBlocks.KLIMALAMPE_SMALL_ITEM.get());
                output.accept(MushroomBlocks.KLIMALAMPE_MEDIUM_ITEM.get());
                output.accept(MushroomBlocks.KLIMALAMPE_LARGE_ITEM.get());
                output.accept(MushroomBlocks.WASSERTANK_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // LSD
                // ═══════════════════════════════════════════════════════════
                output.accept(LSDItems.MUTTERKORN.get());
                output.accept(LSDItems.ERGOT_KULTUR.get());
                output.accept(LSDItems.LYSERGSAEURE.get());
                output.accept(LSDItems.LSD_LOESUNG.get());
                output.accept(LSDItems.BLOTTER_PAPIER.get());
                output.accept(LSDItems.BLOTTER.get());

                // LSD - BLÖCKE
                output.accept(LSDBlocks.FERMENTATIONS_TANK.get());
                output.accept(LSDBlocks.DESTILLATIONS_APPARAT.get());
                output.accept(LSDBlocks.MIKRO_DOSIERER.get());
                output.accept(LSDBlocks.PERFORATIONS_PRESSE.get());

                // ═══════════════════════════════════════════════════════════
                // MDMA / ECSTASY
                // ═══════════════════════════════════════════════════════════
                output.accept(MDMAItems.SAFROL.get());
                output.accept(MDMAItems.MDMA_BASE.get());
                output.accept(MDMAItems.MDMA_KRISTALL.get());
                output.accept(MDMAItems.BINDEMITTEL.get());
                output.accept(MDMAItems.FARBSTOFF.get());
                output.accept(MDMAItems.ECSTASY_PILL.get());

                // MDMA - BLÖCKE
                output.accept(MDMABlocks.REAKTIONS_KESSEL.get());
                output.accept(MDMABlocks.TROCKNUNGS_OFEN.get());
                output.accept(MDMABlocks.PILLEN_PRESSE.get());

                // ═══════════════════════════════════════════════════════════
                // METH
                // ═══════════════════════════════════════════════════════════
                output.accept(MethItems.EPHEDRIN.get());
                output.accept(MethItems.PSEUDOEPHEDRIN.get());
                output.accept(MethItems.ROTER_PHOSPHOR.get());
                output.accept(MethItems.JOD.get());
                output.accept(MethItems.METH_PASTE.get());
                output.accept(MethItems.ROH_METH.get());
                output.accept(MethItems.KRISTALL_METH.get());
                output.accept(MethItems.METH.get());

                // METH - BLÖCKE
                output.accept(MethBlocks.CHEMIE_MIXER.get());
                output.accept(MethBlocks.REDUKTIONSKESSEL.get());
                output.accept(MethBlocks.KRISTALLISATOR.get());
                output.accept(MethBlocks.VAKUUM_TROCKNER.get());

                // ═══════════════════════════════════════════════════════════
                // BIER
                // ═══════════════════════════════════════════════════════════
                output.accept(BeerItems.BARLEY.get());
                output.accept(Items.WHEAT); // Minecraft vanilla wheat
                output.accept(BeerItems.RYE.get());
                output.accept(BeerItems.MALTED_BARLEY.get());
                output.accept(BeerItems.MALTED_WHEAT.get());
                output.accept(BeerItems.MALTED_RYE.get());
                output.accept(BeerItems.ROASTED_BARLEY.get());
                output.accept(BeerItems.CHOCOLATE_MALT.get());
                output.accept(BeerItems.CARAMEL_MALT.get());
                output.accept(BeerItems.MALT_EXTRACT.get());
                output.accept(BeerItems.HOPS_CONE.get());
                output.accept(BeerItems.DRIED_HOPS.get());
                output.accept(BeerItems.HOP_EXTRACT.get());
                output.accept(BeerItems.HOP_PELLETS.get());
                output.accept(BeerItems.YEAST.get());
                output.accept(BeerItems.BREWING_YEAST.get());
                output.accept(BeerItems.LAGER_YEAST.get());
                output.accept(BeerItems.ALE_YEAST.get());
                output.accept(BeerItems.BREWING_SUGAR.get());
                output.accept(BeerItems.IRISH_MOSS.get());
                output.accept(BeerItems.BREWING_SALT.get());
                output.accept(BeerItems.WORT_BUCKET.get());
                output.accept(BeerItems.FERMENTING_BEER.get());
                output.accept(BeerItems.GREEN_BEER.get());
                output.accept(BeerItems.CONDITIONED_BEER.get());
                output.accept(BeerItems.BEER_BOTTLE.get());
                output.accept(BeerItems.BEER_BOTTLE_EMPTY.get());
                output.accept(BeerItems.BEER_CAN_EMPTY.get());
                output.accept(BeerItems.BEER_KEG.get());
                output.accept(BeerItems.BOTTLE_CAP.get());
                output.accept(BeerItems.CROWN_CAP.get());

                // BIER - BLÖCKE
                output.accept(BeerBlocks.MALTING_STATION_ITEM.get());
                output.accept(BeerBlocks.MASH_TUN_ITEM.get());
                output.accept(BeerBlocks.SMALL_BREW_KETTLE_ITEM.get());
                output.accept(BeerBlocks.MEDIUM_BREW_KETTLE_ITEM.get());
                output.accept(BeerBlocks.LARGE_BREW_KETTLE_ITEM.get());
                output.accept(BeerBlocks.SMALL_FERMENTATION_TANK_ITEM.get());
                output.accept(BeerBlocks.MEDIUM_FERMENTATION_TANK_ITEM.get());
                output.accept(BeerBlocks.LARGE_FERMENTATION_TANK_ITEM.get());
                output.accept(BeerBlocks.SMALL_CONDITIONING_TANK_ITEM.get());
                output.accept(BeerBlocks.MEDIUM_CONDITIONING_TANK_ITEM.get());
                output.accept(BeerBlocks.LARGE_CONDITIONING_TANK_ITEM.get());
                output.accept(BeerBlocks.BOTTLING_STATION_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // WEIN
                // ═══════════════════════════════════════════════════════════
                output.accept(WineItems.RIESLING_GRAPE_SEEDLING.get());
                output.accept(WineItems.SPAETBURGUNDER_GRAPE_SEEDLING.get());
                output.accept(WineItems.CHARDONNAY_GRAPE_SEEDLING.get());
                output.accept(WineItems.MERLOT_GRAPE_SEEDLING.get());
                output.accept(WineItems.RIESLING_GRAPES.get());
                output.accept(WineItems.SPAETBURGUNDER_GRAPES.get());
                output.accept(WineItems.CHARDONNAY_GRAPES.get());
                output.accept(WineItems.MERLOT_GRAPES.get());
                output.accept(WineItems.RIESLING_MASH.get());
                output.accept(WineItems.SPAETBURGUNDER_MASH.get());
                output.accept(WineItems.CHARDONNAY_MASH.get());
                output.accept(WineItems.MERLOT_MASH.get());
                output.accept(WineItems.RIESLING_JUICE.get());
                output.accept(WineItems.SPAETBURGUNDER_JUICE.get());
                output.accept(WineItems.CHARDONNAY_JUICE.get());
                output.accept(WineItems.MERLOT_JUICE.get());
                output.accept(WineItems.FERMENTING_WINE.get());
                output.accept(WineItems.YOUNG_WINE.get());
                output.accept(WineItems.WINE_BOTTLE_375ML.get());
                output.accept(WineItems.WINE_BOTTLE_750ML.get());
                output.accept(WineItems.WINE_BOTTLE_1500ML.get());
                output.accept(WineItems.EMPTY_WINE_BOTTLE_375ML.get());
                output.accept(WineItems.EMPTY_WINE_BOTTLE_750ML.get());
                output.accept(WineItems.EMPTY_WINE_BOTTLE_1500ML.get());
                output.accept(WineItems.GLASS_OF_WINE.get());

                // WEIN - BLÖCKE
                output.accept(WineBlocks.RIESLING_GRAPEVINE_POT_ITEM.get());
                output.accept(WineBlocks.SPAETBURGUNDER_GRAPEVINE_POT_ITEM.get());
                output.accept(WineBlocks.CHARDONNAY_GRAPEVINE_POT_ITEM.get());
                output.accept(WineBlocks.MERLOT_GRAPEVINE_POT_ITEM.get());
                output.accept(WineBlocks.CRUSHING_STATION_ITEM.get());
                output.accept(WineBlocks.SMALL_WINE_PRESS_ITEM.get());
                output.accept(WineBlocks.MEDIUM_WINE_PRESS_ITEM.get());
                output.accept(WineBlocks.LARGE_WINE_PRESS_ITEM.get());
                output.accept(WineBlocks.SMALL_FERMENTATION_TANK_ITEM.get());
                output.accept(WineBlocks.MEDIUM_FERMENTATION_TANK_ITEM.get());
                output.accept(WineBlocks.LARGE_FERMENTATION_TANK_ITEM.get());
                output.accept(WineBlocks.SMALL_AGING_BARREL_ITEM.get());
                output.accept(WineBlocks.MEDIUM_AGING_BARREL_ITEM.get());
                output.accept(WineBlocks.LARGE_AGING_BARREL_ITEM.get());
                output.accept(WineBlocks.WINE_BOTTLING_STATION_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // SCHOKOLADE
                // ═══════════════════════════════════════════════════════════
                output.accept(ChocolateItems.COCOA_BEANS.get());
                output.accept(ChocolateItems.ROASTED_COCOA_BEANS.get());
                output.accept(ChocolateItems.COCOA_NIBS.get());
                output.accept(ChocolateItems.COCOA_MASS.get());
                output.accept(ChocolateItems.COCOA_LIQUOR.get());
                output.accept(ChocolateItems.COCOA_BUTTER.get());
                output.accept(ChocolateItems.COCOA_POWDER.get());
                output.accept(ChocolateItems.COCOA_CAKE.get());
                output.accept(ChocolateItems.MILK_POWDER.get());
                output.accept(ChocolateItems.SUGAR.get());
                output.accept(ChocolateItems.VANILLA_EXTRACT.get());
                output.accept(ChocolateItems.LECITHIN.get());
                output.accept(ChocolateItems.CARAMEL.get());
                output.accept(ChocolateItems.NOUGAT.get());
                output.accept(ChocolateItems.HAZELNUTS.get());
                output.accept(ChocolateItems.ALMONDS.get());
                output.accept(ChocolateItems.ROASTED_HAZELNUTS.get());
                output.accept(ChocolateItems.ROASTED_ALMONDS.get());
                output.accept(ChocolateItems.DRIED_FRUITS.get());
                output.accept(ChocolateItems.RAISINS.get());
                output.accept(ChocolateItems.CONCHED_CHOCOLATE.get());
                output.accept(ChocolateItems.TEMPERED_CHOCOLATE.get());
                output.accept(ChocolateItems.CHOCOLATE_MIXTURE.get());
                output.accept(ChocolateItems.CHOCOLATE_MOLD.get());
                output.accept(ChocolateItems.CHOCOLATE_MOLD_BAR.get());
                output.accept(ChocolateItems.CHOCOLATE_MOLD_PRALINE.get());
                output.accept(ChocolateItems.WRAPPER.get());
                output.accept(ChocolateItems.WRAPPER_GOLD.get());
                output.accept(ChocolateItems.BOX.get());
                output.accept(ChocolateItems.BOX_PREMIUM.get());
                output.accept(ChocolateItems.CHOCOLATE_BAR_100G.get());
                output.accept(ChocolateItems.CHOCOLATE_BAR_200G.get());
                output.accept(ChocolateItems.CHOCOLATE_BAR_500G.get());
                output.accept(ChocolateItems.CHOCOLATE_TRUFFLE.get());
                output.accept(ChocolateItems.CHOCOLATE_PRALINE.get());
                output.accept(ChocolateItems.HOT_CHOCOLATE_MIX.get());

                // SCHOKOLADE - BLÖCKE
                output.accept(ChocolateBlocks.ROASTING_STATION_ITEM.get());
                output.accept(ChocolateBlocks.WINNOWING_MACHINE_ITEM.get());
                output.accept(ChocolateBlocks.GRINDING_MILL_ITEM.get());
                output.accept(ChocolateBlocks.PRESSING_STATION_ITEM.get());
                output.accept(ChocolateBlocks.SMALL_CONCHING_MACHINE_ITEM.get());
                output.accept(ChocolateBlocks.MEDIUM_CONCHING_MACHINE_ITEM.get());
                output.accept(ChocolateBlocks.LARGE_CONCHING_MACHINE_ITEM.get());
                output.accept(ChocolateBlocks.TEMPERING_STATION_ITEM.get());
                output.accept(ChocolateBlocks.SMALL_MOLDING_STATION_ITEM.get());
                output.accept(ChocolateBlocks.MEDIUM_MOLDING_STATION_ITEM.get());
                output.accept(ChocolateBlocks.LARGE_MOLDING_STATION_ITEM.get());
                output.accept(ChocolateBlocks.ENROBING_MACHINE_ITEM.get());
                output.accept(ChocolateBlocks.COOLING_TUNNEL_ITEM.get());
                output.accept(ChocolateBlocks.WRAPPING_STATION_ITEM.get());
                output.accept(ChocolateBlocks.CHOCOLATE_STORAGE_CABINET_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // KÄSE
                // ═══════════════════════════════════════════════════════════
                output.accept(Items.MILK_BUCKET); // Minecraft vanilla milk bucket
                output.accept(CheeseItems.RENNET.get());
                output.accept(CheeseItems.CHEESE_CURD.get());
                output.accept(CheeseItems.WHEY.get());
                output.accept(CheeseItems.CHEESE_WHEEL.get());
                output.accept(CheeseItems.GOUDA_WHEEL.get());
                output.accept(CheeseItems.EMMENTAL_WHEEL.get());
                output.accept(CheeseItems.CAMEMBERT_WHEEL.get());
                output.accept(CheeseItems.PARMESAN_WHEEL.get());
                output.accept(CheeseItems.CHEESE_WEDGE.get());
                output.accept(CheeseItems.GOUDA_WEDGE.get());
                output.accept(CheeseItems.EMMENTAL_WEDGE.get());
                output.accept(CheeseItems.CAMEMBERT_WEDGE.get());
                output.accept(CheeseItems.PARMESAN_WEDGE.get());
                output.accept(CheeseItems.SMOKED_CHEESE.get());
                output.accept(CheeseItems.HERB_CHEESE.get());
                output.accept(CheeseItems.CHEESE_CLOTH.get());
                output.accept(CheeseItems.WAX_COATING.get());
                output.accept(CheeseItems.CHEESE_PAPER.get());

                // KÄSE - BLÖCKE
                output.accept(CheeseBlocks.PASTEURIZATION_STATION_ITEM.get());
                output.accept(CheeseBlocks.CURDLING_VAT_ITEM.get());
                output.accept(CheeseBlocks.SMALL_CHEESE_PRESS_ITEM.get());
                output.accept(CheeseBlocks.MEDIUM_CHEESE_PRESS_ITEM.get());
                output.accept(CheeseBlocks.LARGE_CHEESE_PRESS_ITEM.get());
                output.accept(CheeseBlocks.SMALL_AGING_CAVE_ITEM.get());
                output.accept(CheeseBlocks.MEDIUM_AGING_CAVE_ITEM.get());
                output.accept(CheeseBlocks.LARGE_AGING_CAVE_ITEM.get());
                output.accept(CheeseBlocks.PACKAGING_STATION_ITEM.get());

                // ═══════════════════════════════════════════════════════════
                // KAFFEE
                // ═══════════════════════════════════════════════════════════
                output.accept(CoffeeItems.ARABICA_SEEDLING.get());
                output.accept(CoffeeItems.ROBUSTA_SEEDLING.get());
                output.accept(CoffeeItems.LIBERICA_SEEDLING.get());
                output.accept(CoffeeItems.EXCELSA_SEEDLING.get());
                output.accept(CoffeeItems.ARABICA_CHERRY.get());
                output.accept(CoffeeItems.ROBUSTA_CHERRY.get());
                output.accept(CoffeeItems.LIBERICA_CHERRY.get());
                output.accept(CoffeeItems.EXCELSA_CHERRY.get());
                output.accept(CoffeeItems.GREEN_ARABICA_BEANS.get());
                output.accept(CoffeeItems.GREEN_ROBUSTA_BEANS.get());
                output.accept(CoffeeItems.GREEN_LIBERICA_BEANS.get());
                output.accept(CoffeeItems.GREEN_EXCELSA_BEANS.get());
                output.accept(CoffeeItems.ROASTED_COFFEE_BEANS.get());
                output.accept(CoffeeItems.GROUND_COFFEE.get());
                output.accept(CoffeeItems.COFFEE_PACKAGE_250G.get());
                output.accept(CoffeeItems.COFFEE_PACKAGE_500G.get());
                output.accept(CoffeeItems.COFFEE_PACKAGE_1KG.get());
                output.accept(TobaccoItems.WATERING_CAN.get()); // Verwende Tobacco's Watering Can
                output.accept(CoffeeItems.PULPING_TOOL.get());
                output.accept(CoffeeItems.ROASTING_TRAY.get());
                // Verwende Tobacco's Boosters (wiederverwendbar für alle Produktionen)
                output.accept(TobaccoItems.FERTILIZER_BOTTLE.get()); // Yield Booster
                output.accept(TobaccoItems.GROWTH_BOOSTER_BOTTLE.get()); // Growth Booster
                output.accept(TobaccoItems.QUALITY_BOOSTER_BOTTLE.get()); // Quality Booster
                output.accept(CoffeeItems.COFFEE_BAG_SMALL.get());
                output.accept(CoffeeItems.COFFEE_BAG_MEDIUM.get());
                output.accept(CoffeeItems.COFFEE_BAG_LARGE.get());
                output.accept(CoffeeItems.VACUUM_SEAL.get());
                output.accept(CoffeeItems.BREWED_COFFEE.get());
                output.accept(CoffeeItems.ESPRESSO.get());

                // KAFFEE - BLÖCKE
                // Verwende Tobacco Pots (wiederverwendbar für alle Pflanzen)
                output.accept(TobaccoBlocks.TERRACOTTA_POT.get());
                output.accept(TobaccoBlocks.CERAMIC_POT.get());
                output.accept(TobaccoBlocks.IRON_POT.get());
                output.accept(TobaccoBlocks.GOLDEN_POT.get());
                // Verwende Tobacco Drying Racks (wiederverwendbar für Coffee & Tobacco)
                output.accept(TobaccoBlocks.SMALL_DRYING_RACK.get());
                output.accept(TobaccoBlocks.MEDIUM_DRYING_RACK.get());
                output.accept(TobaccoBlocks.BIG_DRYING_RACK.get());
                output.accept(CoffeeBlocks.WET_PROCESSING_STATION.get());
                output.accept(CoffeeBlocks.SMALL_COFFEE_ROASTER.get());
                output.accept(CoffeeBlocks.MEDIUM_COFFEE_ROASTER.get());
                output.accept(CoffeeBlocks.LARGE_COFFEE_ROASTER.get());
                output.accept(CoffeeBlocks.COFFEE_GRINDER.get());
                output.accept(CoffeeBlocks.COFFEE_PACKAGING_TABLE.get());

                // ═══════════════════════════════════════════════════════════
                // HONIG
                // ═══════════════════════════════════════════════════════════
                output.accept(HoneyItems.RAW_HONEYCOMB.get());
                output.accept(HoneyItems.FILTERED_HONEYCOMB.get());
                output.accept(HoneyItems.HONEYCOMB_CHUNK.get());
                output.accept(HoneyItems.RAW_HONEY_BUCKET.get());
                output.accept(HoneyItems.FILTERED_HONEY_BUCKET.get());
                output.accept(HoneyItems.LIQUID_HONEY_BOTTLE.get());
                output.accept(HoneyItems.HONEY_JAR_250G.get());
                output.accept(HoneyItems.HONEY_JAR_500G.get());
                output.accept(HoneyItems.HONEY_JAR_1KG.get());
                output.accept(HoneyItems.BEESWAX.get());
                output.accept(HoneyItems.BEESWAX_BLOCK.get());
                output.accept(HoneyItems.PROPOLIS.get());
                output.accept(HoneyItems.POLLEN.get());
                output.accept(HoneyItems.ROYAL_JELLY.get());
                output.accept(HoneyItems.GLASS_JAR.get());
                output.accept(HoneyItems.GLASS_JAR_SMALL.get());
                output.accept(HoneyItems.GLASS_JAR_LARGE.get());
                output.accept(HoneyItems.JAR_LID.get());
                output.accept(HoneyItems.JAR_LID_GOLD.get());
                output.accept(HoneyItems.CREAMED_HONEY.get());
                output.accept(HoneyItems.CRYSTALLIZED_HONEY.get());
                output.accept(HoneyItems.HONEY_CRYSTALS.get());
                output.accept(HoneyItems.BEEKEEPER_SUIT.get());
                output.accept(HoneyItems.SMOKER.get());
                output.accept(HoneyItems.HIVE_TOOL.get());
                output.accept(HoneyItems.HONEY_CANDY.get());
                output.accept(HoneyItems.HONEYCOMB_TREAT.get());

                // HONIG - BLÖCKE
                output.accept(HoneyBlocks.BEEHIVE_ITEM.get());
                output.accept(HoneyBlocks.ADVANCED_BEEHIVE_ITEM.get());
                output.accept(HoneyBlocks.APIARY_ITEM.get());
                output.accept(HoneyBlocks.HONEY_EXTRACTOR_ITEM.get());
                output.accept(HoneyBlocks.CENTRIFUGAL_EXTRACTOR_ITEM.get());
                output.accept(HoneyBlocks.FILTERING_STATION_ITEM.get());
                output.accept(HoneyBlocks.SMALL_AGING_CHAMBER_ITEM.get());
                output.accept(HoneyBlocks.MEDIUM_AGING_CHAMBER_ITEM.get());
                output.accept(HoneyBlocks.LARGE_AGING_CHAMBER_ITEM.get());
                output.accept(HoneyBlocks.PROCESSING_STATION_ITEM.get());
                output.accept(HoneyBlocks.CREAMING_STATION_ITEM.get());
                output.accept(HoneyBlocks.BOTTLING_STATION_ITEM.get());
                output.accept(HoneyBlocks.HONEY_STORAGE_BARREL_ITEM.get());
                output.accept(HoneyBlocks.HONEY_DISPLAY_CASE_ITEM.get());
            })
            .build()
    );
}
