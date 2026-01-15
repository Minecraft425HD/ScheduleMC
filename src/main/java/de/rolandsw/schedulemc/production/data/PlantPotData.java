package de.rolandsw.schedulemc.production.data;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.data.CannabisPlantData;
import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.data.CocaPlantData;
import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.mushroom.data.MushroomPlantData;
import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.data.PoppyPlantData;
import de.rolandsw.schedulemc.production.core.PotType;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPlantData;

/**
 * Speichert Daten eines universellen Pflanzen-Topfes
 * Unterstützt: Tabak, Cannabis, Koka, Mohn und Pilze
 *
 * Ressourcen-System:
 * - PFLANZEN (Tabak/Cannabis/Koka/Mohn): Verwenden ERDE
 *   - Jede Pflanze verbraucht 33 Erde während des Wachstums
 *   - Resterde bleibt im Topf für die nächste Pflanze
 *   - Erdsäcke fügen immer 33 Erde hinzu
 * - PILZE: Verwenden MIST statt Erde
 *   - Mistsäcke fügen Substrat hinzu (kein Erdsack!)
 *   - Mist wird beim Impfen mit Sporen benötigt
 */
public class PlantPotData {

    private PotType potType;
    private double waterLevel;
    private double soilLevel;
    private TobaccoPlantData plant;
    private CannabisPlantData cannabisPlant;
    private CocaPlantData cocaPlant;
    private PoppyPlantData poppyPlant;
    private MushroomPlantData mushroomPlant;
    private boolean hasSoil;
    private boolean hasMist;

    public PlantPotData(PotType potType) {
        this.potType = potType;
        this.waterLevel = 0;
        this.soilLevel = 0;
        this.plant = null;
        this.cannabisPlant = null;
        this.cocaPlant = null;
        this.poppyPlant = null;
        this.mushroomPlant = null;
        this.hasSoil = false;
        this.hasMist = false;
    }

    public PotType getPotType() {
        return potType;
    }

    public int getWaterLevel() {
        return (int) Math.ceil(waterLevel);
    }

    public int getSoilLevel() {
        return (int) Math.ceil(soilLevel);
    }

    public double getWaterLevelExact() {
        return waterLevel;
    }

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

    public CannabisPlantData getCannabisPlant() {
        return cannabisPlant;
    }

    public CocaPlantData getCocaPlant() {
        return cocaPlant;
    }

    public PoppyPlantData getPoppyPlant() {
        return poppyPlant;
    }

    public MushroomPlantData getMushroomPlant() {
        return mushroomPlant;
    }

    public boolean hasPlant() {
        return plant != null || cannabisPlant != null || cocaPlant != null || poppyPlant != null || mushroomPlant != null;
    }

    public boolean hasTobaccoPlant() {
        return plant != null;
    }

    public boolean hasCannabisPlant() {
        return cannabisPlant != null;
    }

    public boolean hasCocaPlant() {
        return cocaPlant != null;
    }

    public boolean hasPoppyPlant() {
        return poppyPlant != null;
    }

    public boolean hasMushroomPlant() {
        return mushroomPlant != null;
    }

    public boolean hasSoil() {
        return hasSoil;
    }

    public boolean hasMist() {
        return hasMist;
    }

    public void setMist(boolean hasMist) {
        this.hasMist = hasMist;
    }

    /**
     * Fügt Mist für eine bestimmte Anzahl von Pilzkulturen hinzu
     */
    public void addMistForPlants(int plantsPerBag) {
        this.hasMist = true;
        int baseMistPerPlant = PotType.SOIL_PER_PLANT;
        int targetMist = baseMistPerPlant * plantsPerBag;
        this.soilLevel = Math.min(soilLevel + targetMist, potType.getSoilCapacity());
    }

    public void setSoil(boolean hasSoil) {
        this.hasSoil = hasSoil;
        if (!hasSoil) {
            this.soilLevel = 0;
        }
    }

    /**
     * Fügt 33 Erde aus einem Erdsack hinzu (genug für 1 Pflanze)
     * @return true wenn erfolgreich, false wenn nicht genug Platz
     */
    public boolean addSoilFromBag() {
        if (!canAddSoilBag()) {
            return false;
        }

        this.hasSoil = true;
        this.soilLevel = Math.min(soilLevel + PotType.SOIL_PER_PLANT, potType.getSoilCapacity());
        return true;
    }

    /**
     * Prüft ob ein Erdsack hinzugefügt werden kann
     * - Nicht während eine Pflanze wächst
     * - Nur wenn mindestens 33 Erde fehlt
     */
    public boolean canAddSoilBag() {
        // Nicht während Pflanze wächst
        if (hasPlant()) {
            return false;
        }

        // Prüfe ob genug Platz für 33 Erde
        double missingSoil = potType.getSoilCapacity() - soilLevel;
        return missingSoil >= PotType.SOIL_PER_PLANT;
    }

    /**
     * Gibt die fehlende Erde-Menge zurück
     */
    public double getMissingSoil() {
        return potType.getSoilCapacity() - soilLevel;
    }

    /**
     * @deprecated Use addSoilFromBag() instead
     */
    @Deprecated
    public void addSoilForPlants(int plantsPerBag) {
        // Alte Methode für Kompatibilität - fügt jetzt immer 33 hinzu
        addSoilFromBag();
    }

    public void setWaterLevel(double waterLevel) {
        this.waterLevel = Math.max(0, Math.min(waterLevel, getMaxWater()));
    }

    public void setSoilLevel(double soilLevel) {
        this.soilLevel = Math.max(0, Math.min(soilLevel, getMaxSoil()));
        this.hasSoil = soilLevel > 0;
    }

    public void addWater(int amount) {
        waterLevel = Math.min(waterLevel + amount, getMaxWater());
    }

    /**
     * Verbraucht Wasser (präzise Berechnung mit double)
     */
    public boolean consumeWater(double amount) {
        double actualAmount = potType.calculateWaterConsumption(amount);
        if (waterLevel >= actualAmount) {
            waterLevel -= actualAmount;
            return true;
        }
        return false;
    }

    /**
     * Verbraucht Erde (präzise Berechnung mit double)
     * Erde wird NICHT auf 0 gesetzt wenn aufgebraucht - Resterde bleibt!
     */
    public boolean consumeSoil(double amount) {
        double actualAmount = potType.calculateSoilConsumption(amount);
        if (soilLevel >= actualAmount) {
            soilLevel -= actualAmount;
            // hasSoil bleibt true solange soilLevel > 0
            if (soilLevel <= 0.01) {
                hasSoil = false;
                soilLevel = 0;
            }
            return true;
        }
        return false;
    }

    /**
     * Prüft ob genug Erde für eine neue Pflanze vorhanden ist (mindestens 33)
     */
    public boolean hasEnoughSoilForPlant() {
        return soilLevel >= PotType.SOIL_PER_PLANT;
    }

    /**
     * Pflanzt Tabak-Samen
     */
    public boolean plantSeed(TobaccoType type) {
        if (!hasSoil || !hasEnoughSoilForPlant() || hasPlant()) {
            return false;
        }

        this.plant = new TobaccoPlantData(type);
        return true;
    }

    /**
     * Pflanzt Cannabis-Samen
     */
    public boolean plantCannabisSeed(CannabisStrain strain) {
        if (!hasSoil || !hasEnoughSoilForPlant() || hasPlant()) {
            return false;
        }

        this.cannabisPlant = new CannabisPlantData(strain);
        return true;
    }

    /**
     * Pflanzt Koka-Samen
     */
    public boolean plantCocaSeed(CocaType type) {
        if (!hasSoil || !hasEnoughSoilForPlant() || hasPlant()) {
            return false;
        }

        this.cocaPlant = new CocaPlantData(type);
        return true;
    }

    /**
     * Pflanzt Mohn-Samen
     */
    public boolean plantPoppySeed(PoppyType type) {
        if (!hasSoil || !hasEnoughSoilForPlant() || hasPlant()) {
            return false;
        }

        this.poppyPlant = new PoppyPlantData(type);
        return true;
    }

    /**
     * Impft Mist mit Pilzsporen
     */
    public boolean plantMushroomSpore(MushroomType type) {
        if (!hasMist || hasPlant()) {
            return false;
        }

        this.mushroomPlant = new MushroomPlantData(type);
        return true;
    }

    /**
     * Erntet die Tabak-Pflanze
     * WICHTIG: Resterde bleibt erhalten!
     */
    public TobaccoPlantData harvest() {
        if (plant == null || !plant.isFullyGrown()) {
            return null;
        }

        TobaccoPlantData harvested = plant;
        plant = null;
        // Resterde bleibt im Topf!
        return harvested;
    }

    /**
     * Erntet die Cannabis-Pflanze
     * WICHTIG: Resterde bleibt erhalten!
     */
    public CannabisPlantData harvestCannabis() {
        if (cannabisPlant == null || !cannabisPlant.isFullyGrown()) {
            return null;
        }

        CannabisPlantData harvested = cannabisPlant;
        cannabisPlant = null;
        // Resterde bleibt im Topf!
        return harvested;
    }

    /**
     * Erntet die Koka-Pflanze
     * WICHTIG: Resterde bleibt erhalten!
     */
    public CocaPlantData harvestCoca() {
        if (cocaPlant == null || !cocaPlant.isFullyGrown()) {
            return null;
        }

        CocaPlantData harvested = cocaPlant;
        cocaPlant = null;
        // Resterde bleibt im Topf!
        return harvested;
    }

    /**
     * Erntet die Mohn-Pflanze
     * WICHTIG: Resterde bleibt erhalten!
     */
    public PoppyPlantData harvestPoppy() {
        if (poppyPlant == null || !poppyPlant.isFullyGrown()) {
            return null;
        }

        PoppyPlantData harvested = poppyPlant;
        poppyPlant = null;
        // Resterde bleibt im Topf!
        return harvested;
    }

    /**
     * Erntet Pilze (mit Flush-System)
     */
    public MushroomPlantData harvestMushroom() {
        if (mushroomPlant == null || !mushroomPlant.canHarvest()) {
            return null;
        }

        MushroomPlantData harvested = new MushroomPlantData(mushroomPlant.getType());
        harvested.setQuality(mushroomPlant.getQuality());
        harvested.setGrowthStage(mushroomPlant.getGrowthStage());

        boolean moreFlushes = mushroomPlant.harvest();

        if (!moreFlushes) {
            mushroomPlant = null;
            hasMist = false;
        }

        return harvested;
    }

    /**
     * Entfernt die Pflanze (ohne Ernte-Bedingungen)
     */
    public void clearPlant() {
        this.plant = null;
        this.cannabisPlant = null;
        this.cocaPlant = null;
        this.poppyPlant = null;
        this.mushroomPlant = null;
    }

    /**
     * Prüft ob die Pflanze wachsen kann (alle Pflanzentypen)
     */
    public boolean canGrow() {
        if (plant != null && !plant.isFullyGrown()) {
            return checkResourcesForGrowth();
        }

        if (cannabisPlant != null && !cannabisPlant.isFullyGrown()) {
            return checkResourcesForGrowth();
        }

        if (cocaPlant != null && !cocaPlant.isFullyGrown()) {
            return checkResourcesForGrowth();
        }

        if (poppyPlant != null && !poppyPlant.isFullyGrown()) {
            return checkResourcesForGrowth();
        }

        if (mushroomPlant != null && !mushroomPlant.isFullyGrown()) {
            return checkResourcesForMushroomGrowth();
        }

        return false;
    }

    /**
     * Prüft ob Ressourcen für Pilzwachstum vorhanden sind
     */
    private boolean checkResourcesForMushroomGrowth() {
        if (mushroomPlant.isIncubating()) {
            return soilLevel > 0.5;
        }

        double waterNeeded = getMaxWater() / 7.0;
        double soilNeeded = getSoilConsumptionPerStage();
        double tolerance = 0.5;

        return waterLevel >= (potType.calculateWaterConsumption(waterNeeded) - tolerance) &&
                soilLevel >= (potType.calculateSoilConsumption(soilNeeded) - tolerance);
    }

    /**
     * Prüft ob genug Ressourcen für Wachstum vorhanden sind
     */
    private boolean checkResourcesForGrowth() {
        double waterNeeded = getMaxWater() / 7.0;
        double soilNeeded = getSoilConsumptionPerStage();
        double tolerance = 0.5;

        return waterLevel >= (potType.calculateWaterConsumption(waterNeeded) - tolerance) &&
                soilLevel >= (potType.calculateSoilConsumption(soilNeeded) - tolerance);
    }

    /**
     * Berechnet Erde-Verbrauch pro Wachstumsstufe
     * 33 Erde / 7 Stufen = ~4.71 pro Stufe
     */
    public double getSoilConsumptionPerStage() {
        return (double) PotType.SOIL_PER_PLANT / 7.0;
    }

    public float getWaterPercentage() {
        return (float) waterLevel / getMaxWater();
    }

    public float getSoilPercentage() {
        return (float) soilLevel / getMaxSoil();
    }
}
