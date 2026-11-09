package net.narutotoexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.content.entity.TailedBeastEntity;

public class BeastRenderer extends MobRenderer<TailedBeastEntity, BeastModel<TailedBeastEntity>> {
    /** The placeholder model is 1.5 blocks tall at scale 1. */
    private static final float MODEL_HEIGHT = 1.5f;

    public BeastRenderer(EntityRendererProvider.Context context) {
        super(context, new BeastModel<>(context.bakeLayer(BeastModel.LAYER)), 1.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TailedBeastEntity entity) {
        return new ResourceLocation(NarutotoExpansion.MODID, "textures/entity/" + entity.getBeast().key + ".png");
    }

    @Override
    protected void scale(TailedBeastEntity entity, PoseStack poseStack, float partialTick) {
        float s = entity.getBeast().height / MODEL_HEIGHT;
        poseStack.scale(s, s, s);
        this.shadowRadius = entity.getBeast().width * 0.5f;
    }
}
