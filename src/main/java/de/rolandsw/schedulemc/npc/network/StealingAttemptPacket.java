package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.PacketHandler;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
        PacketHandler.handleServerPacket(ctx, player -> {
                Entity entity = player.level().getEntity(npcEntityId);
                if (entity instanceof CustomNPCEntity npc) {
                    // Null-Safety: Prüfe ob NPC-Daten vorhanden sind
                    if (npc.getNpcData() == null) {
                        LOGGER.warn("[STEALING] NPC {} hat keine NPCData!", npc.getId());
                        return;
                    }

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

                                // Performance-Optimierung: Sync nur Wallet statt Full NPC Data
                                npc.syncWalletToClient();

                                // Geld zum Wallet hinzufügen (WalletManager)
                                ItemStack walletItem = player.getInventory().getItem(8);
                                if (walletItem.getItem() instanceof CashItem) {
                                    double previousValue = de.rolandsw.schedulemc.economy.WalletManager.getBalance(player.getUUID());
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("[STEALING] Wallet vorher: {}€", previousValue);
                                    }

                                    // Füge Geld im WalletManager hinzu
                                    de.rolandsw.schedulemc.economy.WalletManager.addMoney(player.getUUID(), stolenMoney);
                                    de.rolandsw.schedulemc.economy.WalletManager.save();

                                    double newValue = de.rolandsw.schedulemc.economy.WalletManager.getBalance(player.getUUID());
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("[STEALING] Wallet nachher: {}€", newValue);
                                    }
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

                        // Null-Safety: Prüfe ob Inventar vorhanden ist
                        var npcInventory = npc.getNpcData().getInventory();
                        if (npcInventory == null) {
                            LOGGER.warn("[STEALING] NPC {} hat kein Inventar!", npc.getId());
                            return;
                        }

                        // Finde nicht-leere Slots
                        for (int i = 0; i < 9; i++) {
                            ItemStack stack = npcInventory.get(i);
                            if (!stack.isEmpty()) {
                                availableSlots.add(i);
                            }
                        }

                        if (!availableSlots.isEmpty()) {
                            // Wähle zufälligen Slot
                            int randomIndex = (int)(Math.random() * availableSlots.size());
                            int slot = availableSlots.get(randomIndex);

                            ItemStack stack = npcInventory.get(slot);
                            if (!stack.isEmpty()) {
                                // Stehle das komplette Item
                                stolenItem = stack.copy();

                                // Entferne vom NPC
                                npcInventory.set(slot, ItemStack.EMPTY);

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
                            LOGGER.debug("[STEALING] Cooldown set for day: {}", currentDay);
                        }

                        // ═══════════════════════════════════════════
                        // ERFOLGSMELDUNG
                        // ═══════════════════════════════════════════
                        player.sendSystemMessage(Component.translatable("message.stealing.success"));

                        if (stolenMoney > 0) {
                            double walletValue = de.rolandsw.schedulemc.economy.WalletManager.getBalance(player.getUUID());
                            player.sendSystemMessage(Component.translatable("message.stealing.money_stolen", String.format("%.2f€", stolenMoney)));
                            player.sendSystemMessage(Component.translatable("message.stealing.wallet_stolen", String.format("%.2f€", walletValue)));
                        }

                        if (!stolenItem.isEmpty()) {
                            player.sendSystemMessage(Component.translatable("message.stealing.item_stolen", stolenItem.getHoverName().getString(), stolenItem.getCount()));
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
                            player.sendSystemMessage(Component.translatable("message.stealing.npc_attacks", npc.getNpcName()));
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
                                player.sendSystemMessage(Component.translatable("message.police.witnessed"));
                            } else {
                                player.sendSystemMessage(Component.translatable("message.stealing.caught"));
                            }
                            player.sendSystemMessage(Component.translatable("message.stealing.wanted_level", stars, currentWantedLevel));

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
                            LOGGER.debug("[STEALING] No witnesses nearby");
                        }
                    }
                }
        });
    }
}
