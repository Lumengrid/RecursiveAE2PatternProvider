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

    public static ItemStack createRecursivePattern(ItemStack originalPattern) {
        try {
            ItemStack newPattern = originalPattern.copy();

            var existingData = newPattern.get(DataComponents.CUSTOM_DATA);
            CompoundTag customData = existingData != null ? existingData.copyTag() : new CompoundTag();
            customData.putBoolean("recursive", true);

            newPattern.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));

            return newPattern;

        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.error("Failed to create recursive pattern for JEI: {}", e.getMessage());
            return originalPattern.copy();
        }
    }
}