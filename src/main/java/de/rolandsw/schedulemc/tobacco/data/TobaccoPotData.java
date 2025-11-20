package de.rolandsw.schedulemc.tobacco.data;

import de.rolandsw.schedulemc.tobacco.PotType;
import de.rolandsw.schedulemc.tobacco.TobaccoType;

/**
 * Speichert Daten eines Tabak-Topfes
 */
public class TobaccoPotData {
    
    private PotType potType;
    private double waterLevel; // Aktuelles Wasser (als double für präzisen Verbrauch)
    private double soilLevel; // Aktuelle Erde (als double für präzisen Verbrauch)
    private TobaccoPlantData plant; // Gepflanzte Tabakpflanze (null wenn leer)
    private boolean hasSoil; // Wurde Erde hinzugefügt?
    
    public TobaccoPotData(PotType potType) {
        this.potType = potType;
        this.waterLevel = 0;
        this.soilLevel = 0;
        this.plant = null;
        this.hasSoil = false;
    }
    
    public PotType getPotType() {
        return potType;
    }
    
    public int getWaterLevel() {
        return (int) Math.ceil(waterLevel); // Runde auf für Anzeige
    }

    public int getSoilLevel() {
        return (int) Math.ceil(soilLevel); // Runde auf für Anzeige
    }

    /**
     * Gibt exakte Wasser-Level als double zurück
     */
    public double getWaterLevelExact() {
        return waterLevel;
    }

    /**
     * Gibt exakte Erde-Level als double zurück
     */
    public double getSoilLevelExact() {
        return soilLevel;
    }
    
    public int getMaxWater() {
        return potType.getWaterCapacity();
    }
    
    public int getMaxSoil() {
        return potType.getSoilCapacity();
    }
    
    public TobaccoPlantData getPlant() {
        return plant;
    }
    
    public boolean hasPlant() {
        return plant != null;
    }
    
    public boolean hasSoil() {
        return hasSoil;
    }
    
    public void setSoil(boolean hasSoil) {
        this.hasSoil = hasSoil;
        if (hasSoil) {
            this.soilLevel = potType.getSoilCapacity();
        } else {
            this.soilLevel = 0;
        }
    }

    /**
     * Fügt Erde für eine bestimmte Anzahl von Pflanzen hinzu
     * @param plantsPerBag Anzahl der Pflanzen, für die die Erde reichen soll
     */
    public void addSoilForPlants(int plantsPerBag) {
        this.hasSoil = true;
        // Berechne benötigte Erde für die angegebene Anzahl von Pflanzen
        // Eine Pflanze benötigt ca. 0.075 Erde pro Tick (alle 5 Ticks)
        // Bei 4 Checks pro Sekunde: 0.075 * 4 = 0.3 pro Sekunde
        // Durchschnittliche Wachstumszeit: ~140 Ticks für Virginia (bei base 700 / 5)
        // 140 * 0.075 = 10.5 Erde pro Pflanze (Basis-Verbrauch ohne Multiplikator)

        // Sicherheitsfaktor: 15 Erde pro Pflanze (Basis) vor Multiplikator
        int baseSoilPerPlant = 15;
        int targetSoil = (int) (baseSoilPerPlant * plantsPerBag / potType.getConsumptionMultiplier());

        // Addiere zur aktuellen Erde (falls schon welche vorhanden)
        this.soilLevel = Math.min(soilLevel + targetSoil, potType.getSoilCapacity());
    }

    /**
     * Setzt Wasser-Level direkt (für Deserialisierung)
     */
    public void setWaterLevel(double waterLevel) {
        this.waterLevel = Math.max(0, Math.min(waterLevel, getMaxWater()));
    }

    /**
     * Setzt Erde-Level direkt (für Deserialisierung)
     */
    public void setSoilLevel(double soilLevel) {
        this.soilLevel = Math.max(0, Math.min(soilLevel, getMaxSoil()));
        this.hasSoil = soilLevel > 0;
    }

    /**
     * Fügt Wasser hinzu
     */
    public void addWater(int amount) {
        waterLevel = Math.min(waterLevel + amount, getMaxWater());
    }

    /**
     * Verbraucht Wasser (präzise Berechnung mit double)
     */
    public boolean consumeWater(double amount) {
        double actualAmount = potType.calculateWaterConsumption(amount);
        if (waterLevel >= actualAmount) {
            waterLevel -= actualAmount; // Kein Cast mehr, präziser Verbrauch
            return true;
        }
        return false;
    }

    /**
     * Verbraucht Erde (präzise Berechnung mit double)
     */
    public boolean consumeSoil(double amount) {
        double actualAmount = potType.calculateSoilConsumption(amount);
        if (soilLevel >= actualAmount) {
            soilLevel -= actualAmount; // Kein Cast mehr, präziser Verbrauch
            // Aktualisiere hasSoil Flag wenn Erde aufgebraucht ist
            if (soilLevel <= 0.01) { // Kleine Toleranz für Floating-Point-Fehler
                hasSoil = false;
                soilLevel = 0;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Pflanzt Samen
     */
    public boolean plantSeed(TobaccoType type) {
        if (!hasSoil || hasPlant()) {
            return false;
        }
        
        this.plant = new TobaccoPlantData(type);
        return true;
    }
    
    /**
     * Erntet die Pflanze
     */
    public TobaccoPlantData harvest() {
        if (plant == null || !plant.isFullyGrown()) {
            return null;
        }

        TobaccoPlantData harvested = plant;
        plant = null;
        return harvested;
    }

    /**
     * Entfernt die Pflanze (ohne Ernte-Bedingungen)
     */
    public void clearPlant() {
        this.plant = null;
    }
    
    /**
     * Prüft ob die Pflanze wachsen kann
     */
    public boolean canGrow() {
        if (plant == null || plant.isFullyGrown()) {
            return false;
        }

        // Minimale Ressourcen-Anforderungen für einen Tick (4x kleiner, da 4x öfter gecheckt)
        double waterNeeded = plant.getType().getWaterConsumption() * 0.0375; // 0.15 / 4
        double soilNeeded = 0.075; // 0.3 / 4

        return waterLevel >= potType.calculateWaterConsumption(waterNeeded) &&
               soilLevel >= potType.calculateSoilConsumption(soilNeeded);
    }
    
    /**
     * Gibt Wasser-Prozentsatz zurück
     */
    public float getWaterPercentage() {
        return (float) waterLevel / getMaxWater();
    }
    
    /**
     * Gibt Erde-Prozentsatz zurück
     */
    public float getSoilPercentage() {
        return (float) soilLevel / getMaxSoil();
    }
}
