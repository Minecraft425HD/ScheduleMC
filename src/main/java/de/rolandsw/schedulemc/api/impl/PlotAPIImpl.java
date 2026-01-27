package de.rolandsw.schedulemc.api.impl;

import de.rolandsw.schedulemc.api.plot.IPlotAPI;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.PlotType;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of IPlotAPI
 *
 * Wrapper für PlotManager mit vollständiger Thread-Safety.
 *
 * @author ScheduleMC Team
 * @version 3.1.0
 * @since 3.0.0
 */
public class PlotAPIImpl implements IPlotAPI {

    private final PlotManager plotManager;

    public PlotAPIImpl() {
        this.plotManager = PlotManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public PlotRegion getPlotAt(BlockPos pos) {
        if (pos == null) {
            throw new IllegalArgumentException("pos cannot be null");
        }
        return plotManager.getPlotAt(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public PlotRegion getPlot(String plotId) {
        if (plotId == null) {
            throw new IllegalArgumentException("plotId cannot be null");
        }
        return plotManager.getPlot(plotId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPlot(String plotId) {
        if (plotId == null) {
            throw new IllegalArgumentException("plotId cannot be null");
        }
        return plotManager.hasPlot(plotId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlotRegion> getPlotsByOwner(UUID ownerUUID) {
        if (ownerUUID == null) {
            throw new IllegalArgumentException("ownerUUID cannot be null");
        }
        return plotManager.getPlotsByOwner(ownerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlotRegion> getAvailablePlots() {
        return plotManager.getAllPlots().stream()
            .filter(plot -> plot.getOwner() == null)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlotRegion> getPlotsForSale() {
        return plotManager.getAllPlots().stream()
            .filter(PlotRegion::isForSale)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlotRegion createPlot(BlockPos pos1, BlockPos pos2, @Nullable String plotName, PlotType type, double price) {
        if (pos1 == null || pos2 == null || type == null) {
            throw new IllegalArgumentException("pos1, pos2 and type cannot be null");
        }
        if (price < 0) {
            throw new IllegalArgumentException("price must be non-negative, got: " + price);
        }
        return plotManager.createPlot(pos1, pos2, plotName, type, price);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removePlot(String plotId) {
        if (plotId == null) {
            throw new IllegalArgumentException("plotId cannot be null");
        }
        return plotManager.removePlot(plotId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPlotCount() {
        return plotManager.getPlotCount();
    }
}
