package de.rolandsw.schedulemc.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Mod-spezifische Events für lose Manager-Kopplung
 *
 * OPTIMIERUNG: Ermöglicht asynchrone Kommunikation zwischen Managern
 * ohne direkte Abhängigkeiten. Vorteile:
 * - Bessere Testbarkeit (Manager können isoliert getestet werden)
 * - Erweiterbarkeit (neue Listener können ohne Codeänderung hinzugefügt werden)
 * - Reduzierte zirkuläre Abhängigkeiten
 *
 * Verwendung:
 * <pre>
 * // Event senden
 * MinecraftForge.EVENT_BUS.post(new ModEvents.BalanceChangedEvent(player, oldBalance, newBalance));
 *
 * // Event empfangen
 * @SubscribeEvent
 * public void onBalanceChanged(ModEvents.BalanceChangedEvent event) {
 *     // Reagiere auf Änderung
 * }
 * </pre>
 */
public class ModEvents {

    // ═══════════════════════════════════════════════════════════
    // ECONOMY EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird ausgelöst wenn sich der Kontostand eines Spielers ändert
     */
    public static class BalanceChangedEvent extends Event {
        private final UUID playerUUID;
        private final double oldBalance;
        private final double newBalance;
        private final ChangeReason reason;
        @Nullable
        private final ServerPlayer player;

        public enum ChangeReason {
            DEPOSIT,
            WITHDRAWAL,
            TRANSFER_IN,
            TRANSFER_OUT,
            LOAN_RECEIVED,
            LOAN_REPAYMENT,
            TAX,
            INTEREST,
            PURCHASE,
            SALE,
            SALARY,
            REWARD,
            OTHER
        }

        public BalanceChangedEvent(UUID playerUUID, double oldBalance, double newBalance,
                                   ChangeReason reason, @Nullable ServerPlayer player) {
            this.playerUUID = playerUUID;
            this.oldBalance = oldBalance;
            this.newBalance = newBalance;
            this.reason = reason;
            this.player = player;
        }

        public UUID getPlayerUUID() { return playerUUID; }
        public double getOldBalance() { return oldBalance; }
        public double getNewBalance() { return newBalance; }
        public double getDelta() { return newBalance - oldBalance; }
        public ChangeReason getReason() { return reason; }
        @Nullable
        public ServerPlayer getPlayer() { return player; }
    }

    /**
     * Wird ausgelöst wenn ein Kredit aufgenommen wird
     */
    public static class LoanTakenEvent extends Event {
        private final UUID playerUUID;
        private final double amount;
        private final double interestRate;
        private final int durationDays;

        public LoanTakenEvent(UUID playerUUID, double amount, double interestRate, int durationDays) {
            this.playerUUID = playerUUID;
            this.amount = amount;
            this.interestRate = interestRate;
            this.durationDays = durationDays;
        }

        public UUID getPlayerUUID() { return playerUUID; }
        public double getAmount() { return amount; }
        public double getInterestRate() { return interestRate; }
        public int getDurationDays() { return durationDays; }
    }

    /**
     * Wird ausgelöst wenn ein Kredit zurückgezahlt wird
     */
    public static class LoanRepaidEvent extends Event {
        private final UUID playerUUID;
        private final double amount;
        private final boolean fullyRepaid;

        public LoanRepaidEvent(UUID playerUUID, double amount, boolean fullyRepaid) {
            this.playerUUID = playerUUID;
            this.amount = amount;
            this.fullyRepaid = fullyRepaid;
        }

        public UUID getPlayerUUID() { return playerUUID; }
        public double getAmount() { return amount; }
        public boolean isFullyRepaid() { return fullyRepaid; }
    }

    // ═══════════════════════════════════════════════════════════
    // NPC EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird ausgelöst wenn ein NPC gespawnt wird
     */
    public static class NPCSpawnedEvent extends Event {
        private final UUID npcUUID;
        private final String npcName;
        private final String npcType;

        public NPCSpawnedEvent(UUID npcUUID, String npcName, String npcType) {
            this.npcUUID = npcUUID;
            this.npcName = npcName;
            this.npcType = npcType;
        }

        public UUID getNpcUUID() { return npcUUID; }
        public String getNpcName() { return npcName; }
        public String getNpcType() { return npcType; }
    }

    /**
     * Wird ausgelöst wenn ein NPC entfernt wird
     */
    public static class NPCRemovedEvent extends Event {
        private final UUID npcUUID;
        private final String npcName;

        public NPCRemovedEvent(UUID npcUUID, String npcName) {
            this.npcUUID = npcUUID;
            this.npcName = npcName;
        }

        public UUID getNpcUUID() { return npcUUID; }
        public String getNpcName() { return npcName; }
    }

    // ═══════════════════════════════════════════════════════════
    // PLOT EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird ausgelöst wenn ein Plot erstellt wird
     */
    public static class PlotCreatedEvent extends Event {
        private final String plotId;
        private final UUID ownerUUID;

        public PlotCreatedEvent(String plotId, UUID ownerUUID) {
            this.plotId = plotId;
            this.ownerUUID = ownerUUID;
        }

        public String getPlotId() { return plotId; }
        public UUID getOwnerUUID() { return ownerUUID; }
    }

    /**
     * Wird ausgelöst wenn ein Plot verkauft/übertragen wird
     */
    public static class PlotTransferredEvent extends Event {
        private final String plotId;
        private final UUID previousOwner;
        private final UUID newOwner;
        private final double price;

        public PlotTransferredEvent(String plotId, UUID previousOwner, UUID newOwner, double price) {
            this.plotId = plotId;
            this.previousOwner = previousOwner;
            this.newOwner = newOwner;
            this.price = price;
        }

        public String getPlotId() { return plotId; }
        public UUID getPreviousOwner() { return previousOwner; }
        public UUID getNewOwner() { return newOwner; }
        public double getPrice() { return price; }
    }

    // ═══════════════════════════════════════════════════════════
    // CRIME EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird ausgelöst wenn ein Spieler Wanted-Level erhält
     */
    public static class WantedLevelChangedEvent extends Event {
        private final UUID playerUUID;
        private final int oldLevel;
        private final int newLevel;
        private final String reason;

        public WantedLevelChangedEvent(UUID playerUUID, int oldLevel, int newLevel, String reason) {
            this.playerUUID = playerUUID;
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
            this.reason = reason;
        }

        public UUID getPlayerUUID() { return playerUUID; }
        public int getOldLevel() { return oldLevel; }
        public int getNewLevel() { return newLevel; }
        public String getReason() { return reason; }
        public boolean isIncreased() { return newLevel > oldLevel; }
    }

    // ═══════════════════════════════════════════════════════════
    // ACHIEVEMENT EVENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird ausgelöst wenn ein Spieler ein Achievement erhält
     */
    public static class AchievementUnlockedEvent extends Event {
        private final UUID playerUUID;
        private final String achievementId;
        private final double reward;

        public AchievementUnlockedEvent(UUID playerUUID, String achievementId, double reward) {
            this.playerUUID = playerUUID;
            this.achievementId = achievementId;
            this.reward = reward;
        }

        public UUID getPlayerUUID() { return playerUUID; }
        public String getAchievementId() { return achievementId; }
        public double getReward() { return reward; }
    }
}
