package com.lumengrid.recursiveae2patternprovider;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/** Identify reversible conversions whose source should be supplied, not recursively packed. */
final class RecipeAlternatives {
    private final Map<Item, List<RecipeHolder<CraftingRecipe>>> index;
    private final Function<CraftingRecipe, ItemStack[]> grid;
    private final Predicate<RecipeHolder<CraftingRecipe>> allowed;

    RecipeAlternatives(Map<Item, List<RecipeHolder<CraftingRecipe>>> index,
                       Function<CraftingRecipe, ItemStack[]> grid,
                       Predicate<RecipeHolder<CraftingRecipe>> allowed) {
        this.index = index;
        this.grid = grid;
        this.allowed = allowed;
    }

    boolean isConversion(CraftingRecipe recipe, Item target) {
        try {
            Item source = null;
            for (var input : grid.apply(recipe)) {
                if (input.isEmpty()) continue;
                if (source != null && source != input.getItem()) return false;
                source = input.getItem();
            }
            if (source == null || source == target) return false;
            for (var reverse : index.getOrDefault(source, List.of())) {
                if (!allowed.test(reverse)) continue;
                boolean found = false;
                for (var input : grid.apply(reverse.value())) {
                    if (input.isEmpty()) continue;
                    if (!input.is(target)) { found = false; break; }
                    found = true;
                }
                if (found) return true;
            }
        } catch (RuntimeException ignored) {
            // An unreadable recipe is not evidence of an inverse conversion.
        }
        return false;
    }
}
