package com.mcclothing.item;

/**
 * Which trinket subslot a piece of clothing fits into. Drives {@link
 * com.mcclothing.trinket.ClothingTrinket#canEquip} and slot-data JSON wiring.
 */
public enum ClothingSlot {
    SHIRT("shirt"),
    APRON("apron"),
    PANTS("pants");

    public final String id;
    ClothingSlot(String id) { this.id = id; }
}
