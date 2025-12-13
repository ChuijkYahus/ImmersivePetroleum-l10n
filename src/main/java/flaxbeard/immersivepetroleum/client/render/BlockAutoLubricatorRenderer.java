package flaxbeard.immersivepetroleum.client.render;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class BlockAutoLubricatorRenderer extends IPBlockEntityRenderer<AutoLubricatorTileEntity>{
	
	@Override
	public boolean shouldRenderOffScreen(@Nonnull AutoLubricatorTileEntity te){
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	@SuppressWarnings("unchecked, rawtypes")
	public void render(@Nonnull AutoLubricatorTileEntity lubricator, float partialTicks, @Nonnull PoseStack transform, @Nonnull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn){
		if(lubricator.isSlave)
			return;
		
		FluidStack fs = lubricator.tank.getFluid();
		float fluidLevel = 0;
		if(!fs.isEmpty()){
			fluidLevel = fs.getAmount() / (float) lubricator.tank.getCapacity();
		}
		
		if(fluidLevel > 0){
			float height = 16;
			
			transform.pushPose();
			{
				float scale = 0.0625f;
				transform.translate(0.25, 0.875, 0.25);
				transform.scale(scale, scale, scale);
				
				VertexConsumer builder = bufferIn.getBuffer(RenderType.solid());
				
				float h = height * fluidLevel;
				GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, h);
				transform.mulPose(Axis.YP.rotationDegrees(90));
				transform.translate(-7.98, 0, 0);
				GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, h);
				transform.mulPose(Axis.YP.rotationDegrees(90));
				transform.translate(-7.98, 0, 0);
				GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, h);
				transform.mulPose(Axis.YP.rotationDegrees(90));
				transform.translate(-7.98, 0, 0);
				GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, h);
				if(h < height){
					transform.mulPose(Axis.XP.rotationDegrees(90));
					transform.translate(0, 0, -h);
					GuiHelper.drawRepeatedFluidSprite(builder, transform, fs, 0, 0, 8, 8);
				}
			}
			transform.popPose();
		}
		
		transform.pushPose();
		{
			final Level level = lubricator.getLevel();
			BlockPos target = lubricator.getBlockPos().relative(lubricator.getFacing());
			
			if(level.getBlockEntity(target) instanceof IMultiblockBE<?> tile){
				ILubricationHandler handler = LubricatedHandler.getHandlerForTile(tile.getHelper());
				
				if(handler != null && handler.isPlacedCorrectly(level, lubricator.getBlockPos(), lubricator.getFacing())){
					IMultiblockBEHelperMaster<?> mbMaster = getMBMaster(level, tile.getHelper());
					
					if(mbMaster != null)
						handler.renderPipes(lubricator, mbMaster, transform, bufferIn, combinedLightIn, combinedOverlayIn);
				}
			}
		}
		transform.popPose();
	}
	
	private static IMultiblockBEHelperMaster<?> getMBMaster(Level level, IMultiblockBEHelper<?> helper){
		if(!(helper instanceof IMultiblockBEHelperMaster<?>) && helper.getContext() != null){
			BlockPos masterPos = helper.getContext().getLevel().toAbsolute(helper.getMultiblock().masterPosInMB());
			BlockEntity be = level.getBlockEntity(masterPos);
			
			if(be instanceof MultiblockBlockEntityMaster<?> master){
				return master.getHelper();
			}
		}
		
		return null;
	}
}
