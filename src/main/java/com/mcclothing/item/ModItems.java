package com.mcclothing.item;

import com.mcclothing.MCClothing;
import com.mcclothing.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModItems {

    // Default colors mirror typical fabric tones; users dye on top.
    private static final int DENIM   = 0x3B5F8A;
    private static final int CANVAS  = 0xCBB48A;
    private static final int LINEN   = 0xE8DEC4;
    private static final int LEATHER = 0x8B5A2B;

    public static final ClothingItem OVERALLS = register("overalls",
        s -> new ClothingItem(s.maxCount(1), ClothingShape.OVERALLS, ClothingSlot.SHIRT, DENIM));
    public static final ClothingItem APRON = register("apron",
        s -> new ClothingItem(s.maxCount(1), ClothingShape.APRON, ClothingSlot.APRON, CANVAS));
    public static final ClothingItem T_SHIRT = register("t_shirt",
        s -> new ClothingItem(s.maxCount(1), ClothingShape.T_SHIRT, ClothingSlot.SHIRT, LINEN));
    public static final ClothingItem VEST = register("vest",
        s -> new ClothingItem(s.maxCount(1), ClothingShape.VEST, ClothingSlot.SHIRT, LEATHER));
    public static final ClothingItem PANTS = register("pants",
        s -> new ClothingItem(s.maxCount(1), ClothingShape.PANTS, ClothingSlot.PANTS, DENIM));
    public static final ClothingItem SHORTS = register("shorts",
        s -> new ClothingItem(s.maxCount(1), ClothingShape.SHORTS, ClothingSlot.PANTS, CANVAS));

    private static <T extends Item> T register(String name, Function<Item.Settings, T> factory) {
        Identifier id = MCClothing.id(name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        T item = factory.apply(new Item.Settings().registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(ModItems::addToGroup);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries ->
            entries.add(ModBlocks.TAILORS_LOOM));
    }

    private static void addToGroup(FabricItemGroupEntries entries) {
        entries.add(T_SHIRT);
        entries.add(VEST);
        entries.add(OVERALLS);
        entries.add(APRON);
        entries.add(PANTS);
        entries.add(SHORTS);
    }

    private ModItems() {}
}
