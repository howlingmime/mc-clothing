package com.mcclothing.screen;

import com.mcclothing.component.ModDataComponents;
import com.mcclothing.item.ClothingItem;
import com.mcclothing.pattern.ClothingPatternEntry;
import com.mcclothing.pattern.ClothingPatterns;
import com.mcclothing.pattern.ClothingPatternsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Slot 0: clothing piece input.
 * Slot 1: dye input.
 * Slot 2: result.
 *
 * The client picks a pattern index from {@link ClothingPatterns#all()}; we
 * sync it via {@link #selectedPattern} (a vanilla Property). Whenever inputs
 * change OR the selection changes, we rebuild the result.
 */
public class TailorsLoomScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final Property selectedPattern = Property.create();

    private final Inventory input = new SimpleInventory(2) {
        @Override public void markDirty() {
            super.markDirty();
            TailorsLoomScreenHandler.this.onContentChanged(this);
        }
    };
    private final CraftingResultInventory output = new CraftingResultInventory();

    private final Slot clothingSlot;
    private final Slot dyeSlot;
    private final Slot resultSlot;

    /** Client-side ctor used by ScreenHandlerType.Factory. */
    public TailorsLoomScreenHandler(int syncId, PlayerInventory inv) {
        this(syncId, inv, ScreenHandlerContext.EMPTY);
    }

    public TailorsLoomScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext context) {
        super(ModScreenHandlers.TAILORS_LOOM, syncId);
        this.context = context;

        this.clothingSlot = addSlot(new Slot(input, 0, 13, 26) {
            @Override public boolean canInsert(ItemStack stack) { return stack.getItem() instanceof ClothingItem; }
        });
        this.dyeSlot = addSlot(new Slot(input, 1, 33, 26) {
            @Override public boolean canInsert(ItemStack stack) { return stack.getItem() instanceof DyeItem; }
        });
        this.resultSlot = addSlot(new Slot(output, 0, 143, 58) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public void onTakeItem(PlayerEntity player, ItemStack stack) {
                clothingSlot.takeStack(1);
                dyeSlot.takeStack(1);
                if (!clothingSlot.hasStack() || !dyeSlot.hasStack()) {
                    selectedPattern.set(0);
                }
                context.run((world, pos) -> world.playSound(null, pos,
                    net.minecraft.sound.SoundEvents.UI_LOOM_TAKE_RESULT,
                    net.minecraft.sound.SoundCategory.BLOCKS, 1f, 1f));
                super.onTakeItem(player, stack);
            }
        });

        // Player inventory + hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
        addProperty(selectedPattern);
        selectedPattern.set(0);
    }

    @Override public boolean canUse(PlayerEntity player) { return true; }

    public int getSelectedPattern() { return selectedPattern.get(); }

    public List<RegistryKey<com.mcclothing.pattern.ClothingPattern>> availablePatterns() {
        return ClothingPatterns.all();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id >= 0 && id < availablePatterns().size()) {
            selectedPattern.set(id);
            updateResult();
            return true;
        }
        return false;
    }

    @Override
    public void onContentChanged(Inventory inv) {
        updateResult();
    }

    private void updateResult() {
        ItemStack cloth = clothingSlot.getStack();
        ItemStack dye   = dyeSlot.getStack();
        if (cloth.isEmpty() || dye.isEmpty() || !(dye.getItem() instanceof DyeItem dyeItem)) {
            output.setStack(0, ItemStack.EMPTY);
            sendContentUpdates();
            return;
        }
        var patternKeys = availablePatterns();
        int idx = selectedPattern.get();
        if (idx < 0 || idx >= patternKeys.size()) {
            output.setStack(0, ItemStack.EMPTY);
            sendContentUpdates();
            return;
        }
        Identifier patternId = patternKeys.get(idx).getValue();

        ItemStack out = cloth.copyWithCount(1);
        ClothingPatternsComponent existing =
            out.getOrDefault(ModDataComponents.CLOTHING_PATTERNS, ClothingPatternsComponent.EMPTY);
        ClothingPatternsComponent next =
            existing.withAdded(new ClothingPatternEntry(patternId, dyeItem.getColor()));
        out.set(ModDataComponents.CLOTHING_PATTERNS, next);
        output.setStack(0, out);
        sendContentUpdates();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();

        if (slotIndex == 2) { // result -> inventory
            if (!insertItem(stack, 3, 39, true)) return ItemStack.EMPTY;
            slot.onQuickTransfer(stack, copy);
        } else if (slotIndex == 0 || slotIndex == 1) {
            if (!insertItem(stack, 3, 39, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof ClothingItem) {
            if (!insertItem(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof DyeItem) {
            if (!insertItem(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else if (slotIndex < 30) {
            if (!insertItem(stack, 30, 39, false)) return ItemStack.EMPTY;
        } else {
            if (!insertItem(stack, 3, 30, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY); else slot.markDirty();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTakeItem(player, stack);
        return copy;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        context.run((world, pos) -> dropInventory(player, input));
    }
}
