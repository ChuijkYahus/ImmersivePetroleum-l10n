package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic;

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
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import flaxbeard.immersivepetroleum.client.gui.elements.PipeConfig;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic.State;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.DerrickShape;
import flaxbeard.immersivepetroleum.common.blocks.stone.WellPipeBlock;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DerrickLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>{
	public static final int REQUIRED_WATER_AMOUNT = 125;
	public static final int REQUIRED_CONCRETE_AMOUNT = 125;
	
	public enum Inventory{
		/** Item Pipe Input */
		INPUT;
		
		public int id(){
			return ordinal();
		}
	}
	
	public static final FluidTank DUMMY_TANK = new FluidTank(0);
	
	/** Template-Location of the Fluid Input Port. (2 0 4) */
	public static final CapabilityPosition FLUID_IN = new CapabilityPosition(2, 0, 4, RelativeBlockFace.BACK);
	
	/** Template-Location of the Fluid Output Port. (4 0 2) */
	public static final CapabilityPosition FLUID_OUT = new CapabilityPosition(4, 0, 2, RelativeBlockFace.LEFT);
	
	/** Template-Location of the Energy Input Ports. (2 1 0) */
	public static final CapabilityPosition ENERGY_IN = new CapabilityPosition(2, 1, 0, RelativeBlockFace.UP);
	
	/** Template-Location of the Redstone Input Port. (0 1 1) */
	public static final BlockPos REDSTONE_IN = new BlockPos(0, 1, 1);
	
	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource){
		return new State(capabilitySource);
	}
	
	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register){
		register.registerAtOrNull(EnergyStorage.BLOCK, ENERGY_IN, state -> state.energy);
		register.register(FluidHandler.BLOCK, (state, pos) -> {
			if(FLUID_IN.equals(pos))
				return state.fluidHandler;
			
			if(FLUID_OUT.equals(pos))
				return state.emptyHandler;
			
			return null;
		});
	}
	
	//@formatter:off
	private static final BlockState[] PARTICLE_STATES = new BlockState[]{
			Blocks.STONE.defaultBlockState(),
			Blocks.GRANITE.defaultBlockState(),
			Blocks.GRAVEL.defaultBlockState(),
			Blocks.DEEPSLATE.defaultBlockState(),
			Blocks.DIORITE.defaultBlockState(),
			Blocks.SAND.defaultBlockState(),
			Blocks.ANDESITE.defaultBlockState(),
	};
	//@formatter:on
	
	@Override
	public void tickClient(IMultiblockContext<State> context){
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		
		if(state.drilling){
			state.rotation += 10;
			state.rotation %= 2160; // 360 * 6
			
			double x = (level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()).getX() + 0.5);
			double y = (level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()).getY() + 1.0);
			double z = (level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()).getZ() + 0.5);
			int r = level.getRawLevel().random.nextInt(PARTICLE_STATES.length);
			for(int i = 0;i < 5;i++){
				float xa = (level.getRawLevel().random.nextFloat() - 0.5F) * 10.0F;
				float ya = 5.0F;
				float za = (level.getRawLevel().random.nextFloat() - 0.5F) * 10.0F;
				
				level.getRawLevel().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, PARTICLE_STATES[r]), x, y, z, xa, ya, za);
			}
		}
		
		if(state.spilling){
			ClientProxy.spawnSpillParticles(level.getRawLevel(), level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()), state.fluidSpilled, 5, 1.25F, state.clientFlow);
		}
	}
	
	@Override
	public void tickServer(IMultiblockContext<State> context){
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		final boolean rsEnabled = state.rsState.isEnabled(context);
		
		if(state.level == null)
			state.level = level::getRawLevel;
		if(state.originPos == null)
			state.originPos = level.getAbsoluteOrigin();
		
		boolean wasActive = false;
		boolean lastDrilling = state.drilling;
		boolean lastSpilling = state.spilling;
		state.drilling = state.spilling = false;
		
		if(level.getAbsoluteOrigin().getY() < level.getRawLevel().getSeaLevel()){
			if(state.fluidSpilled == Fluids.EMPTY){
				state.fluidSpilled = Fluids.WATER;
			}
			state.spilling = true;
		}else{
			WellTileEntity well = createAndGetWell(state.level, state, level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()), getInventory(state, Inventory.INPUT) != ItemStack.EMPTY);
			if(rsEnabled){
				if(state.energy.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), true) >= IPServerConfig.EXTRACTION.derrick_consumption.get()){
					if(well != null){
						if(well.wellPipeLength < well.getMaxPipeLength()){
							if(well.pipes <= 0 && getInventory(state, Inventory.INPUT) != ItemStack.EMPTY){
								ItemStack stack = getInventory(state, Inventory.INPUT);
								if(stack.getCount() > 0){
									stack.shrink(1);
									well.pipes = WellTileEntity.PIPE_WORTH;
									
									if(stack.getCount() <= 0){
										setInventory(state, Inventory.INPUT, ItemStack.EMPTY);
									}
									
									well.setChanged();
									wasActive = true;
								}
							}
							
							if(well.pipes > 0){
								final BlockPos dPos = level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB());
								final BlockPos wPos = well.getBlockPos();
								int realPipeLength = ((dPos.getY() - 1) - wPos.getY());
								
								if(well.phyiscalPipesList.size() < realPipeLength && well.wellPipeLength < realPipeLength){
									if(state.tank.drain(REQUIRED_CONCRETE_AMOUNT, IFluidHandler.FluidAction.SIMULATE).getAmount() >= REQUIRED_CONCRETE_AMOUNT){
										state.energy.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), false);
										
										if(advanceTimer(state)){
											Level world = level.getRawLevel();
											int y = dPos.getY() - 1;
											for(;y > wPos.getY();y--){
												BlockPos current = new BlockPos(dPos.getX(), y, dPos.getZ());
												BlockState blockState = world.getBlockState(current);
												
												if(blockState.getBlock() == Blocks.BEDROCK || blockState.getBlock() == IPContent.Blocks.WELL.get()){
													break;
												}else if(!(blockState.getBlock() == IPContent.Blocks.WELL_PIPE.get() && !blockState.getValue(WellPipeBlock.BROKEN))){
													world.destroyBlock(current, false);
													world.setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());
													
													well.phyiscalPipesList.add(y);
													
													state.tank.drain(REQUIRED_CONCRETE_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
													
													well.usePipe();
													break;
												}
											}
											
											if(well.phyiscalPipesList.size() >= realPipeLength && well.wellPipeLength >= realPipeLength){
												well.pastPhysicalPart = true;
												well.setChanged();
											}
										}
										
										wasActive = true;
										state.drilling = true;
									}
								}else{
									if(!state.tank.getFluid().isEmpty() && ExternalModContent.IE.isConcrete(state.tank.getFluid())){
										// FIXME ! This happens every now and then, and i have not yet nailed down HOW this happens.
										// Void excess concrete.
										state.tank.drain(state.tank.getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
										wasActive = true;
									}
									if(state.tank.drain(REQUIRED_WATER_AMOUNT, IFluidHandler.FluidAction.SIMULATE).getAmount() >= REQUIRED_WATER_AMOUNT){
										state.energy.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), false);
										
										if(advanceTimer(state)){
											restorePhysicalPipeProgress(well, dPos, realPipeLength);
											
											state.tank.drain(REQUIRED_WATER_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
											well.usePipe();
										}
										
										wasActive = true;
										state.drilling = true;
									}
								}
							}
						}
					}
				}
			}
			
			if(well != null && well.wellPipeLength == well.getMaxPipeLength())
				outputReservoirFluid(level, state, level.toAbsolute(FLUID_OUT.posInMultiblock()), context);
		}
		
		if(state.spilling && state.fluidSpilled == Fluids.EMPTY){
			state.fluidSpilled = IPContent.Fluids.CRUDEOIL.get();
		}
		if(!state.spilling && state.fluidSpilled != Fluids.EMPTY){
			state.fluidSpilled = Fluids.EMPTY;
		}
		
		boolean forceSync = false;
		if(state.isRedstoned != !rsEnabled){
			state.isRedstoned = !rsEnabled;
			forceSync = true;
		}
		
		if(forceSync || wasActive || lastDrilling != state.drilling || lastSpilling != state.spilling){
			context.markDirtyAndSync();
		}
	}
	
	// Only accept as much Concrete and Water as needed
	private static boolean acceptsFluid(Supplier<Level> level, State state, BlockPos inPos, FluidStack fs){
		if(fs.isEmpty())
			return false;
		
		WellTileEntity well = createAndGetWell(level, state, inPos, false);
		if(well == null){
			return false;
		}
		
		final Fluid inFluid = fs.getFluid();
		final boolean isConcrete = inFluid == ExternalModContent.IE.fluidConcrete();
		final boolean isWater = inFluid == Fluids.WATER;
		
		if(!isConcrete && !isWater)
			return false;
		
		int realPipeLength = (inPos.getY() - 1) - well.getBlockPos().getY();
		int concreteNeeded = (REQUIRED_CONCRETE_AMOUNT * (realPipeLength - well.wellPipeLength));
		if(concreteNeeded > 0 && isConcrete){
			FluidStack tankFluidStack = state.tank.getFluid();
			
			if((!tankFluidStack.isEmpty() && inFluid != tankFluidStack.getFluid()) || tankFluidStack.getAmount() >= concreteNeeded){
				return false;
			}
			
			return concreteNeeded >= fs.getAmount();
		}
		
		if(concreteNeeded <= 0){
			int waterNeeded = REQUIRED_WATER_AMOUNT * (well.getMaxPipeLength() - well.wellPipeLength);
			if(waterNeeded > 0 && isWater){
				FluidStack tankFluidStack = state.tank.getFluid();
				
				if((!tankFluidStack.isEmpty() && inFluid != tankFluidStack.getFluid()) || tankFluidStack.getAmount() >= waterNeeded){
					return false;
				}
				
				return waterNeeded >= fs.getAmount();
			}
		}
		
		return false;
	}
	
	public static WellTileEntity createAndGetWell(Supplier<Level> level, State state, BlockPos inPos, boolean popList){
		Level rawLevel = level.get();
		
		if(state.wellCache != null && state.wellCache.isRemoved()){
			state.wellCache = null;
		}
		
		if(state.wellCache == null){
			WellTileEntity well = null;
			
			for(int y = inPos.below().getY();y >= rawLevel.getMinBuildHeight() - 1;y--){
				BlockPos current = new BlockPos(inPos.getX(), y, inPos.getZ());
				BlockState blockState = rawLevel.getBlockState(current);
				
				if(blockState.getBlock() == IPContent.Blocks.WELL.get()){
					well = (WellTileEntity) rawLevel.getBlockEntity(current);
					break;
				}else if(blockState.getBlock() == Blocks.BEDROCK){
					rawLevel.setBlockAndUpdate(current, IPContent.Blocks.WELL.get().defaultBlockState());
					well = (WellTileEntity) rawLevel.getBlockEntity(current);
					break;
				}
			}
			
			state.wellCache = well;
		}
		
		if(popList && state.wellCache != null && state.wellCache.tappedReservoirs.isEmpty()){
			if(state.gridStorage != null){
				transferGridDataToWell(inPos, state, state.wellCache);
			}else{
				state.wellCache.tappedReservoirs.add(Utils.toColumnPos(inPos));
				state.wellCache.setChanged();
			}
		}
		
		if(state.wellCache != null){
			state.wellCache.abortSelfDestructSequence();
		}
		
		return state.wellCache;
	}
	
	public ItemStack getInventory(State state, Inventory inv){
		return state.inventory.get(inv.id());
	}
	
	public ItemStack setInventory(State state, Inventory inv, ItemStack stack){
		return state.inventory.set(inv.id(), stack);
	}
	
	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop){
		ItemStack stack = state.inventory.getFirst();
		if(!stack.isEmpty())
			drop.accept(stack);
	}
	
	private boolean advanceTimer(State state){
		if(state.timer-- <= 0){
			state.timer = 10;
			return true;
		}
		
		return false;
	}
	
	public void restorePhysicalPipeProgress(@Nonnull WellTileEntity well, BlockPos dPos, int realPipeLength){
		int min = Math.min(well.wellPipeLength, realPipeLength);
		for(int i = 1;i < min;i++){
			BlockPos current = new BlockPos(dPos.getX(), dPos.getY() - i, dPos.getZ());
			BlockState blockState = well.getLevel().getBlockState(current);
			if(!(blockState.getBlock() instanceof WellPipeBlock)){
				well.getLevel().destroyBlock(current, false);
				well.getLevel().setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());
			}
		}
	}
	
	private void outputReservoirFluid(IMultiblockLevel mbLevel, State state, BlockPos inPos, IMultiblockContext<State> ctx){
		final Level rawLevel = mbLevel.getRawLevel();
		
		WellTileEntity well = createAndGetWell(() -> rawLevel, state, inPos, true);
		if(well == null)
			return;
		
		FluidStack extracted = getExtractedFluidStack(well);
		if(!extracted.isEmpty()){
			Direction front = mbLevel.getOrientation().front();
			boolean mirrored = mbLevel.getOrientation().mirrored();
			
			Direction facing = mirrored ? front.getCounterClockWise() : front.getClockWise();
			BlockPos outPos = mbLevel.toAbsolute(FLUID_OUT.posInMultiblock()).relative(facing, 1);
			
			IFluidHandler fluidHandler = rawLevel.getCapability(FluidHandler.BLOCK, outPos, facing.getOpposite());
			if(fluidHandler != null){
				boolean isIEPipe = rawLevel.getBlockEntity(outPos) instanceof IFluidPipe;
				
				state.spilling = iterativeOutput(fluidHandler, extracted, isIEPipe);
				
			}else{
				state.spilling = true;
			}
		}
		
		if(state.spilling && !extracted.isEmpty() && state.fluidSpilled != extracted.getFluid())
			state.fluidSpilled = extracted.getFluid();
		
		if(!state.spilling && state.fluidSpilled != Fluids.EMPTY)
			state.fluidSpilled = Fluids.EMPTY;
		
	}
	
	/**
	 * <b>This is a hack!</b><br>
	 * <br>
	 * Transfer on IE Pipes is limited to 1000mB in a single tick.
	 * So this outputs multiple times with <b>10</b> attempts max or until everything is transferred
	 */
	private static boolean iterativeOutput(IFluidHandler out, FluidStack extracted, boolean isIEPipe){
		FluidStack fluid = FluidHelper.copyFluid(extracted, extracted.getAmount(), isIEPipe);
		
		int drainedTotal = 0;
		int attempt = 0;
		for(;attempt < 10 && fluid.getAmount() > 0;attempt++){
			int accepted = out.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
			if(accepted == 0)
				return true;
			
			int drained = out.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted), isIEPipe), IFluidHandler.FluidAction.EXECUTE);
			fluid = FluidHelper.copyFluid(extracted, fluid.getAmount() - drained, isIEPipe);
			drainedTotal += drained;
		}
		
		return (extracted.getAmount() - drainedTotal) > 0;
	}
	
	public static void transferGridDataToWell(BlockPos masterPos, State state, @Nullable WellTileEntity well){
		if(well == null)
			return;
		
		int additionalPipes = 0;
		List<ColumnPos> list = new ArrayList<>();
		PipeConfig.Grid grid = state.gridStorage;
		for(int j = 0;j < grid.getHeight();j++){
			for(int i = 0;i < grid.getWidth();i++){
				int type = grid.get(i, j);
				
				if(type > 0){
					switch(type){
						case PipeConfig.PIPE_PERFORATED:
						case PipeConfig.PIPE_PERFORATED_FIXED:{
							int x = i - (grid.getWidth() / 2);
							int z = j - (grid.getHeight() / 2);
							ColumnPos pos = new ColumnPos(masterPos.getX() + x, masterPos.getZ() + z);
							list.add(pos);
						}
						case PipeConfig.PIPE_NORMAL:{
							additionalPipes++;
						}
					}
				}
			}
		}
		
		well.tappedReservoirs = list;
		well.additionalPipes = additionalPipes;
		well.setChanged();
	}
	
	private FluidStack getExtractedFluidStack(@Nonnull WellTileEntity well){
		Fluid extractedFluid = Fluids.EMPTY;
		int extractedAmount = 0;
		for(ColumnPos cPos: well.tappedReservoirs){
			Reservoir reservoir = ReservoirHandler.getReservoir(well.getLevel(), cPos);
			if(reservoir != null){
				if(extractedFluid == Fluids.EMPTY){
					extractedFluid = reservoir.getFluid();
				}else if(reservoir.getFluid() != extractedFluid){
					continue;
				}
				
				extractedAmount += reservoir.extractWithPressure(well.getLevel(), cPos.x(), cPos.z());
			}
		}
		
		return new FluidStack(extractedFluid, extractedAmount);
	}
	
	@Override
	public void onRemoved(IMultiblockContext<State> context){
		if(context.getLevel().getRawLevel().isClientSide)
			return;
		
		IMultiblockLevel mbLevel = context.getLevel();
		
		WellTileEntity well = context.getState().getWell(mbLevel, mbLevel.toRelative(IPContent.Multiblock.DERRICK.masterPosInMB()));
		if(well != null && !well.drillingCompleted){
			if(well.wellPipeLength > 0){
				well.startSelfDestructSequence();
			}else{
				Level rawLevel = mbLevel.getRawLevel();
				if(rawLevel.isLoaded(well.getBlockPos())){
					rawLevel.setBlockAndUpdate(well.getBlockPos(), Blocks.BEDROCK.defaultBlockState());
				}
			}
		}
	}
	
	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
		return DerrickShape.GETTER;
	}
	
	public static class State implements IMultiblockState{
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
		public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
		
		public int timer = 0;
		public int rotation = 0;
		public boolean drilling;
		public boolean spilling;
		private Fluid fluidSpilled = Fluids.EMPTY;
		public final FluidTankFiltered tank;
		public final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
		private WellTileEntity wellCache = null;
		
		/** Stores the current derrick configuration. */
		@Nullable
		public PipeConfig.Grid gridStorage;
		private int clientFlow;
		private Supplier<Level> level;
		public BlockPos originPos;
		
		public boolean isRedstoned;
		
		private final IFluidHandler fluidHandler;
		private final IFluidHandler emptyHandler;
		private final IItemHandler itemHandler;
		
		public State(IInitialMultiblockContext<State> context){
			this.originPos = ((InitialMultiblockContext<State>) context).masterBE().getBlockPos();
			this.level = context.levelSupplier();
			
			Runnable markDirtyRunnable = context.getMarkDirtyRunnable();
			this.tank = new FluidTankFiltered(8000, fluidStack -> acceptsFluid(this.level, this, this.originPos, fluidStack));
			
			this.fluidHandler = ArrayFluidHandler.fillOnly(this.tank, markDirtyRunnable);
			this.emptyHandler = ArrayFluidHandler.drainOnly(DUMMY_TANK, markDirtyRunnable);
			this.itemHandler = new ItemStackHandler(this.inventory);
		}
		
		@Override
		public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			this.drilling = nbt.getBoolean("drilling");
			this.spilling = nbt.getBoolean("spilling");
			this.clientFlow = nbt.getInt("spillflow");
			
			try{
				this.fluidSpilled = BuiltInRegistries.FLUID.get(ResourceLocation.parse(nbt.getString("spillingfluid")));
			}catch(ResourceLocationException rle){
				this.fluidSpilled = Fluids.EMPTY;
			}
			
			if(nbt.contains("grid", Tag.TAG_COMPOUND)){
				this.gridStorage = PipeConfig.Grid.fromCompound(nbt.getCompound("grid"));
			}
			
			this.tank.readFromNBT(nbt.getCompound("tank"), provider);
			
			this.rsState.readSaveNBT(nbt, provider);
			this.isRedstoned = nbt.getBoolean("isRedstoned");
			
			ContainerHelper.loadAllItems(nbt, this.inventory, provider);
		}
		
		@Override
		public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			nbt.putBoolean("drilling", this.drilling);
			nbt.putBoolean("spilling", this.spilling);
			nbt.putInt("spillflow", getReservoirFlow());
			nbt.putString("spillingfluid", RegistryUtils.getRegistryNameOf(this.fluidSpilled).toString());
			
			nbt.put("tank", this.tank.writeToNBT(new CompoundTag(), provider));
			
			if(this.gridStorage != null){
				nbt.put("grid", this.gridStorage.toCompound());
			}
			
			this.rsState.writeSaveNBT(nbt, provider);
			nbt.putBoolean("isRedstoned", this.isRedstoned);
			
			ContainerHelper.saveAllItems(nbt, this.inventory, provider);
		}
		
		@Override
		public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			readSaveNBT(nbt, provider);
		}
		
		@Override
		public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			writeSaveNBT(nbt, provider);
			
		}
		
		private int getReservoirFlow(){
			Reservoir reservoir = ReservoirHandler.getReservoir(level.get(), originPos);
			if(reservoir == null || this.originPos.getY() < level.get().getSeaLevel())
				return 10;
			
			return reservoir.getFlowFromPressure(level.get(), originPos);
		}
		
		public WellTileEntity getWell(IMultiblockLevel level, BlockPos inPos){
			if(this.wellCache != null && this.wellCache.isRemoved()){
				this.wellCache = null;
			}
			
			if(this.wellCache == null){
				Level world = level.getRawLevel();
				WellTileEntity well = null;
				
				for(int y = inPos.below().getY();y >= world.getMinBuildHeight();y--){
					BlockPos current = new BlockPos(inPos.getX(), y, inPos.getZ());
					BlockState blockState = world.getBlockState(current);
					
					if(blockState.getBlock() == IPContent.Blocks.WELL.get()){
						well = (WellTileEntity) world.getBlockEntity(current);
						break;
					}
				}
				
				this.wellCache = well;
			}
			
			return this.wellCache;
		}
	}
}
