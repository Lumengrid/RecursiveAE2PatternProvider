package com.lumengrid.recursiveae2patternprovider;

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
    public RecursiveAE2PatternProvider(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // Register recipe serializers
        RecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
    }
}
