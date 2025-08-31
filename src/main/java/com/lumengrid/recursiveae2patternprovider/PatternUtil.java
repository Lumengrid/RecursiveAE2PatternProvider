package com.lumengrid.recursiveae2patternprovider;

import appeng.core.definitions.AEItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Utility for checking AE2 patterns and recursive flags
 */
public class PatternUtil {
    
    /**
     * Check if an item is any AE2 pattern (crafting, processing, smithing, etc.)
     */
    public static boolean isAE2Pattern(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        try {
            return stack.is(AEItems.CRAFTING_PATTERN.asItem()) ||
                    stack.is(AEItems.PROCESSING_PATTERN.asItem()) ||
                    stack.is(AEItems.SMITHING_TABLE_PATTERN.asItem()) ||
                    stack.is(AEItems.STONECUTTING_PATTERN.asItem());
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.debug("Error checking if item is AE2 pattern: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if a pattern has the recursive flag enabled
     */
    public static boolean isRecursive(ItemStack patternStack) {
        if (patternStack.isEmpty() || !isAE2Pattern(patternStack)) {
            return false;
        }
        
        try {
            var customData = patternStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                return tag.getBoolean("recursive");
            }
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.debug("Failed to read recursive flag: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Create a recursive version of a pattern for JEI display purposes
     */
    public static ItemStack createRecursivePattern(ItemStack originalPattern) {
        try {
            ItemStack newPattern = originalPattern.copy();
            
            // Add recursive flag to custom data
            var existingData = newPattern.get(DataComponents.CUSTOM_DATA);
            CompoundTag customData = existingData != null ? existingData.copyTag() : new CompoundTag();
            customData.putBoolean("recursive", true);
            
            // Use the correct way to set custom data
            newPattern.set(DataComponents.CUSTOM_DATA, 
                CustomData.of(customData));
            
            return newPattern;
            
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.error("Failed to create recursive pattern for JEI: {}", e.getMessage());
            return originalPattern.copy();
        }
    }
}
