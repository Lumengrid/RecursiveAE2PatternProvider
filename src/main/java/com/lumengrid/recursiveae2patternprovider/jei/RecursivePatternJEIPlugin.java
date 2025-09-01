package com.lumengrid.recursiveae2patternprovider.jei;

import com.lumengrid.recursiveae2patternprovider.Config;
import com.lumengrid.recursiveae2patternprovider.PatternUtil;
import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import appeng.core.definitions.AEItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;

@JeiPlugin
public class RecursivePatternJEIPlugin implements IModPlugin {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
        RecursiveAE2PatternProvider.MODID, "recursive_pattern_recipes");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
    
    /**
     * Get the configured recipe item from config
     */
    private ItemStack getConfiguredRecipeItemStack() {
        try {
            String itemName = Config.RECIPE_ITEM.get();
            ResourceLocation itemId = ResourceLocation.parse(itemName);
            Item item = BuiltInRegistries.ITEM.get(itemId);
            return new ItemStack(item);
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.warn("Invalid recipe item configured for JEI: '{}', falling back to iron ingot", Config.RECIPE_ITEM.get());
            return new ItemStack(Items.IRON_INGOT);
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RecursivePatternRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ItemStack configuredRecipeItem = getConfiguredRecipeItemStack();
        
        // Create example recipes to show in JEI
        List<RecursivePatternRecipe> recipes = Arrays.asList(
            // Add recursion: Normal Pattern + Recipe Item → Recursive Pattern
            new RecursivePatternRecipe(
                Arrays.asList(AEItems.CRAFTING_PATTERN.stack(), configuredRecipeItem),
                createRecursivePattern(AEItems.CRAFTING_PATTERN.stack()),
                "Add Recursion"
            ),
            new RecursivePatternRecipe(
                Arrays.asList(AEItems.PROCESSING_PATTERN.stack(), configuredRecipeItem.copy()),
                createRecursivePattern(AEItems.PROCESSING_PATTERN.stack()),
                "Add Recursion"
            ),
            new RecursivePatternRecipe(
                Arrays.asList(AEItems.SMITHING_TABLE_PATTERN.stack(), configuredRecipeItem.copy()),
                createRecursivePattern(AEItems.SMITHING_TABLE_PATTERN.stack()),
                "Add Recursion"
            ),
            // Remove recursion: Recursive Pattern → Normal Pattern
            new RecursivePatternRecipe(
                Arrays.asList(createRecursivePattern(AEItems.CRAFTING_PATTERN.stack())),
                AEItems.CRAFTING_PATTERN.stack(),
                "Remove Recursion"
            ),
            new RecursivePatternRecipe(
                Arrays.asList(createRecursivePattern(AEItems.PROCESSING_PATTERN.stack())),
                AEItems.PROCESSING_PATTERN.stack(),
                "Remove Recursion"
            ),
            new RecursivePatternRecipe(
                Arrays.asList(createRecursivePattern(AEItems.SMITHING_TABLE_PATTERN.stack())),
                AEItems.SMITHING_TABLE_PATTERN.stack(),
                "Remove Recursion"
            )
        );

        registration.addRecipes(RecursivePatternRecipeCategory.RECIPE_TYPE, recipes);
    }

    private ItemStack createRecursivePattern(ItemStack originalPattern) {
        ItemStack recursivePattern = originalPattern.copy();
        // Add the recursive tag to make it visually different
        recursivePattern = PatternUtil.createRecursivePattern(recursivePattern);
        return recursivePattern;
    }

    // Simple recipe class for JEI display
    public static class RecursivePatternRecipe {
        private final List<ItemStack> inputs;
        private final ItemStack output;
        private final String recipeType;

        public RecursivePatternRecipe(List<ItemStack> inputs, ItemStack output, String recipeType) {
            this.inputs = inputs;
            this.output = output;
            this.recipeType = recipeType;
        }

        public List<ItemStack> getInputs() {
            return inputs;
        }

        public ItemStack getOutput() {
            return output;
        }

        public String getRecipeType() {
            return recipeType;
        }
    }
}
