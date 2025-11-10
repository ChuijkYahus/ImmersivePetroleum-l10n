package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

/**
 * Based on the old events from Flaxbeard
 *
 * @author TwistedGate
 */
public class ProjectorEvent extends Event{
	
	public static class PlaceBlock extends ProjectorEvent{
		public PlaceBlock(IMultiblock multiblock, Level templateWorld, BlockPos templatePos, Level world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(multiblock, templateWorld, templatePos, world, worldPos, state, rotation);
		}
		
		public void setBlockState(BlockState state){
			this.state = state;
		}
		
		public void setState(Block block){
			this.state = block.defaultBlockState();
		}
	}
	
	public static class PlaceBlockPost extends ProjectorEvent{
		public PlaceBlockPost(IMultiblock multiblock, Level templateWorld, BlockPos templatePos, Level world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(multiblock, templateWorld, templatePos, world, worldPos, state, rotation);
		}
	}
	
	public static class RenderBlock extends ProjectorEvent{
		public RenderBlock(IMultiblock multiblock, Level templateWorld, BlockPos templatePos, Level world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(multiblock, templateWorld, templatePos, world, worldPos, state, rotation);
		}
		
		public void setState(BlockState state){
			this.state = state;
		}
		
		public void setState(Block block){
			this.state = block.defaultBlockState();
		}
	}
	
	protected IMultiblock multiblock;
	protected Level realWorld;
	protected Level templateWorld;
	protected Rotation rotation;
	protected BlockPos worldPos;
	protected BlockPos templatePos;
	protected BlockState state;
	
	public ProjectorEvent(IMultiblock multiblock, Level templateWorld, BlockPos templatePos, Level world, BlockPos worldPos, BlockState state, Rotation rotation){
		super();
		this.multiblock = multiblock;
		this.realWorld = world;
		this.templateWorld = templateWorld;
		this.worldPos = worldPos;
		this.templatePos = templatePos;
		this.state = state;
		this.rotation = rotation;
	}
	
	public IMultiblock getMultiblock(){
		return multiblock;
	}
	
	public Level getWorld(){
		return this.realWorld;
	}
	
	public Level getTemplateWorld(){
		return this.templateWorld;
	}
	
	public Rotation getRotation(){
		return this.rotation;
	}
	
	public BlockPos getWorldPos(){
		return this.worldPos;
	}
	
	public BlockPos getTemplatePos(){
		return this.templatePos;
	}
	
	public BlockState getState(){
		return this.state;
	}
	
	/** Always returns the BlockState found in the Template */
	public BlockState getTemplateState(){
		return this.templateWorld.getBlockState(this.templatePos);
	}
}
