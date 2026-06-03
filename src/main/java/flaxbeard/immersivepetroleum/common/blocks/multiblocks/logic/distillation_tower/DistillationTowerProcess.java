package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.TANK_INPUT;

public class DistillationTowerProcess extends MultiblockProcessInMachine<DistillationTowerRecipe>{
	static final int[] SLOTS_NONE = new int[0];
	
	public DistillationTowerProcess(RecipeHolder<DistillationTowerRecipe> recipe){
		super(recipe, SLOTS_NONE);
		setInputTanks(TANK_INPUT);
	}
	
	public DistillationTowerProcess(BiFunction<Level, ResourceLocation, DistillationTowerRecipe> loader, CompoundTag compoundTag, HolderLookup.Provider provider){
		super(loader, compoundTag);
	}
	
	@Override
	protected List<IngredientWithSize> getRecipeItemInputs(ProcessContext.ProcessContextInMachine<DistillationTowerRecipe> context, Level level){
		return Collections.emptyList();
	}
	
	@Override
	protected void outputItem(ProcessContext.ProcessContextInMachine<DistillationTowerRecipe> context, ItemStack output, IMultiblockLevel mbLevel){
		if(output == null || output.isEmpty())
			return;
		
		ItemStack outputCopy = output.copy();
		
		final Level rawLevel = mbLevel.getRawLevel();
		
		MultiblockOrientation orientation = mbLevel.getOrientation();
		
		Direction outDir = orientation.mirrored() ? orientation.front().getClockWise() : orientation.front().getCounterClockWise();
		BlockPos outPos = mbLevel.toAbsolute(DistillationTowerLogic.Item_OUT).relative(outDir);
		
		IItemHandler itemHandler = rawLevel.getCapability(Capabilities.ItemHandler.BLOCK, outPos, outDir.getOpposite());
		if(itemHandler != null){
			outputCopy = ItemHandlerHelper.insertItem(itemHandler, outputCopy, false);
		}
		
		if(!outputCopy.isEmpty()){
			double x = outPos.getX() + 0.5;
			double y = outPos.getY() + 0.25;
			double z = outPos.getZ() + 0.5;
			
			Direction facing = orientation.mirrored() ? orientation.front().getOpposite() : orientation.front();
			if(facing != Direction.EAST && facing != Direction.WEST){
				x = outPos.getX() + (facing == Direction.SOUTH ? 0.15 : 0.85);
			}
			if(facing != Direction.NORTH && facing != Direction.SOUTH){
				z = outPos.getZ() + (facing == Direction.WEST ? 0.15 : 0.85);
			}
			
			ItemEntity ei = new ItemEntity(rawLevel, x, y, z, outputCopy);
			ei.setDeltaMovement(0.075 * outDir.getStepX(), 0.025, 0.075 * outDir.getStepZ());
			rawLevel.addFreshEntity(ei);
		}
	}
	
	@Override
	protected boolean canOutputItem(ProcessContext.ProcessContextInMachine<DistillationTowerRecipe> context, ItemStack output) {
		// We return true here because we can always output items, they are just thrown on the ground when not stored
		return true;
	}
}
