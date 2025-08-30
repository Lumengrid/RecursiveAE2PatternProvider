package com.lumengrid.recursiveae2patternprovider.client;

import com.lumengrid.recursiveae2patternprovider.Config;
import com.lumengrid.recursiveae2patternprovider.PatternUtil;
import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Handles adding tooltip information to AE2 patterns showing recursive status
 */
@EventBusSubscriber(modid = RecursiveAE2PatternProvider.MODID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var itemStack = event.getItemStack();
        if (
            !(Config.ENABLE.get() && Config.RECURSION_DEPTH.get() != 0) ||
            !PatternUtil.isAE2Pattern(itemStack) ||
            !PatternUtil.isRecursive(itemStack)
        ) {
            return;
        }

        try {
            var tooltip = event.getToolTip();
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("🔄 Recursive")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
            tooltip.add(Component.literal("Automatically generates dependency patterns")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            if (Config.RECURSION_DEPTH.get() > 0) {
                tooltip.add(Component.literal("Depth: " + Config.RECURSION_DEPTH.get())
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.debug("Error adding pattern tooltip: {}", e.getMessage());
        }
    }
}
