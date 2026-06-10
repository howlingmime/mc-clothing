package com.mcclothing.item;

import com.mcclothing.component.ModDataComponents;
import com.mcclothing.pattern.ClothingPatternsComponent;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

/**
 * Base item class for all clothing pieces.
 *
 * Dyeing piggybacks on vanilla's {@link DyedColorComponent}: the item is added
 * to the {@code minecraft:dyeable} item tag, which makes the crafting-table dye
 * recipe accept it automatically (same path leather armor uses).
 *
 * Pattern data is stored in {@link ClothingPatternsComponent} on the stack —
 * applied via the Tailor's Loom and read by the renderer to overlay textures.
 */
public class ClothingItem extends Item {
    private final ClothingShape shape;
    private final ClothingSlot slot;
    private final int defaultColor;

    public ClothingItem(Settings settings, ClothingShape shape, ClothingSlot slot, int defaultColor) {
        super(settings.component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(defaultColor, false)));
        this.shape = shape;
        this.slot = slot;
        this.defaultColor = defaultColor;
    }

    public ClothingShape shape() { return shape; }
    public ClothingSlot slot()   { return slot; }
    public int defaultColor()    { return defaultColor; }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        ClothingPatternsComponent patterns = stack.get(ModDataComponents.CLOTHING_PATTERNS);
        if (patterns != null) {
            patterns.appendTooltip((Consumer<Text>) tooltip::add);
        }
        DyedColorComponent dye = stack.get(DataComponentTypes.DYED_COLOR);
        if (dye != null && type.isAdvanced()) {
            tooltip.add(Text.translatable("item.color", String.format("#%06X", dye.rgb() & 0xFFFFFF))
                .formatted(Formatting.GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        // No-op; warmth is applied by external mods reading our tags.
    }
}
