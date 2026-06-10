package com.mcclothing.client;

import com.mcclothing.MCClothing;
import com.mcclothing.client.model.ClothingModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public final class ModEntityModelLayers {
    public static final EntityModelLayer CLOTHING = layer("clothing");

    private static EntityModelLayer layer(String name) {
        return new EntityModelLayer(MCClothing.id(name), "main");
    }

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(CLOTHING, ClothingModel::createBaseModelData);
    }

    private ModEntityModelLayers() {}
}
