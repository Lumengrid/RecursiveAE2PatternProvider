package com.lumengrid.recursiveae2patternprovider;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.*;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.pattern.AEProcessingPattern;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class PlanExplanationTest {
    @Test
    void partialPlanExplainsGeneratedSourceButDoesNotClaimItIsRequired(MinecraftServer server) throws Exception {
        var hats = pattern(Items.LEATHER_HELMET, 3, 6);
        var root = processing(List.of(new GenericStack(AEItemKey.of(Items.REDSTONE), 48)), Items.CLOCK, 1);
        PlanExplanation.registerGenerated(hats);
        var plan = calculate(List.of(root, hats), Map.of(Items.LEATHER_HELMET, 6L), Items.CLOCK, 1);
        assertTrue(plan.simulation());
        assertEquals(List.of(AEItemKey.of(Items.REDSTONE)), PlanExplanation.analyze(plan).get(AEItemKey.of(Items.LEATHER_HELMET)));
        // Supplying the intermediate directly removes the source shortage.
        var supplied = calculate(List.of(root, hats), Map.of(Items.REDSTONE, 48L), Items.CLOCK, 1);
        assertFalse(supplied.simulation());
        assertTrue(PlanExplanation.analyze(supplied).isEmpty());
    }

    @Test
    void doesNotAnnotateManuallyEncodedPatterns(MinecraftServer server) throws Exception {
        var manual = pattern(Items.DIRT, 1, 1);
        var plan = calculate(List.of(manual), Map.of(), 1);
        assertTrue(plan.simulation());
        assertTrue(PlanExplanation.analyze(plan).isEmpty());
    }

    private IPatternDetails processing(List<GenericStack> inputs, Item output, int count) {
        return new AEProcessingPattern(AEItemKey.of(PatternDetailsHelper.encodeProcessingPattern(
                inputs, List.of(new GenericStack(AEItemKey.of(output), count)))));
    }

    private IPatternDetails pattern(Item source, int input, int output) {
        var stack = PatternDetailsHelper.encodeProcessingPattern(
                List.of(new GenericStack(AEItemKey.of(source), input)),
                List.of(new GenericStack(AEItemKey.of(Items.REDSTONE), output)));
        return new AEProcessingPattern(AEItemKey.of(stack));
    }

    private ICraftingPlan calculate(List<IPatternDetails> patterns, Map<Item, Long> stock, long amount) throws Exception {
        return calculate(patterns, stock, Items.REDSTONE, amount);
    }

    private ICraftingPlan calculate(List<IPatternDetails> patterns, Map<Item, Long> stock, Item output, long amount) throws Exception {
        var inventory = new KeyCounter();
        stock.forEach((item, count) -> inventory.add(AEItemKey.of(item), count));
        var storage = stub(IStorageService.class, (name, args) -> switch (name) {
            case "getCachedInventory" -> inventory;
            default -> throw new AssertionError(name);
        });
        var crafting = stub(ICraftingService.class, (name, args) -> switch (name) {
            case "getCraftingFor" -> patterns.stream().filter(p -> p.getOutputs().stream()
                    .anyMatch(o -> o.what().equals(args[0]))).toList();
            case "canEmitFor" -> false;
            case "getFuzzyCraftable" -> null;
            default -> throw new AssertionError(name);
        });
        var grid = stub(IGrid.class, (name, args) -> switch (name) {
            case "getStorageService" -> storage;
            case "getCraftingService" -> crafting;
            default -> throw new AssertionError(name);
        });
        var node = stub(IGridNode.class, (name, args) -> switch (name) {
            case "getGrid" -> grid;
            default -> throw new AssertionError(name);
        });
        var requester = stub(ICraftingSimulationRequester.class, (name, args) -> switch (name) {
            case "getGridNode" -> node;
            case "getActionSource" -> IActionSource.empty();
            default -> throw new AssertionError(name);
        });
        var calculation = new CraftingCalculation(null, grid, requester,
                new GenericStack(AEItemKey.of(output), amount), CalculationStrategy.REPORT_MISSING_ITEMS);
        // Run the synchronous calculation directly; bypass the tick scheduler's
        // initial pause in this small fixture, not any crafting decisions.
        var incTime = CraftingCalculation.class.getDeclaredField("incTime");
        incTime.setAccessible(true);
        incTime.setInt(calculation, 0);
        var compute = CraftingCalculation.class.getDeclaredMethod("computePlan");
        compute.setAccessible(true);
        return (ICraftingPlan) compute.invoke(calculation);
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> type, BiFunction<String, Object[], Object> calls) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> calls.apply(method.getName(), args));
    }
}
