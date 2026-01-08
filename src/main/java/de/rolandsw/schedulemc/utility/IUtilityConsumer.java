package de.rolandsw.schedulemc.utility;

import javax.annotation.Nullable;

/**
 * Interface für BlockEntities die Strom/Wasser verbrauchen
 *
 * BlockEntities die dieses Interface implementieren, melden ihren
 * Aktivitätsstatus an das Utility-System, welches dann den korrekten
 * Verbrauch (aktiv vs. idle) berechnet.
 */
public interface IUtilityConsumer {

    /**
     * Gibt zurück ob dieses BlockEntity gerade aktiv arbeitet.
     *
     * Beispiele:
     * - Grow Light: aktiv wenn eingeschaltet
     * - Trocknungsgestell: aktiv wenn Items zum Trocknen drin sind
     * - Meth-Kessel: aktiv wenn Reaktion läuft
     * - Topf: immer aktiv (konstanter Wasserverbrauch)
     *
     * @return true wenn aktiv (100% Verbrauch), false wenn idle (50% Verbrauch)
     */
    boolean isActivelyConsuming();

    /**
     * Optional: Überschreibt den Registry-Verbrauch mit einem custom Wert.
     * Nützlich für Blöcke mit variablem Verbrauch (z.B. basierend auf Upgrade-Level).
     *
     * @return null um Registry-Werte zu verwenden, oder custom UtilityConsumptionData
     */
    @Nullable
    default UtilityConsumptionData getCustomConsumption() {
        return null;
    }

    /**
     * Wird aufgerufen wenn der Verbrauch abgerechnet wird.
     * Kann für Statistiken oder Events verwendet werden.
     *
     * @param electricity Abgerechneter Stromverbrauch in kWh
     * @param water Abgerechneter Wasserverbrauch in Liter
     */
    default void onUtilityBilled(double electricity, double water) {
        // Default: keine Aktion
    }
}
