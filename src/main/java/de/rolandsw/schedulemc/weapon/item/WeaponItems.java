package de.rolandsw.schedulemc.weapon.item;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.weapon.attachment.ScopeAttachment;
import de.rolandsw.schedulemc.weapon.attachment.SilencerAttachment;
import de.rolandsw.schedulemc.weapon.grenade.FlashGrenadeItem;
import de.rolandsw.schedulemc.weapon.grenade.FragGrenadeItem;
import de.rolandsw.schedulemc.weapon.grenade.SmokeGrenadeItem;
import de.rolandsw.schedulemc.weapon.gun.Ak47Item;
import de.rolandsw.schedulemc.weapon.gun.Mp5Item;
import de.rolandsw.schedulemc.weapon.gun.PistolItem;
import de.rolandsw.schedulemc.weapon.gun.RevolverItem;
import de.rolandsw.schedulemc.weapon.gun.ShotgunItem;
import de.rolandsw.schedulemc.weapon.gun.SniperItem;
import de.rolandsw.schedulemc.weapon.melee.BaseballBatItem;
import de.rolandsw.schedulemc.weapon.melee.CombatKnifeItem;
import de.rolandsw.schedulemc.weapon.melee.MacheteItem;
import de.rolandsw.schedulemc.weapon.upgrade.AutoFireUpgradeItem;
import de.rolandsw.schedulemc.weapon.upgrade.BurstFireUpgradeItem;
import de.rolandsw.schedulemc.weapon.upgrade.SingleFireUpgradeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WeaponItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ---- Magazine (zuerst registrieren – werden von Gun-Konstruktoren benötigt) ----
    public static final RegistryObject<Item> AK47_MAGAZINE = ITEMS.register("ak47_magazine",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PISTOL_MAGAZINE = ITEMS.register("pistol_magazine",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SHOTGUN_SHELLS = ITEMS.register("shotgun_shells",
            () -> new Item(new Item.Properties().stacksTo(32)));
    public static final RegistryObject<Item> SNIPER_MAGAZINE = ITEMS.register("sniper_magazine",
            () -> new Item(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> MP5_MAGAZINE = ITEMS.register("mp5_magazine",
            () -> new Item(new Item.Properties().stacksTo(20)));

    // ---- Munitionstypen ----
    public static final RegistryObject<Item> AMMO_STANDARD = ITEMS.register("ammo_standard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> AMMO_AP = ITEMS.register("ammo_ap",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> AMMO_TRACER = ITEMS.register("ammo_tracer",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> AMMO_RUBBER = ITEMS.register("ammo_rubber",
            () -> new Item(new Item.Properties()));

    // ---- Legacy-Munition ----
    public static final RegistryObject<Item> RIFLE_AMMO = ITEMS.register("rifle_ammo",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> PISTOL_AMMO = ITEMS.register("pistol_ammo",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ---- Schusswaffen ----
    public static final RegistryObject<Item> AK47 = ITEMS.register("ak47", Ak47Item::new);
    public static final RegistryObject<Item> PISTOL = ITEMS.register("pistol", PistolItem::new);
    public static final RegistryObject<Item> SHOTGUN = ITEMS.register("shotgun", ShotgunItem::new);
    public static final RegistryObject<Item> SNIPER = ITEMS.register("sniper", SniperItem::new);
    public static final RegistryObject<Item> REVOLVER = ITEMS.register("revolver", RevolverItem::new);
    public static final RegistryObject<Item> MP5 = ITEMS.register("mp5", Mp5Item::new);

    // ---- Nahkampfwaffen ----
    public static final RegistryObject<Item> BASEBALL_BAT = ITEMS.register("baseball_bat", BaseballBatItem::new);
    public static final RegistryObject<Item> MACHETE = ITEMS.register("machete", MacheteItem::new);
    public static final RegistryObject<Item> COMBAT_KNIFE = ITEMS.register("combat_knife", CombatKnifeItem::new);

    // ---- Granaten ----
    public static final RegistryObject<Item> FRAG_GRENADE = ITEMS.register("frag_grenade", FragGrenadeItem::new);
    public static final RegistryObject<Item> SMOKE_GRENADE = ITEMS.register("smoke_grenade", SmokeGrenadeItem::new);
    public static final RegistryObject<Item> FLASH_GRENADE = ITEMS.register("flash_grenade", FlashGrenadeItem::new);

    // ---- Attachments ----
    public static final RegistryObject<Item> SCOPE = ITEMS.register("scope", ScopeAttachment::new);
    public static final RegistryObject<Item> SILENCER = ITEMS.register("silencer", SilencerAttachment::new);

    // ---- Feuermodus-Upgrades ----
    public static final RegistryObject<Item> UPGRADE_SINGLE_PRECISION = ITEMS.register("upgrade_single_precision",
            SingleFireUpgradeItem::new);
    public static final RegistryObject<Item> UPGRADE_BURST = ITEMS.register("upgrade_burst",
            BurstFireUpgradeItem::new);
    public static final RegistryObject<Item> UPGRADE_AUTO = ITEMS.register("upgrade_auto",
            AutoFireUpgradeItem::new);
}
