package com.lumengrid.recursiveae2patternprovider;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AECraftingPattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Shared dependency-pattern generation logic used by all mixins.
 *
 * Performance notes:
 * - The full crafting recipe collection is indexed ONCE per recipe manager
 *   (i.e. once per server start / datapack reload) into an output-item -> recipes
 *   map. Lookups per item are then O(1) instead of scanning the entire recipe
 *   book (30k+ recipes in large packs) for every input item at every recursion
 *   level.
 * - The index is shared across ALL pattern providers, so N providers on a
 *   network no longer each redo the full scan.
 * - Generation is server-side only, so the client level never competes for the
 *   single cache slot in singleplayer.
 *
 * Recipe selection rules (fix for "generated recipe picks a non-working alternative", issue #5):
 * - Recipes that consume their own output (NBT wipe / "clear settings" / upgrade-in-place
 *   recipes) are never used, otherwise AE2 may pick a pattern that needs X to craft X.
 * - Recipes whose encoded inputs contain an item already being crafted higher up in the
 *   chain are skipped (prevents ingot <-> block style cycles).
 * - Only the best {@code maxAlternativesPerItem} recipes per item are generated, chosen
 *   deterministically (plain vanilla shaped/shapeless first, then recipes from the item's
 *   own mod, then by recipe id) instead of "every recipe, in whatever order the recipe
 *   manager returns them".
 * - Items that already have a pattern in the same provider (e.g. a manual pattern) are not
 *   expanded, so the user's own pattern is never shadowed by a generated alternative.
 * - Recipe ids can be blacklisted via config (exact id, or prefix ending with '*').
 */
public final class RecursivePatternGenerator {

    private RecursivePatternGenerator() {}

    /**
     * Cached index plus the identity of the recipe manager it was built from.
     * The manager is held weakly so an unloaded level can still be collected, and
     * the recipe count acts as a cheap fingerprint for in-place recipe replacement
     * (KubeJS / CraftTweaker) on an otherwise unchanged manager instance.
     */
    private record CachedIndex(WeakReference<RecipeManager> manager, int recipeCount,
                               Map<Item, List<RecipeHolder<CraftingRecipe>>> index) {
        boolean matches(RecipeManager current, int currentRecipeCount) {
            return manager.get() == current && recipeCount == currentRecipeCount;
        }
    }

    /**
     * Per-generation settings, read from config once per {@link #generate} call.
     */
    private record Settings(int maxAlternativesPerItem, boolean skipSelfReferential,
                            Set<String> blacklistExact, List<String> blacklistPrefixes) {

        static Settings fromConfig() {
            int maxAlternatives = 1;
            boolean skipSelf = true;
            Set<String> exact = new HashSet<>();
            List<String> prefixes = new ArrayList<>();
            try {
                maxAlternatives = Math.max(1, Config.MAX_ALTERNATIVES_PER_ITEM.get());
                skipSelf = Config.SKIP_SELF_REFERENTIAL_RECIPES.get();
                for (Object entry : Config.RECIPE_BLACKLIST.get()) {
                    if (entry == null) continue;
                    String s = entry.toString().trim();
                    if (s.isEmpty()) continue;
                    if (s.endsWith("*")) {
                        prefixes.add(s.substring(0, s.length() - 1));
                    } else {
                        exact.add(s);
                    }
                }
            } catch (Exception e) {
                RecursiveAE2PatternProvider.LOGGER.warn("Failed to read recipe selection config, using defaults: {}",
                        e.getMessage());
            }
            return new Settings(maxAlternatives, skipSelf, exact, prefixes);
        }

        boolean isBlacklisted(ResourceLocation recipeId) {
            String id = recipeId.toString();
            if (blacklistExact.contains(id)) return true;
            for (String prefix : blacklistPrefixes) {
                if (id.startsWith(prefix)) return true;
            }
            return false;
        }
    }

    /**
     * Mutable state shared by one {@link #generate} call.
     */
    private static final class Context {
        final Level level;
        final int maxDepth;
        final Map<Item, List<RecipeHolder<CraftingRecipe>>> index;
        final Settings settings;
        final Set<String> processedRecipes = new HashSet<>();
        final Set<AEItemKey> processedItems = new HashSet<>();
        /** Items currently being crafted along the active recursion path (cycle detection). */
        final Set<Item> ancestors = new HashSet<>();
        final List<IPatternDetails> generated = new ArrayList<>();

        Context(Level level, int maxDepth, Map<Item, List<RecipeHolder<CraftingRecipe>>> index, Settings settings) {
            this.level = level;
            this.maxDepth = maxDepth;
            this.index = index;
            this.settings = settings;
        }
    }

    private static volatile CachedIndex cached;

    /**
     * Get (building if needed) the output-item -> recipes index for the given level.
     * Automatically rebuilt when the recipe manager instance or its recipe count changes.
     */
    private static Map<Item, List<RecipeHolder<CraftingRecipe>>> getRecipeIndex(Level level) {
        RecipeManager manager = level.getRecipeManager();
        CachedIndex snapshot = cached;
        if (snapshot != null && snapshot.matches(manager, manager.getRecipes().size())) {
            return snapshot.index();
        }
        synchronized (RecursivePatternGenerator.class) {
            int recipeCount = manager.getRecipes().size();
            snapshot = cached;
            if (snapshot != null && snapshot.matches(manager, recipeCount)) {
                return snapshot.index();
            }
            long start = System.nanoTime();
            Map<Item, List<RecipeHolder<CraftingRecipe>>> newIndex = new HashMap<>();
            var registryAccess = level.registryAccess();
            for (var recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
                try {
                    ItemStack output = recipe.value().getResultItem(registryAccess);
                    if (!output.isEmpty()) {
                        newIndex.computeIfAbsent(output.getItem(), k -> new ArrayList<>()).add(recipe);
                    }
                } catch (Exception e) {
                    RecursiveAE2PatternProvider.LOGGER.debug("Skipping recipe {} while indexing: {}",
                            recipe.id(), e.getMessage());
                }
            }
            cached = new CachedIndex(new WeakReference<>(manager), recipeCount, newIndex);
            RecursiveAE2PatternProvider.LOGGER.info(
                    "Indexed {} crafting recipes by output item in {} ms",
                    newIndex.values().stream().mapToInt(List::size).sum(),
                    (System.nanoTime() - start) / 1_000_000);
            return newIndex;
        }
    }

    /**
     * Collect and decode the recursive patterns held in a provider's pattern inventory.
     */
    public static List<IPatternDetails> collectRecursivePatterns(Iterable<ItemStack> patternInventory, Level level) {
        List<IPatternDetails> recursivePatterns = new ArrayList<>();
        if (patternInventory == null || level == null) {
            return recursivePatterns;
        }

        for (ItemStack stack : patternInventory) {
            if (PatternUtil.isRecursive(stack)) {
                var details = PatternDetailsHelper.decodePattern(stack, level);
                if (details != null) {
                    recursivePatterns.add(details);
                }
            }
        }
        return recursivePatterns;
    }

    /**
     * Backwards-compatible overload: no knowledge of the provider's existing patterns.
     */
    public static List<IPatternDetails> generate(List<IPatternDetails> recursivePatterns,
                                                 Level level, int maxDepth) {
        return generate(recursivePatterns, List.of(), level, maxDepth);
    }

    /**
     * Generate dependency patterns for the given recursive patterns.
     *
     * @param recursivePatterns patterns flagged as recursive
     * @param existingPatterns  patterns already present in the provider (manual + recursive).
     *                          Their outputs are never expanded, so a manual pattern always wins
     *                          over a generated alternative.
     * @return the list of auto-generated patterns to append to the provider
     */
    public static List<IPatternDetails> generate(List<IPatternDetails> recursivePatterns,
                                                 Collection<IPatternDetails> existingPatterns,
                                                 Level level, int maxDepth) {
        if (level == null || level.isClientSide() || recursivePatterns.isEmpty()) {
            return new ArrayList<>();
        }

        Context ctx = new Context(level, maxDepth, getRecipeIndex(level), Settings.fromConfig());

        // Items that already have a pattern in this provider must not get generated alternatives.
        if (existingPatterns != null) {
            for (IPatternDetails existing : existingPatterns) {
                markOutputsProcessed(existing, ctx);
            }
        }
        for (IPatternDetails recursivePattern : recursivePatterns) {
            markOutputsProcessed(recursivePattern, ctx);
        }

        for (IPatternDetails recursivePattern : recursivePatterns) {
            List<Item> rootOutputs = new ArrayList<>();
            for (var output : recursivePattern.getOutputs()) {
                if (output.what() instanceof AEItemKey itemKey && ctx.ancestors.add(itemKey.getItem())) {
                    rootOutputs.add(itemKey.getItem());
                }
            }
            try {
                generateDependencyPatterns(recursivePattern, 0, ctx);
            } finally {
                rootOutputs.forEach(ctx.ancestors::remove);
            }
        }
        return ctx.generated;
    }

    private static void markOutputsProcessed(IPatternDetails pattern, Context ctx) {
        try {
            for (var output : pattern.getOutputs()) {
                if (output.what() instanceof AEItemKey itemKey) {
                    ctx.processedItems.add(itemKey);
                }
            }
        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.debug("Failed to read pattern outputs: {}", e.getMessage());
        }
    }

    /**
     * Append generated patterns to the provider's pattern list, registering their
     * inputs when the provider tracks a pattern input collection.
     */
    public static void appendGenerated(List<IPatternDetails> generated,
                                       List<IPatternDetails> patterns,
                                       Collection<? super AEKey> patternInputs) {
        for (var autoPattern : generated) {
            patterns.add(autoPattern);
            if (patternInputs == null) {
                continue;
            }
            for (var patternInput : autoPattern.getInputs()) {
                for (var inputCandidate : patternInput.getPossibleInputs()) {
                    patternInputs.add(inputCandidate.what().dropSecondary());
                }
            }
        }
    }

    /**
     * Recursively generate patterns for all dependencies of a given pattern.
     */
    private static void generateDependencyPatterns(IPatternDetails pattern, int currentDepth, Context ctx) {
        // Check depth limit (-1 means no limit)
        if (ctx.maxDepth >= 0 && currentDepth >= ctx.maxDepth) {
            return;
        }

        for (var patternInput : pattern.getInputs()) {
            for (var inputCandidate : patternInput.getPossibleInputs()) {
                try {
                    var key = inputCandidate.what();
                    if (!(key instanceof AEItemKey itemKey)) {
                        continue;
                    }

                    if (!ctx.processedItems.add(itemKey)) {
                        continue;
                    }

                    findAndCreatePatternsForItem(itemKey.toStack(), pattern, currentDepth, ctx);

                } catch (Exception e) {
                    RecursiveAE2PatternProvider.LOGGER.debug("Error processing input candidate: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Find and create patterns for a specific item using the pre-built index.
     * Only the best valid recipe(s) are used, see class javadoc for the selection rules.
     */
    private static void findAndCreatePatternsForItem(ItemStack targetItem, IPatternDetails parentPattern,
                                                     int currentDepth, Context ctx) {
        Item targetType = targetItem.getItem();

        // O(1) lookup: only recipes whose output item matches the target
        List<RecipeHolder<CraftingRecipe>> candidates = ctx.index.get(targetType);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        String ownerNamespace = BuiltInRegistries.ITEM.getKey(targetType).getNamespace();

        List<RecipeHolder<CraftingRecipe>> ordered = candidates.stream()
                .filter(r -> !ctx.processedRecipes.contains(r.id().toString()))
                .filter(r -> !ctx.settings.isBlacklisted(r.id()))
                .filter(r -> !ctx.settings.skipSelfReferential() || !usesItem(r.value(), targetType))
                .sorted(Comparator
                        // Plain vanilla shaped/shapeless recipes first: custom subclasses are often
                        // NBT-wipe, upgrade or energy-transfer recipes.
                        .comparingInt((RecipeHolder<CraftingRecipe> r) -> isPlainVanilla(r.value()) ? 0 : 1)
                        // Then recipes added by the mod that owns the item.
                        .thenComparingInt(r -> r.id().getNamespace().equals(ownerNamespace) ? 0 : 1)
                        // Deterministic tie-breaker.
                        .thenComparing(r -> r.id().toString()))
                .toList();

        int created = 0;
        for (var recipe : ordered) {
            if (created >= ctx.settings.maxAlternativesPerItem()) {
                break;
            }

            try {
                ItemStack recipeOutput = recipe.value().getResultItem(ctx.level.registryAccess());
                if (recipeOutput.isEmpty() || !recipeOutput.is(targetType)) {
                    continue;
                }
                if (!ItemStack.isSameItemSameComponents(targetItem, recipeOutput)) {
                    continue;
                }

                ItemStack[] inputs = getRecipeInputsAs3x3Grid(recipe.value());

                // Skip recipes that need an item currently being crafted up the chain (A -> B -> A).
                if (containsAny(inputs, ctx.ancestors) || containsItem(inputs, targetType)) {
                    RecursiveAE2PatternProvider.LOGGER.debug("Skipping cyclic recipe {} for {}",
                            recipe.id(), targetType);
                    continue;
                }

                ctx.processedRecipes.add(recipe.id().toString());

                AECraftingPattern aePattern = encodePattern(recipe, inputs, recipeOutput, parentPattern, ctx.level);
                if (aePattern == null || aePattern.getOutputs() == null || aePattern.getOutputs().isEmpty()) {
                    continue;
                }

                ctx.generated.add(aePattern);
                created++;

                // Continue recursion with incremented depth, tracking the current path.
                boolean added = ctx.ancestors.add(targetType);
                try {
                    generateDependencyPatterns(aePattern, currentDepth + 1, ctx);
                } finally {
                    if (added) {
                        ctx.ancestors.remove(targetType);
                    }
                }

            } catch (Exception e) {
                RecursiveAE2PatternProvider.LOGGER.debug("Failed to create dependency pattern for recipe: {} - {}",
                        recipe.id(), e.getMessage());
            }
        }
    }

    private static AECraftingPattern encodePattern(RecipeHolder<CraftingRecipe> recipe, ItemStack[] inputs,
                                                   ItemStack output, IPatternDetails parentPattern,
                                                   Level level) {
        ItemStack patternStack = AEItems.CRAFTING_PATTERN.stack();

        // Extract substitute settings from parent pattern
        boolean allowSubstitutes = Config.DEFAULT_ALLOW_SUBSTITUTES.get();
        boolean allowFluidSubstitutes = Config.DEFAULT_ALLOW_FLUID_SUBSTITUTES.get();

        // If parent pattern is an AECraftingPattern, inherit its substitute settings
        if (parentPattern instanceof AECraftingPattern parentCraftingPattern) {
            allowSubstitutes = parentCraftingPattern.canSubstitute();
            allowFluidSubstitutes = parentCraftingPattern.canSubstituteFluids();
        }

        AECraftingPattern.encode(patternStack, recipe, inputs, output, allowSubstitutes, allowFluidSubstitutes);
        return new AECraftingPattern(Objects.requireNonNull(AEItemKey.of(patternStack)), level);
    }

    /**
     * True if any ingredient of the recipe accepts the given item
     * (e.g. "clear settings" recipes: X -> X).
     */
    private static boolean usesItem(CraftingRecipe recipe, Item item) {
        try {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.isEmpty()) continue;
                for (ItemStack option : ingredient.getItems()) {
                    if (option.is(item)) return true;
                }
            }
        } catch (Exception e) {
            // Broken ingredient: treat as unusable rather than risking a bad pattern.
            return true;
        }
        return false;
    }

    private static boolean isPlainVanilla(CraftingRecipe recipe) {
        Class<?> type = recipe.getClass();
        return type == ShapedRecipe.class || type == ShapelessRecipe.class;
    }

    private static boolean containsAny(ItemStack[] grid, Set<Item> items) {
        if (items.isEmpty()) return false;
        for (ItemStack stack : grid) {
            if (!stack.isEmpty() && items.contains(stack.getItem())) return true;
        }
        return false;
    }

    private static boolean containsItem(ItemStack[] grid, Item item) {
        for (ItemStack stack : grid) {
            if (!stack.isEmpty() && stack.is(item)) return true;
        }
        return false;
    }

    /**
     * Convert recipe ingredients to a 3x3 grid format.
     */
    private static ItemStack[] getRecipeInputsAs3x3Grid(CraftingRecipe recipe) {
        ItemStack[] inputs = new ItemStack[9];
        Arrays.fill(inputs, ItemStack.EMPTY);

        var ingredients = recipe.getIngredients();

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int ingredientIndex = y * width + x;
                    int gridIndex = y * 3 + x;

                    if (ingredientIndex < ingredients.size()) {
                        var ingredient = ingredients.get(ingredientIndex);
                        if (!ingredient.isEmpty() && ingredient.getItems().length > 0) {
                            inputs[gridIndex] = ingredient.getItems()[0].copy();
                        }
                    }
                }
            }
        } else {
            int index = 0;
            for (var ingredient : ingredients) {
                if (!ingredient.isEmpty() && ingredient.getItems().length > 0 && index < 9) {
                    inputs[index] = ingredient.getItems()[0].copy();
                    index++;
                }
            }
        }

        return inputs;
    }
}
