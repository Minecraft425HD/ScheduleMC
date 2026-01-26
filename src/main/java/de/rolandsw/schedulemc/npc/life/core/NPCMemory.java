package de.rolandsw.schedulemc.npc.life.core;

import de.rolandsw.schedulemc.npc.life.NPCLifeConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NPCMemory - Smart-Gedächtnis-System für NPCs
 *
 * Konzept:
 * - Max 10 Detail-Erinnerungen pro Spieler
 * - Bei Tagwechsel werden Erinnerungen zusammengefasst
 * - Tages-Zusammenfassungen werden gespeichert (max 30 Tage)
 * - Spieler-Profile speichern aggregierte Daten permanent
 */
public class NPCMemory {

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS (via NPCLifeConstants)
    // ═══════════════════════════════════════════════════════════

    private static final int MAX_MEMORIES_PER_PLAYER = NPCLifeConstants.Memory.MAX_MEMORIES_PER_PLAYER;
    private static final int MAX_DAILY_SUMMARIES = NPCLifeConstants.Memory.MAX_DAILY_SUMMARIES;
    private static final int MAX_PLAYER_PROFILES = NPCLifeConstants.Memory.MAX_PLAYER_PROFILES;

    // ═══════════════════════════════════════════════════════════
    // DATA STRUCTURES
    // ═══════════════════════════════════════════════════════════

    /** Detail-Erinnerungen pro Spieler (kurzfristig) */
    private final Map<UUID, List<MemoryEntry>> detailMemories = new HashMap<>();

    /** Tages-Zusammenfassungen (mittelfristig) */
    private final List<DailySummary> dailySummaries = new ArrayList<>();

    /** Spieler-Profile (permanent) */
    private final Map<UUID, PlayerProfile> playerProfiles = new HashMap<>();

    /** Letzter bekannter Tag (für Tagwechsel-Erkennung) */
    private long lastKnownDay = -1;

    // ═══════════════════════════════════════════════════════════
    // MEMORY ENTRY (Detail-Erinnerung)
    // ═══════════════════════════════════════════════════════════

    public record MemoryEntry(
        MemoryType type,
        UUID subject,
        String details,
        long gameTime,
        int importance,
        @Nullable BlockPos location
    ) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Type", type.ordinal());
            tag.putUUID("Subject", subject);
            tag.putString("Details", details);
            tag.putLong("GameTime", gameTime);
            tag.putInt("Importance", importance);
            if (location != null) {
                tag.putLong("Location", location.asLong());
            }
            return tag;
        }

        public static MemoryEntry load(CompoundTag tag) {
            return new MemoryEntry(
                MemoryType.fromOrdinal(tag.getInt("Type")),
                tag.getUUID("Subject"),
                tag.getString("Details"),
                tag.getLong("GameTime"),
                tag.getInt("Importance"),
                tag.contains("Location") ? BlockPos.of(tag.getLong("Location")) : null
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DAILY SUMMARY (Tages-Zusammenfassung)
    // ═══════════════════════════════════════════════════════════

    public static class DailySummary {
        private final long day;
        private final UUID playerId;
        private int totalInteractions;
        private int positiveInteractions;
        private int negativeInteractions;
        private int totalTradeValue;
        private String mood; // Durchschnittliche Stimmung
        private List<String> highlights; // Wichtigste 1-3 Events

        public DailySummary(long day, UUID playerId) {
            this.day = day;
            this.playerId = playerId;
            this.totalInteractions = 0;
            this.positiveInteractions = 0;
            this.negativeInteractions = 0;
            this.totalTradeValue = 0;
            this.mood = "neutral";
            this.highlights = new ArrayList<>();
        }

        public void addInteraction(MemoryEntry memory) {
            totalInteractions++;
            if (memory.type().isNegative()) {
                negativeInteractions++;
            } else {
                positiveInteractions++;
            }

            // Highlights für wichtige Erinnerungen
            if (memory.importance() >= NPCLifeConstants.Memory.IMPORTANCE_HIGHLIGHT_THRESHOLD && highlights.size() < 3) {
                highlights.add(memory.details());
            }
        }

        public void setTradeValue(int value) {
            this.totalTradeValue = value;
        }

        public void calculateMood() {
            int ratio = NPCLifeConstants.Memory.POSITIVE_NEGATIVE_RATIO;
            if (positiveInteractions > negativeInteractions * ratio) {
                mood = "positiv";
            } else if (negativeInteractions > positiveInteractions * ratio) {
                mood = "negativ";
            } else {
                mood = "neutral";
            }
        }

        // Getters
        public long getDay() { return day; }
        public UUID getPlayerId() { return playerId; }
        public int getTotalInteractions() { return totalInteractions; }
        public int getPositiveInteractions() { return positiveInteractions; }
        public int getNegativeInteractions() { return negativeInteractions; }
        public int getTotalTradeValue() { return totalTradeValue; }
        public String getMood() { return mood; }
        public List<String> getHighlights() { return highlights; }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Day", day);
            tag.putUUID("PlayerId", playerId);
            tag.putInt("TotalInteractions", totalInteractions);
            tag.putInt("PositiveInteractions", positiveInteractions);
            tag.putInt("NegativeInteractions", negativeInteractions);
            tag.putInt("TotalTradeValue", totalTradeValue);
            tag.putString("Mood", mood);

            ListTag highlightList = new ListTag();
            for (String h : highlights) {
                CompoundTag ht = new CompoundTag();
                ht.putString("Text", h);
                highlightList.add(ht);
            }
            tag.put("Highlights", highlightList);
            return tag;
        }

        public static DailySummary load(CompoundTag tag) {
            DailySummary summary = new DailySummary(tag.getLong("Day"), tag.getUUID("PlayerId"));
            summary.totalInteractions = tag.getInt("TotalInteractions");
            summary.positiveInteractions = tag.getInt("PositiveInteractions");
            summary.negativeInteractions = tag.getInt("NegativeInteractions");
            summary.totalTradeValue = tag.getInt("TotalTradeValue");
            summary.mood = tag.getString("Mood");

            ListTag highlightList = tag.getList("Highlights", Tag.TAG_COMPOUND);
            for (int i = 0; i < highlightList.size(); i++) {
                summary.highlights.add(highlightList.getCompound(i).getString("Text"));
            }
            return summary;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLAYER PROFILE (Permanent)
    // ═══════════════════════════════════════════════════════════

    public static class PlayerProfile {
        private final UUID playerId;
        private int totalTransactions;
        private long totalTradeVolume;
        private int crimeCount;
        private int helpCount;
        private long lastInteraction;
        private final Set<String> reputationTags; // "Großzügig", "Dieb", etc.

        public PlayerProfile(UUID playerId) {
            this.playerId = playerId;
            this.totalTransactions = 0;
            this.totalTradeVolume = 0;
            this.crimeCount = 0;
            this.helpCount = 0;
            this.lastInteraction = 0;
            this.reputationTags = new HashSet<>();
        }

        public void recordTransaction(int value) {
            totalTransactions++;
            totalTradeVolume += value;

            // Tags basierend auf Verhalten
            if (totalTradeVolume > NPCLifeConstants.Memory.TRADE_VOLUME_GOOD_CUSTOMER) {
                reputationTags.add(NPCLifeConstants.PlayerTags.GOOD_CUSTOMER);
            }
            if (totalTransactions > NPCLifeConstants.Memory.TRANSACTIONS_REGULAR_CUSTOMER) {
                reputationTags.add(NPCLifeConstants.PlayerTags.REGULAR_CUSTOMER);
            }
        }

        public void recordCrime() {
            crimeCount++;
            if (crimeCount >= NPCLifeConstants.Memory.CRIMES_FOR_SUSPICIOUS) {
                reputationTags.add(NPCLifeConstants.PlayerTags.SUSPICIOUS);
            }
            if (crimeCount >= NPCLifeConstants.Memory.CRIMES_FOR_CRIMINAL) {
                reputationTags.add(NPCLifeConstants.PlayerTags.CRIMINAL);
            }
            if (crimeCount >= NPCLifeConstants.Memory.CRIMES_FOR_DANGEROUS) {
                reputationTags.add(NPCLifeConstants.PlayerTags.DANGEROUS);
            }
        }

        public void recordHelp() {
            helpCount++;
            if (helpCount >= NPCLifeConstants.Memory.HELPS_FOR_HELPFUL) {
                reputationTags.add(NPCLifeConstants.PlayerTags.HELPFUL);
            }
            if (helpCount >= NPCLifeConstants.Memory.HELPS_FOR_BENEFACTOR) {
                reputationTags.add(NPCLifeConstants.PlayerTags.BENEFACTOR);
            }
        }

        public void updateLastInteraction(long gameTime) {
            this.lastInteraction = gameTime;
        }

        public boolean hasTag(String tag) {
            return reputationTags.contains(tag);
        }

        /**
         * Berechnet das Beziehungslevel basierend auf Interaktionen
         * @return Wert von -100 (Feind) bis +100 (Freund)
         */
        public int getRelationLevel() {
            int level = 0;

            // Positive Faktoren
            level += Math.min(30, totalTransactions * 2);
            level += Math.min(40, helpCount * 10);
            level += (totalTradeVolume > 10000) ? 20 : (int)(totalTradeVolume / 500);

            // Negative Faktoren
            level -= crimeCount * 15;

            // Tags berücksichtigen
            if (hasTag(NPCLifeConstants.PlayerTags.GOOD_CUSTOMER)) level += 10;
            if (hasTag(NPCLifeConstants.PlayerTags.REGULAR_CUSTOMER)) level += 15;
            if (hasTag(NPCLifeConstants.PlayerTags.HELPFUL)) level += 20;
            if (hasTag(NPCLifeConstants.PlayerTags.BENEFACTOR)) level += 25;
            if (hasTag(NPCLifeConstants.PlayerTags.SUSPICIOUS)) level -= 15;
            if (hasTag(NPCLifeConstants.PlayerTags.CRIMINAL)) level -= 30;
            if (hasTag(NPCLifeConstants.PlayerTags.DANGEROUS)) level -= 50;

            return Math.max(-100, Math.min(100, level));
        }

        // Getters
        public UUID getPlayerId() { return playerId; }
        public int getTotalTransactions() { return totalTransactions; }
        public long getTotalTradeVolume() { return totalTradeVolume; }
        public int getCrimeCount() { return crimeCount; }
        public int getHelpCount() { return helpCount; }
        public long getLastInteraction() { return lastInteraction; }
        public Set<String> getReputationTags() { return Collections.unmodifiableSet(reputationTags); }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("PlayerId", playerId);
            tag.putInt("TotalTransactions", totalTransactions);
            tag.putLong("TotalTradeVolume", totalTradeVolume);
            tag.putInt("CrimeCount", crimeCount);
            tag.putInt("HelpCount", helpCount);
            tag.putLong("LastInteraction", lastInteraction);

            ListTag tagsList = new ListTag();
            for (String t : reputationTags) {
                CompoundTag tt = new CompoundTag();
                tt.putString("Tag", t);
                tagsList.add(tt);
            }
            tag.put("ReputationTags", tagsList);
            return tag;
        }

        public static PlayerProfile load(CompoundTag tag) {
            PlayerProfile profile = new PlayerProfile(tag.getUUID("PlayerId"));
            profile.totalTransactions = tag.getInt("TotalTransactions");
            profile.totalTradeVolume = tag.getLong("TotalTradeVolume");
            profile.crimeCount = tag.getInt("CrimeCount");
            profile.helpCount = tag.getInt("HelpCount");
            profile.lastInteraction = tag.getLong("LastInteraction");

            ListTag tagsList = tag.getList("ReputationTags", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagsList.size(); i++) {
                profile.reputationTags.add(tagsList.getCompound(i).getString("Tag"));
            }
            return profile;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // REMEMBER (Erinnerung speichern)
    // ═══════════════════════════════════════════════════════════

    /**
     * Speichert eine neue Erinnerung
     */
    public void remember(MemoryType type, UUID subject, String details, int importance) {
        remember(type, subject, details, importance, null, 0);
    }

    /**
     * Speichert eine neue Erinnerung mit Ort
     */
    public void remember(MemoryType type, UUID subject, String details, int importance, @Nullable BlockPos location) {
        remember(type, subject, details, importance, location, 0);
    }

    /**
     * Speichert eine neue Erinnerung
     */
    public void remember(MemoryType type, UUID subject, String details, int importance,
                         @Nullable BlockPos location, long gameTime) {

        // Detail-Erinnerung erstellen
        MemoryEntry entry = new MemoryEntry(type, subject, details, gameTime, importance, location);

        // Zur Liste hinzufügen
        List<MemoryEntry> memories = detailMemories.computeIfAbsent(subject, k -> new ArrayList<>());

        // Wenn voll, älteste/unwichtigste entfernen
        if (memories.size() >= MAX_MEMORIES_PER_PLAYER) {
            // Sortiere nach Wichtigkeit (aufsteigend) und entferne die unwichtigste
            memories.sort(Comparator.comparingInt(MemoryEntry::importance));
            memories.remove(0);
        }

        memories.add(entry);

        // Spieler-Profil aktualisieren
        PlayerProfile profile = getOrCreateProfile(subject);
        profile.updateLastInteraction(gameTime);

        if (type == MemoryType.TRANSACTION) {
            // Trade-Wert aus Details extrahieren (falls vorhanden)
            try {
                int value = Integer.parseInt(details.replaceAll("[^0-9]", ""));
                profile.recordTransaction(value);
            } catch (NumberFormatException ignored) {
                profile.recordTransaction(0);
            }
        } else if (type == MemoryType.CRIME_WITNESSED || type == MemoryType.CRIME_VICTIM) {
            profile.recordCrime();
        } else if (type == MemoryType.HELP_RECEIVED) {
            profile.recordHelp();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // QUERY (Erinnerungen abfragen)
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob NPC etwas über einen Spieler weiß
     */
    public boolean knows(MemoryType type, UUID subject) {
        List<MemoryEntry> memories = detailMemories.get(subject);
        if (memories != null) {
            return memories.stream().anyMatch(m -> m.type() == type);
        }
        return false;
    }

    /**
     * Gibt alle Erinnerungen über einen Spieler zurück
     */
    public List<MemoryEntry> getMemoriesAbout(UUID subject) {
        return detailMemories.getOrDefault(subject, Collections.emptyList());
    }

    /**
     * Gibt Erinnerungen eines bestimmten Typs zurück
     */
    public List<MemoryEntry> getMemoriesOfType(MemoryType type) {
        return detailMemories.values().stream()
            .flatMap(List::stream)
            .filter(m -> m.type() == type)
            .collect(Collectors.toList());
    }

    /**
     * Gibt das Spieler-Profil zurück (oder null)
     */
    @Nullable
    public PlayerProfile getPlayerProfile(UUID playerId) {
        return playerProfiles.get(playerId);
    }

    /**
     * Gibt das Spieler-Profil zurück oder erstellt ein neues
     */
    public PlayerProfile getOrCreateProfile(UUID playerId) {
        return playerProfiles.computeIfAbsent(playerId, PlayerProfile::new);
    }

    /**
     * Prüft ob ein Spieler einen bestimmten Ruf-Tag hat
     */
    public boolean playerHasTag(UUID playerId, String tag) {
        PlayerProfile profile = playerProfiles.get(playerId);
        return profile != null && profile.hasTag(tag);
    }

    // ═══════════════════════════════════════════════════════════
    // DAY CYCLE (Tagwechsel)
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird beim Tagwechsel aufgerufen - fasst Erinnerungen zusammen
     */
    public void onDayChange(long currentDay) {
        if (lastKnownDay == -1) {
            lastKnownDay = currentDay;
            return;
        }

        // Für jeden Spieler Zusammenfassung erstellen
        for (Map.Entry<UUID, List<MemoryEntry>> entry : detailMemories.entrySet()) {
            UUID playerId = entry.getKey();
            List<MemoryEntry> memories = entry.getValue();

            if (!memories.isEmpty()) {
                DailySummary summary = new DailySummary(lastKnownDay, playerId);

                int tradeValue = 0;
                for (MemoryEntry memory : memories) {
                    summary.addInteraction(memory);

                    // Trade-Wert extrahieren
                    if (memory.type() == MemoryType.TRANSACTION) {
                        try {
                            tradeValue += Integer.parseInt(memory.details().replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                summary.setTradeValue(tradeValue);
                summary.calculateMood();

                dailySummaries.add(summary);
            }
        }

        // Detail-Erinnerungen löschen
        detailMemories.clear();

        // Alte Zusammenfassungen entfernen (älter als 30 Tage)
        dailySummaries.removeIf(s -> currentDay - s.getDay() > MAX_DAILY_SUMMARIES);

        lastKnownDay = currentDay;
    }

    /**
     * Gibt Zusammenfassungen für einen Spieler zurück
     */
    public List<DailySummary> getSummariesFor(UUID playerId) {
        return dailySummaries.stream()
            .filter(s -> s.getPlayerId().equals(playerId))
            .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // FORGET (Vergessen)
    // ═══════════════════════════════════════════════════════════

    /**
     * Vergisst alle Erinnerungen eines bestimmten Typs über einen Spieler
     */
    public void forget(MemoryType type, UUID subject) {
        List<MemoryEntry> memories = detailMemories.get(subject);
        if (memories != null) {
            memories.removeIf(m -> m.type() == type);
        }
    }

    /**
     * Vergisst alle Erinnerungen über einen Spieler
     */
    public void forgetPlayer(UUID playerId) {
        detailMemories.remove(playerId);
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("LastKnownDay", lastKnownDay);

        // Detail-Erinnerungen
        ListTag memoriesTag = new ListTag();
        for (Map.Entry<UUID, List<MemoryEntry>> entry : detailMemories.entrySet()) {
            CompoundTag playerMemories = new CompoundTag();
            playerMemories.putUUID("PlayerId", entry.getKey());

            ListTag entriesList = new ListTag();
            for (MemoryEntry memory : entry.getValue()) {
                entriesList.add(memory.save());
            }
            playerMemories.put("Entries", entriesList);
            memoriesTag.add(playerMemories);
        }
        tag.put("DetailMemories", memoriesTag);

        // Tages-Zusammenfassungen
        ListTag summariesTag = new ListTag();
        for (DailySummary summary : dailySummaries) {
            summariesTag.add(summary.save());
        }
        tag.put("DailySummaries", summariesTag);

        // Spieler-Profile
        ListTag profilesTag = new ListTag();
        for (PlayerProfile profile : playerProfiles.values()) {
            profilesTag.add(profile.save());
        }
        tag.put("PlayerProfiles", profilesTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        lastKnownDay = tag.getLong("LastKnownDay");

        // Detail-Erinnerungen
        detailMemories.clear();
        ListTag memoriesTag = tag.getList("DetailMemories", Tag.TAG_COMPOUND);
        for (int i = 0; i < memoriesTag.size(); i++) {
            CompoundTag playerMemories = memoriesTag.getCompound(i);
            UUID playerId = playerMemories.getUUID("PlayerId");

            List<MemoryEntry> entries = new ArrayList<>();
            ListTag entriesList = playerMemories.getList("Entries", Tag.TAG_COMPOUND);
            for (int j = 0; j < entriesList.size(); j++) {
                entries.add(MemoryEntry.load(entriesList.getCompound(j)));
            }
            detailMemories.put(playerId, entries);
        }

        // Tages-Zusammenfassungen
        dailySummaries.clear();
        ListTag summariesTag = tag.getList("DailySummaries", Tag.TAG_COMPOUND);
        for (int i = 0; i < summariesTag.size(); i++) {
            dailySummaries.add(DailySummary.load(summariesTag.getCompound(i)));
        }

        // Spieler-Profile
        playerProfiles.clear();
        ListTag profilesTag = tag.getList("PlayerProfiles", Tag.TAG_COMPOUND);
        for (int i = 0; i < profilesTag.size(); i++) {
            PlayerProfile profile = PlayerProfile.load(profilesTag.getCompound(i));
            playerProfiles.put(profile.getPlayerId(), profile);
        }
    }

    public static NPCMemory fromTag(CompoundTag tag) {
        NPCMemory memory = new NPCMemory();
        memory.load(tag);
        return memory;
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        int totalMemories = detailMemories.values().stream().mapToInt(List::size).sum();
        return String.format("NPCMemory{memories=%d, summaries=%d, profiles=%d}",
            totalMemories, dailySummaries.size(), playerProfiles.size());
    }

    // ═══════════════════════════════════════════════════════════
    // CONVENIENCE METHODS (API Compatibility)
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt eine Erinnerung hinzu (Alias für remember)
     */
    public void addMemory(UUID playerId, MemoryType type, String details, int importance) {
        remember(type, playerId, details, importance);
    }

    /**
     * Fügt einen Tag zum Spieler-Profil hinzu
     */
    public void addPlayerTag(UUID playerId, String tag) {
        PlayerProfile profile = getOrCreateProfile(playerId);
        profile.reputationTags.add(tag);
    }

    /**
     * Entfernt einen Tag vom Spieler-Profil
     */
    public void removePlayerTag(UUID playerId, String tag) {
        PlayerProfile profile = playerProfiles.get(playerId);
        if (profile != null) {
            profile.reputationTags.remove(tag);
        }
    }

    /**
     * Prüft ob der NPC Erinnerungen über einen Spieler hat
     */
    public boolean hasMemoryOf(UUID playerId) {
        List<MemoryEntry> memories = detailMemories.get(playerId);
        return memories != null && !memories.isEmpty();
    }

    /**
     * Prüft ob ein Spieler einen bestimmten Tag hat (Alias für playerHasTag)
     */
    public boolean hasPlayerTag(UUID playerId, String tag) {
        return playerHasTag(playerId, tag);
    }
}
