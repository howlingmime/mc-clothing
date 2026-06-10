package com.mcclothing.block;

import com.mcclothing.MCClothing;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final TailorsLoomBlock TAILORS_LOOM = registerBlock("tailors_loom",
        new TailorsLoomBlock(AbstractBlock.Settings.copy(Blocks.LOOM)
            .mapColor(MapColor.OAK_TAN)
            .strength(2.5f)
            .sounds(BlockSoundGroup.WOOD)
            .registryKey(blockKey("tailors_loom"))));

    private static <T extends Block> T registerBlock(String name, T block) {
        Identifier id = MCClothing.id(name);
        RegistryKey<Block> bk = RegistryKey.of(RegistryKeys.BLOCK, id);
        Registry.register(Registries.BLOCK, bk, block);

        RegistryKey<Item> ik = RegistryKey.of(RegistryKeys.ITEM, id);
        Registry.register(Registries.ITEM, ik,
            new BlockItem(block, new Item.Settings().registryKey(ik)));
        return block;
    }

    private static RegistryKey<Block> blockKey(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, MCClothing.id(name));
    }

    public static void register() {
        // class-load triggers static initialization → registration
    }

    private ModBlocks() {}
}
