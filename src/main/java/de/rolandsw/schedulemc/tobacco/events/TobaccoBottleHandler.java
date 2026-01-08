package de.rolandsw.schedulemc.tobacco.events;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.production.blockentity.PlantPotBlockEntity;
import de.rolandsw.schedulemc.tobacco.items.TobaccoBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handler für Flaschen-Interaktionen mit Tabakpflanzen
 */
public class TobaccoBottleHandler {
    
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EventHelper.handleRightClickBlock(event, player -> {
            Level level = event.getLevel();
            BlockPos pos = event.getPos();
            InteractionHand hand = event.getHand();
            ItemStack heldItem = player.getItemInHand(hand);

            // Prüfen ob Flasche gehalten wird
            if (!(heldItem.getItem() instanceof TobaccoBottleItem)) {
                return;
            }

            // Prüfen ob es ein Topf ist
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof PlantPotBlockEntity potBE)) {
                return;
            }

            var potData = potBE.getPotData();

            // Prüfen ob Pflanze vorhanden ist
            if (!potData.hasPlant()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Keine Pflanze im Topf!"
                ), true);
                return;
            }

            var plant = potData.getPlant();

            // Prüfen ob Pflanze ausgewachsen ist
            if (plant.isFullyGrown()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Pflanze ist bereits ausgewachsen!"
                ), true);
                return;
            }

            String itemName = heldItem.getItem().toString();
            boolean consumed = false;

            // ═══════════════════════════════════════════════════════════
            // DÜNGER
            // ═══════════════════════════════════════════════════════════
            if (itemName.contains("fertilizer")) {
                if (plant.hasFertilizer()) {
                    player.displayClientMessage(Component.literal(
                        "§c✗ Pflanze wurde bereits gedüngt!"
                    ), true);
                    return;
                }

                plant.applyFertilizer();
                consumed = true;

                player.displayClientMessage(Component.literal(
                    "§a✓ Dünger angewendet!\n" +
                    "§7Effekt: §eMehr Ertrag (max 10g)\n" +
                    "§7Nachteil: §c-1 Qualität"
                ), true);
            }

            // ═══════════════════════════════════════════════════════════
            // WACHSTUMSBESCHLEUNIGER
            // ═══════════════════════════════════════════════════════════
            else if (itemName.contains("growth_booster")) {
                if (plant.hasGrowthBooster()) {
                    player.displayClientMessage(Component.literal(
                        "§c✗ Pflanze hat bereits einen Wachstumsbeschleuniger!"
                    ), true);
                    return;
                }

                plant.applyGrowthBooster();
                consumed = true;

                player.displayClientMessage(Component.literal(
                    "§a✓ Wachstumsbeschleuniger angewendet!\n" +
                    "§7Effekt: §e2x Wachstumsgeschwindigkeit\n" +
                    "§7Nachteil: §c-1 Qualität"
                ), true);
            }

            // ═══════════════════════════════════════════════════════════
            // QUALITÄTSVERBESSERER
            // ═══════════════════════════════════════════════════════════
            else if (itemName.contains("quality_booster")) {
                if (plant.hasQualityBooster()) {
                    player.displayClientMessage(Component.literal(
                        "§c✗ Pflanze hat bereits einen Qualitätsverbesserer!"
                    ), true);
                    return;
                }

                plant.applyQualityBooster();
                consumed = true;

                player.displayClientMessage(Component.literal(
                    "§a✓ Qualitätsverbesserer angewendet!\n" +
                    "§7Effekt: §a+1 Qualität\n" +
                    "§7Neue Qualität: " + plant.getQuality().getColoredName()
                ), true);
            }

            // Flasche verbrauchen
            if (consumed) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }

                potBE.setChanged();
                event.setCanceled(true);
            }
        });
    }
}
