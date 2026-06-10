package com.mcclothing.pattern;

import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

/**
 * Data component storing the ordered pattern stack on a clothing item.
 * Limit kept low (8) so the renderer's overlay cost stays bounded.
 */
public record ClothingPatternsComponent(List<ClothingPatternEntry> entries) {
    public static final int MAX_PATTERNS = 8;
    public static final ClothingPatternsComponent EMPTY = new ClothingPatternsComponent(List.of());

    public static final Codec<ClothingPatternsComponent> CODEC =
        ClothingPatternEntry.CODEC.listOf().xmap(
            list -> new ClothingPatternsComponent(List.copyOf(list)),
            ClothingPatternsComponent::entries);

    public static final PacketCodec<PacketByteBuf, ClothingPatternsComponent> PACKET_CODEC =
        ClothingPatternEntry.PACKET_CODEC
            .collect(PacketCodecs.toList())
            .xmap(ClothingPatternsComponent::new, ClothingPatternsComponent::entries);

    public ClothingPatternsComponent withAdded(ClothingPatternEntry entry) {
        if (entries.size() >= MAX_PATTERNS) return this;
        var next = new java.util.ArrayList<>(entries);
        next.add(entry);
        return new ClothingPatternsComponent(List.copyOf(next));
    }

    public void appendTooltip(Consumer<Text> tooltip) {
        int shown = Math.min(entries.size(), 4);
        for (int i = 0; i < shown; i++) {
            var e = entries.get(i);
            var p = e.resolve();
            if (p == null) continue;
            tooltip.accept(
                Text.translatable(p.translationKey() + "." + e.color().getName())
                    .formatted(Formatting.GRAY));
        }
        if (entries.size() > shown) {
            tooltip.accept(Text.translatable("tooltip.mcclothing.more_patterns", entries.size() - shown)
                .formatted(Formatting.DARK_GRAY));
        }
    }
}
