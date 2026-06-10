package com.mcclothing.trinket;

import com.mcclothing.item.ClothingItem;
import com.mcclothing.item.ClothingSlot;
import com.mcclothing.item.ModItems;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * One trinket implementation that all clothing items share. Equip validation
 * routes through {@link ClothingItem#slot()} so e.g. pants only fit the "pants"
 * subslot, t-shirts/vests/overalls/aprons fit chest subslots.
 *
 * The actual draping render happens in {@link com.mcclothing.client.ClothingTrinketRenderer},
 * registered client-side.
 */
public final class ClothingTrinket implements Trinket {
    public static final ClothingTrinket INSTANCE = new ClothingTrinket();

    @Override
    public boolean canEquip(ItemStack stack, SlotReference ref, LivingEntity entity) {
        if (!(stack.getItem() instanceof ClothingItem clothing)) return false;
        String slotName = ref.inventory().getSlotType().getName();
        return matches(clothing.slot(), slotName);
    }

    private static boolean matches(ClothingSlot slot, String trinketSlotName) {
        return switch (slot) {
            case SHIRT -> trinketSlotName.equals("shirt");
            case APRON -> trinketSlotName.equals("apron");
            case PANTS -> trinketSlotName.equals("pants");
        };
    }

    public static void register() {
        TrinketsApi.registerTrinket(ModItems.T_SHIRT,  INSTANCE);
        TrinketsApi.registerTrinket(ModItems.VEST,     INSTANCE);
        TrinketsApi.registerTrinket(ModItems.OVERALLS, INSTANCE);
        TrinketsApi.registerTrinket(ModItems.APRON,    INSTANCE);
        TrinketsApi.registerTrinket(ModItems.PANTS,    INSTANCE);
        TrinketsApi.registerTrinket(ModItems.SHORTS,   INSTANCE);
    }

    private ClothingTrinket() {}
}
