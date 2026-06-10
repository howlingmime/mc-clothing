package com.mcclothing.pattern;

import com.mcclothing.MCClothing;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Built-in pattern catalog. Mirrors the role of {@code BannerPattern} in vanilla
 * but lives on its own registry so we don't conflict with banner patterns. Add-ons
 * can register more patterns by registering against {@link #REGISTRY} during
 * common init.
 */
public final class ClothingPatterns {
    public static final RegistryKey<Registry<ClothingPattern>> REGISTRY_KEY =
        RegistryKey.ofRegistry(MCClothing.id("clothing_pattern"));

    public static final Registry<ClothingPattern> REGISTRY =
        FabricRegistryBuilder.createSimple(REGISTRY_KEY).buildAndRegister();

    private static final List<RegistryKey<ClothingPattern>> KEYS = new ArrayList<>();

    public static final RegistryKey<ClothingPattern> STRIPE     = key("stripe");
    public static final RegistryKey<ClothingPattern> CHECKERED  = key("checkered");
    public static final RegistryKey<ClothingPattern> PLAID      = key("plaid");
    public static final RegistryKey<ClothingPattern> POCKET     = key("pocket");
    public static final RegistryKey<ClothingPattern> HEM_TRIM   = key("hem_trim");
    public static final RegistryKey<ClothingPattern> COLLAR     = key("collar");
    public static final RegistryKey<ClothingPattern> EMBROIDERY = key("embroidery");
    public static final RegistryKey<ClothingPattern> PATCH      = key("patch");

    private static RegistryKey<ClothingPattern> key(String name) {
        RegistryKey<ClothingPattern> k = RegistryKey.of(REGISTRY_KEY, MCClothing.id(name));
        KEYS.add(k);
        return k;
    }

    public static List<RegistryKey<ClothingPattern>> all() {
        return List.copyOf(KEYS);
    }

    public static void register() {
        for (RegistryKey<ClothingPattern> k : KEYS) {
            String path = k.getValue().getPath();
            Registry.register(REGISTRY, k,
                new ClothingPattern(k.getValue(), "pattern." + MCClothing.MOD_ID + "." + path));
        }
    }

    private ClothingPatterns() {}
}
