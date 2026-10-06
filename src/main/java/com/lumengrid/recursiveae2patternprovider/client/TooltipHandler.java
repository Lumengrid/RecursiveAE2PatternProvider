package com.lumengrid.recursiveae2patternprovider.client;

import appeng.core.definitions.AEItems;
import com.lumengrid.recursiveae2patternprovider.Config;
import com.lumengrid.recursiveae2patternprovider.PatternUtil;
import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Handles adding clean, AE2-styled tooltip information to recursive crafting patterns
 */
@EventBusSubscriber(modid = RecursiveAE2PatternProvider.MODID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (
                !(Config.ENABLE.get() && Config.RECURSION_DEPTH.get() != 0) ||
                        stack.isEmpty() ||
                        !stack.is(AEItems.CRAFTING_PATTERN.asItem()) ||
                        !PatternUtil.isRecursive(stack)
        ) {
            return;
        }

        try {
            List<Component> tooltip = event.getToolTip();
            Component recursiveComponent = Component.translatable("tooltip.recursiveae2patternprovider.recursive_pattern")
                    .withStyle(ChatFormatting.GREEN);

            int insertIndex = -1;
            for (int i = 0; i < tooltip.size(); i++) {
                Component component = tooltip.get(i);

                String key = "";
                if (component.getContents() instanceof TranslatableContents translatable) {
                    key = translatable.getKey().toLowerCase();
                }
                String visibleText = component.getString().toLowerCase();
                if (key.contains("encoded") || visibleText.contains("encoded") || visibleText.contains("codificato")) {
                    insertIndex = i;
                    break;
                }
            }

            if (insertIndex != -1) {
                tooltip.add(insertIndex, recursiveComponent);
            } else {
                tooltip.add(recursiveComponent);
            }
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.debug("Error adding pattern tooltip: {}", e.getMessage());
        }
    }
}