package net.narutotoexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.content.entity.BijuudamaEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Draws the Bijuudama as a camera-facing, fully bright, tinted sphere sprite (placeholder texture). */
public class BijuudamaRenderer extends EntityRenderer<BijuudamaEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NarutotoExpansion.MODID, "textures/entity/bijuudama.png");

    public BijuudamaRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BijuudamaEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        float scale = entity.getVisualScale();
        poseStack.translate(0.0f, entity.getBbHeight() / 2.0f, 0.0f);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        int color = entity.getColor();
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        int light = LightTexture.FULL_BRIGHT;
        vertex(vc, matrix, normal, light, r, g, b, -0.5f, -0.5f, 0.0f, 1.0f);
        vertex(vc, matrix, normal, light, r, g, b, 0.5f, -0.5f, 1.0f, 1.0f);
        vertex(vc, matrix, normal, light, r, g, b, 0.5f, 0.5f, 1.0f, 0.0f);
        vertex(vc, matrix, normal, light, r, g, b, -0.5f, 0.5f, 0.0f, 0.0f);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, int light, int r, int g, int b,
                               float x, float y, float u, float v) {
        vc.vertex(matrix, x, y, 0.0f).color(r, g, b, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BijuudamaEntity entity) {
        return TEXTURE;
    }
}
