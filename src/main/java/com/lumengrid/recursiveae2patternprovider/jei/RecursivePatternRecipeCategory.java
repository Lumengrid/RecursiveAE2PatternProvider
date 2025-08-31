package com.lumengrid.recursiveae2patternprovider.jei;

import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import appeng.core.definitions.AEItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RecursivePatternRecipeCategory implements IRecipeCategory<RecursivePatternJEIPlugin.RecursivePatternRecipe> {
    
    public static final RecipeType<RecursivePatternJEIPlugin.RecursivePatternRecipe> RECIPE_TYPE = 
        RecipeType.create(RecursiveAE2PatternProvider.MODID, "recursive_pattern", RecursivePatternJEIPlugin.RecursivePatternRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public RecursivePatternRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
        this.background = guiHelper.createDrawable(location, 29, 16, 116, 54);
        this.icon = guiHelper.createDrawableItemStack(AEItems.CRAFTING_PATTERN.stack());
        this.title = Component.translatable("jei.category.recursiveae2patternprovider.recursive_pattern");
    }

    @Override
    public RecipeType<RecursivePatternJEIPlugin.RecursivePatternRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecursivePatternJEIPlugin.RecursivePatternRecipe recipe, IFocusGroup focuses) {
        if (recipe.getInputs().size() == 2) {
            // Pattern + Iron Ingot recipe
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                   .addItemStack(recipe.getInputs().get(0)); // Pattern
            
            builder.addSlot(RecipeIngredientRole.INPUT, 19, 1)
                   .addItemStack(recipe.getInputs().get(1)); // Iron Ingot
        } else if (recipe.getInputs().size() == 1) {
            // Pattern alone recipe
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                   .addItemStack(recipe.getInputs().get(0)); // Recursive Pattern
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
               .addItemStack(recipe.getOutput()); // Result
    }

    @Override
    public boolean isHandled(RecursivePatternJEIPlugin.RecursivePatternRecipe recipe) {
        return true;
    }
}
