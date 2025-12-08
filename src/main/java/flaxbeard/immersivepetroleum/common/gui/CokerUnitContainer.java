package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.CokerUnitMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokerUnitLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokingChamber;
import flaxbeard.immersivepetroleum.common.gui.IPSlot.FluidContainer.FluidFilter;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokerUnitLogic.BufferTanks;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokerUnitLogic.Chambers;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokerUnitLogic.State;

public class CokerUnitContainer extends MultiblockAwareGuiContainer{
	
	public static CokerUnitContainer makeServer(MenuType<?> type, int id, Inventory player, MultiblockMenuContext<State> ctx){
		State state = ctx.mbContext().getState();
		BlockPos pos = ctx.mbContext().getLevel().getAbsoluteOrigin();
		Level level = ctx.mbContext().getLevel().getRawLevel();
		
		//@formatter:off
		return new CokerUnitContainer(
			multiblockCtx(type, id, ctx),
			player,
			new ItemStackHandler(state.inventory),
			state.bufferTanks,
			state.chambers,
			state.energy,
			level,
			pos.mutable()
		);
		//@formatter:on
	}
	
	public static CokerUnitContainer makeClient(MenuType<?> type, int id, Inventory player){
		//@formatter:off
		return new CokerUnitContainer(
			clientCtx(type, id),
			player,
			new ItemStackHandler(CokerUnitLogic.Inventory.values().length),
			new BufferTanks(),
			new Chambers(),
			new AveragingEnergyStorage(24000),
			player.player.level(),
			BlockPos.ZERO.mutable()
		);
		//@formatter:on
	}
	
	public final AveragingEnergyStorage energy;
	public final FluidTankFiltered input;
	public final FluidTankFiltered output;
	public final ItemStackHandler items;
	
	public ChamberWrapper primary, secondary;
	
	public final Level level;
	private final MutableBlockPos pos;
	
	private CokerUnitContainer(MenuContext ctx, Inventory playerInventory, ItemStackHandler items, BufferTanks tanks, Chambers chambers, AveragingEnergyStorage energy, Level level, MutableBlockPos pos){
		super(ctx, CokerUnitMultiblock.INSTANCE);
		this.items = items;
		this.input = tanks.input();
		this.output = tanks.output();
		this.energy = energy;
		this.level = level;
		this.pos = pos;
		
		this.primary = new ChamberWrapper(chambers.primary());
		this.secondary = new ChamberWrapper(chambers.secondary());
		
		addSlot(new IPSlot.CokerInput(this.items, CokerUnitLogic.Inventory.INPUT.id(), 20, 71));
		addSlot(new IPSlot(this.items, CokerUnitLogic.Inventory.INPUT_FILLED.id(), 9, 14, stack -> FluidUtil.getFluidHandler(stack).map(h -> {
			if(h.getTanks() <= 0 || h.getFluidInTank(0).isEmpty()){
				return false;
			}
			
			FluidStack fs = h.getFluidInTank(0);
			if(fs.isEmpty() || (tanks.input().getFluidAmount() > 0 && !fs.is(tanks.input().getFluid().getFluid()))){
				return false;
			}
			
			return CokerUnitRecipe.hasRecipeWithInput(fs, true);
		}).orElse(false)));
		addSlot(new IPSlot.ItemOutput(this.items, CokerUnitLogic.Inventory.INPUT_EMPTY.id(), 9, 45));
		
		addSlot(new IPSlot.FluidContainer(this.items, CokerUnitLogic.Inventory.OUTPUT_EMPTY.id(), 175, 14, FluidFilter.EMPTY));
		addSlot(new IPSlot.ItemOutput(this.items, CokerUnitLogic.Inventory.OUTPUT_FILLED.id(), 175, 45));
		
		this.ownSlotCount = CokerUnitLogic.Inventory.values().length;
		
		addPlayerInventorySlots(playerInventory, 20, 105);
		addPlayerHotbarSlots(playerInventory, 20, 163);
		
		addGenericData(GenericContainerData.energy(energy));
		addGenericData(tanks.input().getContainerData());
		addGenericData(tanks.output().getContainerData());
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BLOCK_POS, this.pos::immutable, this.pos::set));
		
		this.primary.addData(this);
		this.secondary.addData(this);
	}
	
	public BlockPos pos(){
		return this.pos.immutable();
	}
	
	public static class ChamberWrapper{
		private final CokingChamber chamber;
		
		private Integer inputAmount = null;
		private Integer outputAmount = null;
		
		public ChamberWrapper(CokingChamber chamber){
			this.chamber = chamber;
		}
		
		public void addData(CokerUnitContainer container){
			container.addGenericData(new GenericContainerData<>(GenericDataSerializers.INT32, this::getInputAmount, this::setInputAmount));
			container.addGenericData(new GenericContainerData<>(GenericDataSerializers.INT32, this::getOutputAmount, this::setOutputAmount));
			container.addGenericData(this.chamber.getTank().getContainerData());
		}
		
		public int getInputAmount(){
			if(this.inputAmount == null)
				return this.chamber.getInputAmount();
			
			return this.inputAmount;
		}
		
		public int getOutputAmount(){
			if(this.outputAmount == null)
				return this.chamber.getOutputAmount();
			
			return this.outputAmount;
		}
		
		public void setInputAmount(int inputAmount){
			this.inputAmount = inputAmount;
		}
		
		public void setOutputAmount(int outputAmount){
			this.outputAmount = outputAmount;
		}
		
		public CokingChamber get(){
			return new CokingChamberDummy(this);
		}
		
		private static class CokingChamberDummy extends CokingChamber{
			private final Supplier<Integer> inputSupplier;
			private final Supplier<Integer> outputSupplier;
			private final FluidTankFiltered tank;
			
			public CokingChamberDummy(ChamberWrapper wrapper){
				super(wrapper.chamber.getCapacity(), wrapper.chamber.getTank().getCapacity());
				this.inputSupplier = wrapper::getInputAmount;
				this.outputSupplier = wrapper::getOutputAmount;
				this.tank = wrapper.chamber.getTank();
			}
			
			@Override
			public int getInputAmount(){
				return this.inputSupplier.get();
			}
			
			@Override
			public int getOutputAmount(){
				return this.outputSupplier.get();
			}
			
			@Override
			public FluidTankFiltered getTank(){
				return this.tank;
			}
			
			@Override
			public ItemStack getInputItem(){
				return ItemStack.EMPTY;
			}
			
			@Override
			public ItemStack getOutputItem(){
				return ItemStack.EMPTY;
			}
			
			@Nullable
			@Override
			public RecipeHolder<CokerUnitRecipe> getRecipe(){
				return null;
			}
			
			@Override
			public boolean tick(IMultiblockContext<CokerUnitLogic.State> context, int chamberId){
				return false;
			}
		}
	}
}
