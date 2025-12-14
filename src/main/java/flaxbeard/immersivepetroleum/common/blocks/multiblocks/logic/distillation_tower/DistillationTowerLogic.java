package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.IReadWriteNBT;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.DistillationTowerShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import flaxbeard.immersivepetroleum.common.util.inventory.EnumInventory;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import flaxbeard.immersivepetroleum.common.util.inventory.MultiFluidTankFiltered;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.State;

public class DistillationTowerLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>{
	
	/** Input Tank ID */
	public static final int TANK_INPUT = 0;
	
	/** Output Tank ID */
	public static final int TANK_OUTPUT = 1;
	
	public enum Inventory{
		/** Fluid Input (Filled Bucket) */
		INPUT_FILLED,
		/** Fluid Input (Empty Bucket) */
		INPUT_EMPTY,
		/** Fluid Output (Empty Bucket) */
		OUTPUT_EMPTY,
		/** Fluid Output (Filled Bucket) */
		OUTPUT_FILLED;
		
		public int id(){
			return ordinal();
		}
		
		public static int size(){
			return values().length;
		}
	}
	
	/** Template-Location of the Fluid Input Port. (3 0 3) */
	public static final CapabilityPosition Fluid_IN = new CapabilityPosition(3, 0, 3, RelativeBlockFace.LEFT);
	
	/** Template-Location of the Fluid Output Port. (1 0 3) */
	public static final CapabilityPosition Fluid_OUT = new CapabilityPosition(1, 0, 3, RelativeBlockFace.BACK);
	
	/** Template-Location of the Item Output Port. (0 0 1) */
	public static final BlockPos Item_OUT = new BlockPos(0, 0, 1);
	
	/** Template-Location of the Energy Input Port. (3 1 3) */
	public static final CapabilityPosition ENERGY_IN = new CapabilityPosition(3, 1, 3, RelativeBlockFace.UP);
	
	/** Template-Location of the Redstone Input Port. (0 1 3) */
	public static final BlockPos REDSTONE_IN = new BlockPos(0, 1, 3);
	
	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource){
		return new State(capabilitySource);
	}
	
	@Override
	public void tickClient(IMultiblockContext<State> context){
		final State state = context.getState();
		
		if(state.cooldownTicks > 0){
			state.cooldownTicks--;
		}
		
		if(state.wasActive){
			state.cooldownTicks = 20;
		}
	}
	
	@Override
	public void tickServer(IMultiblockContext<State> context){
		final State state = context.getState();
		final Level level = context.getLevel().getRawLevel();
		final boolean rsEnabled = state.rsState.isEnabled(context);
		
		boolean update = false;
		
		if(state.wasActive){
			state.wasActive = false;
			update = true;
		}
		
		if(rsEnabled){
			if(state.energy.getEnergyStored() > 0 && state.processor.getQueueSize() < state.processor.getMaxQueueSize()){
				if(state.tanks.input().getFluidAmount() > 0){
					RecipeHolder<DistillationTowerRecipe> holder = DistillationTowerRecipe.findRecipe(state.tanks.input().getFluid());
					
					if(holder != null){
						DistillationTowerRecipe recipe = holder.value();
						
						if(state.tanks.input().getFluidAmount() >= recipe.getInputFluid().amount() && state.energy.getEnergyStored() >= recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime()){
							MultiblockProcessInMachine<DistillationTowerRecipe> process = new DistillationTowerProcess(holder);
							if(state.processor.addProcessToQueue(process, level, true)){
								state.processor.addProcessToQueue(process, level, false);
								update = true;
							}
						}
					}
				}
			}
			
			if(state.processor.tickServer(state, context.getLevel(), !state.processor.getQueue().isEmpty())){
				state.wasActive = true;
				update = true;
			}
		}
		
		if(!state.inventory.get(Inventory.INPUT_FILLED).isEmpty() && state.tanks.input().getFluidAmount() < state.tanks.input().getCapacity()){
			final ItemStack inputEmpty = state.inventory.get(Inventory.INPUT_EMPTY);
			
			if(inputEmpty.getCount() < inputEmpty.getMaxStackSize()){
				ItemStack emptyContainer = FluidHelper.tryDrainContainer(state.inventory.get(Inventory.INPUT_FILLED), state.tanks.input());
				
				if(!emptyContainer.isEmpty()){
					if(!inputEmpty.isEmpty() && inputEmpty.isStackable() && inputEmpty.getCount() < inputEmpty.getMaxStackSize()){
						inputEmpty.grow(emptyContainer.getCount());
					}else if(inputEmpty.isEmpty()){
						state.inventory.set(Inventory.INPUT_EMPTY, emptyContainer.copy());
					}
					
					state.inventory.get(Inventory.INPUT_FILLED).shrink(1);
					if(state.inventory.get(Inventory.INPUT_FILLED).getCount() <= 0){
						state.inventory.set(Inventory.INPUT_FILLED, ItemStack.EMPTY);
					}
					update = true;
				}
			}
		}
		
		if(state.tanks.output().getFluidAmount() > 0){
			final MultiFluidTankFiltered outTank = state.tanks.output();
			
			if(state.inventory.get(Inventory.OUTPUT_EMPTY) != ItemStack.EMPTY && outTank.getTanks() > 0){
				for(int i = outTank.getTanks() - 1;i >= 0;i--){
					FluidStack fs = outTank.getFluidInTank(i);
					
					if(fs.getAmount() > 0){
						ItemStack filledContainer = FluidHelper.fillFluidContainer(outTank, fs, state.inventory.get(Inventory.OUTPUT_EMPTY), state.inventory.get(Inventory.OUTPUT_FILLED));
						if(!filledContainer.isEmpty()){
							ItemStack inv_3_stack = state.inventory.get(Inventory.OUTPUT_FILLED);
							if(inv_3_stack.getCount() == 1 && !FluidHelper.isFluidContainerFull(filledContainer)){
								state.inventory.set(Inventory.OUTPUT_FILLED, filledContainer.copy());
							}else{
								if(!inv_3_stack.isEmpty() && inv_3_stack.isStackable() && inv_3_stack.getCount() < inv_3_stack.getMaxStackSize()){
									inv_3_stack.grow(filledContainer.getCount());
								}else if(inv_3_stack.isEmpty()){
									state.inventory.set(Inventory.OUTPUT_FILLED, filledContainer.copy());
								}
								
								state.inventory.get(Inventory.OUTPUT_EMPTY).shrink(1);
								if(state.inventory.get(Inventory.OUTPUT_EMPTY).getCount() <= 0){
									state.inventory.set(Inventory.OUTPUT_EMPTY, ItemStack.EMPTY);
								}
							}
							
							update = true;
							break;
						}
					}
				}
			}
			
			MultiblockOrientation orientation = context.getLevel().getOrientation();
			
			BlockPos outPos = context.getLevel().toAbsolute(Fluid_OUT.posInMultiblock()).relative(orientation.front().getOpposite());
			update |= FluidUtil.getFluidHandler(level, outPos, orientation.front()).map(output -> {
				boolean ret = false;
				if(!state.tanks.input().getFluid().isEmpty()){
					List<FluidStack> toDrain = new ArrayList<>();
					boolean iePipe = level.getBlockEntity(outPos) instanceof IFluidPipe;
					
					// Tries to Output the output-fluids in parallel
					for(int i = 0;i < outTank.getTanks();i++){
						FluidStack target = outTank.getFluidInTank(i);
						
						FluidStack outStack = FluidHelper.copyFluid(target, Math.min(target.getAmount(), 100), iePipe);
						
						int accepted = output.fill(outStack, FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(FluidHelper.copyFluid(outStack, Math.min(outStack.getAmount(), accepted), iePipe), FluidAction.EXECUTE);
							
							toDrain.add(new FluidStack(target.getFluid(), drained));
							ret = true;
						}
					}
					
					// If this were to be done in the for-loop it would throw a concurrent exception
					toDrain.forEach(fluid -> outTank.drain(fluid, FluidAction.EXECUTE));
				}
				
				return ret;
			}).orElse(false);
		}
		
		if(update){
			context.markDirtyAndSync();
		}
	}
	
	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register){
		register.registerAtOrNull(Capabilities.EnergyStorage.BLOCK, ENERGY_IN, state -> state.energy);
		register.register(Capabilities.FluidHandler.BLOCK, (state, pos) -> {
			if(Fluid_IN.equalsOrNullFace(pos))
				return state.fluidInput;
			
			if(Fluid_OUT.equalsOrNullFace(pos))
				return state.fluidOutput;
			
			return null;
		});
	}
	
	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
		return DistillationTowerShape.GETTER;
	}
	
	public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<DistillationTowerRecipe>{
		public static final int ENERGY_STORAGE_CAPACITY = 16000;
		
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_STORAGE_CAPACITY);
		public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
		
		public final MultiblockProcessor.InMachineProcessor<DistillationTowerRecipe> processor;
		
		public final EnumInventory<Inventory> inventory = new EnumInventory<>(Inventory.class);
		public final Tanks tanks = Tanks.server();
		
		/** Flickering avoidance for the "Active" Texture overlay */
		public int cooldownTicks = 0;
		public boolean wasActive = false;
		
		private final IFluidHandler fluidInput;
		private final IFluidHandler fluidOutput;
		
		public State(IInitialMultiblockContext<State> context){
			this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0, 1, context.getMarkDirtyRunnable(), State::recipeFromId);
			
			this.fluidInput = ArrayFluidHandler.fillOnly(tanks.input(), context.getMarkDirtyRunnable());
			this.fluidOutput = ArrayFluidHandler.drainOnly(tanks.output(), context.getMarkDirtyRunnable());
		}
		
		private static DistillationTowerRecipe recipeFromId(Level level, ResourceLocation id){
			return DistillationTowerRecipe.recipes.get(id).value();
		}
		
		@Override
		public AveragingEnergyStorage getEnergy(){
			return this.energy;
		}
		
		@Override
		public IFluidTank[] getInternalTanks(){
			return this.tanks.array();
		}
		
		@Override
		public int[] getOutputTanks(){
			return new int[]{1};
		}
		
		@Override
		public boolean additionalCanProcessCheck(MultiblockProcess<DistillationTowerRecipe, ?> process, Level level){
			int outputAmount = 0;
			for(FluidStack outputFluid: process.getRecipe(level).getFluidOutputs()){
				outputAmount += outputFluid.getAmount();
			}
			
			return this.tanks.output().getCapacity() >= (this.tanks.output().getFluidAmount() + outputAmount);
		}
		
		@Override
		public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			this.tanks.readNBT(nbt.getCompound("tanks"), provider);
			this.energy.deserializeNBT(provider, nbt.getCompound("energy"));
			this.processor.fromNBT(nbt.getCompound("recipeworker"), DistillationTowerProcess::new, provider);
			
			this.inventory.load(nbt, provider);
			this.rsState.readSaveNBT(nbt, provider);
			
			this.wasActive = nbt.getBoolean("wasActive");
		}
		
		@Override
		public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			nbt.put("tanks", this.tanks.writeNBT(provider));
			nbt.put("energy", this.energy.serializeNBT(provider));
			nbt.put("recipeworker", this.processor.toNBT(provider));
			
			this.inventory.save(nbt, provider);
			this.rsState.writeSaveNBT(nbt, provider);
			
			nbt.putBoolean("wasActive", this.wasActive);
		}
		
		@Override
		public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			readSaveNBT(nbt, provider);
		}
		
		@Override
		public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			writeSaveNBT(nbt, provider);
		}
	}
	
	public static final class Tanks implements IReadWriteNBT{
		public static final int CAPACITY = 24 * FluidType.BUCKET_VOLUME;
		
		public static Tanks server(){
			//@formatter:off
			return new Tanks(
				new FluidTankFiltered(CAPACITY, fs -> DistillationTowerRecipe.findRecipe(fs) != null),
				new MultiFluidTankFiltered(CAPACITY)
			);
			//@formatter:on
		}
		
		public static Tanks client(){
			//@formatter:off
			return new Tanks(
				new FluidTankFiltered(Tanks.CAPACITY),
				new MultiFluidTankFiltered(Tanks.CAPACITY)
			);
			//@formatter:on
		}
		
		private final FluidTankFiltered input;
		private final MultiFluidTankFiltered output;
		private final IFluidTank[] array;
		
		private Tanks(FluidTankFiltered input, MultiFluidTankFiltered output){
			this.input = input;
			this.output = output;
			this.array = new IFluidTank[]{
				input,
				output
			};
		}
		
		@Override
		public void readNBT(CompoundTag nbt, HolderLookup.Provider provider){
			this.input.readFromNBT(nbt.getCompound("input"), provider);
			this.output.readFromNBT(nbt.getCompound("output"), provider);
		}
		
		@Override
		public CompoundTag writeNBT(HolderLookup.Provider provider){
			CompoundTag nbt = new CompoundTag();
			nbt.put("input", this.input.writeToNBT(new CompoundTag(), provider));
			nbt.put("output", this.output.writeToNBT(new CompoundTag(), provider));
			return nbt;
		}
		
		public FluidTankFiltered input(){
			return this.input;
		}
		
		public MultiFluidTankFiltered output(){
			return this.output;
		}
		
		public IFluidTank[] array(){
			return this.array;
		}
	}
}
