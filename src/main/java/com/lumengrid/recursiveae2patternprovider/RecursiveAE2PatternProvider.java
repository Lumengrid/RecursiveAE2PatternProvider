package com.lumengrid.recursiveae2patternprovider;

import com.lumengrid.recursiveae2patternprovider.network.ModNetwork;
import com.lumengrid.recursiveae2patternprovider.recipe.RecipeSerializers;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(RecursiveAE2PatternProvider.MODID)
public class RecursiveAE2PatternProvider {
    public static final String MODID = "recursiveae2patternprovider";
    public static final Logger LOGGER = LogUtils.getLogger();
    /** Changes whenever the recipe-selection behaviour changes; logged so a stale jar is obvious. */
    public static final String BUILD_MARKER = "alt-priority-2026-10-27-user-patterns-first";

    public RecursiveAE2PatternProvider(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Recursive AE2 Pattern Provider loaded, build={}", BUILD_MARKER);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(ModNetwork::registerPayloadHandlers);
        RecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
    }
}
