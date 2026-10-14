package com.lumengrid.recursiveae2patternprovider;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class RecipeAlternativesTest {
    private final Map<Item, List<RecipeHolder<CraftingRecipe>>> index = new HashMap<>();

    @Test
    void packingAndUnpackingAreStockConversions(MinecraftServer server) {
        var unpack = add("test:unpack", Items.REDSTONE, 9, Items.REDSTONE_BLOCK, "X");
        var pack = add("test:pack", Items.REDSTONE_BLOCK, 1, Items.REDSTONE, "XXX", "XXX", "XXX");
        assertTrue(helper().isConversion(unpack, Items.REDSTONE));
        assertTrue(helper().isConversion(pack, Items.REDSTONE_BLOCK));
    }

    @Test
    void oneWayHatRecipeIsNotAnInverseConversion(MinecraftServer server) {
        var recipe = add("test:hat", Items.REDSTONE, 6, Items.LEATHER_HELMET, "XXX");
        assertFalse(helper().isConversion(recipe, Items.REDSTONE));
    }

    @Test
    void blacklistedReverseDoesNotEstablishConversion(MinecraftServer server) {
        var unpack = add("test:unpack", Items.REDSTONE, 9, Items.REDSTONE_BLOCK, "X");
        add("test:pack", Items.REDSTONE_BLOCK, 1, Items.REDSTONE, "XXX", "XXX", "XXX");
        var helper = new RecipeAlternatives(index, this::grid, r -> false);
        assertFalse(helper.isConversion(unpack, Items.REDSTONE));
    }

    @Test
    void mixedIngredientRecipeIsNotPacking(MinecraftServer server) {
        var pattern = ShapedRecipePattern.of(Map.of('X', Ingredient.of(Items.REDSTONE), 'Y', Ingredient.of(Items.GOLD_INGOT)), "XY");
        var recipe = new ShapedRecipe(null, null, pattern, new ItemStackTemplate(Items.CLOCK));
        assertFalse(helper().isConversion(recipe, Items.CLOCK));
    }

    private CraftingRecipe add(String id, Item output, int count, Item input, String... rows) {
        var pattern = ShapedRecipePattern.of(Map.of('X', Ingredient.of(input)), rows);
        CraftingRecipe recipe = new ShapedRecipe(null, null, pattern, new ItemStackTemplate(output, count));
        var holder = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.parse(id)), recipe);
        index.computeIfAbsent(output, k -> new ArrayList<>()).add(holder);
        return recipe;
    }

    private RecipeAlternatives helper() {
        return new RecipeAlternatives(index, this::grid, r -> true);
    }

    private ItemStack[] grid(CraftingRecipe recipe) {
        try {
            var method = RecursivePatternGenerator.class.getDeclaredMethod("getRecipeInputsAs3x3Grid", CraftingRecipe.class);
            method.setAccessible(true);
            return (ItemStack[]) method.invoke(null, recipe);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
