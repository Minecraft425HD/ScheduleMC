package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.production.IProductionAPI;
import de.rolandsw.schedulemc.production.config.ProductionConfig;
import de.rolandsw.schedulemc.production.config.ProductionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ProductionRegistry productionRegistry;

    public ProductionAPIImpl() {
        this.productionRegistry = ProductionRegistry.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ProductionConfig getProduction(String productionId) {
        if (productionId == null) {
            throw new IllegalArgumentException("productionId cannot be null");
        }
        return productionRegistry.get(productionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProduction(String productionId) {
        if (productionId == null) {
            throw new IllegalArgumentException("productionId cannot be null");
        }
        return productionRegistry.has(productionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ProductionConfig> getAllProductions() {
        return new ArrayList<>(productionRegistry.getAll());
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
        productionRegistry.register(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterProduction(String productionId) {
        if (productionId == null) {
            throw new IllegalArgumentException("productionId cannot be null");
        }
        return productionRegistry.unregister(productionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProductionCount() {
        return productionRegistry.getCount();
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

    // ═══════════════════════════════════════════════════════════
    // EXTENDED API v3.2.0 - Enhanced External Configurability
    // ═══════════════════════════════════════════════════════════

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAllProductionIds() {
        return getAllProductions().stream()
            .map(ProductionConfig::getId)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductionConfig.ProductionCategory[] getCategories() {
        return ProductionConfig.ProductionCategory.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setProductionBasePrice(String productionId, double newBasePrice) {
        if (productionId == null) {
            throw new IllegalArgumentException("productionId cannot be null");
        }
        if (newBasePrice < 0) {
            throw new IllegalArgumentException("newBasePrice must be non-negative, got: " + newBasePrice);
        }
        ProductionConfig config = productionRegistry.get(productionId);
        if (config == null) {
            return false;
        }
        // ProductionConfig is immutable - base price cannot be changed at runtime
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProductionStatistics() {
        Collection<ProductionConfig> allProductions = getAllProductions();
        int totalCount = allProductions.size();

        Map<ProductionConfig.ProductionCategory, Long> countByCategory = allProductions.stream()
            .collect(Collectors.groupingBy(ProductionConfig::getCategory, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Production Statistics: %d total productions\n", totalCount));
        for (ProductionConfig.ProductionCategory category : ProductionConfig.ProductionCategory.values()) {
            long count = countByCategory.getOrDefault(category, 0L);
            sb.append(String.format("  %s: %d\n", category.name(), count));
        }
        return sb.toString().trim();
    }
}
