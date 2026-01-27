package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.production.IProductionAPI;
import de.rolandsw.schedulemc.production.config.ProductionConfig;
import de.rolandsw.schedulemc.production.config.ProductionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of IProductionAPI
 *
 * Wrapper für ProductionRegistry mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class ProductionAPIImpl implements IProductionAPI {

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ProductionConfig getProduction(String productionId) {
        if (productionId == null) {
            throw new IllegalArgumentException("productionId cannot be null");
        }
        return ProductionRegistry.getProduction(productionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProduction(String productionId) {
        if (productionId == null) {
            throw new IllegalArgumentException("productionId cannot be null");
        }
        return ProductionRegistry.hasProduction(productionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ProductionConfig> getAllProductions() {
        return ProductionRegistry.getAllProductions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductionConfig> getProductionsByCategory(ProductionConfig.ProductionCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
        return getAllProductions().stream()
            .filter(config -> config.getCategory() == category)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerProduction(ProductionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        ProductionRegistry.register(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterProduction(String productionId) {
        if (productionId == null) {
            throw new IllegalArgumentException("productionId cannot be null");
        }
        return ProductionRegistry.unregister(productionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProductionCount() {
        return ProductionRegistry.getProductionCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startProduction(BlockPos position, String productionId) {
        if (position == null || productionId == null) {
            throw new IllegalArgumentException("position and productionId cannot be null");
        }
        // Note: Actual implementation would require access to Level and BlockEntity
        // This is a placeholder that would need to be implemented with proper BlockEntity access
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stopProduction(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        // Note: Actual implementation would require access to Level and BlockEntity
        // This is a placeholder that would need to be implemented with proper BlockEntity access
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getProductionProgress(BlockPos position) {
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        // Note: Actual implementation would require access to Level and BlockEntity
        // This is a placeholder that would need to be implemented with proper BlockEntity access
        return -1.0;
    }
}
