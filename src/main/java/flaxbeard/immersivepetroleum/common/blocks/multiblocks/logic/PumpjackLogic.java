package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.PumpjackShape;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellPipeTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Function;
import java.util.function.Supplier;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.PumpjackLogic.State;

public class PumpjackLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>{
	
	/** Template-Location of the Energy Input Port. (0, 1, 5) */
	public static final BlockPos REDSTONE_IN = new BlockPos(0, 1, 5);
	
	/** Template-Location of the Redstone Input Port. (2, 1, 5) */
	public static final CapabilityPosition ENERGY_IN = new CapabilityPosition(2, 1, 5, RelativeBlockFace.UP);
	
	/** Template-Location of the Eastern Fluid Output Port. (2, 0, 2) */
	public static final MultiblockFace EAST_PORT_OFFSET = new MultiblockFace(3, 0, 2, RelativeBlockFace.RIGHT);
	public static final CapabilityPosition EAST_PORT = CapabilityPosition.opposing(EAST_PORT_OFFSET);
	
	/** Template-Location of the Western Fluid Output Port. (0, 0, 2) */
	public static final MultiblockFace WEST_PORT_OFFSET = new MultiblockFace(-1, 0, 2, RelativeBlockFace.LEFT);
	public static final CapabilityPosition WEST_PORT = CapabilityPosition.opposing(WEST_PORT_OFFSET);
	
	/** Template-Location of the Bottom Fluid Output Port. (1, 0, 0) <b>(Also Master Block)</b> */
	public static final BlockPos DOWN_PORT = new BlockPos(1, 0, 0);
	
	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource){
		return new State(capabilitySource);
	}
	
	@Override
	public void tickClient(IMultiblockContext<State> context){
		final State state = context.getState();
		
		if(state.wasActive){
			state.activeTicks++;
		}
	}
	
	@Override
	public void tickServer(IMultiblockContext<State> context){
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		final boolean rsEnabled = state.rsState.isEnabled(context);
		
		boolean active = false;
		if(rsEnabled){
			BlockEntity teLow = level.getBlockEntity(DOWN_PORT.below());
			
			if(teLow instanceof WellPipeTileEntity pipe){
				WellTileEntity well = pipe.getWell();
				
				if(well != null){
					int consumption = IPServerConfig.EXTRACTION.pumpjack_consumption.get();
					int extracted = state.energy.extractEnergy(consumption, true);
					
					if(extracted >= consumption){
						// Does any reservoir still have pressure?
						boolean foundPressurizedReservoir = false;
						for(ColumnPos cPos: well.tappedReservoirs){
							Reservoir reservoir = ReservoirHandler.getReservoir(level.getRawLevel(), cPos);
							
							if(reservoir != null && reservoir.getPressure(level.getRawLevel(), cPos.x(), cPos.z()) > 0.0F){
								foundPressurizedReservoir = true;
								break;
							}
						}
						
						// Skip if there is (Simulates pumpjack not being able to handle high pressures)
						if(!foundPressurizedReservoir){
							int extractSpeed = IPServerConfig.EXTRACTION.pumpjack_speed.get();
							
							IFluidHandler portEast_output = state.east_port_output.get();
							IFluidHandler portWest_output = state.west_port_output.get();
							
							for(ColumnPos cPos: well.tappedReservoirs){
								Reservoir reservoir = ReservoirHandler.getReservoir(level.getRawLevel(), cPos);
								
								if(reservoir != null){
									FluidStack fluid = new FluidStack(reservoir.getFluid(), reservoir.extract(extractSpeed, FluidAction.SIMULATE));
									
									if(portEast_output != null){
										int accepted = portEast_output.fill(fluid, FluidAction.SIMULATE);
										if(accepted > 0){
											int drained = portEast_output.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted)), FluidAction.EXECUTE);
											reservoir.extract(drained, FluidAction.EXECUTE);
											fluid = FluidHelper.copyFluid(fluid, fluid.getAmount() - drained);
											active = true;
										}
									}
									
									if(portWest_output != null && fluid.getAmount() > 0){
										int accepted = portWest_output.fill(fluid, FluidAction.SIMULATE);
										if(accepted > 0){
											int drained = portWest_output.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted)), FluidAction.EXECUTE);
											reservoir.extract(drained, FluidAction.EXECUTE);
											active = true;
										}
									}
								}
							}
							
							if(active){
								state.energy.extractEnergy(consumption, false);
								state.activeTicks++;
							}
						}
					}
				}
			}
		}
		
		if(active != state.wasActive){
			context.markMasterDirty();
			context.requestMasterBESync();
		}
		state.wasActive = active;
	}
	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register){
		register.registerAtOrNull(Capabilities.EnergyStorage.BLOCK, ENERGY_IN, state -> state.energy);
		register.register(Capabilities.FluidHandler.BLOCK, (state, pos) -> {
			if(EAST_PORT.equalsOrNullFace(pos) || WEST_PORT.equalsOrNullFace(pos))
				return state.fakeFluidHandler;
			
			return null;
		});
	}
	
	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
		return PumpjackShape.GETTER;
	}
	
	public static class State implements IMultiblockState{
		public static final FluidTank FAKE_TANK = new FluidTank(0);
		
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
		public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
		
		public boolean wasActive = false;
		public float activeTicks = 0;
		
		private final IFluidHandler fakeFluidHandler = FAKE_TANK;
		
		private final Supplier<IFluidHandler> east_port_output;
		private final Supplier<IFluidHandler> west_port_output;
		
		public State(IInitialMultiblockContext<State> context){
			this.east_port_output = context.getCapabilityAt(Capabilities.FluidHandler.BLOCK, EAST_PORT_OFFSET);
			this.west_port_output = context.getCapabilityAt(Capabilities.FluidHandler.BLOCK, WEST_PORT_OFFSET);
		}
		
		@Override
		public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			nbt.putBoolean("wasActive", this.wasActive);
			nbt.put("energy", this.energy.serializeNBT(provider));
			this.rsState.writeSaveNBT(nbt, provider);
		}
		
		@Override
		public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			boolean lastActive = this.wasActive;
			this.wasActive = nbt.getBoolean("wasActive");
			if(!this.wasActive && lastActive){
				this.activeTicks++;
			}
			this.energy.deserializeNBT(provider, nbt.getCompound("energy"));
			this.rsState.readSaveNBT(nbt, provider);
		}
		
		@Override
		public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			nbt.putBoolean("wasActive", this.wasActive);
		}
		
		@Override
		public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			boolean lastActive = this.wasActive;
			this.wasActive = nbt.getBoolean("wasActive");
			if(!this.wasActive && lastActive){
				this.activeTicks++;
			}
		}
	}
}
