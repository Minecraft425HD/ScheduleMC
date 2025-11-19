package de.rolandsw.schedulemc.tobacco.data;

import de.rolandsw.schedulemc.tobacco.PotType;
import de.rolandsw.schedulemc.tobacco.TobaccoType;

/**
 * Speichert Daten eines Tabak-Topfes
 */
public class TobaccoPotData {
    
    private PotType potType;
    private int waterLevel; // Aktuelles Wasser
    private int soilLevel; // Aktuelle Erde
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
        return waterLevel;
    }
    
    public int getSoilLevel() {
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
     * Fügt Wasser hinzu
     */
    public void addWater(int amount) {
        waterLevel = Math.min(waterLevel + amount, getMaxWater());
    }
    
    /**
     * Verbraucht Wasser
     */
    public boolean consumeWater(double amount) {
        double actualAmount = potType.calculateWaterConsumption(amount);
        if (waterLevel >= actualAmount) {
            waterLevel -= (int) actualAmount;
            return true;
        }
        return false;
    }
    
    /**
     * Verbraucht Erde
     */
    public boolean consumeSoil(double amount) {
        double actualAmount = potType.calculateSoilConsumption(amount);
        if (soilLevel >= actualAmount) {
            soilLevel -= (int) actualAmount;
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
     * Prüft ob die Pflanze wachsen kann
     */
    public boolean canGrow() {
        if (plant == null || plant.isFullyGrown()) {
            return false;
        }

        // Braucht Wasser und Erde (reduzierte Werte wie in tick())
        double waterNeeded = plant.getType().getWaterConsumption() * 0.15;
        double soilNeeded = 0.3;

        return waterLevel >= potType.calculateWaterConsumption(waterNeeded) &&
               soilLevel >= potType.calculateSoilConsumption(soilNeeded);
    }
    
    /**
     * Lässt die Pflanze wachsen (tick)
     */
    public void tick() {
        if (plant == null || plant.isFullyGrown()) {
            return;
        }

        if (canGrow()) {
            // Verbrauche Ressourcen (reduziert für längeres Wachstum)
            consumeWater(plant.getType().getWaterConsumption() * 0.15);
            consumeSoil(0.3);

            // Pflanze wachsen lassen
            plant.tick();
        }
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
