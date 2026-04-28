package de.rolandsw.schedulemc.multiblock;

/**
 * Markierungsinterface für Blöcke, die einen Multiblock-Boost liefern können.
 *
 * Implementierende Blöcke werden von {@link MultiblockHelper#scanForBoost} erkannt.
 * Die boosted BlockEntity muss {@code MultiblockHelper.scanForBoost()} in ihrem
 * {@code tick()} aufrufen, um den Multiplikator zu aktualisieren.
 *
 * Verwendungsbeispiel (Booster-Block):
 * <pre>
 *   public class FanBlock extends HorizontalDirectionalBlock implements IMultiblockBooster {
 *       {@literal @}Override public float getBoostMultiplier() { return 2.5f; }
 *   }
 * </pre>
 *
 * Verwendungsbeispiel (Empfänger-BlockEntity):
 * <pre>
 *   float boost = MultiblockHelper.scanForBoost(level, worldPosition, 4);
 *   int boostedTicks = Math.round(rawTicks * boost);
 * </pre>
 */
public interface IMultiblockBooster {

    /**
     * Gibt den Geschwindigkeitsmultiplikator dieses Boosters zurück.
     *
     * Wert 1.0 = kein Boost (Base), 2.0 = doppelte Geschwindigkeit usw.
     * Mehrere Booster addieren ihren Bonus auf den Basiswert:
     *   totalMultiplier = 1.0 + Σ(booster.getBoostMultiplier() - 1.0)
     *
     * @return Multiplikator ≥ 1.0
     */
    float getBoostMultiplier();
}
