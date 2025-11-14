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
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.IReadWriteNBT;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.DistillationTowerShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import flaxbeard.immersivepetroleum.common.util.inventory.MultiFluidTankFiltered;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.State;

public class DistillationTowerLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>{
	
	/** Input Tank ID */
	public static final int TANK_INPUT = 0;
	
	/** Output Tank ID */
	public static final int TANK_OUTPUT = 1;
	
	/** Inventory Fluid Input (Filled Bucket) */
	public static final int INV_0 = 0;
	
	/** Inventory Fluid Input (Empty Bucket) */
	public static final int INV_1 = 1;
	
	/** Inventory Fluid Output (Empty Bucket) */
	public static final int INV_2 = 2;
	
	/** Inventory Fluid Output (Filled Bucket) */
	public static final int INV_3 = 3;
	
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
	}
	
	@Override
	public void tickServer(IMultiblockContext<State> context){
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		final boolean rsEnabled = state.rsState.isEnabled(context);
		
		boolean update = false;
		
		if(state.wasActive && state.cooldownTicks == 0){
			state.wasActive = false;
			update = true;
		}
		
		if(state.cooldownTicks > 0){
			state.cooldownTicks--;
		}
		
		if(rsEnabled){
			if(state.energy.getEnergyStored() > 0 && state.processor.getQueueSize() < state.processor.getMaxQueueSize()){
				if(state.tanks.input().getFluidAmount() > 0){
					RecipeHolder<DistillationTowerRecipe> holder = DistillationTowerRecipe.findRecipe(state.tanks.input().getFluid());
					
					if(holder != null){
						DistillationTowerRecipe recipe = holder.value();
						
						if(state.tanks.input().getFluidAmount() >= recipe.getInputFluid().amount() && state.energy.getEnergyStored() >= recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime()){
							MultiblockProcessInMachine<DistillationTowerRecipe> process = new DistillationTowerProcess(holder).setInputTanks(TANK_INPUT);
							if(state.processor.addProcessToQueue(process, level.getRawLevel(), true)){
								state.processor.addProcessToQueue(process, level.getRawLevel(), false);
								update = true;
							}
						}
					}
				}
			}
			
			if(!state.processor.getQueue().isEmpty()){
				state.wasActive = true;
				state.cooldownTicks = 10;
				update = true;
			}
			
			state.processor.tickServer(state, level, state.wasActive);
		}
		
		if(state.inventory.get(INV_0) != ItemStack.EMPTY && state.tanks.input().getFluidAmount() < state.tanks.input().getCapacity()){
			ItemStack emptyContainer = ItemStack.EMPTY;//Utils.drainFluidContainer(state.tanks.input(), state.inventory.get(INV_0), state.inventory.get(INV_1));
			if(!emptyContainer.isEmpty()){
				final ItemStack inv_1_stack = state.inventory.get(INV_1);
				
				if(!inv_1_stack.isEmpty() && inv_1_stack.isStackable() && inv_1_stack.getCount() < inv_1_stack.getMaxStackSize()){
					inv_1_stack.grow(emptyContainer.getCount());
				}else if(inv_1_stack.isEmpty()){
					state.inventory.set(INV_1, emptyContainer.copy());
				}
				
				state.inventory.get(INV_0).shrink(1);
				if(state.inventory.get(INV_0).getCount() <= 0){
					state.inventory.set(INV_0, ItemStack.EMPTY);
				}
				update = true;
			}
		}
		
		if(state.tanks.output().getFluidAmount() > 0){
			if(state.inventory.get(INV_2) != ItemStack.EMPTY){
				
				if(state.tanks.output().getFluidTypes() > 0){
					MultiFluidTank outTank = state.tanks.output();
					
					for(int i = outTank.getFluidTypes() - 1;i >= 0;i--){
						FluidStack fs = outTank.getFluidInTank(i);
						
						if(fs.getAmount() > 0){
							ItemStack filledContainer = FluidHelper.fillFluidContainer(outTank, fs, state.inventory.get(INV_2), state.inventory.get(INV_3));
							if(!filledContainer.isEmpty()){
								ItemStack inv_3_stack = state.inventory.get(INV_3);
								if(inv_3_stack.getCount() == 1 && !FluidHelper.isFluidContainerFull(filledContainer)){
									state.inventory.set(INV_3, filledContainer.copy());
								}else{
									if(!inv_3_stack.isEmpty() && inv_3_stack.isStackable() && inv_3_stack.getCount() < inv_3_stack.getMaxStackSize()){
										inv_3_stack.grow(filledContainer.getCount());
									}else if(inv_3_stack.isEmpty()){
										state.inventory.set(INV_3, filledContainer.copy());
									}
									
									state.inventory.get(INV_2).shrink(1);
									if(state.inventory.get(INV_2).getCount() <= 0){
										state.inventory.set(INV_2, ItemStack.EMPTY);
									}
								}
								
								update = true;
								break;
							}
						}
					}
				}
			}
			
			MultiblockOrientation orientation = level.getOrientation();
			
			BlockPos outPos = level.toAbsolute(Fluid_OUT.posInMultiblock()).relative(orientation.front().getOpposite());
			update |= FluidUtil.getFluidHandler(level.getRawLevel(), outPos, orientation.front()).map(output -> {
				boolean ret = false;
				if(!state.tanks.input().fluids.isEmpty()){
					List<FluidStack> toDrain = new ArrayList<>();
					boolean iePipe = level.getRawLevel().getBlockEntity(outPos) instanceof IFluidPipe;
					
					// Tries to Output the output-fluids in parallel
					for(FluidStack target: state.tanks.output().fluids){
						FluidStack outStack = FluidHelper.copyFluid(target, Math.min(target.getAmount(), 100), iePipe);
						
						int accepted = output.fill(outStack, IFluidHandler.FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = output.fill(FluidHelper.copyFluid(outStack, Math.min(outStack.getAmount(), accepted), iePipe), IFluidHandler.FluidAction.EXECUTE);
							
							toDrain.add(new FluidStack(target.getFluid(), drained));
							ret = true;
						}
					}
					
					// If this were to be done in the for-loop it would throw a concurrent exception
					toDrain.forEach(fluid -> state.tanks.output().drain(fluid, IFluidHandler.FluidAction.EXECUTE));
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
		
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
		public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
		
		public final MultiblockProcessor.InMachineProcessor<DistillationTowerRecipe> processor;
		
		public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
		public final Tanks tanks = new Tanks();
		
		/** Flickering avoidance for the "On" Texture overlay */
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
		public MultiFluidTankFiltered[] getInternalTanks(){
			return this.tanks.asArray();
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
			this.cooldownTicks = nbt.getInt("cooldownTicks");
			this.processor.fromNBT(nbt.getCompound("recipeworker"), DistillationTowerProcess::new, provider);
			
			this.inventory = readInventory(nbt.getCompound("inventory"), provider);
			this.rsState.readSaveNBT(nbt, provider);
			
			this.wasActive = nbt.getBoolean("wasActive");
		}
		
		@Override
		public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			nbt.put("tanks", this.tanks.writeNBT(provider));
			nbt.put("energy", this.energy.serializeNBT(provider));
			nbt.putInt("cooldownTicks", this.cooldownTicks);
			nbt.put("recipeworker", this.processor.toNBT(provider));
			
			nbt.put("inventory", writeInventory(this.inventory, provider));
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
		
		protected NonNullList<ItemStack> readInventory(CompoundTag nbt, HolderLookup.Provider provider){
			NonNullList<ItemStack> list = NonNullList.create();
			ContainerHelper.loadAllItems(nbt, list, provider);
			
			if(list.isEmpty()){ // In case it loaded none
				list = this.inventory.size() == 4 ? this.inventory : NonNullList.withSize(4, ItemStack.EMPTY);
			}else if(list.size() < 4){ // Padding in case it loaded less than 4
				while(list.size() < 4)
					list.add(ItemStack.EMPTY);
			}
			return list;
		}
		
		protected CompoundTag writeInventory(NonNullList<ItemStack> list, HolderLookup.Provider provider){
			return ContainerHelper.saveAllItems(new CompoundTag(), list, provider);
		}
	}
	
	public record Tanks(MultiFluidTankFiltered input, MultiFluidTankFiltered output) implements IReadWriteNBT{
		public static final int CAPACITY = 24 * FluidType.BUCKET_VOLUME;
		
		public Tanks(){
			this(new MultiFluidTankFiltered(CAPACITY, fs -> DistillationTowerRecipe.findRecipe(fs) != null), new MultiFluidTankFiltered(CAPACITY));
		}
		
		public MultiFluidTankFiltered[] asArray(){
			return new MultiFluidTankFiltered[]{input, output};
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
	}
}
