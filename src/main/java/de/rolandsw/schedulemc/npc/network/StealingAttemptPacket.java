package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Packet für Diebstahl-Versuch (Ergebnis des Minigames)
 */
public class StealingAttemptPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int npcEntityId;
    private final boolean success;
    private final int stealType; // 0 = Geld, 1 = Items

    public StealingAttemptPacket(int npcEntityId, boolean success, int stealType) {
        this.npcEntityId = npcEntityId;
        this.success = success;
        this.stealType = stealType;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcEntityId);
        buf.writeBoolean(success);
        buf.writeInt(stealType);
    }

    public static StealingAttemptPacket decode(FriendlyByteBuf buf) {
        return new StealingAttemptPacket(buf.readInt(), buf.readBoolean(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(npcEntityId);
                if (entity instanceof CustomNPCEntity npc) {
                    long currentDay = player.level().getDayTime() / 24000;

                    if (success) {
                        // ═══════════════════════════════════════════
                        // ERFOLGREICHER DIEBSTAHL
                        // ═══════════════════════════════════════════
                        ItemStack stolenItem = ItemStack.EMPTY;
                        double stolenMoney = 0.0;

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[STEALING] Player: {} - StealType: {}", player.getName().getString(), stealType);
                        }

                    if (stealType == 0) {
                        // ═══════════════════════════════════════════
                        // GELD STEHLEN (50% des NPC Guthabens)
                        // ═══════════════════════════════════════════
                        int npcWallet = npc.getNpcData().getWallet();
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[STEALING] NPC Wallet: {}€", npcWallet);
                        }

                        if (npcWallet > 0) {
                            int stolenAmount = (int)(npcWallet * 0.5);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[STEALING] Berechnet 50%: {}€", stolenAmount);
                            }

                            if (stolenAmount > 0) {
                                npc.getNpcData().removeMoney(stolenAmount);
                                stolenMoney = stolenAmount;
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("[STEALING] Geld vom NPC entfernt: {}€", stolenAmount);
                                }

                                // Geld zum Wallet-Item hinzufügen
                                ItemStack walletItem = player.getInventory().getItem(8);
                                if (walletItem.getItem() instanceof CashItem) {
                                    double previousValue = CashItem.getValue(walletItem);
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("[STEALING] Wallet-Item vorher: {}€", previousValue);
                                    }

                                    CashItem.addValue(walletItem, stolenMoney);

                                    double newValue = CashItem.getValue(walletItem);
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("[STEALING] Wallet-Item nachher: {}€", newValue);
                                    }

                                    // Auch WalletManager aktualisieren
                                    WalletManager.addMoney(player.getUUID(), stolenMoney);
                                    WalletManager.save();
                                } else {
                                    LOGGER.warn("[STEALING] WARNUNG: Kein Wallet-Item in Slot 8!");
                                }
                            }
                        }
                    } else if (stealType == 1) {
                        // ═══════════════════════════════════════════
                        // ITEM STEHLEN (nur 1 Item)
                        // ═══════════════════════════════════════════
                        List<Integer> availableSlots = new ArrayList<>();

                        // Finde nicht-leere Slots
                        for (int i = 0; i < 9; i++) {
                            ItemStack stack = npc.getNpcData().getInventory().get(i);
                            if (!stack.isEmpty()) {
                                availableSlots.add(i);
                            }
                        }

                        if (!availableSlots.isEmpty()) {
                            // Wähle zufälligen Slot
                            int randomIndex = (int)(Math.random() * availableSlots.size());
                            int slot = availableSlots.get(randomIndex);

                            ItemStack stack = npc.getNpcData().getInventory().get(slot);
                            if (!stack.isEmpty()) {
                                // Stehle das komplette Item
                                stolenItem = stack.copy();

                                // Entferne vom NPC
                                npc.getNpcData().getInventory().set(slot, ItemStack.EMPTY);

                                // Gib dem Spieler
                                if (!player.getInventory().add(stolenItem)) {
                                    // Inventar voll - droppe Item
                                    player.drop(stolenItem, false);
                                }

                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("[STEALING] Item gestohlen: {} x{}", stolenItem.getHoverName().getString(), stolenItem.getCount());
                                }
                            }
                        }
                    }

                        // Setze Cooldown (aktueller Tag)
                        npc.getNpcData().getCustomData().putLong("LastSteal_" + player.getStringUUID(), currentDay);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[STEALING] Cooldown gesetzt für Tag: {}", currentDay);
                        }

                        // ═══════════════════════════════════════════
                        // ERFOLGSMELDUNG
                        // ═══════════════════════════════════════════
                        player.sendSystemMessage(Component.literal("§a✓ Diebstahl erfolgreich!"));

                        if (stolenMoney > 0) {
                            ItemStack walletItem = player.getInventory().getItem(8);
                            if (walletItem.getItem() instanceof CashItem) {
                                double walletValue = CashItem.getValue(walletItem);
                                player.sendSystemMessage(Component.literal("§7+ " + String.format("%.2f€", stolenMoney) + " gestohlen"));
                                player.sendSystemMessage(Component.literal("§7Geldbörse: " + String.format("%.2f€", walletValue)));
                            }
                        }

                        if (!stolenItem.isEmpty()) {
                            player.sendSystemMessage(Component.literal("§7+ " + stolenItem.getHoverName().getString() + " x" + stolenItem.getCount() + " gestohlen"));
                        }
                    } else {
                        // ═══════════════════════════════════════════
                        // FEHLGESCHLAGENER DIEBSTAHL
                        // ═══════════════════════════════════════════
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[STEALING] Fehlgeschlagen - Player: {}", player.getName().getString());
                        }

                        // 33% Chance: NPC attackiert Spieler
                        if (Math.random() < 0.33) {
                            npc.setTarget(player);
                            player.sendSystemMessage(Component.literal("§c⚠ " + npc.getNpcName() + " greift dich an!"));
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[STEALING] NPC {} attackiert Spieler", npc.getNpcName());
                            }
                        }
                    }

                    // ═══════════════════════════════════════════
                    // ZEUGEN-DETEKTION & WANTED-LEVEL
                    // ═══════════════════════════════════════════
                    // Suche NPCs in 16 Block Radius
                    List<CustomNPCEntity> witnesses = player.level().getEntitiesOfClass(
                        CustomNPCEntity.class,
                        AABB.ofSize(player.position(), 16, 16, 16)
                    );

                    // Entferne das Opfer aus der Zeugenliste
                    witnesses.remove(npc);

                    if (!witnesses.isEmpty()) {
                        // Prüfe ob POLIZEI dabei ist
                        boolean policePresent = false;
                        for (CustomNPCEntity witness : witnesses) {
                            if (witness.getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI) {
                                policePresent = true;
                                break;
                            }
                        }

                        double detectionChance;
                        if (policePresent) {
                            // POLIZEI anwesend = 100% Erkennung!
                            detectionChance = 1.0;
                        } else {
                            // Normale Zeugen: 15% pro Zeuge, max 90%
                            detectionChance = Math.min(0.9, witnesses.size() * 0.15);
                        }

                        if (Math.random() < detectionChance) {
                            // Verbrechen wurde gesehen!
                            CrimeManager.addWantedLevel(player.getUUID(), 2, currentDay);

                            int currentWantedLevel = CrimeManager.getWantedLevel(player.getUUID());
                            String stars = "⭐".repeat(currentWantedLevel);

                            if (policePresent) {
                                player.sendSystemMessage(Component.literal("§c⚠ POLIZEI hat dich gesehen!"));
                            } else {
                                player.sendSystemMessage(Component.literal("§c⚠ Du wurdest beim Stehlen gesehen!"));
                            }
                            player.sendSystemMessage(Component.literal("§c" + stars + " Fahndungsstufe: " + currentWantedLevel));

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[CRIME] Player {} gesehen beim Stehlen - Wanted Level: {} ({} Zeugen{})",
                                    player.getName().getString(), currentWantedLevel, witnesses.size(),
                                    policePresent ? ", POLIZEI dabei!" : "");
                            }
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[STEALING] Nicht entdeckt (Chance: {:.1f}%)", detectionChance * 100);
                            }
                        }
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[STEALING] Keine Zeugen in der Nähe");
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
