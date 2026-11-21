package com.lumengrid.recursiveae2patternprovider;

import appeng.api.crafting.IPatternDetails;
import com.google.common.collect.MapMaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * AE2 tries the patterns that make the same item in the order {@code getCraftingFor} returns them, and
 * within one provider priority that order is arbitrary. Patterns the player encoded come first, then
 * generated ones, then the costlier ingredient variants of those, and generated stock-driven recipes (block -> items, tier uncrafts) last: they are only
 * useful when the packed item is already in storage.
 */
public final class PatternOrdering {
    // Weak and keyed by identity. Patterns are equal by definition and providers regenerate them, so a
    // WeakHashMap would keep the first instance as the key and forget the mark once that one is collected.
    private static final Map<IPatternDetails, Boolean> STOCK_DRIVEN = new MapMaker().weakKeys().makeMap();

    private static final Map<IPatternDetails, Boolean> VARIANTS = new MapMaker().weakKeys().makeMap();

    /** Position among the patterns generated for the same item; lower is the better-ranked recipe. */
    private static final Map<IPatternDetails, Integer> RANK = new MapMaker().weakKeys().makeMap();

    private PatternOrdering() {}

    /** Called by the generator in the order {@link RecipeSelection} ranked the recipes of one item. */
    public static void setRank(IPatternDetails pattern, int rank) {
        RANK.put(pattern, rank);
    }

    private static void byRank(List<IPatternDetails> patterns) {
        patterns.sort(Comparator.comparingInt(pattern -> RANK.getOrDefault(pattern, Integer.MAX_VALUE)));
    }

    /** Called by the generator for patterns {@link RecipeSelection#isStockDriven} flagged when it created them. */
    public static void markStockDriven(IPatternDetails pattern) {
        STOCK_DRIVEN.put(pattern, Boolean.TRUE);
    }

    /** Called by the generator for every ingredient variant after a recipe's cheapest one. */
    public static void markVariant(IPatternDetails pattern) {
        VARIANTS.put(pattern, Boolean.TRUE);
    }

    public static Collection<IPatternDetails> userPatternsFirst(Collection<IPatternDetails> patterns) {
        if (patterns.size() < 2) return patterns;
        List<IPatternDetails> user = new ArrayList<>(patterns.size());
        List<IPatternDetails> generated = new ArrayList<>();
        List<IPatternDetails> variants = new ArrayList<>();
        List<IPatternDetails> stockDriven = new ArrayList<>();
        for (var pattern : patterns) {
            if (!PlanExplanation.isGenerated(pattern)) user.add(pattern);
            else if (STOCK_DRIVEN.containsKey(pattern)) stockDriven.add(pattern);
            else if (VARIANTS.containsKey(pattern)) variants.add(pattern);
            else generated.add(pattern);
        }
        // Nothing to reorder: all user-defined.
        if (user.size() == patterns.size()) return patterns;
        // AE2's order within a group is arbitrary too; restore the generator's ranking.
        byRank(generated);
        byRank(variants);
        byRank(stockDriven);
        user.addAll(generated);
        user.addAll(variants);
        user.addAll(stockDriven);
        return user;
    }
}
