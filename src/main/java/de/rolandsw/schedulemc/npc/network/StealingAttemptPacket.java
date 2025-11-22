package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Packet für Diebstahl-Versuch (Ergebnis des Minigames)
 */
public class StealingAttemptPacket {
    private final int npcEntityId;
    private final boolean success;

    public StealingAttemptPacket(int npcEntityId, boolean success) {
        this.npcEntityId = npcEntityId;
        this.success = success;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcEntityId);
        buf.writeBoolean(success);
    }

    public static StealingAttemptPacket decode(FriendlyByteBuf buf) {
        return new StealingAttemptPacket(buf.readInt(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && success) {
                Entity entity = player.level().getEntity(npcEntityId);
                if (entity instanceof CustomNPCEntity npc) {
                    // Erfolgreicher Diebstahl!
                    List<ItemStack> stolenItems = new ArrayList<>();
                    double stolenMoney = 0.0;

                    // 1. Geld stehlen (50% des NPC Guthabens)
                    // TODO: Implementiere NPC Wallet System
                    double npcMoney = Math.random() * 500; // Placeholder
                    stolenMoney = npcMoney * 0.5;

                    // 2. Items stehlen (zufällig 1-3 Items aus Hotbar)
                    int itemsToSteal = 1 + (int)(Math.random() * 3); // 1-3 Items
                    List<Integer> availableSlots = new ArrayList<>();

                    // Finde nicht-leere Slots
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = npc.getNpcData().getInventory().get(i);
                        if (!stack.isEmpty()) {
                            availableSlots.add(i);
                        }
                    }

                    // Stehle zufällige Items
                    for (int i = 0; i < Math.min(itemsToSteal, availableSlots.size()); i++) {
                        int randomIndex = (int)(Math.random() * availableSlots.size());
                        int slot = availableSlots.remove(randomIndex);

                        ItemStack stack = npc.getNpcData().getInventory().get(slot);
                        if (!stack.isEmpty()) {
                            // Stehle einen Teil des Stacks (50-100%)
                            int amountToSteal = Math.max(1, (int)(stack.getCount() * (0.5 + Math.random() * 0.5)));
                            ItemStack stolen = stack.copy();
                            stolen.setCount(amountToSteal);

                            // Entferne vom NPC
                            stack.shrink(amountToSteal);
                            npc.getNpcData().getInventory().set(slot, stack);

                            // Gib dem Spieler
                            if (!player.getInventory().add(stolen)) {
                                // Inventar voll - droppe Item
                                player.drop(stolen, false);
                            }

                            stolenItems.add(stolen);
                        }
                    }

                    // 3. Geld zum Spieler Wallet hinzufügen
                    if (stolenMoney > 0) {
                        WalletManager.addMoney(player.getUUID(), stolenMoney);
                        WalletManager.save();
                    }

                    // 4. Erfolgsmeldung
                    StringBuilder message = new StringBuilder("§a✓ Diebstahl erfolgreich!");
                    if (stolenMoney > 0) {
                        message.append("\n§7+ ").append(String.format("%.2f€", stolenMoney));
                    }
                    if (!stolenItems.isEmpty()) {
                        message.append("\n§7+ ").append(stolenItems.size()).append(" Items gestohlen");
                    }

                    player.sendSystemMessage(Component.literal(message.toString()));

                    // TODO: Füge Wanted-Level hinzu (für zukünftiges Polizei-System)
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
