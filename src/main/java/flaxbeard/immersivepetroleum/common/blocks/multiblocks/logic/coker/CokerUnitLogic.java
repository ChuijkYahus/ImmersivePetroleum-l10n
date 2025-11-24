package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker;

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
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.IReadWriteNBT;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.CokerShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokerUnitLogic.State;

public class CokerUnitLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>{
	
	public enum Inventory{
		/** Inventory Item Input */
		INPUT,
		/** Inventory Fluid Input (Filled Bucket) */
		INPUT_FILLED,
		/** Inventory Fluid Input (Empty Bucket) */
		INPUT_EMPTY,
		/** Inventory Fluid Output (Empty Bucket) */
		OUTPUT_EMPTY,
		/** Inventory Fluid Output (Filled Bucket) */
		OUTPUT_FILLED;
		
		public int id(){
			return ordinal();
		}
	}
	
	/** Input Fluid Tank<br> */
	public static final int TANK_INPUT = 0;
	
	/** Output Fluid Tank<br> */
	public static final int TANK_OUTPUT = 1;
	
	/** Coker Chamber A<br> */
	public static final int CHAMBER_A = 0;
	
	/** Coker Chamber B<br> */
	public static final int CHAMBER_B = 1;
	
	/** Template-Location of the Chamber A Item Output */
	public static final CapabilityPosition Chamber_A_OUT = new CapabilityPosition(2, 0, 2, RelativeBlockFace.BACK);
	
	/** Template-Location of the Chamber B Item Output */
	public static final CapabilityPosition Chamber_B_OUT = new CapabilityPosition(6, 0, 2, RelativeBlockFace.BACK);
	
	/** Template-Location of the Fluid Input Port. (2 0 4)<br> */
	public static final CapabilityPosition Fluid_IN = new CapabilityPosition(2, 0, 4, RelativeBlockFace.BACK);
	
	/** Template-Location of the Fluid Output Port. (5 0 4)<br> */
	public static final CapabilityPosition Fluid_OUT = new CapabilityPosition(5, 0, 4, RelativeBlockFace.BACK);
	
	/** Template-Location of the Item Input Port. (3 0 4)<br> */
	public static final CapabilityPosition Item_IN = new CapabilityPosition(3, 0, 4, RelativeBlockFace.BACK);
	
	/** Template-Location of the Energy Input Ports.<br><pre>1 1 0<br>2 1 0<br>3 1 0</pre><br> */
	public static final CapabilityPosition[] Energy_IN = new CapabilityPosition[]{new CapabilityPosition(6, 1, 4, RelativeBlockFace.BACK), new CapabilityPosition(7, 1, 4, RelativeBlockFace.BACK)};
	
	/** Template-Location of the Redstone Input Port. (6 1 4)<br> */
	public static final BlockPos Redstone_IN = new BlockPos(6, 1, 4);
	
	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource){
		InitialMultiblockContext<State> capSource = (InitialMultiblockContext<State>) capabilitySource;
		return new State(capabilitySource, capSource.masterBE().getBlockPos());
	}
	
	@Override
	public void tickClient(IMultiblockContext<State> context){
		final State state = context.getState();
		final IMultiblockLevel mbLevel = context.getLevel();
		final Level level = mbLevel.getRawLevel();
		
		if(!state.rsState.isEnabled(context)){
			return;
		}
		
		final CokingChamber[] chambers = state.chambers.get();
		boolean debug = false;
		for(int i = 0;i < chambers.length;i++){
			if(debug || chambers[i].getState() == CokingChamber.State.DUMPING){
				BlockPos cOutPos = mbLevel.toAbsolute(i == 0 ? Chamber_A_OUT.posInMultiblock() : Chamber_B_OUT.posInMultiblock());
				Vec3 origin = new Vec3(cOutPos.getX() + 0.5, cOutPos.getY() + 2.125, cOutPos.getZ() + 0.5);
				for(int j = 0;j < 10;j++){
					double rX = (Math.random() - 0.5) * 0.4;
					double rY = (Math.random() - 0.5) * 0.5;
					double rdx = (Math.random() - 0.5) * 0.10;
					double rdy = (Math.random() - 0.5) * 0.10;
					
					level.addParticle(ParticleTypes.SMOKE, origin.x + rX, origin.y, origin.z + rY, rdx, -(Math.random() * 0.06 + 0.11), rdy);
				}
			}
		}
	}
	
	@Override
	public void tickServer(IMultiblockContext<State> context){
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		final boolean rsEnabled = state.rsState.isEnabled(context);
		
		boolean update = false;
		
		if(rsEnabled){
			ItemStack inputStack = state.getInventory(Inventory.INPUT);
			FluidStack inputFluid = state.bufferTanks.input().getFluid();
			
			if(!inputStack.isEmpty() && inputFluid.getAmount() > 0 && CokerUnitRecipe.hasRecipeWithInput(inputStack, inputFluid)){
				CokerUnitRecipe recipe = CokerUnitRecipe.findRecipe(inputStack, inputFluid);
				
				if(recipe != null && inputStack.getCount() >= recipe.getInputItem().getCount() && inputFluid.getAmount() >= recipe.getInputFluid().getAmount()){
					for(CokingChamber chamber: state.chambers.get()){
						boolean skipNext = false;
						
						switch(chamber.getState()){
							case STANDBY -> {
								if(chamber.setRecipe(recipe)){
									update = true;
									skipNext = true;
								}
							}
							case PROCESSING -> {
								int acceptedStack = chamber.addStack(state.copyStack(inputStack, recipe.getInputItem().getCount()), true);
								if(acceptedStack >= recipe.getInputItem().getCount()){
									acceptedStack = Math.min(acceptedStack, inputStack.getCount());
									
									chamber.addStack(state.copyStack(inputStack, acceptedStack), false);
									inputStack.shrink(acceptedStack);
									
									skipNext = true;
									update = true;
								}
							}
							default -> {
							}
						}
						
						if(skipNext){
							break;
						}
					}
				}
			}
			
			final CokingChamber[] chambers = state.chambers.get();
			for(int i = 0;i < chambers.length;i++){
				update |= chambers[i].tick(context, i);
			}
		}
		
		if(!state.getInventory(Inventory.INPUT_FILLED).isEmpty() && state.bufferTanks.input().getFluidAmount() < state.bufferTanks.input().getCapacity()){
			ItemStack container = Utils.drainFluidContainer(state.bufferTanks.input(), state.getInventory(Inventory.INPUT_FILLED), state.getInventory(Inventory.INPUT_EMPTY));
			if(!container.isEmpty()){
				if(!state.getInventory(Inventory.INPUT_EMPTY).isEmpty() && ItemHandlerHelper.canItemStacksStack(state.getInventory(Inventory.INPUT_EMPTY), container)){
					state.getInventory(Inventory.INPUT_EMPTY).grow(container.getCount());
				}else if(state.getInventory(Inventory.INPUT_EMPTY).isEmpty()){
					state.setInventory(Inventory.INPUT_EMPTY, container.copy());
				}
				
				state.getInventory(Inventory.INPUT_FILLED).shrink(1);
				if(state.getInventory(Inventory.INPUT_FILLED).getCount() <= 0){
					state.setInventory(Inventory.INPUT_FILLED, ItemStack.EMPTY);
				}
				
				update = true;
			}
		}
		
		if(state.bufferTanks.output().getFluidAmount() > 0){
			if(!state.getInventory(Inventory.OUTPUT_EMPTY).isEmpty()){
				ItemStack filledContainer = FluidHelper.fillFluidContainer(state.bufferTanks.output(), state.getInventory(Inventory.OUTPUT_EMPTY), state.getInventory(Inventory.OUTPUT_FILLED), null);
				if(!filledContainer.isEmpty()){
					
					if(state.getInventory(Inventory.OUTPUT_FILLED).getCount() == 1 && !FluidHelper.isFluidContainerFull(filledContainer)){
						state.setInventory(Inventory.OUTPUT_FILLED, filledContainer.copy());
					}else{
						if(!state.getInventory(Inventory.OUTPUT_FILLED).isEmpty() && ItemHandlerHelper.canItemStacksStack(state.getInventory(Inventory.OUTPUT_FILLED), filledContainer)){
							state.getInventory(Inventory.OUTPUT_FILLED).grow(filledContainer.getCount());
						}else if(state.getInventory(Inventory.OUTPUT_FILLED).isEmpty()){
							state.setInventory(Inventory.OUTPUT_FILLED, filledContainer.copy());
						}
						
						state.getInventory(Inventory.OUTPUT_EMPTY).shrink(1);
						if(state.getInventory(Inventory.OUTPUT_EMPTY).getCount() <= 0){
							state.setInventory(Inventory.OUTPUT_EMPTY, ItemStack.EMPTY);
						}
					}
					
					update = true;
				}
			}
			
			BlockPos outPos = level.toAbsolute(Fluid_OUT.posInMultiblock()).relative(level.getOrientation().front().getOpposite());
			update |= FluidUtil.getFluidHandler(level.getRawLevel(), outPos, level.getOrientation().front()).map(out -> {
				if(state.bufferTanks.output().getFluidAmount() > 0){
					FluidStack fs = state.bufferTanks.output().getFluid();
					fs = FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), 250));
					int accepted = out.fill(fs, IFluidHandler.FluidAction.SIMULATE);
					if(accepted > 0){
						boolean iePipe = level.getBlockEntity(outPos) instanceof IFluidPipe;
						int drained = out.fill(FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), accepted), iePipe), IFluidHandler.FluidAction.EXECUTE);
						state.bufferTanks.output().drain(FluidHelper.copyFluid(fs, drained), IFluidHandler.FluidAction.EXECUTE);
						return true;
					}
				}
				return false;
			}).orElse(false);
		}
		
		if(update){
			context.markDirtyAndSync();
		}
		
		updateComparatorOutput(context);
		
	}
	
	private void updateComparatorOutput(IMultiblockContext<State> context){
		boolean update = false;
		State state = context.getState();
		
		ItemStack stack = state.getInventory(Inventory.INPUT);
		if(!stack.isEmpty()){
			int compared = Mth.clamp(Mth.floor(stack.getCount() / (float) Math.min(64, stack.getMaxStackSize()) * 15), 0, 15);
			if(compared != state.lastCompared){
				state.lastCompared = compared;
				update = true;
			}
		}else if(state.lastCompared != 0){
			state.lastCompared = 0;
			update = true;
		}
		
		if(update){
			BlockPos p = context.getLevel().toAbsolute(Redstone_IN);
			context.getLevel().getRawLevel().updateNeighborsAt(p, context.getLevel().getBlockState(p).getBlock());
		}
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap){
		final State state = ctx.getState();
		
		if(cap == ForgeCapabilities.ITEM_HANDLER){
			if(position.equalsOrNullFace(Item_IN))
				return state.itemInput.cast(ctx);
			
		}else if(cap == ForgeCapabilities.FLUID_HANDLER){
			if(position.equalsOrNullFace(Fluid_OUT))
				return state.fluidOutput.cast(ctx);
			
			else if(position.equalsOrNullFace(Fluid_IN))
				return state.fluidInput.cast(ctx);
			
		}else if(cap == ForgeCapabilities.ENERGY){
			if(position.equalsOrNullFace(Energy_IN[0]) || position.equalsOrNullFace(Energy_IN[1]))
				return state.energyCap.cast(ctx);
		}
		
		return LazyOptional.empty();
	}
	
	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
		return CokerShape.GETTER;
	}
	
	public static class State implements IMultiblockState{
		
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(24000);
		public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
		
		int lastCompared = 0;
		
		public final NonNullList<ItemStack> inventory = NonNullList.withSize(Inventory.values().length, ItemStack.EMPTY);
		public final BufferTanks bufferTanks = new BufferTanks();
		public final Chambers chambers = new Chambers();
		
		private final StoredCapability<IItemHandler> itemInput = new StoredCapability<>(new FilteredItemStackhandler(this.inventory));
		private final StoredCapability<IEnergyStorage> energyCap = new StoredCapability<>(this.energy);
		private final StoredCapability<IFluidHandler> fluidInput;
		private final StoredCapability<IFluidHandler> fluidOutput;
		public BlockPos masterPos;
		
		public State(IInitialMultiblockContext<State> context, BlockPos pos){
			this.masterPos = pos;
			
			this.fluidInput = new StoredCapability<>(ArrayFluidHandler.fillOnly(this.bufferTanks.input(), context.getMarkDirtyRunnable()));
			this.fluidOutput = new StoredCapability<>(ArrayFluidHandler.drainOnly(this.bufferTanks.output(), context.getMarkDirtyRunnable()));
		}
		
		@Override
		public void writeSaveNBT(CompoundTag nbt){
			nbt.put("buffertanks", this.bufferTanks.writeNBT());
			nbt.put("chambers", this.chambers.writeNBT());
			nbt.put("energy", this.energy.serializeNBT());
			nbt.put("inventory", writeInventory(this.inventory));
			this.rsState.writeSaveNBT(nbt);
		}
		
		@Override
		public void readSaveNBT(CompoundTag nbt){
			this.bufferTanks.readNBT(nbt.getCompound("buffertanks"));
			this.chambers.readNBT(nbt.getCompound("chambers"));
			readInventory(nbt.getCompound("inventory"));
			this.energy.deserializeNBT(nbt.getCompound("energy"));
			this.rsState.readSaveNBT(nbt);
		}
		
		@Override
		public void writeSyncNBT(CompoundTag nbt){
			writeSaveNBT(nbt);
		}
		
		@Override
		public void readSyncNBT(CompoundTag nbt){
			readSaveNBT(nbt);
		}
		
		protected void readInventory(CompoundTag nbt){
			NonNullList<ItemStack> list = NonNullList.create();
			ContainerHelper.loadAllItems(nbt, list);
			
			for(int i = 0;i < this.inventory.size();i++){
				ItemStack stack = ItemStack.EMPTY;
				if(i < list.size()){
					stack = list.get(i);
				}
				
				this.inventory.set(i, stack);
			}
		}
		
		protected CompoundTag writeInventory(NonNullList<ItemStack> list){
			return ContainerHelper.saveAllItems(new CompoundTag(), list);
		}
		
		public ItemStack getInventory(Inventory inv){
			return this.inventory.get(inv.id());
		}
		
		public ItemStack setInventory(Inventory inv, ItemStack stack){
			return this.inventory.set(inv.id(), stack);
		}
		
		public ItemStack copyStack(ItemStack stack, int amount){
			ItemStack copy = stack.copy();
			copy.setCount(amount);
			return copy;
		}
	}
	
	private static class FilteredItemStackhandler extends ItemStackHandler{
		public FilteredItemStackhandler(NonNullList<ItemStack> inventory){
			super(inventory);
		}
		
		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack){
			if(slot == Inventory.INPUT.id()){
				ItemStack existing = getStackInSlot(slot);
				return (!existing.isEmpty() && ItemStack.isSameItem(existing, stack)) || CokerUnitRecipe.hasRecipeWithInput(stack, true);
			}
			
			return false;
		}
	}
	
	public static class BufferTanks implements IReadWriteNBT{
		private final FluidTank input;
		private final FluidTank output;
		
		public BufferTanks(){
			this.input = new FluidTank(16000);
			this.output = new FluidTank(16000);
		}
		
		public FluidTank input(){
			return this.input;
		}
		
		public FluidTank output(){
			return this.output;
		}
		
		@Override
		public void readNBT(CompoundTag nbt){
			this.input.readFromNBT(nbt.getCompound("input"));
			this.output.readFromNBT(nbt.getCompound("output"));
		}
		
		@Override
		public CompoundTag writeNBT(){
			CompoundTag nbt = new CompoundTag();
			nbt.put("input", this.input.writeToNBT(new CompoundTag()));
			nbt.put("output", this.output.writeToNBT(new CompoundTag()));
			return nbt;
		}
	}
	
	public static class Chambers implements IReadWriteNBT{
		private final CokingChamber[] array;
		public Chambers(){
			this.array = new CokingChamber[]{new CokingChamber(64, 8000), new CokingChamber(64, 8000)};
		}
		
		public CokingChamber[] get(){
			return this.array;
		}
		
		public CokingChamber primary(){
			return this.array[CHAMBER_A];
		}
		
		public CokingChamber secondary(){
			return this.array[CHAMBER_B];
		}
		
		@Override
		public void readNBT(CompoundTag nbt){
			this.array[CHAMBER_A].readFromNBT(nbt.getCompound("primary"));
			this.array[CHAMBER_B].readFromNBT(nbt.getCompound("secondary"));
		}
		
		@Override
		public CompoundTag writeNBT(){
			CompoundTag nbt = new CompoundTag();
			nbt.put("primary", this.array[CHAMBER_A].writeToNBT(new CompoundTag()));
			nbt.put("secondary", this.array[CHAMBER_B].writeToNBT(new CompoundTag()));
			return nbt;
		}
	}
}
