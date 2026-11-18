package com.lumengrid.recursiveae2patternprovider;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** A tag slot must resolve to its cheapest member, not to whichever item the tag lists first. */
@ExtendWith(EphemeralTestServerProvider.class)
class IngredientPreferenceTest {
    private final Map<Item, List<RecipeHolder<CraftingRecipe>>> index = new HashMap<>();

    @Test
    void plainGlassIsPrimaryEvenWhenStainedGlassIsListedFirst(MinecraftServer server) {
        var recipe = glassBottleRecipe();
        var selection = selectionWithStainedGlassRecipe();
        var variants = RecipeInputVariants.generate(recipe, stainedGrid(), new ItemStack(Items.GLASS_BOTTLE),
                null, 64, selection::itemCost);
        assertEquals(2, variants.size());
        assertTrue(variants.getFirst()[0].is(Items.GLASS));
        assertTrue(variants.getLast()[0].is(Items.BLACK_STAINED_GLASS));
    }

    @Test
    void singleVariantModeAlsoUsesTheCheapestMember(MinecraftServer server) {
        var selection = selectionWithStainedGlassRecipe();
        var variants = RecipeInputVariants.generate(glassBottleRecipe(), stainedGrid(),
                new ItemStack(Items.GLASS_BOTTLE), null, 1, selection::itemCost);
        assertEquals(1, variants.size());
        assertTrue(variants.getFirst()[0].is(Items.GLASS));
    }

    @Test
    void equalCostMembersKeepTheRecipesOwnChoice(MinecraftServer server) {
        var recipe = new ShapedRecipe(null, null, ShapedRecipePattern.of(
                Map.of('X', Ingredient.of(Items.OAK_PLANKS, Items.ACACIA_PLANKS)), "X"), new ItemStackTemplate(Items.STICK));
        var original = new ItemStack[9];
        Arrays.fill(original, ItemStack.EMPTY);
        original[0] = new ItemStack(Items.OAK_PLANKS);
        var variants = RecipeInputVariants.generate(recipe, original, new ItemStack(Items.STICK), null, 64, item -> 1);
        assertTrue(variants.getFirst()[0].is(Items.OAK_PLANKS));
    }

    @Test
    void cheapestSetSurvivesTheVariantCapWhenSeveralTagSlotsMultiply(MinecraftServer server) {
        var glass = Ingredient.of(Items.BLACK_STAINED_GLASS, Items.BLUE_STAINED_GLASS, Items.GLASS);
        var wool = Ingredient.of(Items.BLACK_WOOL, Items.BLUE_WOOL, Items.WHITE_WOOL);
        var plank = Ingredient.of(Items.ACACIA_PLANKS, Items.BIRCH_PLANKS, Items.OAK_PLANKS);
        var recipe = new ShapedRecipe(null, null, ShapedRecipePattern.of(Map.of('G', glass, 'W', wool, 'P', plank), "GWP"),
                new ItemStackTemplate(Items.PAINTING));
        var original = new ItemStack[9];
        Arrays.fill(original, ItemStack.EMPTY);
        original[0] = new ItemStack(Items.BLACK_STAINED_GLASS);
        original[1] = new ItemStack(Items.BLACK_WOOL);
        original[2] = new ItemStack(Items.ACACIA_PLANKS);
        Set<Item> cheap = Set.of(Items.GLASS, Items.WHITE_WOOL, Items.OAK_PLANKS);
        var variants = RecipeInputVariants.generate(recipe, original, new ItemStack(Items.PAINTING), null, 4,
                item -> cheap.contains(item) ? 1 : 2);
        assertEquals(4, variants.size());
        assertTrue(variants.getFirst()[0].is(Items.GLASS));
        assertTrue(variants.getFirst()[1].is(Items.WHITE_WOOL));
        assertTrue(variants.getFirst()[2].is(Items.OAK_PLANKS));
    }

    @Test
    void itemCostUsesTheCheapestRecipeAndCountsSmeltedItemsAsRaw(MinecraftServer server) {
        var selection = selectionWithStainedGlassRecipe();
        assertEquals(1, selection.itemCost(Items.GLASS));
        assertEquals(1, selection.itemCost(Items.BLACK_DYE));
        assertTrue(selection.itemCost(Items.BLACK_STAINED_GLASS) > 1);
    }

    @Test
    void variantsAreTriedAfterPrimaryAndBeforeStockDrivenPatterns(MinecraftServer server) {
        IPatternDetails stock = generated(Items.IRON_BLOCK);
        IPatternDetails variant = generated(Items.BLACK_STAINED_GLASS);
        IPatternDetails primary = generated(Items.GLASS);
        PatternOrdering.markStockDriven(stock);
        PatternOrdering.markVariant(variant);
        assertEquals(List.of(primary, variant, stock),
                List.copyOf(PatternOrdering.userPatternsFirst(List.of(stock, variant, primary))));
    }

    /** AE2 hands back same-priority patterns in arbitrary order; the better-ranked recipe must still come first. */
    @Test
    void generatedPatternsKeepTheGeneratorsRanking(MinecraftServer server) {
        IPatternDetails best = generated(Items.OAK_PLANKS);
        IPatternDetails worse = generated(Items.STICK);
        PatternOrdering.setRank(best, 0);
        PatternOrdering.setRank(worse, 1);
        assertEquals(List.of(best, worse), List.copyOf(PatternOrdering.userPatternsFirst(List.of(worse, best))));
    }

    /** Providers regenerate equal patterns as new instances; the marks must not die with the old instance. */
    @Test
    void regeneratedPatternKeepsItsMarksOnceTheOldInstanceIsCollected(MinecraftServer server) throws InterruptedException {
        IPatternDetails primary = generated(Items.GLASS);
        IPatternDetails old = generated(Items.GOLD_BLOCK);
        PatternOrdering.markStockDriven(old);
        IPatternDetails regenerated = generated(Items.GOLD_BLOCK);
        PatternOrdering.markStockDriven(regenerated);
        assertEquals(old, regenerated);
        assertNotSame(old, regenerated);

        var collected = new java.lang.ref.WeakReference<>(old);
        old = null;
        for (int i = 0; i < 100 && collected.get() != null; i++) {
            System.gc();
            Thread.sleep(20);
        }
        assertNull(collected.get(), "old instance was not collected; test cannot observe the bug");

        assertEquals(List.of(primary, regenerated),
                List.copyOf(PatternOrdering.userPatternsFirst(List.of(regenerated, primary))));
    }

    private static IPatternDetails generated(Item input) {
        IPatternDetails pattern = new AEProcessingPattern(AEItemKey.of(PatternDetailsHelper.encodeProcessingPattern(
                List.of(new GenericStack(AEItemKey.of(input), 1)),
                List.of(new GenericStack(AEItemKey.of(Items.GLASS_BOTTLE), 1)))));
        PlanExplanation.registerGenerated(pattern);
        return pattern;
    }

    private static CraftingRecipe glassBottleRecipe() {
        return new ShapedRecipe(null, null, ShapedRecipePattern.of(
                Map.of('X', Ingredient.of(Items.BLACK_STAINED_GLASS, Items.GLASS)), "X"), new ItemStackTemplate(Items.GLASS_BOTTLE));
    }

    private static ItemStack[] stainedGrid() {
        var grid = new ItemStack[9];
        Arrays.fill(grid, ItemStack.EMPTY);
        grid[0] = new ItemStack(Items.BLACK_STAINED_GLASS);
        return grid;
    }

    private RecipeSelection selectionWithStainedGlassRecipe() {
        var pattern = ShapedRecipePattern.of(Map.of('G', Ingredient.of(Items.GLASS), 'D', Ingredient.of(Items.BLACK_DYE)),
                "GGG", "GDG", "GGG");
        CraftingRecipe recipe = new ShapedRecipe(null, null, pattern, new ItemStackTemplate(Items.BLACK_STAINED_GLASS, 8));
        var id = ResourceKey.create(Registries.RECIPE, Identifier.parse("minecraft:black_stained_glass"));
        index.computeIfAbsent(Items.BLACK_STAINED_GLASS, k -> new ArrayList<>()).add(new RecipeHolder<>(id, recipe));
        return new RecipeSelection(index, Set.of(Items.GLASS), r -> true, r -> {
            var grid = new ItemStack[9];
            Arrays.fill(grid, ItemStack.EMPTY);
            Arrays.fill(grid, 0, 8, new ItemStack(Items.GLASS));
            grid[8] = new ItemStack(Items.BLACK_DYE);
            return grid;
        });
    }
}
