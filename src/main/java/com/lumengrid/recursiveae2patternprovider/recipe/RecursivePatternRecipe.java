package com.lumengrid.recursiveae2patternprovider.recipe;

import com.lumengrid.recursiveae2patternprovider.Config;
import com.lumengrid.recursiveae2patternprovider.PatternUtil;
import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import appeng.core.definitions.AEItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class RecursivePatternRecipe implements CraftingRecipe {

    private Item getConfiguredRecipeItem() {
        try {
            String itemName = Config.RECIPE_ITEM.get();
            Identifier itemId = Identifier.parse(itemName);
            return BuiltInRegistries.ITEM.get(itemId).map(Holder::value).orElse(Items.IRON_INGOT);
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.warn("Invalid recipe item configured: '{}', falling back to iron ingot", Config.RECIPE_ITEM.get());
            return Items.IRON_INGOT;
        }
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack pattern = ItemStack.EMPTY;
        ItemStack recipeItem = ItemStack.EMPTY;
        int itemCount = 0;
        Item configuredItem = getConfiguredRecipeItem();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (PatternUtil.isAE2Pattern(stack)) {
                    pattern = stack;
                    RecursiveAE2PatternProvider.LOGGER.debug("Found AE2 pattern in recipe: {}",
                            BuiltInRegistries.ITEM.getKey(stack.getItem()));
                } else if (stack.is(configuredItem)) {
                    recipeItem = stack;
                }
            }
        }

        if (itemCount == 2 && !pattern.isEmpty() && !recipeItem.isEmpty() && !PatternUtil.isRecursive(pattern)) {
            return true;
        } else if (itemCount == 1 && !pattern.isEmpty() && PatternUtil.isRecursive(pattern)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack pattern = ItemStack.EMPTY;
        boolean hasRecipeItem = false;
        int itemCount = 0;
        Item configuredItem = getConfiguredRecipeItem();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (PatternUtil.isAE2Pattern(stack)) {
                    pattern = stack;
                } else if (stack.is(configuredItem)) {
                    hasRecipeItem = true;
                }
            }
        }

        if (!pattern.isEmpty()) {
            if (itemCount == 2 && hasRecipeItem) {
                RecursiveAE2PatternProvider.LOGGER.debug("Adding recursive flag to: {}",
                        BuiltInRegistries.ITEM.getKey(pattern.getItem()));
                return createRecursivePattern(pattern);
            } else if (itemCount == 1 && PatternUtil.isRecursive(pattern)) {
                RecursiveAE2PatternProvider.LOGGER.debug("Removing recursive flag from: {}",
                        BuiltInRegistries.ITEM.getKey(pattern.getItem()));
                return removeRecursiveFlag(pattern);
            }
        }

        return ItemStack.EMPTY;
    }

    private ItemStack createRecursivePattern(ItemStack originalPattern) {
        try {
            ItemStack newPattern = originalPattern.copy();

            var existingData = newPattern.get(DataComponents.CUSTOM_DATA);
            CompoundTag customData = existingData != null ? existingData.copyTag() : new CompoundTag();
            customData.putBoolean("recursive", true);

            newPattern.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));

            RecursiveAE2PatternProvider.LOGGER.debug("Created recursive pattern with NBT: {}", customData);
            return newPattern;

        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.error("Failed to create recursive pattern: {}", e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    private ItemStack removeRecursiveFlag(ItemStack recursivePattern) {
        try {
            ItemStack newPattern = recursivePattern.copy();

            var existingData = newPattern.get(DataComponents.CUSTOM_DATA);
            if (existingData != null) {
                CompoundTag customData = existingData.copyTag();
                customData.remove("recursive");

                if (customData.isEmpty()) {
                    newPattern.remove(DataComponents.CUSTOM_DATA);
                    RecursiveAE2PatternProvider.LOGGER.debug("Removed all custom data from pattern");
                } else {
                    newPattern.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
                    RecursiveAE2PatternProvider.LOGGER.debug("Removed recursive flag, remaining NBT: {}", customData);
                }
            }

            return newPattern;

        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.error("Failed to remove recursive flag: {}", e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return AEItems.CRAFTING_PATTERN.stack();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
        return RecipeSerializers.RECURSIVE_PATTERN_SERIALIZER.get();
    }

    @Override
    public RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}