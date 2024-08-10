package invmod.client.render;

import invmod.common.entity.EntityIMCreeper;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.client.render.entity.feature.EnergySwirlOverlayFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Copy of CreeperEntityRenderer modified to use different textures
 *
 * @see net.minecraft.client.render.entity.CreeperEntityRenderer
 */
public class RenderIMCreeper extends LivingEntityRenderer<EntityIMCreeper, CreeperEntityModel<EntityIMCreeper>> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/creeper/creeper.png");

	public RenderIMCreeper(EntityRendererFactory.Context context) {
        super(context, new CreeperEntityModel<>(context.getPart(EntityModelLayers.CREEPER)), 0.5F);
        this.addFeature(new ChargeFeature(this, context.getModelLoader()));
    }

    @Override
    protected void scale(EntityIMCreeper creeperEntity, MatrixStack matrices, float tickDelta) {
        float fuseTime = creeperEntity.getClientFuseTime(tickDelta);
        float magnitude = 1 + MathHelper.sin(fuseTime * 100) * fuseTime * 0.01F;
        fuseTime = (float)Math.pow(MathHelper.clamp(fuseTime, 0, 1), 3);
        float horScale = (1 + fuseTime * 0.4F) * magnitude;
        float verScale = (1 + fuseTime * 0.1F) / magnitude;
        matrices.scale(horScale, verScale, horScale);
    }

    @Override
    protected float getAnimationCounter(EntityIMCreeper creeperEntity, float tickDelta) {
        float fuseTime = creeperEntity.getClientFuseTime(tickDelta);
        return (int)(fuseTime * 10) % 2 == 0 ? 0 : MathHelper.clamp(fuseTime, 0.5F, 1);
    }

    @Override
    public Identifier getTexture(EntityIMCreeper creeperEntity) {
        return TEXTURE;
    }

    /**
     * Copy of {@link CreeperChargeFeatureRenderer}
     *
     * @see net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer
     */
    private static final class ChargeFeature extends EnergySwirlOverlayFeatureRenderer<EntityIMCreeper, CreeperEntityModel<EntityIMCreeper>> {
        private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/creeper/creeper_armor.png");
        private final CreeperEntityModel<EntityIMCreeper> model;

        public ChargeFeature(FeatureRendererContext<EntityIMCreeper, CreeperEntityModel<EntityIMCreeper>> context, EntityModelLoader loader) {
            super(context);
            model = new CreeperEntityModel<>(loader.getModelPart(EntityModelLayers.CREEPER_ARMOR));
        }

        @Override
        protected float getEnergySwirlX(float partialAge) {
            return partialAge * 0.01F;
        }

        @Override
        protected Identifier getEnergySwirlTexture() {
            return SKIN;
        }

        @Override
        protected EntityModel<EntityIMCreeper> getEnergySwirlModel() {
            return model;
        }
    }
}