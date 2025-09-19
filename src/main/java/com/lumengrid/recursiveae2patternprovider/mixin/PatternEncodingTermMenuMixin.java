package com.lumengrid.recursiveae2patternprovider.mixin;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.lumengrid.recursiveae2patternprovider.PatternUtil;
import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PatternEncodingTermMenu.class)
public class PatternEncodingTermMenuMixin {

    @Shadow
    private appeng.menu.slot.RestrictedInputSlot encodedPatternSlot;

    /**
     * Preserves the recursive flag when re-encoding patterns
     * This injection happens right after a new encoded pattern is created and before it's set in the slot
     */
    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lappeng/menu/slot/RestrictedInputSlot;set(Lnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void preserveRecursiveFlag(CallbackInfo ci, ItemStack encodedPattern) {
        try {
            // Only proceed if we have a valid encoded pattern to set
            if (encodedPattern == null || encodedPattern.isEmpty()) {
                return;
            }

            // Check if the existing pattern in the slot has the recursive flag
            ItemStack existingPattern = encodedPatternSlot.getItem();
            if (!existingPattern.isEmpty() && 
                PatternDetailsHelper.isEncodedPattern(existingPattern) && 
                PatternUtil.isRecursive(existingPattern)) {
                
                RecursiveAE2PatternProvider.LOGGER.debug("Preserving recursive flag during pattern re-encoding");
                
                // Preserve the recursive flag by adding it to the new pattern
                var existingData = encodedPattern.get(DataComponents.CUSTOM_DATA);
                CompoundTag customData = existingData != null ? existingData.copyTag() : new CompoundTag();
                customData.putBoolean("recursive", true);
                
                // Set the custom data with the recursive flag
                encodedPattern.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
                
                RecursiveAE2PatternProvider.LOGGER.debug("Successfully preserved recursive flag in re-encoded pattern");
            }
            
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.error("Failed to preserve recursive flag during pattern encoding: {}", e.getMessage(), e);
        }
    }
}
