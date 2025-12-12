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
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.IReadWriteNBT;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.CokerShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import flaxbeard.immersivepetroleum.common.util.inventory.EnumInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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
		
		final CokingChamber[] chambers = state.chambers.array();
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
			ItemStack inputStack = state.inventory.get(Inventory.INPUT);
			FluidStack inputFluid = state.bufferTanks.input().getFluid();
			
			if(!inputStack.isEmpty() && inputFluid.getAmount() > 0 && CokerUnitRecipe.hasRecipeWithInput(inputStack, inputFluid)){
				RecipeHolder<CokerUnitRecipe> holder = CokerUnitRecipe.findRecipe(inputStack, inputFluid);
				
				if(holder != null){
					final CokerUnitRecipe recipe = holder.value();
					
					if(inputStack.getCount() >= recipe.getInputItem().getCount() && inputFluid.getAmount() >= recipe.getInputFluid().amount()){
						for(CokingChamber chamber: state.chambers.array()){
							boolean skipNext = false;
							
							switch(chamber.getState()){
								case STANDBY -> {
									if(chamber.setRecipe(holder)){
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
				
				
			}
			
			final CokingChamber[] chambers = state.chambers.array();
			for(int i = 0;i < chambers.length;i++){
				update |= chambers[i].tick(context, i);
			}
		}
		
		if(!state.inventory.get(Inventory.INPUT_FILLED).isEmpty() && state.bufferTanks.input().getFluidAmount() < state.bufferTanks.input().getCapacity()){
			final ItemStack inv_input_filled = state.inventory.get(Inventory.INPUT_FILLED);
			final ItemStack inv_input_empty = state.inventory.get(Inventory.INPUT_EMPTY);
			
			ItemStack container = FluidHelper.tryDrainContainer(inv_input_filled, state.bufferTanks.input());
			if(!container.isEmpty()){
				if(!inv_input_empty.isEmpty() && inv_input_empty.isStackable() && inv_input_empty.getCount() < inv_input_empty.getMaxStackSize()){
					inv_input_empty.grow(container.getCount());
				}else if(inv_input_empty.isEmpty()){
					state.inventory.set(Inventory.INPUT_EMPTY, container.copy());
				}
				
				inv_input_filled.shrink(1);
				if(inv_input_filled.getCount() <= 0){
					state.inventory.set(Inventory.INPUT_FILLED, ItemStack.EMPTY);
				}
				
				update = true;
			}
		}
		
		if(state.bufferTanks.output().getFluidAmount() > 0){
			final ItemStack inv_output_empty = state.inventory.get(Inventory.OUTPUT_EMPTY);
			final ItemStack inv_output_filled = state.inventory.get(Inventory.OUTPUT_FILLED);
			
			if(!inv_output_empty.isEmpty()){
				ItemStack container = FluidHelper.fillFluidContainer(state.bufferTanks.output(), inv_output_empty, inv_output_filled, null);
				
				if(!container.isEmpty()){
					if(inv_output_filled.getCount() == 1 && !FluidHelper.isFluidContainerFull(container)){
						state.inventory.set(Inventory.OUTPUT_FILLED, container.copy());
						
					}else{
						if(!inv_output_filled.isEmpty() && inv_output_filled.isStackable() && inv_output_filled.getCount() < inv_output_filled.getMaxStackSize()){
							inv_output_filled.grow(container.getCount());
						}else if(inv_output_filled.isEmpty()){
							state.inventory.set(Inventory.OUTPUT_FILLED, container.copy());
						}
						
						inv_output_empty.shrink(1);
						if(inv_output_empty.getCount() <= 0){
							state.inventory.set(Inventory.OUTPUT_EMPTY, ItemStack.EMPTY);
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
		
		ItemStack stack = state.inventory.get(Inventory.INPUT);
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
	public void registerCapabilities(CapabilityRegistrar<State> register){
		register.registerAtOrNull(Capabilities.EnergyStorage.BLOCK, Energy_IN[0], state -> state.energy);
		register.registerAtOrNull(Capabilities.EnergyStorage.BLOCK, Energy_IN[1], state -> state.energy);
		register.registerAt(Capabilities.ItemHandler.BLOCK, Item_IN, state -> state.itemInput);
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
		return CokerShape.GETTER;
	}
	
	public static class State implements IMultiblockState{
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(24000);
		public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
		
		int lastCompared = 0;
		
		public final EnumInventory<Inventory> inventory = new EnumInventory<>(Inventory.class);
		public final BufferTanks bufferTanks = new BufferTanks();
		public final Chambers chambers = new Chambers();
		
		private final IItemHandler itemInput = new FilteredItemStackhandler(this.inventory.getInternal());
		private final ArrayFluidHandler fluidInput;
		private final ArrayFluidHandler fluidOutput;
		public BlockPos masterPos;
		
		public State(IInitialMultiblockContext<State> context, BlockPos pos){
			this.masterPos = pos;
			
			this.fluidInput = ArrayFluidHandler.fillOnly(this.bufferTanks.input(), context.getMarkDirtyRunnable());
			this.fluidOutput = ArrayFluidHandler.drainOnly(this.bufferTanks.output(), context.getMarkDirtyRunnable());
		}
		
		@Override
		public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			nbt.put("buffertanks", this.bufferTanks.writeNBT(provider));
			nbt.put("chambers", this.chambers.writeNBT(provider));
			nbt.put("energy", this.energy.serializeNBT(provider));
			this.inventory.save(nbt, provider);
			this.rsState.writeSaveNBT(nbt, provider);
		}
		
		@Override
		public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			this.bufferTanks.readNBT(nbt.getCompound("buffertanks"), provider);
			this.chambers.readNBT(nbt.getCompound("chambers"), provider);
			this.inventory.load(nbt, provider);
			this.energy.deserializeNBT(provider, nbt.getCompound("energy"));
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
		public static final int CAPACITY = 16 * FluidType.BUCKET_VOLUME;
		
		private final FluidTankFiltered input;
		private final FluidTankFiltered output;
		
		public BufferTanks(){
			this.input = new FluidTankFiltered(CAPACITY);
			this.output = new FluidTankFiltered(CAPACITY);
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
		
		public FluidTankFiltered output(){
			return this.output;
		}
	}
	
	public static class Chambers implements IReadWriteNBT{
		final CokingChamber primary;
		final CokingChamber secondary;
		final CokingChamber[] array;
		public Chambers(){
			this.primary = new CokingChamber(64, 8000);
			this.secondary = new CokingChamber(64, 8000);
			this.array = new CokingChamber[]{
				this.primary,
				this.secondary
			};
		}
		
		public CokingChamber primary(){
			return this.primary;
		}
		
		public CokingChamber secondary(){
			return this.secondary;
		}
		
		public CokingChamber[] array(){
			return this.array;
		}
		
		@Override
		public void readNBT(CompoundTag nbt, HolderLookup.Provider provider){
			this.primary.readFromNBT(nbt.getCompound("primary"), provider);
			this.secondary.readFromNBT(nbt.getCompound("secondary"), provider);
		}
		
		@Override
		public CompoundTag writeNBT(HolderLookup.Provider provider){
			CompoundTag nbt = new CompoundTag();
			nbt.put("primary", this.primary.writeToNBT(new CompoundTag(), provider));
			nbt.put("secondary", this.secondary.writeToNBT(new CompoundTag(), provider));
			return nbt;
		}
	}
	
}
