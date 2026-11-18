package com.lumengrid.recursiveae2patternprovider;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class RecursivePatternGeneratorTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void preservesEmptyCornersOfClockRecipe(net.minecraft.server.MinecraftServer server) throws Exception {
        var recipe = recipe(" G ", "GRG", " G ");
        var grid = grid(recipe);
        assertTrue(recipe.matches(CraftingInput.of(3, 3, Arrays.asList(grid)), null));
        for (int slot : new int[]{0, 2, 6, 8}) assertTrue(grid[slot].isEmpty());
        assertTrue(grid[4].is(Items.REDSTONE));
    }

    @Test
    void preservesEmptySlotInTwoWideRecipe(net.minecraft.server.MinecraftServer server) throws Exception {
        var recipe = recipe("G ", "GR");
        var grid = grid(recipe);
        assertTrue(recipe.matches(CraftingInput.of(3, 3, Arrays.asList(grid)), null));
        assertTrue(grid[1].isEmpty());
        assertTrue(grid[3].is(Items.GOLD_INGOT));
        assertTrue(grid[4].is(Items.REDSTONE));
    }

    @Test
    void preservesFilledRecipe(net.minecraft.server.MinecraftServer server) throws Exception {
        var recipe = recipe("GG", "GR");
        assertTrue(recipe.matches(CraftingInput.of(3, 3, Arrays.asList(grid(recipe))), null));
    }

    /** Cucumber's gear upgrade recipes (Mystical Agriculture armor and tools) are shaped but not ShapedRecipe. */
    @Test
    void moddedShapedRecipeKeepsItsHoles() throws Exception {
        var recipe = new ModdedShaped(recipe(" G ", "GRG", " G ").pattern);
        var inputs = grid(recipe);
        assertTrue(inputs[0].isEmpty());
        assertTrue(inputs[4].is(Items.REDSTONE));
        assertTrue(recipe.matches(CraftingInput.of(3, 3, Arrays.asList(inputs)), null));

        var variants = RecipeInputVariants.generate(recipe, inputs, new ItemStack(Items.CLOCK), null, 1);
        assertEquals(1, variants.size());
    }

    private static final class ModdedShaped extends NormalCraftingRecipe {
        private final ShapedRecipePattern pattern;

        ModdedShaped(ShapedRecipePattern pattern) {
            super(null, null);
            this.pattern = pattern;
        }

        @Override
        public ItemStack assemble(CraftingInput input) {
            return new ItemStack(Items.CLOCK);
        }

        @Override
        public boolean matches(CraftingInput input, Level level) {
            return pattern.matches(input);
        }

        @Override
        public RecipeSerializer<? extends NormalCraftingRecipe> getSerializer() {
            return null;
        }

        @Override
        protected PlacementInfo createPlacementInfo() {
            return PlacementInfo.createFromOptionals(pattern.ingredients());
        }

        @Override
        public List<RecipeDisplay> display() {
            return List.of(new ShapedCraftingRecipeDisplay(pattern.width(), pattern.height(),
                    java.util.Collections.nCopies(pattern.width() * pattern.height(), SlotDisplay.Empty.INSTANCE),
                    SlotDisplay.Empty.INSTANCE, SlotDisplay.Empty.INSTANCE));
        }
    }

    private static ShapedRecipe recipe(String... rows) {
        var pattern = ShapedRecipePattern.of(Map.of(
                'G', Ingredient.of(Items.GOLD_INGOT),
                'R', Ingredient.of(Items.REDSTONE)), rows);
        // These tests only use the recipe's placement and matching behavior.
        return new ShapedRecipe(null, null, pattern, null);
    }

    private static ItemStack[] grid(CraftingRecipe recipe) throws Exception {
        var method = RecursivePatternGenerator.class.getDeclaredMethod("getRecipeInputsAs3x3Grid", CraftingRecipe.class);
        method.setAccessible(true);
        return (ItemStack[]) method.invoke(null, recipe);
    }
}
