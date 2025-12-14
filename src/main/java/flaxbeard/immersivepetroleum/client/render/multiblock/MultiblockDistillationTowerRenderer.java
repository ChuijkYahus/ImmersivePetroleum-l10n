package flaxbeard.immersivepetroleum.client.render.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DistillationTowerMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.State;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class MultiblockDistillationTowerRenderer extends IPMultiblockRenderer<State>{
	
	static final Face ACTIVE_BOILER_SIDE = Face.of(0, 0, 16, 24);
	static final Face ACTIVE_BOILER_FRONT = Face.of(16, 0, 32, 24);
	static final Face ACTIVE_BOILER_BACK = Face.of(16, 24, 32, 24);
	
	public MultiblockDistillationTowerRenderer(){
		super(() -> DistillationTowerMultiblock.INSTANCE);
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
				case NORTH -> {
					transform.translate(3, 0, 4);
				}
				case SOUTH -> {
					transform.mulPose(ROT_180);
					transform.translate(2, 0, 3);
				}
				case EAST -> {
					transform.mulPose(ROT_270);
					transform.translate(3, 0, 3);
				}
				case WEST -> {
					transform.mulPose(ROT_90);
					transform.translate(2, 0, 4);
				}
				default -> {
				}
			}
			
			VertexConsumer buf = bufferIn.getBuffer(IPRenderTypes.DISTILLATION_TOWER_ACTIVE_OVERLAY);
			if(te.getHelper().getContext().getLevel().getOrientation().mirrored()){
				transform.pushPose();
				{
					transform.translate(-4.0, 0.0, -4.0);
					final QuickDraw draw = new QuickDraw(buf, transform, 0xFFFFFFFF, combinedOverlayIn, LightTexture.FULL_BRIGHT);
					
					// Active Boiler Front
					Face face0 = ACTIVE_BOILER_FRONT;
					draw.vertex(-0.0015F, 0.5F, face0.w16(),				face0.u1(), face0.v1());
					draw.vertex(-0.0015F, 0.5F + face0.h16(), face0.w16(),	face0.u1(), face0.v0());
					draw.vertex(-0.0015F, 0.5F + face0.h16(), 0.0F,			face0.u0(), face0.v0());
					draw.vertex(-0.0015F, 0.5F, 0.0F,						face0.u0(), face0.v1());
					
					// Active Boiler Back
					Face face1 = ACTIVE_BOILER_BACK;
					draw.vertex(1.0015F, 0.5F + face1.h16(), 0.0F,			face1.u1(), face1.v0());
					draw.vertex(1.0015F, 0.5F + face1.h16(), face1.w16(),	face1.u0(), face1.v0());
					draw.vertex(1.0015F, 0.5F, face1.w16(),					face1.u0(), face1.v1());
					draw.vertex(1.0015F, 0.5F, 0.0F,						face1.u1(), face1.v1());
					
					// Active Boiler Side
					Face face2 = ACTIVE_BOILER_SIDE;
					draw.vertex(face2.w16(), 0.5F, 2.0015F,					face2.u1(), face2.v1());
					draw.vertex(face2.w16(), 0.5F + face2.h16(), 2.0015F,	face2.u1(), face2.v0());
					draw.vertex(0.0F, 0.5F + face2.h16(), 2.0015F,			face2.u0(), face2.v0());
					draw.vertex(0.0F, 0.5F, 2.0015F,						face2.u0(), face2.v1());
				}
				transform.popPose();
				
			}else{
				transform.pushPose();
				{
					transform.translate(-2.0, 0.0, -4.0);
					final QuickDraw draw = new QuickDraw(buf, transform, 0xFFFFFFFF, combinedOverlayIn, LightTexture.FULL_BRIGHT);
					
					// Active Boiler Front
					Face face1 = ACTIVE_BOILER_FRONT;
					draw.vertex(1.0015F, 0.5F + face1.h16(), 0.0F,			face1.u0(), face1.v0());
					draw.vertex(1.0015F, 0.5F + face1.h16(), face1.w16(),	face1.u1(), face1.v0());
					draw.vertex(1.0015F, 0.5F, face1.w16(),					face1.u1(), face1.v1());
					draw.vertex(1.0015F, 0.5F, 0.0F,						face1.u0(), face1.v1());
					
					// Active Boiler Back
					Face face0 = ACTIVE_BOILER_BACK;
					draw.vertex(-0.0015F, 0.5F, face0.w16(),				face0.u0(), face0.v1());
					draw.vertex(-0.0015F, 0.5F + face0.h16(), face0.w16(),	face0.u0(), face0.v0());
					draw.vertex(-0.0015F, 0.5F + face0.h16(), 0.0F,			face0.u1(), face0.v0());
					draw.vertex(-0.0015F, 0.5F, 0.0F,						face0.u1(), face0.v1());
					
					// Active Boiler Side
					Face face2 = ACTIVE_BOILER_SIDE;
					draw.vertex(face2.w16(), 0.5F, 2.0015F,					face2.u0(), face2.v1());
					draw.vertex(face2.w16(), 0.5F + face2.h16(), 2.0015F,	face2.u0(), face2.v0());
					draw.vertex(0.0F, 0.5F + face2.h16(), 2.0015F,			face2.u1(), face2.v0());
					draw.vertex(0.0F, 0.5F, 2.0015F,						face2.u1(), face2.v1());
				}
				transform.popPose();
			}
		}
		transform.popPose();
	}
	
}
