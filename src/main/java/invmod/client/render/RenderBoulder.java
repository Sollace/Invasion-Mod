package invmod.client.render;

import invmod.common.InvasionMod;
import invmod.common.entity.EntityIMBoulder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RenderBoulder extends EntityRenderer<EntityIMBoulder> {
	private static final Identifier TEXTURE = InvasionMod.id("textures/boulder.png");

    private final ModelBoulder model = new ModelBoulder(ModelBoulder.getTexturedModelData().createModel());

    public RenderBoulder(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(EntityIMBoulder entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        model.render(matrices, vertexConsumers.getBuffer(model.getLayer(getTexture(entity))), light, 0);
    }

    @Override
    public Identifier getTexture(EntityIMBoulder entity) {
        return TEXTURE;
    }
}