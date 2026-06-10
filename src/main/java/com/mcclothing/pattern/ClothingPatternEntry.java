package com.mcclothing.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

/**
 * One layer in a clothing piece's pattern stack: a pattern id + the dye color
 * tinting that pattern. Stored as a list inside {@link ClothingPatternsComponent}.
 *
 * We encode by raw {@link Identifier} (not RegistryEntry) so this component
 * survives across pattern-registry changes — an unknown id just renders as
 * absent rather than nuking the whole stack.
 */
public record ClothingPatternEntry(Identifier patternId, DyeColor color) {
    public static final Codec<ClothingPatternEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
        Identifier.CODEC.fieldOf("pattern").forGetter(ClothingPatternEntry::patternId),
        DyeColor.CODEC.fieldOf("color").forGetter(ClothingPatternEntry::color)
    ).apply(i, ClothingPatternEntry::new));

    public static final PacketCodec<PacketByteBuf, ClothingPatternEntry> PACKET_CODEC = PacketCodec.tuple(
        Identifier.PACKET_CODEC, ClothingPatternEntry::patternId,
        DyeColor.PACKET_CODEC,   ClothingPatternEntry::color,
        ClothingPatternEntry::new
    );

    public ClothingPattern resolve() {
        return ClothingPatterns.REGISTRY.get(patternId);
    }
}
