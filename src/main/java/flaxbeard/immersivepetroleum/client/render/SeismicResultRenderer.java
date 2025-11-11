package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.client.render.dyn.DynamicTextureWrapper;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.items.SurveyResultItem;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.survey.ISurveyInfo;
import flaxbeard.immersivepetroleum.common.util.survey.SurveyScan;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import org.joml.Matrix4f;

/**
 * @author TwistedGate
 */
public class SeismicResultRenderer{
	
	static final Tesselator TESSELATOR = new Tesselator();
	
	private static final ResourceLocation OVERLAY = ResourceUtils.ip("textures/gui/seismicsurvey_overlay.png");
	
	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event){
		if(event.getItemStack().getItem() instanceof SurveyResultItem && MCUtil.getPlayer().distanceTo(event.getItemFrameEntity()) < 1000){
			if(ISurveyInfo.from(event.getItemStack()) instanceof SurveyScan scan){
				DynamicTextureWrapper wrapper = DynamicTextureWrapper.getOrCreate(SurveyScan.SCAN_SIZE, SurveyScan.SCAN_SIZE, scan);
				
				if(wrapper != null){
					PoseStack matrix = event.getPoseStack();
					// MultiBufferSource buffer = event.getMultiBufferSource(); // Breaks things left and right
					MultiBufferSource.BufferSource buffer = RenderUtils.immediate();
					
					matrix.pushPose();
					{
						int light = event.getPackedLight();
						int rot = event.getItemFrameEntity().getRotation();
						float scale = 1F / SurveyScan.SCAN_SIZE;
						matrix.mulPose(Axis.ZP.rotationDegrees(180F - -rot * 45F));
						matrix.translate(-0.5F, -0.5F, -0.00625F);
						matrix.scale(scale, scale, 1F);
						
						matrix.pushPose();
						{
							int a = wrapper.width;
							int b = wrapper.height;
							VertexConsumer builder = buffer.getBuffer(wrapper.renderType);
							Matrix4f mat = matrix.last().pose();
							
							builder.addVertex(mat, 0, 0, 0).setColor(-1).setUv(1, 1).setLight(light);
							builder.addVertex(mat, 0, b, 0).setColor(-1).setUv(1, 0).setLight(light);
							builder.addVertex(mat, a, b, 0).setColor(-1).setUv(0, 0).setLight(light);
							builder.addVertex(mat, a, 0, 0).setColor(-1).setUv(0, 1).setLight(light);
						}
						matrix.popPose();
						
						// Draw Grid-Overlay
						matrix.pushPose();
						{
							final int w = SurveyScan.SCAN_SIZE;
							final int h = SurveyScan.SCAN_SIZE;
							
							matrix.translate(0, 0, -0.002F);
							
							VertexConsumer builder = buffer.getBuffer(RenderType.text(OVERLAY));
							Matrix4f mat = matrix.last().pose();
							
							builder.addVertex(mat, 0, 0, 0).setColor(-1).setUv(0, 0).setLight(light);
							builder.addVertex(mat, 0, h, 0).setColor(-1).setUv(0, 1).setLight(light);
							builder.addVertex(mat, w, h, 0).setColor(-1).setUv(1, 1).setLight(light);
							builder.addVertex(mat, w, 0, 0).setColor(-1).setUv(1, 0).setLight(light);
						}
						matrix.popPose();
					}
					matrix.popPose();
					
					buffer.endBatch();
					event.setCanceled(true);
				}
			}
		}
	}
}
