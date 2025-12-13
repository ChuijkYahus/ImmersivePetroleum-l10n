package flaxbeard.immersivepetroleum.client.render;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.SeismicSurveyTileEntity;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.List;

public class SeismicSurveyBarrelRenderer extends IPBlockEntityRenderer<SeismicSurveyTileEntity>{
	
	public static final ModelResourceLocation BARREL = ResourceUtils.ipModel("block/dyn/seismic_survey_tool_barrel");
	
	@Override
	public boolean shouldRenderOffScreen(@Nonnull SeismicSurveyTileEntity pBlockEntity){
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(SeismicSurveyTileEntity te, float partialTicks, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource buffer, int light, int overlay){
		if(te.isSlave || !te.getLevel().hasChunkAt(te.getBlockPos())){
			return;
		}
		
		matrix.pushPose();
		{
			double d = te.timer / (double) SeismicSurveyTileEntity.DELAY;
			
			matrix.translate(0, -0.125 * (1 - d), 0);
			
			List<BakedQuad> quads = MCUtil.getModel(BARREL).getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
			Pose last = matrix.last();
			VertexConsumer solid = buffer.getBuffer(RenderType.solid());
			for(BakedQuad quad: quads){
				solid.putBulkData(last, quad, 1.0F, 1.0F, 1.0F, 1.0F, light, overlay);
			}
		}
		matrix.popPose();
	}
}
