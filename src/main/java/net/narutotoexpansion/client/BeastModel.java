package net.narutotoexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.content.entity.TailedBeastEntity;

/**
 * PLACEHOLDER model built in code (no model asset files): a quadruped with one tail per tail of the beast.
 * Replace with a real model (e.g. GeckoLib) when original art exists; the entity logic does not depend on it.
 */
public class BeastModel<T extends TailedBeastEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(NarutotoExpansion.MODID, "beast"), "main");
    private static final int MAX_TAILS = 9;
    private static final float RAD = 0.017453292f;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;
    private final ModelPart[] tails = new ModelPart[MAX_TAILS];

    public BeastModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.legFrontLeft = root.getChild("leg_fl");
        this.legFrontRight = root.getChild("leg_fr");
        this.legBackLeft = root.getChild("leg_bl");
        this.legBackRight = root.getChild("leg_br");
        for (int i = 0; i < MAX_TAILS; i++) {
            tails[i] = root.getChild("tail" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -6.0f, -10.0f, 12.0f, 11.0f, 20.0f),
                PartPose.offset(0.0f, 13.0f, 0.0f));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-5.0f, -5.0f, -8.0f, 10.0f, 9.0f, 8.0f)
                        .texOffs(36, 32).addBox(-5.0f, -8.0f, -4.0f, 3.0f, 3.0f, 2.0f)
                        .texOffs(36, 32).addBox(2.0f, -8.0f, -4.0f, 3.0f, 3.0f, 2.0f)
                        .texOffs(0, 50).addBox(-3.0f, 0.0f, -12.0f, 6.0f, 4.0f, 4.0f),
                PartPose.offset(0.0f, 9.0f, -10.0f));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(36, 40).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 9.0f, 4.0f);
        root.addOrReplaceChild("leg_fl", leg, PartPose.offset(4.0f, 15.0f, -7.0f));
        root.addOrReplaceChild("leg_fr", leg, PartPose.offset(-4.0f, 15.0f, -7.0f));
        root.addOrReplaceChild("leg_bl", leg, PartPose.offset(4.0f, 15.0f, 7.0f));
        root.addOrReplaceChild("leg_br", leg, PartPose.offset(-4.0f, 15.0f, 7.0f));

        CubeListBuilder tail = CubeListBuilder.create().texOffs(0, 56).addBox(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 14.0f);
        for (int i = 0; i < MAX_TAILS; i++) {
            root.addOrReplaceChild("tail" + i, tail, PartPose.offset(0.0f, 10.0f, 9.0f));
        }
        return LayerDefinition.create(mesh, 64, 80);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * RAD;
        head.xRot = headPitch * RAD;
        float swing = Mth.cos(limbSwing * 0.6662f) * 1.2f * limbSwingAmount;
        legFrontLeft.xRot = swing;
        legBackRight.xRot = swing;
        legFrontRight.xRot = -swing;
        legBackLeft.xRot = -swing;

        int count = entity.getBeast().tails();
        for (int i = 0; i < MAX_TAILS; i++) {
            ModelPart tail = tails[i];
            tail.visible = i < count;
            if (i < count) {
                float spread = (i - (count - 1) / 2.0f) * (count > 1 ? 0.55f / Math.max(1, count - 1) * 2.0f : 0.0f);
                tail.yRot = spread + Mth.sin(ageInTicks * 0.08f + i) * 0.08f;
                tail.xRot = -0.9f + Mth.cos(ageInTicks * 0.06f + i * 0.7f) * 0.06f;
            }
        }
        head.xRot += entity.isWeakened() ? 0.5f : 0.0f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
