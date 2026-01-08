package de.rolandsw.schedulemc.mushroom.blockentity;

import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.mushroom.blocks.KlimalampeBlock;
import de.rolandsw.schedulemc.mushroom.blocks.KlimalampeTier;
import de.rolandsw.schedulemc.mushroom.blocks.TemperatureMode;
import de.rolandsw.schedulemc.production.blockentity.PlantPotBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity für Klimalampe - reguliert Temperatur für benachbarte Töpfe
 */
public class KlimalampeBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;
    private final KlimalampeTier tier;
    private int tickCounter = 0;

    public KlimalampeBlockEntity(BlockPos pos, BlockState state, KlimalampeTier tier) {
        super(MushroomBlockEntities.KLIMALAMPE.get(), pos, state);
        this.tier = tier;
    }

    public KlimalampeBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, KlimalampeTier.SMALL);
    }

    public KlimalampeTier getTier() {
        return tier;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        // Nur alle 20 Ticks prüfen (1x pro Sekunde)
        if (tickCounter < 20) return;
        tickCounter = 0;

        // Bei automatischen Lampen: Modus basierend auf benachbarten Pilzen anpassen
        if (tier.isAutomatic()) {
            autoAdjustMode();
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    /**
     * Automatische Modus-Anpassung basierend auf benachbarten Pilzkulturen
     */
    private void autoAdjustMode() {
        if (level == null) return;

        BlockState state = getBlockState();
        TemperatureMode currentMode = state.getValue(KlimalampeBlock.MODE);

        // Prüfe benachbarte Töpfe auf Pilzkulturen
        TemperatureMode neededMode = detectNeededMode();

        // Nur ändern wenn nötig
        if (neededMode != currentMode) {
            level.setBlock(worldPosition, state.setValue(KlimalampeBlock.MODE, neededMode), 3);
        }
    }

    /**
     * Erkennt welcher Modus für benachbarte Pilze benötigt wird
     */
    private TemperatureMode detectNeededMode() {
        if (level == null) return TemperatureMode.OFF;

        // Prüfe alle horizontalen Nachbarn
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = worldPosition.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);

            if (be instanceof PlantPotBlockEntity potBE) {
                var potData = potBE.getPotData();

                // Prüfe ob Pilz im Topf
                if (potData.hasMushroomPlant()) {
                    var mushroom = potData.getMushroomPlant();

                    // Azurescens braucht Kälte
                    if (mushroom.getType() == MushroomType.AZURESCENS) {
                        return TemperatureMode.COLD;
                    }

                    // Mexicana braucht Wärme
                    if (mushroom.getType() == MushroomType.MEXICANA) {
                        return TemperatureMode.WARM;
                    }

                    // Cubensis ist neutral
                    return TemperatureMode.OFF;
                }
            }
        }

        return TemperatureMode.OFF;
    }

    /**
     * Gibt aktuellen Temperatur-Modus zurück
     */
    public TemperatureMode getCurrentMode() {
        return getBlockState().getValue(KlimalampeBlock.MODE);
    }

    /**
     * Gibt Wachstumsbonus für diesen Tier zurück
     */
    public double getGrowthBonus() {
        return tier.getGrowthBonus();
    }

    /**
     * Gibt Qualitätsbonus für diesen Tier zurück
     */
    public double getQualityBonus() {
        return tier.getQualityBonus();
    }

    @Override
    public boolean isActivelyConsuming() {
        // Lampe verbraucht Strom, wenn sie nicht im OFF-Modus ist
        return getCurrentMode() != TemperatureMode.OFF;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Tier", tier.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // Tier wird im Konstruktor gesetzt
    }
}
