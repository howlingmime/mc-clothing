package com.mcclothing.screen;

import com.mcclothing.MCClothing;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public final class ModScreenHandlers {
    public static final ScreenHandlerType<TailorsLoomScreenHandler> TAILORS_LOOM =
        new ScreenHandlerType<>(TailorsLoomScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureSet.empty());

    public static void register() {
        Registry.register(Registries.SCREEN_HANDLER, MCClothing.id("tailors_loom"), TAILORS_LOOM);
    }

    private ModScreenHandlers() {}
}
