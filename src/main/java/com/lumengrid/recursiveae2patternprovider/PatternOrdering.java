package com.lumengrid.recursiveae2patternprovider;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AE2 tries the patterns that make the same item in the order {@code getCraftingFor} returns them, and
 * within one provider priority that order is arbitrary. Patterns the player encoded come first, then
 * generated ones, and generated unpack recipes (block -> items) last: they are only useful when the
 * packed item is already in storage.
 */
public final class PatternOrdering {
    private PatternOrdering() {}

    public static Collection<IPatternDetails> userPatternsFirst(Collection<IPatternDetails> patterns) {
        if (patterns.size() < 2) return patterns;
        List<IPatternDetails> user = new ArrayList<>(patterns.size());
        List<IPatternDetails> generated = new ArrayList<>();
        List<IPatternDetails> unpack = new ArrayList<>();
        for (var pattern : patterns) {
            if (!PlanExplanation.isGenerated(pattern)) user.add(pattern);
            else if (isUnpack(pattern)) unpack.add(pattern);
            else generated.add(pattern);
        }
        // Nothing to reorder: all user-defined, or all of the same kind.
        if ((user.isEmpty() ? 0 : 1) + (generated.isEmpty() ? 0 : 1) + (unpack.isEmpty() ? 0 : 1) < 2) {
            return patterns;
        }
        user.addAll(generated);
        user.addAll(unpack);
        return user;
    }

    /** One kind of item in, a different single item out, and more of it out than in. */
    static boolean isUnpack(IPatternDetails pattern) {
        try {
            AEKey input = null;
            long consumed = 0;
            for (var slot : pattern.getInputs()) {
                var possible = slot.getPossibleInputs();
                if (possible.length == 0) continue;
                AEKey key = possible[0].what();
                if (input != null && !input.equals(key)) return false;
                input = key;
                consumed += possible[0].amount() * slot.getMultiplier();
            }
            AEKey output = null;
            long produced = 0;
            for (var stack : pattern.getOutputs()) {
                if (output != null && !output.equals(stack.what())) return false;
                output = stack.what();
                produced += stack.amount();
            }
            return input != null && output != null && !input.equals(output) && produced > consumed;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
