package flaxbeard.immersivepetroleum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Function;

public abstract class IPModel extends Model{
	public IPModel(Function<ResourceLocation, RenderType> renderTypeIn){
		super(renderTypeIn);
	}
	
	/**
	 * This is where the model parts should be created, to keep things separate. (And for easier refreshing)
	 */
	public abstract void init();
	
	public abstract void render(@Nonnull PoseStack matrixStackIn, @Nonnull VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color);
	
	// Overridden just to have proper arg names
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color){
		render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
	}
	
	/** Creates a single cube */
	protected final CubeListBuilder singleCube(float pOriginX, float pOriginY, float pOriginZ, float pDimensionX, float pDimensionY, float pDimensionZ){
		return singleCube(0, 0, pOriginX, pOriginY, pOriginZ, pDimensionX, pDimensionY, pDimensionZ);
	}
	
	/** Creates a single cube with texture offset */
	protected final CubeListBuilder singleCube(int pXTexOffs, int pYTexOffs, float pOriginX, float pOriginY, float pOriginZ, float pDimensionX, float pDimensionY, float pDimensionZ){
		return CubeListBuilder.create().texOffs(pXTexOffs, pYTexOffs).addBox(pOriginX, pOriginY, pOriginZ, pDimensionX, pDimensionY, pDimensionZ);
	}
	
	/** Creates an empty CubeListBuilder. Same as doing <code>CubeListBuilder.create();</code> */
	protected final CubeListBuilder empty(){
		return CubeListBuilder.create();
	}
}
