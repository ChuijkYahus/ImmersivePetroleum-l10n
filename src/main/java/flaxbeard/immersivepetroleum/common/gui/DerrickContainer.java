package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DerrickMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic.State;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class DerrickContainer extends MultiblockAwareGuiContainer{
	
	public static DerrickContainer makeServer(MenuType<?> type, int id, Inventory player, MultiblockMenuContext<State> ctx){
		State state = ctx.mbContext().getState();
		BlockPos pos = ctx.mbContext().getLevel().getAbsoluteOrigin();
		Level level = ctx.mbContext().getLevel().getRawLevel();
		
		return new DerrickContainer(multiblockCtx(type, id, ctx),
			player,
			new ItemStackHandler(state.inventory),
			state.tank,
			state.energy,
			level,
			pos.mutable());
	}
	
	public static DerrickContainer makeClient(MenuType<?> type, int id, Inventory player){
		return new DerrickContainer(clientCtx(type, id),
			player,
			new ItemStackHandler(1),
			new FluidTankFiltered(8000),
			new AveragingEnergyStorage(16000),
			player.player.level(),
			BlockPos.ZERO.mutable());
	}
	
	public final IEnergyStorage energy;
	public final FluidTankFiltered tank;
	public final ItemStackHandler items;
	public final Level level;
	private final MutableBlockPos pos;
	
	private DerrickContainer(MenuContext ctx, Inventory playerInventory, ItemStackHandler items, FluidTankFiltered tank, AveragingEnergyStorage energy, Level level, MutableBlockPos pos){
		super(ctx, DerrickMultiblock.INSTANCE);
		this.items = items;
		this.energy = energy;
		this.tank = tank;
		this.level = level;
		this.pos = pos;
		
		this.addSlot(new SlotItemHandler(this.items, 0, 37, 27){
			@Override
			public boolean mayPlace(@Nonnull ItemStack stack){
				return ExternalModContent.IE.isPipe(stack);
			}
		});
		
		this.ownSlotCount = 1;
		
		addPlayerInventorySlots(playerInventory, 20, 90);
		addPlayerHotbarSlots(playerInventory, 20, 148);
		
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BLOCK_POS, this.pos::immutable, this.pos::set));
		addGenericData(GenericContainerData.energy(energy));
		addGenericData(tank.getContainerData());
	}
	
	public BlockPos pos(){
		return this.pos.immutable();
	}
}
