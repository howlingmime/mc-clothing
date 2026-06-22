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
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final TailorsLoomBlock TAILORS_LOOM = registerBlock("tailors_loom",
        new TailorsLoomBlock(AbstractBlock.Settings.copy(Blocks.LOOM)
            .mapColor(MapColor.OAK_TAN)
            .strength(2.5f)
            .sounds(BlockSoundGroup.WOOD)));

    private static <T extends Block> T registerBlock(String name, T block) {
        Identifier id = MCClothing.id(name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void register() {
        // class-load triggers static initialization → registration
    }

    private ModBlocks() {}
}
