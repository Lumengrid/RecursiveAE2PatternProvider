package com.lumengrid.recursiveae2patternprovider.recipe;

import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RecursiveAE2PatternProvider.MODID);

    public static final Supplier<RecipeSerializer<RecursivePatternRecipe>> RECURSIVE_PATTERN_SERIALIZER =
            RECIPE_SERIALIZERS.register("recursive_pattern", () -> new RecipeSerializer<>(
                    MapCodec.unit(RecursivePatternRecipe::new),
                    StreamCodec.of(
                            (buf, recipe) -> {},
                            buf -> new RecursivePatternRecipe()
                    )
            ));
}