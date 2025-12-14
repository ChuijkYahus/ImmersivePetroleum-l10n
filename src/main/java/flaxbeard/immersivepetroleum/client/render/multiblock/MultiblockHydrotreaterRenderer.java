package flaxbeard.immersivepetroleum.client.render.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.HydroTreaterMultiblock;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater.HydroTreaterLogic.State;

public class MultiblockHydrotreaterRenderer extends IPMultiblockRenderer<State>{
	
	static final Face ACTIVE_FRONT = Face.of(0, 0, 32, 40);
	static final Face ACTIVE_SIDE = Face.of(32, 0, 16, 40);
	
	public MultiblockHydrotreaterRenderer(){
		super(() -> HydroTreaterMultiblock.INSTANCE);
	}
	
	@Override
	public void render(@Nonnull MultiblockBlockEntityMaster<State> te, float partialTicks, @Nonnull PoseStack transform, @Nonnull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn){
		if(te.isRemoved() || te.getLevel() == null || !te.getLevel().hasChunkAt(te.getBlockPos()))
			return;
		
		if(te.getHelper().getState().cooldownTicks <= 0)
			return;
		
		combinedOverlayIn = OverlayTexture.NO_OVERLAY;
		
		transform.pushPose();
		{
			Direction rotation = te.getHelper().getContext().getLevel().getOrientation().front();
			switch(rotation){
				case EAST -> {
					transform.mulPose(ROT_270);
					transform.translate(0, 0, 0);
				}
				case SOUTH -> {
					transform.mulPose(ROT_180);
					transform.translate(-1, 0, 0);
				}
				case WEST -> {
					transform.mulPose(ROT_90);
					transform.translate(-1, 0, 1);
				}
				case NORTH -> {
					transform.translate(0, 0, 1);
				}
				default -> {
				}
			}
			
			VertexConsumer buf = bufferIn.getBuffer(IPRenderTypes.HYDROTREATER_ACTIVE_OVERLAY);
			if(te.getHelper().getContext().getLevel().getOrientation().mirrored()){
				transform.pushPose();
				{
					final QuickDraw draw = new QuickDraw(buf, transform, 0xFFFFFFFF, combinedOverlayIn, LightTexture.FULL_BRIGHT);
					
					// Active Boiler Front
					Face face1 = ACTIVE_SIDE;
					draw.vertex(1.0015F, 0.5F + face1.h16(), 0.0F,			face1.u0(), face1.v0());
					draw.vertex(1.0015F, 0.5F + face1.h16(), face1.w16(),	face1.u1(), face1.v0());
					draw.vertex(1.0015F, 0.5F, face1.w16(),					face1.u1(), face1.v1());
					draw.vertex(1.0015F, 0.5F, 0.0F,						face1.u0(), face1.v1());
					
					// Active Boiler Back
					Face face0 = ACTIVE_SIDE;
					draw.vertex(-1.0015F, 0.5F, face0.w16(),				face0.u0(), face0.v1());
					draw.vertex(-1.0015F, 0.5F + face0.h16(), face0.w16(),	face0.u0(), face0.v0());
					draw.vertex(-1.0015F, 0.5F + face0.h16(), 0.0F,			face0.u1(), face0.v0());
					draw.vertex(-1.0015F, 0.5F, 0.0F,						face0.u1(), face0.v1());
					
					// Active Boiler Side
					Face face2 = ACTIVE_FRONT;
					draw.vertex(face2.w16() - 1.0F, 0.5F, 1.0015F,					face2.u0(), face2.v1());
					draw.vertex(face2.w16() - 1.0F, 0.5F + face2.h16(), 1.0015F,	face2.u0(), face2.v0());
					draw.vertex(-1.0F, 0.5F + face2.h16(), 1.0015F,					face2.u1(), face2.v0());
					draw.vertex(-1.0F, 0.5F, 1.0015F,								face2.u1(), face2.v1());
				}
				transform.popPose();
			}else{
				transform.pushPose();
				{
					final QuickDraw draw = new QuickDraw(buf, transform, 0xFFFFFFFF, combinedOverlayIn, LightTexture.FULL_BRIGHT);
					
					// Active Boiler Front
					Face face1 = ACTIVE_SIDE;
					draw.vertex(2.0015F, 0.5F + face1.h16(), 0.0F,			face1.u0(), face1.v0());
					draw.vertex(2.0015F, 0.5F + face1.h16(), face1.w16(),	face1.u1(), face1.v0());
					draw.vertex(2.0015F, 0.5F, face1.w16(),					face1.u1(), face1.v1());
					draw.vertex(2.0015F, 0.5F, 0.0F,						face1.u0(), face1.v1());
					
					// Active Boiler Back
					Face face0 = ACTIVE_SIDE;
					draw.vertex(-0.0015F, 0.5F, face0.w16(),				face0.u0(), face0.v1());
					draw.vertex(-0.0015F, 0.5F + face0.h16(), face0.w16(),	face0.u0(), face0.v0());
					draw.vertex(-0.0015F, 0.5F + face0.h16(), 0.0F,			face0.u1(), face0.v0());
					draw.vertex(-0.0015F, 0.5F, 0.0F,						face0.u1(), face0.v1());
					
					// Active Boiler Side
					Face face2 = ACTIVE_FRONT;
					draw.vertex(face2.w16(), 0.5F, 1.0015F,					face2.u1(), face2.v1());
					draw.vertex(face2.w16(), 0.5F + face2.h16(), 1.0015F,	face2.u1(), face2.v0());
					draw.vertex(0.0F, 0.5F + face2.h16(), 1.0015F,			face2.u0(), face2.v0());
					draw.vertex(0.0F, 0.5F, 1.0015F,						face2.u0(), face2.v1());
					
				}
				transform.popPose();
			}
		}
		transform.popPose();
	}
}
