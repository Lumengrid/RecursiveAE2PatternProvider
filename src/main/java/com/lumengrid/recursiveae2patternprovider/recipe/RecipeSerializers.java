package com.lumengrid.recursiveae2patternprovider.recipe;

import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
        DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER, RecursiveAE2PatternProvider.MODID);
    
    public static final Supplier<RecipeSerializer<RecursivePatternRecipe>> RECURSIVE_PATTERN_SERIALIZER =
        RECIPE_SERIALIZERS.register("recursive_pattern", RecursivePatternRecipeSerializer::new);
    
    public static class RecursivePatternRecipeSerializer implements RecipeSerializer<RecursivePatternRecipe> {
        private static final MapCodec<RecursivePatternRecipe> CODEC = MapCodec.unit(RecursivePatternRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, RecursivePatternRecipe> STREAM_CODEC = 
            StreamCodec.of(
                (buf, recipe) -> {
                    // Nothing to write - recipe is stateless
                },
                buf -> new RecursivePatternRecipe()
            );
        
        @Override
        public MapCodec<RecursivePatternRecipe> codec() {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RecursivePatternRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
