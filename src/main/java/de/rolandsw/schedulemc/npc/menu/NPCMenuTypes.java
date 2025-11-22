package de.rolandsw.schedulemc.npc.menu;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry für NPC Menus
 */
public class NPCMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<MenuType<NPCSpawnerMenu>> NPC_SPAWNER_MENU =
        MENUS.register("npc_spawner_menu", () -> IForgeMenuType.create(NPCSpawnerMenu::new));

    public static final RegistryObject<MenuType<NPCInteractionMenu>> NPC_INTERACTION_MENU =
        MENUS.register("npc_interaction_menu", () -> IForgeMenuType.create(NPCInteractionMenu::new));

    public static final RegistryObject<MenuType<MerchantShopMenu>> MERCHANT_SHOP_MENU =
        MENUS.register("merchant_shop_menu", () -> IForgeMenuType.create(MerchantShopMenu::new));

    public static final RegistryObject<MenuType<ShopEditorMenu>> SHOP_EDITOR_MENU =
        MENUS.register("shop_editor_menu", () -> IForgeMenuType.create(ShopEditorMenu::new));
}
