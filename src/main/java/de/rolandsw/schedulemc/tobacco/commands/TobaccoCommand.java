package de.rolandsw.schedulemc.tobacco.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.rolandsw.schedulemc.tobacco.PotType;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoPotBlockEntity;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Tobacco-System Commands
 */
public class TobaccoCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tobacco")
                
                // /tobacco info
                .then(Commands.literal("info")
                        .executes(TobaccoCommand::showInfo))
                
                // /tobacco give <item>
                .then(Commands.literal("give")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("item", StringArgumentType.word())
                                .executes(TobaccoCommand::giveItem)))
                
                // /tobacco stats
                .then(Commands.literal("stats")
                        .executes(TobaccoCommand::showStats))
        );
    }
    
    /**
     * Zeigt Infos über Topf/Pflanze an aktueller Position
     */
    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos pos = player.blockPosition().below();
            
            BlockEntity be = player.level().getBlockEntity(pos);
            if (!(be instanceof TobaccoPotBlockEntity potBE)) {
                ctx.getSource().sendFailure(Component.literal(
                    "§c✗ Du stehst nicht auf einem Tabak-Topf!"
                ));
                return 0;
            }
            
            var potData = potBE.getPotData();
            PotType type = potData.getPotType();
            
            StringBuilder info = new StringBuilder();
            info.append("§6╔══════════════════════════════╗\n");
            info.append("§6║ ").append(type.getColoredName()).append(" §6║\n");
            info.append("§6╠══════════════════════════════╣\n");
            info.append("§7│ Wasser: §b").append(potData.getWaterLevel()).append("/").append(type.getWaterCapacity()).append("\n");
            info.append("§7│ Erde: §6").append(potData.getSoilLevel()).append("/").append(type.getSoilCapacity()).append("\n");
            info.append("§7│ Verbrauch: §e").append((int)(type.getConsumptionMultiplier() * 100)).append("%\n");
            
            if (potData.hasPlant()) {
                var plant = potData.getPlant();
                info.append("§6╠══════════════════════════════╣\n");
                info.append("§7│ Sorte: ").append(plant.getType().getColoredName()).append("\n");
                info.append("§7│ Wachstum: §e").append(plant.getGrowthStage()).append("/3 §7(").append(plant.getGrowthStage() * 25).append("%)\n");
                info.append("§7│ Qualität: ").append(plant.getQuality().getColoredName()).append("\n");
                info.append("§7│ Ertrag: §e~").append(plant.getHarvestYield()).append(" Blätter\n");
                
                if (plant.hasFertilizer()) info.append("§7│ §a✓ Gedüngt\n");
                if (plant.hasGrowthBooster()) info.append("§7│ §b✓ Beschleunigt\n");
                if (plant.hasQualityBooster()) info.append("§7│ §6✓ Qualität verbessert\n");
            }
            
            info.append("§6╚══════════════════════════════╝");
            
            ctx.getSource().sendSuccess(() -> Component.literal(info.toString()), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Gibt Items (Admin)
     */
    private static int giveItem(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String itemName = StringArgumentType.getString(ctx, "item");
            
            ItemStack stack = switch (itemName.toLowerCase()) {
                // Samen
                case "virginia_seeds" -> new ItemStack(TobaccoItems.VIRGINIA_SEEDS.get(), 64);
                case "burley_seeds" -> new ItemStack(TobaccoItems.BURLEY_SEEDS.get(), 64);
                case "oriental_seeds" -> new ItemStack(TobaccoItems.ORIENTAL_SEEDS.get(), 64);
                case "havana_seeds" -> new ItemStack(TobaccoItems.HAVANA_SEEDS.get(), 64);
                
                // Flaschen
                case "fertilizer" -> new ItemStack(TobaccoItems.FERTILIZER_BOTTLE.get(), 16);
                case "growth_booster" -> new ItemStack(TobaccoItems.GROWTH_BOOSTER_BOTTLE.get(), 16);
                case "quality_booster" -> new ItemStack(TobaccoItems.QUALITY_BOOSTER_BOTTLE.get(), 16);
                
                // Werkzeuge
                case "watering_can" -> de.rolandsw.schedulemc.tobacco.items.WateringCanItem.createFull();
                
                default -> ItemStack.EMPTY;
            };
            
            if (stack.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal(
                    "§c✗ Unbekanntes Item: " + itemName + "\n" +
                    "§7Verfügbar: virginia_seeds, burley_seeds, oriental_seeds, havana_seeds,\n" +
                    "§7fertilizer, growth_booster, quality_booster, watering_can"
                ));
                return 0;
            }
            
            player.getInventory().add(stack);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§a✓ Item erhalten: §e" + itemName
            ), true);
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Zeigt Statistiken
     */
    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            
            // TODO: Hier könnten Statistiken gespeichert werden
            // Aktuell nur Platzhalter
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§6╔══════════════════════════════╗\n" +
                "§6║ §eTABAK STATISTIKEN §6║\n" +
                "§6╠══════════════════════════════╣\n" +
                "§7│ Gepflanzt: §e0\n" +
                "§7│ Geerntet: §e0\n" +
                "§7│ Verkauft: §e0€\n" +
                "§6╚══════════════════════════════╝"
            ), false);
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
