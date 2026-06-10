package com.mcclothing.compat;

import com.mcclothing.MCClothing;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.RegistryKeys;

/**
 * Public tag handles for any mod that wants to plug clothing into its own
 * warmth / temperature system (e.g. Overly Realistic). Read with
 * {@code stack.isIn(WarmthTags.LIGHT)}.
 *
 * Suggested numeric weights to apply when handling each tag, leaving final
 * scaling to the temperature mod:
 *   LIGHT  ≈ 0.10 of "leather armor base"
 *   MEDIUM ≈ 0.25
 *   HEAVY  ≈ 0.45
 *
 * Stacks of multiple clothing pieces add. Patterns don't change warmth.
 */
public final class WarmthTags {
    public static final TagKey<Item> LIGHT  = tag("warmth/light");
    public static final TagKey<Item> MEDIUM = tag("warmth/medium");
    public static final TagKey<Item> HEAVY  = tag("warmth/heavy");

    private static TagKey<Item> tag(String path) {
        return TagKey.of(RegistryKeys.ITEM, MCClothing.id(path));
    }

    private WarmthTags() {}
}
