package com.lumengrid.recursiveae2patternprovider;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE = BUILDER
            .comment("Enable recursive AE2 pattern generation. When enabled, the mod will automatically generate dependency patterns for missing crafting ingredients. Works with all AE2 pattern types (crafting, processing, smithing, stonecutting, etc.)")
            .define("enableRecursiveAE2PatternProvider", true);

    public static final ModConfigSpec.IntValue RECURSION_DEPTH = BUILDER
            .comment("Maximum recursion depth for dependency pattern generation. -1 = no limit, 0 = disable recursion, positive values = max depth")
            .defineInRange("recursionDepth", -1, -1, 100);

    public static final ModConfigSpec.BooleanValue DEFAULT_ALLOW_SUBSTITUTES = BUILDER
            .comment("Default setting for allowing item substitutes in auto-generated patterns when parent pattern has no substitute info. When true, allows equivalent items (e.g., different wood types) to be used in recipes")
            .define("defaultAllowSubstitutes", false);

    public static final ModConfigSpec.BooleanValue DEFAULT_ALLOW_FLUID_SUBSTITUTES = BUILDER
            .comment("Default setting for allowing fluid substitutes in auto-generated patterns when parent pattern has no substitute info. When true, allows equivalent fluids to be used in recipes")
            .define("defaultAllowFluidSubstitutes", false);

    public static final ModConfigSpec.ConfigValue<String> RECIPE_ITEM = BUILDER
            .comment("Item required to craft recursive patterns. Use format: 'namespace:item_name'. Default is 'minecraft:iron_ingot'")
            .define("recipeItem", "minecraft:iron_ingot");

    static final ModConfigSpec SPEC = BUILDER.build();
}
