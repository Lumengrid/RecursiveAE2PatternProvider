package com.lumengrid.recursiveae2patternprovider;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

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

    public static final ModConfigSpec.IntValue MAX_ALTERNATIVES_PER_ITEM = BUILDER
            .comment("How many crafting recipes to generate per dependency item. 1 = only the best recipe (recommended). Higher values re-introduce alternative recipes that AE2 may pick instead of the expected one")
            .defineInRange("maxAlternativesPerItem", 1, 1, 64);

    public static final ModConfigSpec.BooleanValue SKIP_SELF_REFERENTIAL_RECIPES = BUILDER
            .comment("Ignore recipes that consume their own output (e.g. 'clear settings' / NBT-wipe recipes) when generating dependency patterns")
            .define("skipSelfReferentialRecipes", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> RECIPE_BLACKLIST = BUILDER
            .comment("Recipe ids that are never used for auto-generated patterns. Use an exact id ('modid:recipe_name') or a prefix ending with '*' ('modid:*', 'modid:reset/*')")
            .defineListAllowEmpty("recipeBlacklist", List.of(), () -> "", o -> o instanceof String);

    static final ModConfigSpec SPEC = BUILDER.build();
}
