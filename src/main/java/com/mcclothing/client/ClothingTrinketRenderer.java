package com.mcclothing.client;

import com.mcclothing.MCClothing;
import com.mcclothing.client.model.ClothingModel;
import com.mcclothing.item.ClothingItem;
import com.mcclothing.item.ClothingShape;
import com.mcclothing.pattern.ClothingPatternEntry;
import com.mcclothing.pattern.ClothingPatternsComponent;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renders any equipped clothing item over the player. One renderer instance
 * covers every clothing type — we cache one {@link ClothingModel} per
 * {@link ClothingShape} (lazy-built on first render once the EntityModelLoader
 * is available).
 *
 * Texture path: {@code textures/entity/clothing/<shape>.png}
 * Pattern overlays: {@code textures/entity/clothing/pattern/<patternId>.png},
 * one draw call per layer, tinted by the layer's dye color.
 */
public final class ClothingTrinketRenderer implements TrinketRenderer {
    public static final ClothingTrinketRenderer INSTANCE = new ClothingTrinketRenderer();

    private final Map<ClothingShape, ClothingModel> models = new EnumMap<>(ClothingShape.class);

    @Override
    public void render(ItemStack stack, SlotReference slot, net.minecraft.client.render.entity.model.EntityModel<? extends LivingEntity> contextModel,
                       MatrixStack matrices, VertexConsumerProvider provider, int light,
                       LivingEntity entity, float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {

        if (!(stack.getItem() instanceof ClothingItem clothing)) return;
        if (!(contextModel instanceof BipedEntityModel<?> biped)) return;

        ClothingModel model = modelFor(clothing.shape());
        model.copyTransforms(biped);

        // Base layer: cloth color from DyedColorComponent.
        DyedColorComponent dye = stack.get(DataComponentTypes.DYED_COLOR);
        int rgb = dye != null ? dye.rgb() : clothing.defaultColor();
        Identifier baseTex = MCClothing.id("textures/entity/clothing/" + textureName(clothing.shape()) + ".png");
        VertexConsumer vc = provider.getBuffer(RenderLayer.getEntityTranslucent(baseTex));
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8)  & 0xFF) / 255f;
        float b = ( rgb        & 0xFF) / 255f;
        model.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, r, g, b, 1f);

        // Pattern overlays.
        ClothingPatternsComponent patterns = stack.get(com.mcclothing.component.ModDataComponents.CLOTHING_PATTERNS);
        if (patterns != null && !patterns.entries().isEmpty()) {
            for (ClothingPatternEntry entry : patterns.entries()) {
                var pattern = entry.resolve();
                if (pattern == null) continue;
                Identifier patTex = MCClothing.id(
                    "textures/entity/clothing/pattern/" + pattern.assetId().getPath() + ".png");
                float[] cc = colorComponents(entry.color());
                VertexConsumer pvc = provider.getBuffer(RenderLayer.getEntityTranslucent(patTex));
                model.render(matrices, pvc, light, OverlayTexture.DEFAULT_UV, cc[0], cc[1], cc[2], 1f);
            }
        }
    }

    private ClothingModel modelFor(ClothingShape shape) {
        ClothingModel m = models.get(shape);
        if (m == null) {
            EntityModelLoader loader = MinecraftClient.getInstance().getEntityModelLoader();
            m = new ClothingModel(loader.getModelPart(ModEntityModelLayers.CLOTHING), shape);
            models.put(shape, m);
        }
        return m;
    }

    private static String textureName(ClothingShape shape) {
        return shape.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static float[] colorComponents(DyeColor color) {
        float[] c = color.getColorComponents();
        return new float[] { c[0], c[1], c[2] };
    }

    public static void register() {
        TrinketRendererRegistry.registerRenderer(com.mcclothing.item.ModItems.OVERALLS, INSTANCE);
        TrinketRendererRegistry.registerRenderer(com.mcclothing.item.ModItems.APRON,    INSTANCE);
        TrinketRendererRegistry.registerRenderer(com.mcclothing.item.ModItems.T_SHIRT,  INSTANCE);
        TrinketRendererRegistry.registerRenderer(com.mcclothing.item.ModItems.VEST,     INSTANCE);
        TrinketRendererRegistry.registerRenderer(com.mcclothing.item.ModItems.PANTS,    INSTANCE);
        TrinketRendererRegistry.registerRenderer(com.mcclothing.item.ModItems.SHORTS,   INSTANCE);
    }

    private ClothingTrinketRenderer() {}
}
