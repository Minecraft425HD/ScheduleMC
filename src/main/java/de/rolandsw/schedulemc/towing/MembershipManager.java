package de.rolandsw.schedulemc.towing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player memberships for the towing service
 */
public class MembershipManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, MembershipData> memberships = new ConcurrentHashMap<>();
    private static final File file = new File("config/plotmod_towing_memberships.json");
    private static final Gson gson = GsonHelper.get();

    private static final MembershipPersistenceManager persistence =
        new MembershipPersistenceManager(file, gson);

    public static void load() {
        persistence.load();
    }

    public static void save() {
        persistence.save();
    }

    public static void saveIfNeeded() {
        persistence.saveIfNeeded();
    }

    private static void markDirty() {
        persistence.markDirty();
    }

    /**
     * Gets membership for player (or NONE if no membership)
     */
    public static MembershipData getMembership(UUID playerId) {
        return memberships.computeIfAbsent(playerId, uuid -> new MembershipData(uuid, MembershipTier.NONE));
    }

    /**
     * Sets membership tier for player
     */
    public static boolean setMembershipTier(UUID playerId, MembershipTier tier, ServerPlayer player) {
        MembershipData data = getMembership(playerId);

        // If upgrading/changing to paid tier, charge immediately
        if (tier != MembershipTier.NONE) {
            double fee = tier.getMonthlyFee();
            if (!EconomyManager.hasEnoughMoney(playerId, fee)) {
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("towing.membership.insufficient_funds",
                        String.format("%.2f", fee)));
                }
                return false;
            }

            EconomyManager.withdraw(playerId, fee);
            if (player != null) {
                player.sendSystemMessage(Component.translatable("towing.membership.activated",
                    Component.translatable(tier.getTranslationKey()),
                    String.format("%.2f", fee)));
            }
        } else {
            if (player != null) {
                player.sendSystemMessage(Component.translatable("towing.membership.cancelled"));
            }
        }

        data.setTier(tier);
        markDirty();
        LOGGER.info("Player {} membership changed to {}", playerId, tier);
        return true;
    }

    /**
     * Process membership payments for all players
     * Called daily by server tick
     */
    public static void processPayments(long currentTime) {
        for (MembershipData data : memberships.values()) {
            if (!data.isActive() || data.getTier() == MembershipTier.NONE) {
                continue;
            }

            if (currentTime >= data.getNextPaymentDate()) {
                processSinglePayment(data);
            }
        }
    }

    private static void processSinglePayment(MembershipData data) {
        UUID playerId = data.getPlayerId();
        MembershipTier tier = data.getTier();
        double fee = tier.getMonthlyFee();

        if (EconomyManager.hasEnoughMoney(playerId, fee)) {
            EconomyManager.withdraw(playerId, fee);
            data.renew();
            markDirty();
            LOGGER.info("Membership renewed for player {}: {} ({}€)", playerId, tier, fee);
        } else {
            data.cancel();
            markDirty();
            LOGGER.info("Membership cancelled for player {} (insufficient funds: {}€)", playerId, fee);
        }
    }

    /**
     * Sends warning to player if payment is due soon
     */
    public static void sendPaymentWarnings(ServerPlayer player) {
        MembershipData data = getMembership(player.getUUID());
        if (!data.isActive() || data.getTier() == MembershipTier.NONE) {
            return;
        }

        int daysLeft = data.getDaysUntilPayment();
        double fee = data.getTier().getMonthlyFee();

        if (daysLeft == 3) {
            player.sendSystemMessage(Component.translatable("towing.membership.warning.3days",
                Component.translatable(data.getTier().getTranslationKey()),
                String.format("%.2f", fee)));
        } else if (daysLeft == 1) {
            player.sendSystemMessage(Component.translatable("towing.membership.warning.1day",
                Component.translatable(data.getTier().getTranslationKey()),
                String.format("%.2f", fee)));
        }
    }

    /**
     * Calculates towing cost after membership discount
     */
    public static double calculateTowingCost(UUID playerId, double baseCost) {
        MembershipData data = getMembership(playerId);
        return data.getTier().calculatePlayerCost(baseCost);
    }

    /**
     * Records a tow for statistics
     */
    public static void recordTow(UUID playerId) {
        MembershipData data = getMembership(playerId);
        data.incrementTows();
        markDirty();
    }

    @Nullable
    public static String getLastError() {
        return persistence.getLastError();
    }

    public static boolean isHealthy() {
        return persistence.isHealthy();
    }

    /**
     * Data class for JSON serialization
     */
    private static class MembershipSaveData {
        int tierOrdinal;
        long subscriptionStartDate;
        long nextPaymentDate;
        boolean active;
        int towsThisPeriod;
    }

    /**
     * Persistence manager
     */
    private static class MembershipPersistenceManager extends AbstractPersistenceManager<Map<String, MembershipSaveData>> {

        public MembershipPersistenceManager(File dataFile, Gson gson) {
            super(dataFile, gson);
        }

        @Override
        protected Type getDataType() {
            return new TypeToken<Map<String, MembershipSaveData>>(){}.getType();
        }

        @Override
        protected void onDataLoaded(Map<String, MembershipSaveData> data) {
            memberships.clear();

            data.forEach((uuidStr, saveData) -> {
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    MembershipTier tier = MembershipTier.fromOrdinal(saveData.tierOrdinal);

                    MembershipData membershipData = new MembershipData(playerId, tier);
                    membershipData.setActive(saveData.active);
                    membershipData.setNextPaymentDate(saveData.nextPaymentDate);

                    // Restore tow count
                    for (int i = 0; i < saveData.towsThisPeriod; i++) {
                        membershipData.incrementTows();
                    }

                    memberships.put(playerId, membershipData);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Invalid player UUID in memberships: {}", uuidStr, e);
                }
            });
        }

        @Override
        protected Map<String, MembershipSaveData> getCurrentData() {
            Map<String, MembershipSaveData> saveMap = new HashMap<>();

            memberships.forEach((playerId, data) -> {
                // Only save if player has an active membership or had one
                if (data.getTier() != MembershipTier.NONE || data.getTowsThisPeriod() > 0) {
                    MembershipSaveData saveData = new MembershipSaveData();
                    saveData.tierOrdinal = data.getTier().ordinal();
                    saveData.subscriptionStartDate = data.getSubscriptionStartDate();
                    saveData.nextPaymentDate = data.getNextPaymentDate();
                    saveData.active = data.isActive();
                    saveData.towsThisPeriod = data.getTowsThisPeriod();

                    saveMap.put(playerId.toString(), saveData);
                }
            });

            return saveMap;
        }

        @Override
        protected String getComponentName() {
            return "Towing Membership System";
        }

        @Override
        protected String getHealthDetails() {
            long activeCount = memberships.values().stream()
                .filter(MembershipData::isActive)
                .count();
            return String.format("%d total, %d active", memberships.size(), activeCount);
        }

        @Override
        protected void onCriticalLoadFailure() {
            memberships.clear();
        }
    }
}
