package com.lumengrid.recursiveae2patternprovider.client;

import com.lumengrid.recursiveae2patternprovider.Config;
import com.lumengrid.recursiveae2patternprovider.PatternUtil;
import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Handles adding tooltip information to AE2 patterns showing recursive status
 */
@EventBusSubscriber(modid = RecursiveAE2PatternProvider.MODID, value = Dist.CLIENT)
public class TooltipHandler {
    
    /**
     * Get the display name of the configured recipe item
     */
    private static String getConfiguredItemDisplayName() {
        try {
            String itemName = Config.RECIPE_ITEM.get();
            ResourceLocation itemId = ResourceLocation.parse(itemName);
            var item = BuiltInRegistries.ITEM.get(itemId);
            return item.getName(item.getDefaultInstance()).getString();
        } catch (Exception e) {
            return Items.IRON_INGOT.getName(Items.IRON_INGOT.getDefaultInstance()).getString();
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var itemStack = event.getItemStack();
        if (
            !(Config.ENABLE.get() && Config.RECURSION_DEPTH.get() != 0) ||
            !PatternUtil.isAE2Pattern(itemStack)
        ) {
            return;
        }

        boolean isRecursive = PatternUtil.isRecursive(itemStack);
        String configuredItemName = getConfiguredItemDisplayName();

        try {
            var tooltip = event.getToolTip();
            
            if (isRecursive) {
                // Recursive pattern tooltip
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("🔄 Recursive")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
                tooltip.add(Component.literal("Automatically generates dependency patterns")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                if (Config.RECURSION_DEPTH.get() > 0) {
                    tooltip.add(Component.literal("Depth: " + Config.RECURSION_DEPTH.get())
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Craft alone to remove recursion")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            } else {
                // Non-recursive pattern tooltip
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Craft with " + configuredItemName + " to make recursive")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
            }
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.debug("Error adding pattern tooltip: {}", e.getMessage());
        }
    }
}
