package flaxbeard.immersivepetroleum.common.lubehandlers;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.client.model.IPModel;
import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.client.model.ModelLubricantPipes;
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

public class CrusherLubricationHandler implements ILubricationHandler<IMultiblockBEHelperMaster<CrusherLogic.State>, CrusherLogic.State>{
	private static final BlockPos RELATIVE_GHOST_POS = new BlockPos(2, 0, -1);
	
	@Override
	public boolean isPlacedCorrectly(Level world, BlockPos lubricatorPosition, Direction lubricatorFacing){
		final BlockPos target = lubricatorPosition.relative(lubricatorFacing);
		
		if(world.getBlockEntity(target) instanceof IMultiblockBE<?> mb && mb.getHelper().getContext() != null){
			IMultiblockLevel level = mb.getHelper().getContext().getLevel();
			
			if(level.toRelative(lubricatorPosition).equals(RELATIVE_GHOST_POS))
				return level.getOrientation().front().getOpposite() == lubricatorFacing;
		}
		
		return false;
	}
	
	@Override
	public GhostInfo getGhostBlockPosition(Level world, IMultiblockBEHelperMaster<CrusherLogic.State> mbte){
		IMultiblockLevel level = mbte.getContext().getLevel();
		
		BlockPos position = level.toAbsolute(RELATIVE_GHOST_POS);
		Direction facing = level.getOrientation().front().getOpposite();
		return new GhostInfo(position, facing);
	}
	
	@Override
	public boolean isMachineEnabled(Level world, IMultiblockBEHelperMaster<CrusherLogic.State> mbte){
		return mbte.getState().shouldRenderActive();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void lubricateClient(ClientLevel world, Fluid lubricant, int ticks, IMultiblockBEHelperMaster<CrusherLogic.State> mbte){
		// Animation of it can be sped up with this.
		// But it's just too fast and looks awful, so I turned it off instead.
		/*
		if(mbte.getState().shouldRenderActive()){
			IClientTickableComponent<CrusherLogic.State> tick = (IClientTickableComponent<CrusherLogic.State>) mbte.getMultiblock().logic();
			tick.tickClient(mbte.getContext());
		}
		*/
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void lubricateServer(ServerLevel world, Fluid lubricant, int ticks, IMultiblockBEHelperMaster<CrusherLogic.State> mbte){
		if(ticks % 4 == 0){
			IServerTickableComponent<CrusherLogic.State> tick = (IServerTickableComponent<CrusherLogic.State>) mbte.getMultiblock().logic();
			tick.tickServer(mbte.getContext());
		}
	}
	
	@Override
	public void spawnLubricantParticles(ClientLevel world, BlockPos lubricatorPosition, Direction facing, IMultiblockBEHelperMaster<CrusherLogic.State> mbte){
		boolean mirrored = mbte.getContext().getLevel().getOrientation().mirrored();
		Direction f = mirrored ? facing : facing.getOpposite();
		
		float location = world.random.nextFloat();
		
		boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.NEGATIVE ^ !mirrored;
		float xO = 2.5F;
		float zO = -0.1F;
		float yO = 1.3F;
		
		if(location > .5F){
			xO = 1.0F;
			yO = 3.0F;
			zO = 1.65F;
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
	private static Supplier<IPModel> pipes;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(AutoLubricatorTileEntity lubricator, IMultiblockBEHelperMaster<CrusherLogic.State> mbte, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		final Direction rotation = mbte.getContext().getLevel().getOrientation().front();
		
		if(rotation == Direction.NORTH){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90F));
			matrix.translate(-3, 0, 0);
			
		}else if(rotation == Direction.SOUTH){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270F));
			matrix.translate(-2, 0, -1);
			
		}else if(rotation == Direction.EAST){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0F));
			matrix.translate(-2, 0, 0);
			
		}else if(rotation == Direction.WEST){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180F));
			matrix.translate(-3, 0, -1);
		}
		
		if(pipes == null)
			pipes = IPModels.getSupplier(ModelLubricantPipes.Crusher.ID);
		
		IPModel model;
		if((model = pipes.get()) != null){
			model.renderToBuffer(matrix, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight, combinedOverlay);
		}
	}
}
