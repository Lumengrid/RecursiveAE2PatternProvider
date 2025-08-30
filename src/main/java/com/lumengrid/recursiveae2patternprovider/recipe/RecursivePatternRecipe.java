package com.lumengrid.recursiveae2patternprovider.recipe;

import com.lumengrid.recursiveae2patternprovider.PatternUtil;
import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import appeng.core.definitions.AEItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Toggle recipe: Pattern + Iron Ingot = Recursive Pattern (or vice versa)
 * - Normal Pattern + Iron Ingot → Recursive Pattern
 * - Recursive Pattern + Iron Ingot → Normal Pattern
 * Works with ALL AE2 pattern types (crafting, processing, smithing, stonecutting, etc.)
 * Note: Recipe won't show in JEI due to dynamic nature, but works in crafting table
 */
public class RecursivePatternRecipe implements CraftingRecipe {
    
    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack pattern = ItemStack.EMPTY;
        ItemStack iron = ItemStack.EMPTY;
        int itemCount = 0;
        
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (PatternUtil.isAE2Pattern(stack)) {
                    // Accept any AE2 pattern type (recursive or not)
                    pattern = stack;
                    RecursiveAE2PatternProvider.LOGGER.debug("Found AE2 pattern in recipe: {}", 
                        stack.getItem().builtInRegistryHolder().key().location());
                } else if (stack.is(Items.IRON_INGOT)) {
                    iron = stack;
                }
            }
        }
        
        return itemCount == 2 && !pattern.isEmpty() && !iron.isEmpty();
    }
    
    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (PatternUtil.isAE2Pattern(stack)) {
                if (PatternUtil.isRecursive(stack)) {
                    // Remove recursive flag
                    RecursiveAE2PatternProvider.LOGGER.debug("Removing recursive flag from: {}",
                        stack.getItem().builtInRegistryHolder().key().location());
                    return removeRecursiveFlag(stack);
                } else {
                    // Add recursive flag
                    RecursiveAE2PatternProvider.LOGGER.debug("Adding recursive flag to: {}",
                        stack.getItem().builtInRegistryHolder().key().location());
                    return createRecursivePattern(stack);
                }
            }
        }
        return ItemStack.EMPTY;
    }
    
    private ItemStack createRecursivePattern(ItemStack originalPattern) {
        try {
            ItemStack newPattern = originalPattern.copy();
            
            // Add recursive flag to custom data
            var existingData = newPattern.get(DataComponents.CUSTOM_DATA);
            CompoundTag customData = existingData != null ? existingData.copyTag() : new CompoundTag();
            customData.putBoolean("recursive", true);
            
            // Use the correct way to set custom data
            newPattern.set(DataComponents.CUSTOM_DATA, 
                CustomData.of(customData));
            
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
            
            // Remove recursive flag from custom data
            var existingData = newPattern.get(DataComponents.CUSTOM_DATA);
            if (existingData != null) {
                CompoundTag customData = existingData.copyTag();
                
                // Remove the recursive key
                customData.remove("recursive");
                
                // If custom data is now empty, remove the component entirely
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
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        // Return a generic crafting pattern for recipe book display
        // The actual result depends on the input pattern type
        return AEItems.CRAFTING_PATTERN.stack();
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializers.RECURSIVE_PATTERN_SERIALIZER.get();
    }
    
    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }
    
    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}
