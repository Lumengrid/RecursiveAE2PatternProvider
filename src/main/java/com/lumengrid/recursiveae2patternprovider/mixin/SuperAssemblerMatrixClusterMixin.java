package com.lumengrid.recursiveae2patternprovider.mixin;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.crafting.ICraftingService;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import com.extendedae_plus.content.matrix.supermatrix.SuperAssemblerMatrixCluster;
import com.extendedae_plus.util.crafting.StrictMolecularAssemblerPattern;
import com.lumengrid.recursiveae2patternprovider.Config;
import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import com.lumengrid.recursiveae2patternprovider.RecursivePatternGenerator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SuperAssemblerMatrixCluster.class, remap = false)
public abstract class SuperAssemblerMatrixClusterMixin {

    @Inject(method = "getAvailablePatterns", at = @At("RETURN"), cancellable = true)
    private void recursiveae2$appendDependencyPatterns(CallbackInfoReturnable<List<IPatternDetails>> cir) {
        try {
            if (!Config.ENABLE.get()) {
                return;
            }

            int maxDepth = Config.RECURSION_DEPTH.get();
            if (maxDepth == 0) {
                return;
            }

            List<IPatternDetails> original = cir.getReturnValue();
            if (original == null || original.isEmpty()) {
                return;
            }

            SuperAssemblerMatrixCluster self = (SuperAssemblerMatrixCluster) (Object) this;
            var core = self.getCore();
            if (core == null) {
                return;
            }
            Level level = core.getLevel();
            if (level == null || level.isClientSide()) {
                return;
            }

            List<IPatternDetails> recursivePatterns = new ArrayList<>();
            for (InternalInventory inventory : self.getPatternInventories()) {
                for (IPatternDetails details : RecursivePatternGenerator.collectRecursivePatterns(inventory, level)) {
                    if (details instanceof IMolecularAssemblerSupportedPattern) {
                        recursivePatterns.add(details);
                    }
                }
            }
            if (recursivePatterns.isEmpty()) {
                return;
            }

            ICraftingService craftingService = core.getMainNode().getGrid() != null ? core.getMainNode().getGrid().getService(ICraftingService.class) : null;

            List<IPatternDetails> generated =
                    RecursivePatternGenerator.generate(recursivePatterns, original, level, maxDepth, craftingService);
            if (generated.isEmpty()) {
                return;
            }

            List<IPatternDetails> result = new ArrayList<>(original.size() + generated.size());
            result.addAll(original);
            for (IPatternDetails pattern : generated) {
                if (pattern instanceof IMolecularAssemblerSupportedPattern supported) {
                    result.add(StrictMolecularAssemblerPattern.wrap(supported));
                }
            }
            cir.setReturnValue(result);

            RecursiveAE2PatternProvider.LOGGER.debug("Generated {} dependency patterns for Super Assembler Matrix",
                    result.size() - original.size());

        } catch (Exception e) {
            RecursiveAE2PatternProvider.LOGGER.error("Failed to inject dependency patterns for Super Assembler Matrix: {}",
                    e.getMessage(), e);
        }
    }
}