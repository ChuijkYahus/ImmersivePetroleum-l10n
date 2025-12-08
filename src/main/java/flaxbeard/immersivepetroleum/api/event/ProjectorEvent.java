package flaxbeard.immersivepetroleum.api.event;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;

/**
 * Based on the old events from Flaxbeard
 *
 * @author TwistedGate
 */
public class ProjectorEvent extends Event{
	
	public static class PlaceBlock extends ProjectorEvent{
		public PlaceBlock(IMultiblock multiblock, @Nullable Level templateWorld, BlockPos templatePos, Level world, BlockPos worldPos, BlockState state, Rotation rotation){
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
		public PlaceBlockPost(IMultiblock multiblock, @Nullable Level templateWorld, BlockPos templatePos, Level world, BlockPos worldPos, BlockState state, Rotation rotation){
			super(multiblock, templateWorld, templatePos, world, worldPos, state, rotation);
		}
	}
	
	public static class RenderBlock extends ProjectorEvent{
		public RenderBlock(IMultiblock multiblock, @Nullable Level templateWorld, BlockPos templatePos, Level world, BlockPos worldPos, BlockState state, Rotation rotation){
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
	protected @Nullable Level templateWorld;
	protected Rotation rotation;
	protected BlockPos worldPos;
	protected BlockPos templatePos;
	protected BlockState state;
	
	public ProjectorEvent(IMultiblock multiblock, @Nullable Level templateWorld, BlockPos templatePos, Level world, BlockPos worldPos, BlockState state, Rotation rotation){
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
	
	@Nullable
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
	
	/** Returns the BlockState found in the Template, or null on server-side. */
	@Nullable
	public BlockState getTemplateState(){
		if(this.templateWorld == null)
			return null;
		
		return this.templateWorld.getBlockState(this.templatePos);
	}
}
