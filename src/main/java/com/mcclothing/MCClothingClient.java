package com.mcclothing;

import com.mcclothing.client.ClothingTrinketRenderer;
import com.mcclothing.client.ModEntityModelLayers;
import com.mcclothing.client.screen.TailorsLoomScreen;
import com.mcclothing.item.ModItems;
import com.mcclothing.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;

public final class MCClothingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModEntityModelLayers.register();
        ClothingTrinketRenderer.register();
        HandledScreens.register(ModScreenHandlers.TAILORS_LOOM, TailorsLoomScreen::new);

        // Inventory icon tint: read DyedColorComponent like vanilla leather armor does.
        ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> tintIndex == 0
                ? DyedColorComponent.getColor(stack, 0xFFFFFF)
                : 0xFFFFFFFF,
            ModItems.OVERALLS, ModItems.APRON, ModItems.T_SHIRT,
            ModItems.VEST, ModItems.PANTS, ModItems.SHORTS
        );
    }
}
