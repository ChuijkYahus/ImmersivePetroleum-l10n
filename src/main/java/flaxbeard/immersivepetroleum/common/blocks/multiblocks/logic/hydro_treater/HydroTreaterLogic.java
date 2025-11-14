package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.IReadWriteNBT;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.HydroTreaterShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Function;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater.HydroTreaterLogic.State;

public class HydroTreaterLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>{
	/** Primary Fluid Input Tank<br> */
	public static final int TANK_INPUT_A = 0;
	
	/** Secondary Fluid Input Tank<br> */
	public static final int TANK_INPUT_B = 1;
	
	/** Output Fluid Tank<br> */
	public static final int TANK_OUTPUT = 2;
	
	/** Template-Location of the Fluid Input Port. (1 0 3)<br> */
	public static final CapabilityPosition Fluid_IN_A = new CapabilityPosition(1, 0, 3, RelativeBlockFace.BACK);
	
	/** Template-Location of the Fluid Input Port. (2 2 1)<br> */
	public static final CapabilityPosition Fluid_IN_B = new CapabilityPosition(2, 2, 1, RelativeBlockFace.UP);
	
	/** Template-Location of the Fluid Output Port. (0 1 2)<br> */
	public static final MultiblockFace FLUID_OUT = new MultiblockFace(0, 1, 2, RelativeBlockFace.UP);
	public static final CapabilityPosition Fluid_OUT = new CapabilityPosition(0, 1, 2, RelativeBlockFace.UP);
	
	/** Template-Location of the Item Output Port. (0 0 2)<br> */
	public static final BlockPos Item_OUT = new BlockPos(0, 0, 2);
	
	/** Template-Location of the Energy Input Ports. (2 2 3)<br> */
	public static final CapabilityPosition Energy_IN = new CapabilityPosition(2, 2, 3, RelativeBlockFace.UP);
	
	/** Template-Location of the Redstone Input Port. (0 1 3)<br> */
	public static final BlockPos Redstone_IN = new BlockPos(0, 1, 3);
	
	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource){
		return new State(capabilitySource);
	}
	
	@Override
	public void tickClient(IMultiblockContext<State> context){
	}
	
	@Override
	public void tickServer(IMultiblockContext<State> context){
		boolean update = false;
		
		State state = context.getState();
		Level level = context.getLevel().getRawLevel();
		
		if(state.rsState.isEnabled(context)){
			if(state.energy.getEnergyStored() > 0 && state.processor.getQueueSize() < state.processor.getMaxQueueSize()){
				if(state.tanks.primary().getFluidAmount() > 0 || state.tanks.secondary().getFluidAmount() > 0){
					RecipeHolder<HighPressureRefineryRecipe> holder = HighPressureRefineryRecipe.findRecipe(state.tanks.primary().getFluid(), state.tanks.secondary().getFluid());
					
					if(holder != null){
						HighPressureRefineryRecipe recipe = holder.value();
						
						if(state.energy.getEnergyStored() >= recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime()){
							if(state.tanks.primary().getFluidAmount() >= recipe.getInputFluid().amount() && (recipe.getSecondaryInputFluid() == null || (state.tanks.secondary().getFluidAmount() >= recipe.getSecondaryInputFluid().amount()))){
								int[] inputs, inputAmounts;
								
								if(recipe.getSecondaryInputFluid() != null){
									inputs = new int[]{TANK_INPUT_A, TANK_INPUT_B};
									inputAmounts = new int[]{recipe.getInputFluid().amount(), recipe.getSecondaryInputFluid().amount()};
								}else{
									inputs = new int[]{TANK_INPUT_A};
									inputAmounts = new int[]{recipe.getInputFluid().amount()};
								}
								
								MultiblockProcessInMachine<HighPressureRefineryRecipe> process = new HydroTreaterProcess(holder, inputs).setInputAmounts(inputAmounts);
								if(state.processor.addProcessToQueue(process, level, true)){
									state.processor.addProcessToQueue(process, level, false);
									update = true;
								}
							}
						}
					}
					
				}
			}
		}
		
		if(!state.processor.getQueue().isEmpty()){
			update = true;
			state.processor.tickServer(state, context.getLevel(), true);
		}
		
		if(state.tanks.output().getFluidAmount() > 0){
			BlockPos outPos = context.getLevel().toAbsolute(Fluid_OUT.posInMultiblock()).above();
			update |= FluidUtil.getFluidHandler(level, outPos, Direction.DOWN).map(output -> {
				boolean ret = false;
				FluidStack target = state.tanks.output().getFluid();
				target = FluidHelper.copyFluid(target, Math.min(target.getAmount(), 1000));
				
				int accepted = output.fill(target, IFluidHandler.FluidAction.SIMULATE);
				if(accepted > 0){
					int drained = output.fill(FluidHelper.copyFluid(target, Math.min(target.getAmount(), accepted)), IFluidHandler.FluidAction.EXECUTE);
					
					state.tanks.output().drain(new FluidStack(target.getFluid(), drained), IFluidHandler.FluidAction.EXECUTE);
					ret = true;
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
		register.registerAtOrNull(Capabilities.EnergyStorage.BLOCK, Energy_IN, state -> state.energy);
		register.register(Capabilities.FluidHandler.BLOCK, (state, pos) -> {
			if(Fluid_IN_A.equalsOrNullFace(pos))
				return state.fluidInputMain;
			
			if(Fluid_IN_B.equalsOrNullFace(pos))
				return state.fluidInputSecondary;
			
			if(Fluid_OUT.equalsOrNullFace(pos))
				return state.fluidOutput;
			
			return null;
		});
	}
	
	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
		return HydroTreaterShape.GETTER;
	}
	
	public static class State implements IMultiblockState, ProcessContext.ProcessContextInMachine<HighPressureRefineryRecipe>{
		
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(8000);
		public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
		
		public final Tanks tanks = new Tanks();
		
		public final MultiblockProcessor.InMachineProcessor<HighPressureRefineryRecipe> processor;
		
		private final IFluidHandler fluidInputMain;
		private final IFluidHandler fluidInputSecondary;
		private final IFluidHandler fluidOutput;
		
		private final IFluidHandler outputRef;
		
		public State(IInitialMultiblockContext<State> context){
			this.processor = new MultiblockProcessor.InMachineProcessor<>(1, 0, 1, context.getMarkDirtyRunnable(), State::getRecipeForId);
			
			this.outputRef = context.getCapabilityAt(Capabilities.FluidHandler.BLOCK, FLUID_OUT).get();
			this.fluidInputMain = ArrayFluidHandler.fillOnly(tanks.primary(), context.getMarkDirtyRunnable());
			this.fluidInputSecondary = ArrayFluidHandler.fillOnly(tanks.secondary(), context.getMarkDirtyRunnable());
			this.fluidOutput = ArrayFluidHandler.drainOnly(tanks.output(), context.getMarkDirtyRunnable());
		}
		
		private static HighPressureRefineryRecipe getRecipeForId(Level level, ResourceLocation id){
			return HighPressureRefineryRecipe.recipes.get(id).value();
		}
		
		@Override
		public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			nbt.put("tanks", this.tanks.writeNBT(provider));
			nbt.put("energy", this.energy.serializeNBT(provider));
			nbt.put("processor", this.processor.toNBT(provider));
			this.rsState.writeSaveNBT(nbt, provider);
		}
		
		@Override
		public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			this.tanks.readNBT(nbt.getCompound("tanks"), provider);
			this.energy.deserializeNBT(provider, nbt.getCompound("energy"));
			this.processor.fromNBT(nbt.get("processor"), HydroTreaterProcess::new, provider);
			this.rsState.readSaveNBT(nbt, provider);
		}
		
		@Override
		public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			writeSaveNBT(nbt, provider);
		}
		
		@Override
		public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			readSaveNBT(nbt, provider);
		}
		
		@Override
		public AveragingEnergyStorage getEnergy(){
			return this.energy;
		}
		
		@Override
		public IFluidTank[] getInternalTanks(){
			return this.tanks.asArray();
		}
		
		@Override
		public int[] getOutputTanks(){
			return new int[]{TANK_OUTPUT};
		}
		
		@Override
		public boolean additionalCanProcessCheck(MultiblockProcess<HighPressureRefineryRecipe, ?> process, Level level){
			int outputAmount = 0;
			for(FluidStack outputFluid: process.getRecipe(level).getFluidOutputs()){
				outputAmount += outputFluid.getAmount();
			}
			
			return this.tanks.output().getCapacity() >= (this.tanks.output().getFluidAmount() + outputAmount);
		}
	}
	
	// TODO Rewrite this
	public record Tanks(FluidTank primary, FluidTank secondary, FluidTank output) implements IReadWriteNBT{
		public Tanks(){
			this(
				new FluidTank(12000, fluidStack -> HighPressureRefineryRecipe.hasRecipeWithInput(fluidStack, true)),
				new FluidTank(12000, fluidStack -> HighPressureRefineryRecipe.hasRecipeWithSecondaryInput(fluidStack, true)),
				new FluidTank(12000)
			);
		}
		
		public IFluidTank[] asArray(){
			return new IFluidTank[]{primary(), secondary(), output()};
		}
		
		@Override
		public void readNBT(CompoundTag nbt, HolderLookup.Provider provider){
			this.primary.readFromNBT(provider, nbt.getCompound("primary"));
			this.secondary.readFromNBT(provider, nbt.getCompound("secondary"));
			this.output.readFromNBT(provider, nbt.getCompound("output"));
		}
		
		@Override
		public CompoundTag writeNBT(HolderLookup.Provider provider){
			CompoundTag nbt = new CompoundTag();
			nbt.put("primary", this.primary.writeToNBT(provider, new CompoundTag()));
			nbt.put("secondary", this.secondary.writeToNBT(provider, new CompoundTag()));
			nbt.put("output", this.output.writeToNBT(provider, new CompoundTag()));
			return nbt;
		}
	}
}
