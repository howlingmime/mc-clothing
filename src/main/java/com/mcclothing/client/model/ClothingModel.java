package com.mcclothing.client.model;

import com.mcclothing.item.ClothingShape;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.LivingEntity;

/**
 * Bipedal clothing model. All parts are children of the vanilla biped skeleton
 * via {@link #copyTransforms} so the cloth follows the player's animation
 * (running, sneaking, swinging arms). Each shape toggles only the parts it
 * needs — overalls show body + straps + legs (no sleeves), t-shirts show body +
 * sleeves, etc.
 *
 * Vertex extrusion is +0.3 over the player skin so the cloth sits visibly on
 * top without z-fighting (vanilla armor uses 1.0 / 0.5 layers; ours is a single
 * thin layer because clothing can stack with armor).
 */
public class ClothingModel extends BipedEntityModel<LivingEntity> {
    public static final float EXTRUSION = 0.30f;

    public final ModelPart bodyOverlay;
    public final ModelPart straps;        // overalls only
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart frontPanel;    // apron only
    public final ModelPart leftLeg;
    public final ModelPart rightLeg;

    private final ClothingShape shape;

    public ClothingModel(ModelPart root, ClothingShape shape) {
        super(root);
        this.shape = shape;
        this.bodyOverlay = root.getChild("body_overlay");
        this.straps      = root.getChild("straps");
        this.leftSleeve  = root.getChild("left_sleeve");
        this.rightSleeve = root.getChild("right_sleeve");
        this.frontPanel  = root.getChild("front_panel");
        this.leftLeg     = root.getChild("left_leg_overlay");
        this.rightLeg    = root.getChild("right_leg_overlay");
        applyVisibility();
    }

    private void applyVisibility() {
        bodyOverlay.visible = shape.body;
        straps.visible      = shape.straps;
        leftSleeve.visible  = shape.sleeves;
        rightSleeve.visible = shape.sleeves;
        frontPanel.visible  = shape.frontPanel;
        leftLeg.visible     = shape.legs;
        rightLeg.visible    = shape.legs;
    }

    /** Sync clothing parts to whatever pose the host biped is in this frame. */
    public void copyTransforms(BipedEntityModel<?> host) {
        bodyOverlay.copyTransform(host.body);
        straps.copyTransform(host.body);
        frontPanel.copyTransform(host.body);
        leftSleeve.copyTransform(host.leftArm);
        rightSleeve.copyTransform(host.rightArm);
        leftLeg.copyTransform(host.leftLeg);
        rightLeg.copyTransform(host.rightLeg);
        // BipedEntityModel-required parts — keep them invisible but in sync so
        // animations don't NPE on head/hat references.
        head.copyTransform(host.head);
        hat.visible = false;
    }

    public static TexturedModelData createBaseModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        // BipedEntityModel needs these placeholder parts to exist even if hidden.
        root.addChild(EntityModelPartNames.HEAD,
            ModelPartBuilder.create(), ModelTransform.NONE);
        root.addChild(EntityModelPartNames.HAT,
            ModelPartBuilder.create(), ModelTransform.NONE);
        root.addChild(EntityModelPartNames.BODY,
            ModelPartBuilder.create(), ModelTransform.NONE);
        root.addChild(EntityModelPartNames.LEFT_ARM,
            ModelPartBuilder.create(), ModelTransform.NONE);
        root.addChild(EntityModelPartNames.RIGHT_ARM,
            ModelPartBuilder.create(), ModelTransform.NONE);
        root.addChild(EntityModelPartNames.LEFT_LEG,
            ModelPartBuilder.create(), ModelTransform.NONE);
        root.addChild(EntityModelPartNames.RIGHT_LEG,
            ModelPartBuilder.create(), ModelTransform.NONE);

        // Body shell: 8x12x4 torso with slight extrusion.
        root.addChild("body_overlay",
            ModelPartBuilder.create()
                .uv(16, 16)
                .cuboid(-4f, 0f, -2f, 8f, 12f, 4f, new net.minecraft.client.model.Dilation(EXTRUSION)),
            ModelTransform.NONE);

        // Overalls straps — two thin bands going from waistband over the shoulders.
        ModelPartData strapsPart = root.addChild("straps",
            ModelPartBuilder.create(), ModelTransform.NONE);
        strapsPart.addChild("strap_left",
            ModelPartBuilder.create()
                .uv(0, 48)
                .cuboid(1.2f, -0.2f, -2.6f, 1.6f, 12f, 0.6f, new net.minecraft.client.model.Dilation(0)),
            ModelTransform.NONE);
        strapsPart.addChild("strap_right",
            ModelPartBuilder.create()
                .uv(8, 48)
                .cuboid(-2.8f, -0.2f, -2.6f, 1.6f, 12f, 0.6f, new net.minecraft.client.model.Dilation(0)),
            ModelTransform.NONE);

        // Sleeves — short, capping the upper arm.
        root.addChild("left_sleeve",
            ModelPartBuilder.create()
                .uv(40, 32)
                .cuboid(-1f, -2f, -2f, 4f, 5f, 4f, new net.minecraft.client.model.Dilation(EXTRUSION)),
            ModelTransform.pivot(5f, 2f, 0f));
        root.addChild("right_sleeve",
            ModelPartBuilder.create()
                .uv(40, 16)
                .cuboid(-3f, -2f, -2f, 4f, 5f, 4f, new net.minecraft.client.model.Dilation(EXTRUSION)),
            ModelTransform.pivot(-5f, 2f, 0f));

        // Apron front panel — flat rectangle hanging off the torso front.
        root.addChild("front_panel",
            ModelPartBuilder.create()
                .uv(0, 32)
                .cuboid(-4f, 0f, -2.6f, 8f, 14f, 0.4f, new net.minecraft.client.model.Dilation(0)),
            ModelTransform.NONE);

        // Leg overlays.
        root.addChild("left_leg_overlay",
            ModelPartBuilder.create()
                .uv(0, 16)
                .cuboid(-2f, 0f, -2f, 4f, 12f, 4f, new net.minecraft.client.model.Dilation(EXTRUSION)),
            ModelTransform.pivot(1.9f, 12f, 0f));
        root.addChild("right_leg_overlay",
            ModelPartBuilder.create()
                .uv(16, 48)
                .cuboid(-2f, 0f, -2f, 4f, 12f, 4f, new net.minecraft.client.model.Dilation(EXTRUSION)),
            ModelTransform.pivot(-1.9f, 12f, 0f));

        return TexturedModelData.of(data, 64, 64);
    }
}
