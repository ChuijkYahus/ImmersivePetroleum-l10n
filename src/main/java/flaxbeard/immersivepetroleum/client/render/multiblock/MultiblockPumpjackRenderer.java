package flaxbeard.immersivepetroleum.client.render.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.client.model.IPModel;
import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.client.model.ModelPumpjack;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.PumpjackMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.PumpjackLogic.State;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class MultiblockPumpjackRenderer extends IPMultiblockRenderer<State>{
	private static final Supplier<IPModel> pumpjackarm = IPModels.getSupplier(ModelPumpjack.ID);
	
	public MultiblockPumpjackRenderer(){
		super(() -> PumpjackMultiblock.INSTANCE);
	}
	
	@Override
	public int getViewDistance(){
		return 100;
	}
	
	@Override
	public void render(@Nonnull MultiblockBlockEntityMaster<State> te, float partialTicks, @Nonnull PoseStack transform, @Nonnull MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn){
		if(te.isRemoved() || te.getLevel() == null || !te.getLevel().hasChunkAt(te.getBlockPos()))
			return;
		
		transform.pushPose();
		Direction rotation = te.getHelper().getContext().getLevel().getOrientation().front();
		switch(rotation){
			case NORTH -> {
				transform.mulPose(ROT_90);
				transform.translate(-6, 0, -1);
			}
			case EAST -> {
				transform.translate(-5, 0, -1);
			}
			case SOUTH -> {
				transform.mulPose(ROT_270);
				transform.translate(-5, 0, -2);
			}
			case WEST -> {
				transform.mulPose(ROT_180);
				transform.translate(-6, 0, -2);
			}
			default -> {
			}
		}
		
		ModelPumpjack model;
		if((model = (ModelPumpjack) pumpjackarm.get()) != null){
			float ticks = te.getHelper().getState().activeTicks + (te.getHelper().getState().wasActive ? partialTicks : 0);
			model.ticks = 1.5F * ticks;
			
			model.renderToBuffer(transform, buffer.getBuffer(model.renderType(ModelPumpjack.TEXTURE)), combinedLightIn, combinedOverlayIn, -1);
		}
		transform.popPose();
	}
}
