package com.mcclothing;

import com.mcclothing.block.ModBlocks;
import com.mcclothing.component.ModDataComponents;
import com.mcclothing.item.ModItems;
import com.mcclothing.pattern.ClothingPatterns;
import com.mcclothing.screen.ModScreenHandlers;
import com.mcclothing.trinket.ClothingTrinket;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MCClothing implements ModInitializer {
    public static final String MOD_ID = "mcclothing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModDataComponents.register();
        ClothingPatterns.register();
        ModItems.register();
        ModBlocks.register();
        ModScreenHandlers.register();
        ClothingTrinket.register();
        LOGGER.info("MC Clothing initialized");
    }
}
