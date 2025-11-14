package flaxbeard.immersivepetroleum.client.render;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.render.tile.IEBlockEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class MultiblockDistillationTowerRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<DistillationTowerLogic.State>>{
	@Override
	public boolean shouldRenderOffScreen(@Nonnull MultiblockBlockEntityMaster<DistillationTowerLogic.State> te){
		return true;
	}
	
	@Override
	public void render(@Nonnull MultiblockBlockEntityMaster<DistillationTowerLogic.State> te, float partialTicks, @Nonnull PoseStack transform, @Nonnull MultiBufferSource bufferIn, int light, int overlay){
		if(te.isRemoved() || te.getLevel() == null || !te.getLevel().hasChunkAt(te.getBlockPos()))
			return;
		
		
		if(te.getHelper().getState().wasActive){
			overlay = OverlayTexture.NO_OVERLAY;
			
			transform.pushPose();
			{
				Direction rotation = te.getHelper().getContext().getLevel().getOrientation().front();
				switch(rotation){
					case NORTH -> {
						// transform.rotate(new Quaternion(0, 0, 0, true));
						transform.translate(3, 0, 4);
					}
					case SOUTH -> {
						transform.mulPose(Axis.YP.rotationDegrees(180F));
						transform.translate(2, 0, 3);
					}
					case EAST -> {
						transform.mulPose(Axis.YP.rotationDegrees(270F));
						transform.translate(3, 0, 3);
					}
					case WEST -> {
						transform.mulPose(Axis.YP.rotationDegrees(90F));
						transform.translate(2, 0, 4);
					}
					default -> {
					}
				}
				
				// Is it the most efficient way of doing this? Probably not.
				// Does it make me look smart af? hell yeah!
				VertexConsumer buf = bufferIn.getBuffer(IPRenderTypes.DISTILLATION_TOWER_ACTIVE);
				if(te.getHelper().getContext().getLevel().getOrientation().mirrored()){
					transform.pushPose();
					{
						transform.translate(-4.0, 0.0, -4.0);
						final QuickDraw draw = new QuickDraw(buf, transform, 0xBFBFBFFF, overlay, light);
						
						// Active Boiler Front
						int ux = 96, vy = 134;
						int w = 32, h = 24;
						float uw = w / 256F, vh = h / 256F, u0 = ux / 256F, v0 = vy / 256F, u1 = u0 + uw, v1 = v0 + vh;
						
						draw.vertex(-0.0015F, 0.5F, w / 16F,			u1, v1);
						draw.vertex(-0.0015F, 0.5F + h / 16F, w / 16F,	u1, v0);
						draw.vertex(-0.0015F, 0.5F + h / 16F, 0.0F,		u0, v0);
						draw.vertex(-0.0015F, 0.5F, 0.0F,				u0, v1);
						
						// Active Boiler Back
						ux = 96; vy = 158;
						w = 32; h = 24;
						uw = w / 256F; vh = h / 256F; u0 = ux / 256F; v0 = vy / 256F; u1 = u0 + uw; v1 = v0 + vh;
						
						draw.vertex(1.0015F, 0.5F + h / 16F, 0.0F,		u1, v0);
						draw.vertex(1.0015F, 0.5F + h / 16F, w / 16F,	u0, v0);
						draw.vertex(1.0015F, 0.5F, w / 16F,				u0, v1);
						draw.vertex(1.0015F, 0.5F, 0.0F,				u1, v1);
						
						// Active Boiler Side
						ux = 80; vy = 134;
						w = 16; h = 24;
						uw = w / 256F; vh = h / 256F; u0 = ux / 256F; v0 = vy / 256F; u1 = u0 + uw; v1 = v0 + vh;
						
						draw.vertex(w / 16F, 0.5F, 2.0015F,				u1, v1);
						draw.vertex(w / 16F, 0.5F + h / 16F, 2.0015F,	u1, v0);
						draw.vertex(0.0F, 0.5F + h / 16F, 2.0015F,		u0, v0);
						draw.vertex(0.0F, 0.5F, 2.0015F,				u0, v1);
					}
					transform.popPose();
					
				}else{
					transform.pushPose();
					{
						transform.translate(-2.0, 0.0, -4.0);
						final QuickDraw draw = new QuickDraw(buf, transform, 0xBFBFBFFF, overlay, light);
						
						// Active Boiler Back
						int ux = 96, vy = 158;
						int w = 32, h = 24;
						float uw = w / 256F, vh = h / 256F, u0 = ux / 256F, v0 = vy / 256F, u1 = u0 + uw, v1 = v0 + vh;
						
						draw.vertex(-0.0015F, 0.5F, w / 16F, u0, v1);
						draw.vertex(-0.0015F, 0.5F + h / 16F, w / 16F, u0, v0);
						draw.vertex(-0.0015F, 0.5F + h / 16F, 0.0F, u1, v0);
						draw.vertex(-0.0015F, 0.5F, 0.0F, u1, v1);
						
						// Active Boiler Front
						ux = 96; vy = 134;
						w = 32; h = 24;
						uw = w / 256F; vh = h / 256F; u0 = ux / 256F; v0 = vy / 256F; u1 = u0 + uw; v1 = v0 + vh;
						
						draw.vertex(1.0015F, 0.5F + h / 16F, 0.0F, u0, v0);
						draw.vertex(1.0015F, 0.5F + h / 16F, w / 16F, u1, v0);
						draw.vertex(1.0015F, 0.5F, w / 16F, u1, v1);
						draw.vertex(1.0015F, 0.5F, 0.0F, u0, v1);
						
						// Active Boiler Side
						ux = 80; vy = 134;
						w = 16; h = 24;
						uw = w / 256F; vh = h / 256F; u0 = ux / 256F; v0 = vy / 256F; u1 = u0 + uw; v1 = v0 + vh;
						
						draw.vertex(w / 16F, 0.5F, 2.0015F, u0, v1);
						draw.vertex(w / 16F, 0.5F + h / 16F, 2.0015F, u0, v0);
						draw.vertex(0.0F, 0.5F + h / 16F, 2.0015F, u1, v0);
						draw.vertex(0.0F, 0.5F, 2.0015F, u1, v1);
					}
					transform.popPose();
				}
			}
			transform.popPose();
		}
	}
	
	private record QuickDraw(VertexConsumer buf, PoseStack pose, int brightnessRGBA, int overlay, int light){
		private void vertex(float x, float y, float z, float u, float v){
			//@formatter:off
			this.buf.addVertex(this.pose.last().pose(), x, y, z)
				.setColor(this.brightnessRGBA)
				.setUv(u, v)
				.setOverlay(this.overlay)
				.setLight(this.light)
				.setNormal(1, 1, 1);
			//@formatter:on
		}
	}
}
