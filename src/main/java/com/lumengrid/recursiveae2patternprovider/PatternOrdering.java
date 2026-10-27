package com.lumengrid.recursiveae2patternprovider;

import appeng.api.crafting.IPatternDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * AE2 tries the patterns that make the same item in the order {@code getCraftingFor} returns them, and
 * within one provider priority that order is arbitrary. Patterns the player encoded come first, then
 * generated ones, and generated stock-driven recipes (block -> items, tier uncrafts) last: they are only
 * useful when the packed item is already in storage.
 */
public final class PatternOrdering {
    private static final Map<IPatternDetails, Boolean> STOCK_DRIVEN = Collections.synchronizedMap(new WeakHashMap<>());

    private PatternOrdering() {}

    /** Called by the generator for patterns {@link RecipeSelection#isStockDriven} flagged when it created them. */
    public static void markStockDriven(IPatternDetails pattern) {
        STOCK_DRIVEN.put(pattern, Boolean.TRUE);
    }

    public static Collection<IPatternDetails> userPatternsFirst(Collection<IPatternDetails> patterns) {
        if (patterns.size() < 2) return patterns;
        List<IPatternDetails> user = new ArrayList<>(patterns.size());
        List<IPatternDetails> generated = new ArrayList<>();
        List<IPatternDetails> stockDriven = new ArrayList<>();
        for (var pattern : patterns) {
            if (!PlanExplanation.isGenerated(pattern)) user.add(pattern);
            else if (STOCK_DRIVEN.containsKey(pattern)) stockDriven.add(pattern);
            else generated.add(pattern);
        }
        // Nothing to reorder: all user-defined, or all of the same kind.
        if ((user.isEmpty() ? 0 : 1) + (generated.isEmpty() ? 0 : 1) + (stockDriven.isEmpty() ? 0 : 1) < 2) {
            return patterns;
        }
        user.addAll(generated);
        user.addAll(stockDriven);
        return user;
    }
}
