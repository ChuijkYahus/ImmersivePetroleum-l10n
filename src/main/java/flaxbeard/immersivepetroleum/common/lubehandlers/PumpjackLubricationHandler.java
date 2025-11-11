package flaxbeard.immersivepetroleum.common.lubehandlers;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import com.mojang.blaze3d.vertex.PoseStack;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.IPModel;
import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.PumpjackLogic;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class PumpjackLubricationHandler implements ILubricationHandler<IMultiblockBEHelperMaster<PumpjackLogic.State>, PumpjackLogic.State>{
	private static final BlockPos RELATIVE_GHOST_POS = new BlockPos(3, 0, 4);
	
	@Override
	public boolean isPlacedCorrectly(Level world, BlockPos lubricatorPosition, Direction lubricatorFacing){
		final BlockPos target = lubricatorPosition.relative(lubricatorFacing);
		
		if(world.getBlockEntity(target) instanceof IMultiblockBE<?> mb && mb.getHelper().getContext() != null){
			IMultiblockLevel level = mb.getHelper().getContext().getLevel();
			
			if(level.toRelative(lubricatorPosition).equals(RELATIVE_GHOST_POS)){
				MultiblockOrientation orientation = level.getOrientation();
				return orientation.front().getClockWise() == (orientation.mirrored() ? lubricatorFacing : lubricatorFacing.getOpposite());
			}
		}
		
		return false;
	}
	
	@Override
	public GhostInfo getGhostBlockPosition(Level world, IMultiblockBEHelperMaster<PumpjackLogic.State> mbte){
		IMultiblockLevel level = mbte.getContext().getLevel();
		
		BlockPos position = level.toAbsolute(RELATIVE_GHOST_POS);
		
		MultiblockOrientation orientation = level.getOrientation();
		Direction facing = (orientation.mirrored() ? orientation.front().getOpposite() : orientation.front()).getCounterClockWise();
		
		return new GhostInfo(position, facing);
	}
	
	@Override
	public boolean isMachineEnabled(Level world, IMultiblockBEHelperMaster<PumpjackLogic.State> mbte){
		return mbte.getState().wasActive;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void lubricateClient(ClientLevel world, Fluid lubricant, int ticks, IMultiblockBEHelperMaster<PumpjackLogic.State> mbte){
		mbte.getState().activeTicks += 1F / 4F;
	}
	
	@Override
	public void lubricateServer(ServerLevel world, Fluid lubricant, int ticks, IMultiblockBEHelperMaster<PumpjackLogic.State> mbte){
		if(ticks % 4 == 0){
			mbte.tickServer();
		}
	}
	
	@Override
	public void spawnLubricantParticles(ClientLevel world, BlockPos lubricatorPosition, Direction facing, IMultiblockBEHelperMaster<PumpjackLogic.State> mbte){
		Direction f = mbte.getContext().getLevel().getOrientation().mirrored() ? facing : facing.getOpposite();
		float location = world.random.nextFloat();
		
		boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mbte.getContext().getLevel().getOrientation().mirrored();
		float xO = 2.5F;
		float zO = -.15F;
		float yO = 2.25F;
		
		if(location > .5F){
			xO = 1.7F;
			yO = 2.9F;
			zO = -1.5F;
			
		}
		
		if(facing.getAxisDirection() == AxisDirection.NEGATIVE)
			xO = -xO + 1;
		if(!flip)
			zO = -zO + 1;
		
		float x = lubricatorPosition.getX() + (f.getAxis() == Axis.X ? xO : zO);
		float y = lubricatorPosition.getY() + yO;
		float z = lubricatorPosition.getZ() + (f.getAxis() == Axis.X ? zO : xO);
		
		for(int i = 0;i < 3;i++){
			float r1 = (world.random.nextFloat() - .5F) * 2F;
			float r2 = (world.random.nextFloat() - .5F) * 2F;
			float r3 = world.random.nextFloat();
			
			world.addParticle(ParticleTypes.FALLING_HONEY, x, y, z, r1 * 0.04F, r3 * 0.0125F, r2 * 0.025F);
		}
	}
	
	private static final ResourceLocation TEXTURE = ResourceUtils.ip("textures/models/lube_pipe.png");
	
	@OnlyIn(Dist.CLIENT)
	private static Supplier<IPModel> pipes_normal;
	
	@OnlyIn(Dist.CLIENT)
	private static Supplier<IPModel> pipes_mirrored;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(AutoLubricatorTileEntity lubricator, IMultiblockBEHelperMaster<PumpjackLogic.State> mbte, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		if(mbte.getContext() == null)
			return;
		
		final MultiblockOrientation orientation = mbte.getContext().getLevel().getOrientation();
		final boolean mirrored = orientation.mirrored();
		final Direction rotation = orientation.front();
		
		if(rotation == Direction.NORTH){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90F));
			matrix.translate(-2, 0, mirrored ? 1 : -3);
			
		}else if(rotation == Direction.SOUTH){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270F));
			matrix.translate(-1, 0, mirrored ? 0 : -4);
			
		}else if(rotation == Direction.EAST){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0F));
			matrix.translate(-1, 0, mirrored ? 1 : -3);
			
		}else if(rotation == Direction.WEST){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180F));
			matrix.translate(-2, 0, mirrored ? 0 : -4);
		}
		
		IPModel model;
		if(mirrored){
			if(pipes_mirrored == null)
				pipes_mirrored = IPModels.getSupplier(ModelLubricantPipes.Pumpjack.ID_MIRRORED);
			
			model = pipes_mirrored.get();
		}else{
			if(pipes_normal == null)
				pipes_normal = IPModels.getSupplier(ModelLubricantPipes.Pumpjack.ID_NORMAL);
			
			model = pipes_normal.get();
		}
		
		if(model != null){
			model.renderToBuffer(matrix, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight, combinedOverlay);
		}
	}
}
