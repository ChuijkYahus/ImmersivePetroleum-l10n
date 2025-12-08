package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.HydroTreaterMultiblock;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.energy.IEnergyStorage;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater.HydroTreaterLogic.State;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater.HydroTreaterLogic.Tanks;

public class HydrotreaterContainer extends MultiblockAwareGuiContainer{
	
	public static HydrotreaterContainer makeServer(MenuType<?> type, int id, Inventory player, MultiblockMenuContext<State> ctx){
		State state = ctx.mbContext().getState();
		
		Tanks tanks = state.tanks;
		AveragingEnergyStorage energy = state.getEnergy();
		
		return new HydrotreaterContainer(multiblockCtx(type, id, ctx), tanks, energy);
	}
	
	public static HydrotreaterContainer makeClient(MenuType<?> type, int id, Inventory player){
		Tanks tanks = Tanks.client();
		AveragingEnergyStorage energy = new AveragingEnergyStorage(State.ENERGY_STORAGE_CAPACITY);
		
		return new HydrotreaterContainer(clientCtx(type, id), tanks, energy);
	}
	
	public final FluidTankFiltered primary;
	public final FluidTankFiltered secondary;
	public final FluidTankFiltered output;
	public final IEnergyStorage energy;
	
	private HydrotreaterContainer(MenuContext ctx, Tanks tanks, AveragingEnergyStorage energy){
		super(ctx, HydroTreaterMultiblock.INSTANCE);
		this.primary = tanks.primary();
		this.secondary = tanks.secondary();
		this.output = tanks.output();
		this.energy = energy;
		
		addGenericData(this.primary.getContainerData());
		addGenericData(this.secondary.getContainerData());
		addGenericData(this.output.getContainerData());
		addGenericData(GenericContainerData.energy(energy));
	}
}
