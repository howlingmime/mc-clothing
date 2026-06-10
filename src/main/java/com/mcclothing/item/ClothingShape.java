package com.mcclothing.item;

/**
 * Identifies which body parts a clothing piece renders. Read by the client model
 * to toggle straps, sleeves, leg extensions, etc.
 */
public enum ClothingShape {
    OVERALLS(true, true, false, true, true),
    APRON(false, false, false, true, false),
    T_SHIRT(true, true, true, false, false),
    VEST(true, false, true, false, false),
    PANTS(false, false, false, false, true),
    SHORTS(false, false, false, false, true);

    public final boolean body;
    public final boolean straps;
    public final boolean sleeves;
    public final boolean frontPanel;
    public final boolean legs;

    ClothingShape(boolean body, boolean straps, boolean sleeves, boolean frontPanel, boolean legs) {
        this.body = body;
        this.straps = straps;
        this.sleeves = sleeves;
        this.frontPanel = frontPanel;
        this.legs = legs;
    }
}
