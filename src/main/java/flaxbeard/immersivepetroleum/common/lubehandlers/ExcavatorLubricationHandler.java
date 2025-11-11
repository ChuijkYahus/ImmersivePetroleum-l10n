package flaxbeard.immersivepetroleum.common.lubehandlers;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BucketWheelLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ExcavatorLogic;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ExcavatorLubricationHandler implements ILubricationHandler<IMultiblockBEHelperMaster<ExcavatorLogic.State>, ExcavatorLogic.State>{
	private static final BlockPos RELATIVE_GHOST_POS = new BlockPos(3, 0, 1);
	
	@Override
	public boolean isPlacedCorrectly(Level world, BlockPos lubricatorPosition, Direction lubricatorFacing){
		final BlockPos target = lubricatorPosition.relative(lubricatorFacing);
		
		if(world.getBlockEntity(target) instanceof IMultiblockBE<?> mb && mb.getHelper().getContext() != null){
			IMultiblockLevel level = mb.getHelper().getContext().getLevel();
			
			if(level.toRelative(lubricatorPosition).equals(RELATIVE_GHOST_POS)){
				MultiblockOrientation orientation = level.getOrientation();
				Direction dir = orientation.mirrored() ? orientation.front().getClockWise() : orientation.front().getCounterClockWise();
				
				return dir == lubricatorFacing;
			}
		}
		
		return false;
	}
	
	@Override
	public GhostInfo getGhostBlockPosition(Level world, IMultiblockBEHelperMaster<ExcavatorLogic.State> mbte){
		IMultiblockLevel level = mbte.getContext().getLevel();
		
		BlockPos position = level.toAbsolute(RELATIVE_GHOST_POS);
		
		MultiblockOrientation orientation = level.getOrientation();
		Direction facing = orientation.mirrored() ? orientation.front().getClockWise() : orientation.front().getCounterClockWise();
		
		return new GhostInfo(position, facing);
	}
	
	@Override
	public boolean isMachineEnabled(Level world, IMultiblockBEHelperMaster<ExcavatorLogic.State> mbte){
		MultiblockBlockEntityMaster<BucketWheelLogic.State> wheelMaster = getWheelMaster(world, mbte);
		
		return wheelMaster != null && wheelMaster.getHelper().getState().active;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void lubricateClient(ClientLevel world, Fluid lubricant, int ticks, IMultiblockBEHelperMaster<ExcavatorLogic.State> mbte){
		MultiblockBlockEntityMaster<BucketWheelLogic.State> wheelMaster = getWheelMaster(world, mbte);
		if(wheelMaster == null)
			return;
		
		wheelMaster.getHelper().getState().rotation += (float) (IEServerConfig.MACHINES.excavator_speed.get() / 4D);
		wheelMaster.getHelper().getState().rotation %= 360; // just a precaution
	}
	
	@Override
	public void lubricateServer(ServerLevel world, Fluid lubricant, int ticks, IMultiblockBEHelperMaster<ExcavatorLogic.State> mbte){
		MultiblockBlockEntityMaster<BucketWheelLogic.State> wheelMaster = getWheelMaster(world, mbte);
		if(wheelMaster == null)
			return;
		
		if(ticks % 4 == 0){
			wheelMaster.getHelper().tickServer();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	private MultiblockBlockEntityMaster<BucketWheelLogic.State> getWheelMaster(Level world, IMultiblockBEHelperMaster<ExcavatorLogic.State> mbte){
		BlockPos wheelPos = mbte.getContext().getLevel().toAbsolute(ExcavatorLogic.WHEEL_CENTER);
		BlockEntity center = world.getBlockEntity(wheelPos);
		
		return center instanceof MultiblockBlockEntityMaster<?> wheel ? (MultiblockBlockEntityMaster<BucketWheelLogic.State>) wheel : null;
	}
	
	@Override
	public void spawnLubricantParticles(ClientLevel world, BlockPos lubricatorPosition, Direction facing, IMultiblockBEHelperMaster<ExcavatorLogic.State> mbte){
		boolean mirrored = mbte.getContext().getLevel().getOrientation().mirrored();
		Direction f = mirrored ? facing : facing.getOpposite();
		
		float location = world.random.nextFloat();
		
		boolean flip = f.getAxis() == Axis.Z ^ facing.getAxisDirection() == AxisDirection.POSITIVE ^ !mirrored;
		float xO = 1.2F;
		float zO = -.5F;
		float yO = .5F;
		
		if(location > .5F){
			xO = 0.9F;
			yO = 0.8F;
			zO = 1.75F;
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
	private static Supplier<IPModel> pipes_normal;
	private static Supplier<IPModel> pipes_mirrored;
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderPipes(AutoLubricatorTileEntity lubricator, IMultiblockBEHelperMaster<ExcavatorLogic.State> mbte, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		final MultiblockOrientation orientation = mbte.getContext().getLevel().getOrientation();
		final boolean mirrored = orientation.mirrored();
		final Direction rotation = orientation.front();
		
		if(rotation == Direction.NORTH){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90F));
			matrix.translate(-5, 0, mirrored ? 1 : -3);
			
		}else if(rotation == Direction.SOUTH){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270F));
			matrix.translate(-4, 0, mirrored ? 0 : -4);
			
		}else if(rotation == Direction.EAST){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0F));
			matrix.translate(-4, 0, mirrored ? 1 : -3);
			
		}else if(rotation == Direction.WEST){
			matrix.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180F));
			matrix.translate(-5, 0, mirrored ? 0 : -4);
		}
		
		IPModel model;
		if(mirrored){
			if(pipes_mirrored == null)
				pipes_mirrored = IPModels.getSupplier(ModelLubricantPipes.Excavator.ID_MIRRORED);
			
			model = pipes_mirrored.get();
		}else{
			if(pipes_normal == null)
				pipes_normal = IPModels.getSupplier(ModelLubricantPipes.Excavator.ID_NORMAL);
			
			model = pipes_normal.get();
		}
		
		if(model != null){
			model.renderToBuffer(matrix, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight, combinedOverlay);
		}
	}
}
