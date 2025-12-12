package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DistillationTowerMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic;
import flaxbeard.immersivepetroleum.common.gui.IPSlot.FluidContainer.FluidFilter;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import flaxbeard.immersivepetroleum.common.util.inventory.MultiFluidTankFiltered;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.ItemStackHandler;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.Inventory.INPUT_EMPTY;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.Inventory.INPUT_FILLED;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.Inventory.OUTPUT_EMPTY;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.Inventory.OUTPUT_FILLED;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.State;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.Tanks;

public class DistillationTowerContainer extends MultiblockAwareGuiContainer{
	
	public static DistillationTowerContainer makeServer(MenuType<?> type, int id, Inventory player, MultiblockMenuContext<State> ctx){
		State state = ctx.mbContext().getState();
		
		Tanks tanks = state.tanks;
		AveragingEnergyStorage energy = state.getEnergy();
		
		return new DistillationTowerContainer(multiblockCtx(type, id, ctx), player, new ItemStackHandler(state.inventory.getInternal()), tanks, energy);
	}
	
	public static DistillationTowerContainer makeClient(MenuType<?> type, int id, Inventory player){
		Tanks tanks = Tanks.client();
		AveragingEnergyStorage energy = new AveragingEnergyStorage(State.ENERGY_STORAGE_CAPACITY);
		
		return new DistillationTowerContainer(clientCtx(type, id), player, new ItemStackHandler(DistillationTowerLogic.Inventory.size()), tanks, energy);
	}
	
	public final ItemStackHandler handler;
	public final IEnergyStorage energy;
	public final FluidTankFiltered input;
	public final MultiFluidTankFiltered output;
	
	private DistillationTowerContainer(MenuContext ctx, Inventory playerInventory, ItemStackHandler handler, Tanks tanks, AveragingEnergyStorage energy){
		super(ctx, DistillationTowerMultiblock.INSTANCE);
		this.handler = handler;
		this.energy = energy;
		
		this.input = tanks.input();
		this.output = tanks.output();
		
		addSlot(new IPSlot(handler, INPUT_FILLED.id(), 12, 17, stack -> FluidUtil.getFluidHandler(stack).map(h -> {
			if(h.getTanks() <= 0){
				return false;
			}
			
			FluidStack fs = h.getFluidInTank(0);
			if(fs.isEmpty() || (input.getFluidAmount() > 0 && !fs.is(input.getFluid().getFluid()))){
				return false;
			}
			
			RecipeHolder<DistillationTowerRecipe> recipe = DistillationTowerRecipe.findRecipe(fs);
			return recipe != null;
		}).orElse(false)));
		addSlot(new IPSlot.ItemOutput(handler, INPUT_EMPTY.id(), 12, 53));
		
		addSlot(new IPSlot.FluidContainer(handler, OUTPUT_EMPTY.id(), 134, 17, FluidFilter.EMPTY));
		addSlot(new IPSlot.ItemOutput(handler, OUTPUT_FILLED.id(), 134, 53));
		
		this.ownSlotCount = 4;
		
		addPlayerInventorySlots(playerInventory, 8, 85);
		addPlayerHotbarSlots(playerInventory, 8, 143);
		
		addGenericData(input.getContainerData());
		addGenericData(output.getContainerData());
		addGenericData(GenericContainerData.energy(energy));
	}
}
