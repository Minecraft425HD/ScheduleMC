package de.rolandsw.schedulemc.vehicle.recipes;

import com.google.gson.JsonObject;
import de.rolandsw.schedulemc.vehicle.items.ItemBioDieselCanister;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class BioDieselCanisterRecipe extends ShapedRecipe {

    public BioDieselCanisterRecipe(ShapedRecipe baseRecipe) {
        super(baseRecipe.getId(), baseRecipe.getGroup(),
              CraftingBookCategory.MISC, baseRecipe.getWidth(),
              baseRecipe.getHeight(), baseRecipe.getIngredients(),
              ItemBioDieselCanister.createPreFilledStack());
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return ItemBioDieselCanister.createPreFilledStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BIO_DIESEL_CANISTER_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<BioDieselCanisterRecipe> {

        @Override
        public BioDieselCanisterRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe baseRecipe = RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json);
            return new BioDieselCanisterRecipe(baseRecipe);
        }

        @Override
        public BioDieselCanisterRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapedRecipe baseRecipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer);
            return new BioDieselCanisterRecipe(baseRecipe);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BioDieselCanisterRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
