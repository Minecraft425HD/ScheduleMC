package de.rolandsw.schedulemc.npc.crime.prison;

import de.rolandsw.schedulemc.region.PlotArea;
import net.minecraft.core.BlockPos;

import java.util.UUID;

/**
 * Gefängniszelle - Erweiterung von PlotArea mit Gefängnis-spezifischen Features
 */
public class PrisonCell extends PlotArea {

    private int cellNumber;
    private int securityLevel;
    private UUID currentInmate;
    private long inmateSince;
    private long releaseTime;

    public PrisonCell(String id, int cellNumber, String parentPlotId,
                      BlockPos minCorner, BlockPos maxCorner, int securityLevel) {
        super(id, "Zelle " + cellNumber, parentPlotId, minCorner, maxCorner, 0);

        this.cellNumber = cellNumber;
        this.securityLevel = Math.min(5, Math.max(1, securityLevel));
        this.currentInmate = null;
        this.inmateSince = 0;
        this.releaseTime = 0;

        this.setForRent(false);
    }

    public boolean isOccupied() {
        return currentInmate != null;
    }

    public boolean isFree() {
        return currentInmate == null;
    }

    public void assignInmate(UUID inmate, long releaseTime) {
        this.currentInmate = inmate;
        this.inmateSince = System.currentTimeMillis();
        this.releaseTime = releaseTime;
    }

    public void releaseInmate() {
        this.currentInmate = null;
        this.inmateSince = 0;
        this.releaseTime = 0;
    }

    public BlockPos getSpawnPosition() {
        return new BlockPos(
            (getMinCorner().getX() + getMaxCorner().getX()) / 2,
            getMinCorner().getY() + 1,
            (getMinCorner().getZ() + getMaxCorner().getZ()) / 2
        );
    }

    public int getCellNumber() { return cellNumber; }
    public int getSecurityLevel() { return securityLevel; }
    public UUID getCurrentInmate() { return currentInmate; }
    public long getInmateSince() { return inmateSince; }
    public long getReleaseTime() { return releaseTime; }

    public void setSecurityLevel(int level) {
        this.securityLevel = Math.min(5, Math.max(1, level));
    }

    public void setCellNumber(int cellNumber) {
        this.cellNumber = cellNumber;
    }

    @Override
    public String toString() {
        String status = isOccupied() ? "BELEGT" : "FREI";
        return String.format("Zelle[Nr=%d, Sicherheit=%d, Status=%s]",
            cellNumber, securityLevel, status);
    }
}
