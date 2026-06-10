package com.mcclothing.component;

import com.mcclothing.MCClothing;
import com.mcclothing.pattern.ClothingPatternsComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModDataComponents {
    public static final ComponentType<ClothingPatternsComponent> CLOTHING_PATTERNS =
        ComponentType.<ClothingPatternsComponent>builder()
            .codec(ClothingPatternsComponent.CODEC)
            .packetCodec(ClothingPatternsComponent.PACKET_CODEC)
            .cache()
            .build();

    public static void register() {
        Registry.register(Registries.DATA_COMPONENT_TYPE,
            MCClothing.id("clothing_patterns"), CLOTHING_PATTERNS);
    }

    private ModDataComponents() {}
}
