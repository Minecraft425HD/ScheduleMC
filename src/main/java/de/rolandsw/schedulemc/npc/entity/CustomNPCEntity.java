package de.rolandsw.schedulemc.npc.entity;

import de.rolandsw.schedulemc.managers.NPCEntityRegistry;
import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCPersonality;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.goals.MoveToHomeGoal;
import de.rolandsw.schedulemc.npc.goals.MoveToLeisureGoal;
import de.rolandsw.schedulemc.npc.goals.MoveToWorkGoal;
import de.rolandsw.schedulemc.npc.goals.PolicePatrolGoal;
import de.rolandsw.schedulemc.npc.goals.PoliceStationGoal;
import de.rolandsw.schedulemc.npc.menu.NPCInteractionMenu;
import de.rolandsw.schedulemc.npc.pathfinding.NPCPathNavigation;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * Custom NPC Entity
 * - Kein Villager
 * - Eigener Player-Skin
 * - Interaktionssystem (Dialog, Kaufen, Verkaufen)
 * - Erweiterbar für zukünftige Features
 */
public class CustomNPCEntity extends PathfinderMob {

    // Synced Data für Client-Server Kommunikation
    private static final EntityDataAccessor<String> NPC_NAME =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SKIN_FILE =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> NPC_TYPE_ORDINAL =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MERCHANT_CATEGORY_ORDINAL =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> PERSONALITY =
        SynchedEntityData.defineId(CustomNPCEntity.class, EntityDataSerializers.STRING);

    // NPC Daten (Server-Side)
    private NPCData npcData;

    // Performance-Optimierung: Player-Lookup Throttling
    private int playerLookupCounter = 0;
    private static final int PLAYER_LOOKUP_INTERVAL = 20; // Alle 20 Ticks (1 Sekunde)

    public CustomNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.npcData = new NPCData();
        this.setMaxUpStep(1.5F); // Ermöglicht das Steigen auf Blöcke und Treppen (erhöht für bessere Navigation)
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(NPC_NAME, "NPC");
        this.entityData.define(SKIN_FILE, "default.png");
        this.entityData.define(NPC_TYPE_ORDINAL, 0); // BEWOHNER
        this.entityData.define(MERCHANT_CATEGORY_ORDINAL, 0); // BAUMARKT
        this.entityData.define(PERSONALITY, NPCPersonality.AUSGEWOGEN.name()); // Standard-Persönlichkeit
    }

    @Override
    protected void registerGoals() {
        // Grundlegende AI Goals
        this.goalSelector.addGoal(0, new FloatGoal(this)); // Schwimmen
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true)); // Türen öffnen (und schließen)

        // Registriere ALLE Goals - die Goals prüfen selbst ob sie aktiv sein sollen
        // Police Goals (nur aktiv für POLIZEI NPCs)
        this.goalSelector.addGoal(2, new PolicePatrolGoal(this)); // Patrouillieren zwischen Punkten
        this.goalSelector.addGoal(3, new PoliceStationGoal(this)); // An Station bleiben (wenn keine Patrol)

        // Normal NPC Goals (nur aktiv für BEWOHNER/VERKAEUFER)
        this.goalSelector.addGoal(4, new MoveToHomeGoal(this)); // Nach Hause gehen (Heimzeit)
        this.goalSelector.addGoal(5, new MoveToWorkGoal(this)); // Zur Arbeit gehen (Arbeitszeit)
        this.goalSelector.addGoal(6, new MoveToLeisureGoal(this)); // Zu Freizeitorten gehen (Freizeit)

        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F)); // Spieler anschauen
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this)); // Zufällig umschauen
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new NPCPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Normal: Öffne Interaktions-GUI
            openInteractionMenu(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // Admin: SHIFT + Linksklick öffnet Shop-Editor (nur für Verkäufer)
        if (!this.level().isClientSide && source.getEntity() instanceof ServerPlayer serverPlayer) {
            ItemStack heldItem = serverPlayer.getMainHandItem();

            // Vehicle Spawn Tool: Linksklick auf AUTOHAENDLER NPC verknüpft das Tool
            if (heldItem.getItem() instanceof de.rolandsw.schedulemc.vehicle.items.VehicleSpawnTool) {
                if (getMerchantCategory() == de.rolandsw.schedulemc.npc.data.MerchantCategory.AUTOHAENDLER) {
                    de.rolandsw.schedulemc.vehicle.items.VehicleSpawnTool.linkToDealer(heldItem, getNpcData().getNpcUUID(), serverPlayer);
                    return false; // Verhindere Schaden
                } else {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("⚠ Dieser NPC ist kein Autohändler!")
                        .withStyle(net.minecraft.ChatFormatting.RED));
                    return false;
                }
            }

            // Shop-Editor für Admins
            if (serverPlayer.isShiftKeyDown() && serverPlayer.hasPermissions(2)) {
                if (getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.VERKAEUFER) {
                    openShopEditor(serverPlayer);
                    return false; // Verhindere Schaden
                }
            }
        }

        // Normal: Kein Schaden (NPCs sind invulnerable)
        return super.hurt(source, amount);
    }

    /**
     * Öffnet das Interaktionsmenü für den Spieler
     */
    private void openInteractionMenu(ServerPlayer player) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
            (id, playerInventory, p) -> new NPCInteractionMenu(id, playerInventory, this),
            Component.literal(this.getNpcName())
        ), buf -> {
            buf.writeInt(this.getId()); // Entity ID für Client-Side
        });
    }

    /**
     * Öffnet den Shop-Editor für Admins (nur Verkäufer)
     */
    private void openShopEditor(ServerPlayer player) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
            (id, playerInventory, p) -> new de.rolandsw.schedulemc.npc.menu.ShopEditorMenu(id, playerInventory, this),
            Component.literal("Shop Editor: " + this.getNpcName())
        ), buf -> {
            buf.writeInt(this.getId()); // Entity ID
            // Sende aktuelle Shop-Items
            var shopItems = this.getNpcData().getBuyShop().getEntries();
            buf.writeInt(shopItems.size());
            for (var entry : shopItems) {
                buf.writeItem(entry.getItem());
                buf.writeInt(entry.getPrice());
                buf.writeBoolean(entry.isUnlimited());
                buf.writeInt(entry.getStock());
            }
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Server-Side Logic

            // Look at nearest player (Throttled für Performance)
            playerLookupCounter++;
            if (playerLookupCounter >= PLAYER_LOOKUP_INTERVAL) {
                playerLookupCounter = 0;

                if (npcData.getBehavior().shouldLookAtPlayer()) {
                    Player nearestPlayer = this.level().getNearestPlayer(this, 8.0D);
                    if (nearestPlayer != null) {
                        this.getLookControl().setLookAt(nearestPlayer, 10.0F, 10.0F);
                    }
                }
            }

            // Tägliches Einkommen wird jetzt global durch NPCDailySalaryHandler verwaltet
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("NPCData", npcData.save(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("NPCData")) {
            npcData.load(tag.getCompound("NPCData"));
            // Sync to client
            syncToClient();
        }
    }

    /**
     * Called when entity is added to the world
     * Performance-Optimierung: Registriere im NPCEntityRegistry für O(1) UUID Lookups
     */
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        // Nur Server-Side registrieren
        if (!this.level().isClientSide && this.level() instanceof ServerLevel) {
            NPCEntityRegistry.registerNPC(this);
        }
    }

    /**
     * Synchronisiert wichtige Daten zum Client
     */
    private void syncToClient() {
        this.entityData.set(NPC_NAME, npcData.getNpcName());
        this.entityData.set(SKIN_FILE, npcData.getSkinFileName());
        this.entityData.set(NPC_TYPE_ORDINAL, npcData.getNpcType().ordinal());
        this.entityData.set(MERCHANT_CATEGORY_ORDINAL, npcData.getMerchantCategory().ordinal());

        // Personality synchronisieren
        String personalityStr = npcData.getCustomData().getString("personality");
        if (!personalityStr.isEmpty()) {
            this.entityData.set(PERSONALITY, personalityStr);
        }
    }

    /**
     * Synchronisiert nur Wallet zum Client (Performance-Optimierung)
     * Nutzt leichtgewichtiges SyncNPCBalancePacket (2-10 Bytes) statt Full Sync (500-2000 Bytes)
     */
    public void syncWalletToClient() {
        if (!level().isClientSide && level() instanceof net.minecraft.server.level.ServerLevel) {
            de.rolandsw.schedulemc.npc.network.NPCNetworkHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> this),
                new de.rolandsw.schedulemc.npc.network.SyncNPCBalancePacket(this.getId(), npcData.getWallet())
            );
        }
    }

    // Custom Name Handling
    @Override
    public Component getName() {
        return Component.literal(getNpcName());
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return Component.literal(getNpcName());
    }

    // Getters & Setters
    public NPCData getNpcData() {
        return npcData;
    }

    public void setNpcData(NPCData data) {
        this.npcData = data;
        syncToClient();
    }

    public String getNpcName() {
        return this.entityData.get(NPC_NAME);
    }

    public void setNpcName(String name) {
        this.npcData.setNpcName(name);
        this.entityData.set(NPC_NAME, name);
    }

    public String getSkinFileName() {
        return this.entityData.get(SKIN_FILE);
    }

    public void setSkinFileName(String skinFile) {
        this.npcData.setSkinFileName(skinFile);
        this.entityData.set(SKIN_FILE, skinFile);
    }

    /**
     * Gibt den NPC-Typ zurück (Client-safe via synced data)
     */
    public de.rolandsw.schedulemc.npc.data.NPCType getNpcType() {
        return de.rolandsw.schedulemc.npc.data.NPCType.fromOrdinal(this.entityData.get(NPC_TYPE_ORDINAL));
    }

    /**
     * Gibt die Verkäufer-Kategorie zurück (Client-safe via synced data)
     */
    public de.rolandsw.schedulemc.npc.data.MerchantCategory getMerchantCategory() {
        return de.rolandsw.schedulemc.npc.data.MerchantCategory.fromOrdinal(this.entityData.get(MERCHANT_CATEGORY_ORDINAL));
    }

    /**
     * Gibt die Persönlichkeit des NPCs zurück (Client-safe via synced data)
     * Wird für Tobacco Purchase Decision System verwendet
     */
    public NPCPersonality getPersonality() {
        String personalityStr = this.entityData.get(PERSONALITY);
        if (personalityStr.isEmpty()) {
            return NPCPersonality.AUSGEWOGEN; // Standard-Persönlichkeit
        }
        try {
            return NPCPersonality.valueOf(personalityStr);
        } catch (IllegalArgumentException e) {
            return NPCPersonality.AUSGEWOGEN;
        }
    }

    /**
     * Setzt die Persönlichkeit des NPCs
     */
    public void setPersonality(NPCPersonality personality) {
        npcData.getCustomData().putString("personality", personality.name());
        this.entityData.set(PERSONALITY, personality.name());
    }

    // Verhindern von Despawning
    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public void checkDespawn() {
        // NPCs despawnen nie
    }

    // Verhindern von Schaden (optional - kann angepasst werden)
    @Override
    public boolean isInvulnerable() {
        return true; // NPCs sind unsterblich (kann später konfigurierbar gemacht werden)
    }

    // Cleanup bei Entity-Removal
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);

        // Performance-Optimierung: Unregistriere aus NPCEntityRegistry
        if (!this.level().isClientSide) {
            NPCEntityRegistry.unregisterNPC(this);
        }

        // Unregistriere Namen aus dem Registry nur auf Server-Seite
        if (!this.level().isClientSide) {
            String npcName = getNpcName();
            if (npcName != null && !npcName.isEmpty()) {
                NPCNameRegistry.unregisterName(npcName);
                NPCNameRegistry.saveIfNeeded();

                // Sende aktualisierte Namen-Liste an alle Clients
                if (this.level() instanceof ServerLevel serverLevel && serverLevel.getServer() != null) {
                    de.rolandsw.schedulemc.npc.events.NPCNameSyncHandler.broadcastNameUpdate(
                        serverLevel.getServer()
                    );
                }
            }
        }
    }
}
