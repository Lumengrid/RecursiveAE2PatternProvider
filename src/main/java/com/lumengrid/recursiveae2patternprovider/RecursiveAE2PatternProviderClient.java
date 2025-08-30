package com.lumengrid.recursiveae2patternprovider;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = RecursiveAE2PatternProvider.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = RecursiveAE2PatternProvider.MODID, value = Dist.CLIENT)
public class RecursiveAE2PatternProviderClient {
    public RecursiveAE2PatternProviderClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        RecursiveAE2PatternProvider.LOGGER.info("HELLO FROM CLIENT SETUP");
        RecursiveAE2PatternProvider.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
