package de.rolandsw.schedulemc.vehicle.recipes;

import de.rolandsw.schedulemc.vehicle.Main;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Main.MODID);

    public static final RegistryObject<RecipeSerializer<BioDieselCanisterRecipe>> BIO_DIESEL_CANISTER_SERIALIZER =
        RECIPE_SERIALIZERS.register("bio_diesel_canister", BioDieselCanisterRecipe.Serializer::new);

    public static void init() {
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
