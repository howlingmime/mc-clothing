package com.mcclothing.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

/**
 * A registered pattern that can be applied to clothing via the Tailor's Loom.
 *
 * @param assetId  identifier used to resolve the overlay texture
 *                 ({@code textures/entity/clothing/pattern/<assetId>.png}).
 * @param translationKey lang key for tooltip / loom button label.
 */
public record ClothingPattern(Identifier assetId, String translationKey) {
    public static final Codec<ClothingPattern> CODEC = RecordCodecBuilder.create(i -> i.group(
        Identifier.CODEC.fieldOf("asset_id").forGetter(ClothingPattern::assetId),
        Codec.STRING.fieldOf("translation_key").forGetter(ClothingPattern::translationKey)
    ).apply(i, ClothingPattern::new));

    public static final PacketCodec<PacketByteBuf, ClothingPattern> PACKET_CODEC = PacketCodec.tuple(
        Identifier.PACKET_CODEC, ClothingPattern::assetId,
        PacketCodecs.STRING, ClothingPattern::translationKey,
        ClothingPattern::new
    );
}
