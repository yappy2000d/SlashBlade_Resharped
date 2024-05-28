package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.EntityDrive;
import mods.flammpfeil.slashblade.init.SBItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class DriveRenderer<T extends EntityDrive> extends EntityRenderer<T>
{
    private static final ResourceLocation modelLocation = new ResourceLocation(SlashBlade.MODID, "model/util/drive.obj");
    private static final ResourceLocation textureLocation = new ResourceLocation(SlashBlade.MODID, "model/util/slash.png");

    static private LazyLoadedValue<ItemStack> enchantedItem = new LazyLoadedValue<ItemStack>(() -> new ItemStack(SBItems.proudsoul));

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return textureLocation;
    }
    public DriveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStackIn)) {

            matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(entity.getRotationRoll()));
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));


            WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

            int lifetime = entity.getLifetime();

            float progress = Math.min(lifetime, (entity.tickCount+partialTicks)) / lifetime;

            double deathTime = lifetime;
            double baseAlpha = (Math.min(deathTime, Math.max(0, (lifetime - (entity.tickCount) - partialTicks))) / deathTime);
            baseAlpha = -Math.pow(baseAlpha - 1, 4.0)+1.0;

            matrixStackIn.scale(1,0.25f,1);

            float baseScale = 1.2f;
            matrixStackIn.scale(baseScale,baseScale,baseScale);

            float yscale = 0.03f;
            float scale = entity.getBaseSize() * Mth.lerp(progress, 0.03f,0.035f);

            int color = entity.getColor() & 0xFFFFFF;

            IConcentrationRank.ConcentrationRanks rank = entity.getRankCode();

            //rank color overwrite
            if(rank.level < IConcentrationRank.ConcentrationRanks.C.level){
                color = 0x555555;
            }


            ResourceLocation rl = getTextureLocation(entity);

            //baseAlpha = 1.0f;
            int alpha = ((0xFF & (int) (0xFF * baseAlpha)) << 24);

            //black alpha insidee
            if(IConcentrationRank.ConcentrationRanks.S.level <= rank.level)
                try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                    float windscale = entity.getBaseSize() * Mth.lerp(progress, 0.035f,0.03f);
                    matrixStackIn.scale(windscale, yscale, windscale);
                    Face.setAlphaOverride(Face.alphaOverrideYZZ);
                    Face.setUvOperator(1, 1, 0, -0.8f + progress * 0.3f);
                    BladeRenderState.setCol(0x222222 | alpha);
                    BladeRenderState.renderOverridedColorWrite(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn, packedLightIn);
                }

            //color alpha base
            if(IConcentrationRank.ConcentrationRanks.D.level <= rank.level)
                try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                    matrixStackIn.scale(scale, yscale, scale);
                    Face.setAlphaOverride(Face.alphaOverrideYZZ);
                    Face.setUvOperator(1,1,0, -0.35f + progress * -0.15f);
                    BladeRenderState.setCol(color | alpha);
                    BladeRenderState.renderOverridedColorWrite(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn, packedLightIn);
                }

            //white add outside
            if(IConcentrationRank.ConcentrationRanks.B.level <= rank.level)
                try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                    float windscale = entity.getBaseSize() * Mth.lerp(progress, 0.03f,0.0375f);
                    matrixStackIn.scale(windscale, yscale, windscale);
                    Face.setAlphaOverride(Face.alphaOverrideYZZ);
                    Face.setUvOperator(1, 1, 0, -0.5f + progress * -0.2f);
                    BladeRenderState.setCol(0x404040 | alpha);
                    BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn, packedLightIn);
                }

            //color add base
            try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                matrixStackIn.scale(scale, yscale, scale);
                Face.setAlphaOverride(Face.alphaOverrideYZZ);
                Face.setUvOperator(1, 1, 0, -0.35f + progress * -0.15f);
                BladeRenderState.setCol(color | alpha);
                BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn, packedLightIn);
            }
        }
    }
}
