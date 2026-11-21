package com.lumengrid.recursiveae2patternprovider;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.ToDoubleFunction;

/** Primary ingredient variants let AE2 craft different accepted ingredient outputs. */
final class RecipeInputVariants {
    private record Group(List<Item> options, List<Integer> slots) {}

    static List<ItemStack[]> generate(CraftingRecipe recipe, ItemStack[] original, ItemStack expected,
                                     Level level, int limit) {
        return generate(recipe, original, expected, level, limit, item -> 0);
    }

    /** The first result is the cheapest valid set; on equal cost a slot keeps its item from {@code original}. */
    static List<ItemStack[]> generate(CraftingRecipe recipe, ItemStack[] original, ItemStack expected,
                                     Level level, int limit, ToDoubleFunction<Item> cost) {
        List<ItemStack[]> result = new ArrayList<>();
        if (limit <= 0) return result;
        var placement = recipe.placementInfo();
        Map<List<Item>, List<Integer>> groups = new LinkedHashMap<>();
        var ingredients = placement.ingredients();
        int[] shape = shapedSize(recipe);
        for (int slot = 0; slot < 9; slot++) {
            int ingredientIndex;
            if (shape != null) {
                int x = slot % 3;
                int y = slot / 3;
                if (x >= shape[0] || y >= shape[1]) continue;
                ingredientIndex = placement.slotsToIngredientIndex().getInt(y * shape[0] + x);
            } else {
                ingredientIndex = slot;
            }
            if (ingredientIndex < 0 || ingredientIndex >= ingredients.size()) continue;
            var options = ingredients.get(ingredientIndex).items().map(Holder::value).distinct()
                    .sorted(Comparator.comparingDouble(cost)
                            .thenComparingInt((Item item) -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft") ? 0 : 1)
                            .thenComparing(item -> BuiltInRegistries.ITEM.getKey(item).toString())).toList();
            if (options.size() > 1) groups.computeIfAbsent(options, k -> new ArrayList<>()).add(slot);
        }
        var choices = groups.entrySet().stream().map(e -> new Group(e.getKey(), e.getValue())).toList();
        ItemStack[] cheapest = original.clone();
        for (var choice : choices) {
            Item best = choice.options().getFirst();
            for (int slot : choice.slots()) {
                if (cost.applyAsDouble(best) < cost.applyAsDouble(original[slot].getItem())) cheapest[slot] = new ItemStack(best);
            }
        }
        addIfValid(recipe, cheapest, expected, level, result);
        if (result.size() < limit) addIfValid(recipe, original, expected, level, result);
        enumerate(recipe, original.clone(), expected, level, choices, 0, limit, new int[]{512}, result);
        return result;
    }

    /**
     * Width and height of a shaped recipe, or null when it is shapeless. Modded shaped recipes (Cucumber's
     * gear upgrades behind Mystical Agriculture armor) do not extend ShapedRecipe, so ask the display.
     */
    static int[] shapedSize(CraftingRecipe recipe) {
        if (recipe instanceof ShapedRecipe shaped) return new int[]{shaped.getWidth(), shaped.getHeight()};
        for (var display : recipe.display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) return new int[]{shaped.width(), shaped.height()};
        }
        return null;
    }

    private static void enumerate(CraftingRecipe recipe, ItemStack[] inputs, ItemStack expected, Level level,
                                  List<Group> groups, int index, int limit, int[] attempts, List<ItemStack[]> result) {
        if (result.size() >= limit || attempts[0] <= 0) return;
        if (index == groups.size()) {
            attempts[0]--;
            addIfValid(recipe, inputs, expected, level, result);
            return;
        }
        var group = groups.get(index);
        for (var item : group.options()) {
            for (int slot : group.slots()) inputs[slot] = new ItemStack(item);
            enumerate(recipe, inputs, expected, level, groups, index + 1, limit, attempts, result);
            if (result.size() >= limit || attempts[0] <= 0) break;
        }
    }

    private static void addIfValid(CraftingRecipe recipe, ItemStack[] inputs, ItemStack expected, Level level,
                                   List<ItemStack[]> result) {
        var input = CraftingInput.of(3, 3, Arrays.asList(inputs));
        try {
            if (!recipe.matches(input, level) || !ItemStack.matches(expected, recipe.assemble(input))) return;
            for (var existing : result) {
                boolean same = true;
                for (int slot = 0; slot < 9; slot++) {
                    if (!ItemStack.matches(existing[slot], inputs[slot])) { same = false; break; }
                }
                if (same) return;
            }
            result.add(Arrays.stream(inputs).map(ItemStack::copy).toArray(ItemStack[]::new));
        } catch (RuntimeException ignored) {
            // Custom recipes may require a particular combination or context.
        }
    }
}
