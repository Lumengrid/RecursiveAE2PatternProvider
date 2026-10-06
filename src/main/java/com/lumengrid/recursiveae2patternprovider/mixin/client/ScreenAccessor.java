package com.lumengrid.recursiveae2patternprovider.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Renderable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("renderables")
    List<Renderable> getRenderables();

    @Accessor("children")
    List<GuiEventListener> getChildren();
}