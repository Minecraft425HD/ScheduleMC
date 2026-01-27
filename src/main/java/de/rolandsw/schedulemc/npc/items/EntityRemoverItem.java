package de.rolandsw.schedulemc.npc.items;

import de.rolandsw.schedulemc.managers.NPCEntityRegistry;
import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityVehicleBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Admin-Tool zum Entfernen von NPCs und Fahrzeugen
 * - Linksklick auf NPC: Entfernt den NPC permanent
 * - Linksklick auf Fahrzeug: Entfernt das Fahrzeug permanent
 * - Nur für Admins mit OP-Level 2+ verfügbar
 */
public class EntityRemoverItem extends Item {

    public EntityRemoverItem() {
        super(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .fireResistant()
        );
    }

    /**
     * Wird aufgerufen wenn ein Spieler mit dem Item per Linksklick auf eine Entity klickt
     * Behandelt sowohl NPCs als auch Fahrzeuge
     * Diese Methode wird über ein Event (AttackEntityEvent) in EntityRemoverHandler aufgerufen
     */
    public static boolean onEntityInteract(Player player, Entity target, ItemStack stack) {
        if (player.level().isClientSide) {
            return false;
        }

        if (!(stack.getItem() instanceof EntityRemoverItem)) {
            return false;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.hasPermissions(2)) {
                serverPlayer.sendSystemMessage(
                    Component.translatable("item.entity_remover.no_permission")
                        .withStyle(ChatFormatting.RED)
                );
                return false;
            }

            // Prüfe ob es ein NPC ist
            if (target instanceof CustomNPCEntity npc) {
                String npcName = npc.getNpcName();

                // Entferne aus Registry
                NPCEntityRegistry.unregisterNPC(npc);
                NPCNameRegistry.unregisterName(npcName);

                // Memory Leak Prevention - Cleanup NPC-bezogene Daten
                de.rolandsw.schedulemc.npc.events.PoliceSearchBehavior.cleanupNPC(npc.getUUID());
                de.rolandsw.schedulemc.npc.events.PoliceBackupSystem.cleanupNPC(npc.getUUID());

                // Entferne Entity
                npc.discard();

                serverPlayer.sendSystemMessage(
                    Component.translatable("item.entity_remover.npc_removed")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(" ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(npcName).withStyle(ChatFormatting.YELLOW))
                );

                return true;
            }

            // Prüfe ob es ein Fahrzeug ist
            if (target instanceof EntityVehicleBase vehicle) {
                String vehicleType = vehicle.getType().getDescription().getString();

                // Entferne Entity
                vehicle.discard();

                serverPlayer.sendSystemMessage(
                    Component.translatable("item.entity_remover.vehicle_removed")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(" ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(vehicleType).withStyle(ChatFormatting.AQUA))
                );

                return true;
            }
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.entity_remover.tooltip.line1")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.entity_remover.tooltip.line2")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.entity_remover.tooltip.warning")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.entity_remover.tooltip.op_required")
            .withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Enchanted Glint Effect
    }
}
