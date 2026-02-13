package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.economy.IEconomyAPI;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of IEconomyAPI
 *
 * Wrapper für EconomyManager mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class EconomyAPIImpl implements IEconomyAPI {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final EconomyManager economyManager;

    public EconomyAPIImpl() {
        this.economyManager = EconomyManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getBalance(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return economyManager.getBalance(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAccount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        return economyManager.hasAccount(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAccount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        if (hasAccount(playerUUID)) {
            throw new IllegalStateException("Account already exists for " + playerUUID);
        }
        economyManager.createAccount(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deposit(UUID playerUUID, double amount) {
        deposit(playerUUID, amount, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deposit(UUID playerUUID, double amount, @Nullable String description) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be positive, got: " + amount);
        }
        economyManager.deposit(playerUUID, amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean withdraw(UUID playerUUID, double amount) {
        return withdraw(playerUUID, amount, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean withdraw(UUID playerUUID, double amount, @Nullable String description) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be positive, got: " + amount);
        }
        return economyManager.withdraw(playerUUID, amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean transfer(UUID fromUUID, UUID toUUID, double amount) {
        return transfer(fromUUID, toUUID, amount, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean transfer(UUID fromUUID, UUID toUUID, double amount, @Nullable String description) {
        if (fromUUID == null || toUUID == null) {
            throw new IllegalArgumentException("UUIDs cannot be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be positive, got: " + amount);
        }
        return economyManager.transfer(fromUUID, toUUID, amount, description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBalance(UUID playerUUID, double amount) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        economyManager.setBalance(playerUUID, Math.max(0, amount));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAccount(UUID playerUUID) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        economyManager.deleteAccount(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getStartBalance() {
        return ModConfigHandler.COMMON.START_BALANCE.get();
    }

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Double> getAllBalances() {
        return Collections.unmodifiableMap(EconomyManager.getAllAccounts());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTotalMoneyInCirculation() {
        return EconomyManager.getAllAccounts().values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAccountCount() {
        return EconomyManager.getAllAccounts().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map.Entry<UUID, Double>> getTopBalances(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1, got: " + limit);
        }
        return EconomyManager.getAllAccounts().entrySet().stream()
            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAfford(UUID playerUUID, double amount) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative, got: " + amount);
        }
        return getBalance(playerUUID) >= amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean batchTransfer(UUID fromUUID, Map<UUID, Double> recipients, @Nullable String description) {
        if (fromUUID == null) {
            throw new IllegalArgumentException("fromUUID cannot be null");
        }
        if (recipients == null) {
            throw new IllegalArgumentException("recipients cannot be null");
        }

        // Calculate total amount needed
        double totalAmount = recipients.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();

        // Check if sender can afford the total
        if (!canAfford(fromUUID, totalAmount)) {
            return false;
        }

        // Perform all transfers
        for (Map.Entry<UUID, Double> entry : recipients.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("recipient UUID cannot be null");
            }
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException("transfer amount must be non-negative");
            }
            boolean success = transfer(fromUUID, entry.getKey(), entry.getValue(), description);
            if (!success) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getTransactionHistory(UUID playerUUID, int limit) {
        if (playerUUID == null) {
            throw new IllegalArgumentException("playerUUID cannot be null");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1, got: " + limit);
        }
        LOGGER.debug("Stub: getTransactionHistory not fully implemented - transaction history not directly accessible");
        return Collections.emptyList();
    }
}
