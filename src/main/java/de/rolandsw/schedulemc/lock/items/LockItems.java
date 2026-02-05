package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.lock.LockType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Schloss-Items.
 *
 * 5 Schloss-Typen + 3 Schluessel-Rohlinge + Schluesselring + Dietrich = 10 Items
 */
public class LockItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ─── Schloesser ───
    public static final RegistryObject<Item> SIMPLE_LOCK = ITEMS.register("simple_lock",
            () -> new DoorLockItem(LockType.SIMPLE));
    public static final RegistryObject<Item> SECURITY_LOCK = ITEMS.register("security_lock",
            () -> new DoorLockItem(LockType.SECURITY));
    public static final RegistryObject<Item> HIGH_SECURITY_LOCK = ITEMS.register("high_security_lock",
            () -> new DoorLockItem(LockType.HIGH_SECURITY));
    public static final RegistryObject<Item> COMBINATION_LOCK = ITEMS.register("combination_lock",
            () -> new DoorLockItem(LockType.COMBINATION));
    public static final RegistryObject<Item> DUAL_LOCK = ITEMS.register("dual_lock",
            () -> new DoorLockItem(LockType.DUAL));

    // ─── Schluessel-Rohlinge (3 Stufen) ───
    public static final RegistryObject<Item> KEY_BLANK_COPPER = ITEMS.register("key_blank_copper",
            () -> new KeyItem(0, "Kupfer-Rohling"));
    public static final RegistryObject<Item> KEY_BLANK_IRON = ITEMS.register("key_blank_iron",
            () -> new KeyItem(1, "Eisen-Rohling"));
    public static final RegistryObject<Item> KEY_BLANK_NETHERITE = ITEMS.register("key_blank_netherite",
            () -> new KeyItem(2, "Netherite-Rohling"));

    // ─── Schluesselring ───
    public static final RegistryObject<Item> KEY_RING = ITEMS.register("key_ring",
            () -> new KeyRingItem());

    // ─── Dietrich-Set ───
    public static final RegistryObject<Item> LOCK_PICK = ITEMS.register("lock_pick",
            () -> new LockPickItem());

    // ─── Hacking-Tool ───
    public static final RegistryObject<Item> HACKING_TOOL = ITEMS.register("hacking_tool",
            () -> new HackingToolItem());
}
