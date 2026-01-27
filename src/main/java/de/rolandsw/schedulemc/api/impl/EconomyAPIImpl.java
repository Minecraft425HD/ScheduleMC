package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.economy.IEconomyAPI;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import javax.annotation.Nullable;
import java.util.UUID;

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
}
