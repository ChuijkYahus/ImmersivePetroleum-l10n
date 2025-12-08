package flaxbeard.immersivepetroleum.common.gui;

import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class IPSlot extends SlotItemHandler{
	private final Predicate<ItemStack> consumer;
	
	public IPSlot(IItemHandler inventoryIn, int index, int xPosition, int yPosition){
		super(inventoryIn, index, xPosition, yPosition);
		this.consumer = null;
	}
	
	public IPSlot(IItemHandler inventoryIn, int index, int xPosition, int yPosition, Predicate<ItemStack> placeCheck){
		super(inventoryIn, index, xPosition, yPosition);
		this.consumer = placeCheck;
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack){
		if(this.consumer != null){
			return this.consumer.test(stack);
		}
		return super.mayPlace(stack);
	}
	
	public static class ItemOutput extends IPSlot{
		public ItemOutput(IItemHandler inventoryIn, int id, int x, int y){
			super(inventoryIn, id, x, y);
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack stack){
			return false;
		}
	}
	
	public static class CokerInput extends IPSlot{
		public CokerInput(IItemHandler inv, int id, int x, int y){
			super(inv, id, x, y);
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack stack){
			return !stack.isEmpty() && CokerUnitRecipe.hasRecipeWithInput(stack, true);
		}
	}
	
	public static class FluidContainer extends IPSlot{
		FluidFilter filter;
		public FluidContainer(IItemHandler inv, int id, int x, int y, FluidFilter filter){
			super(inv, id, x, y);
			this.filter = filter;
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack stack){
			IFluidHandlerItem capability = stack.getCapability(Capabilities.FluidHandler.ITEM);
			if(capability != null && capability.getTanks() > 0){
				return switch(this.filter){
					case FULL -> !capability.getFluidInTank(0).isEmpty();
					case EMPTY -> capability.getFluidInTank(0).isEmpty();
					case ANY -> true;
				};
			}
			return false;
		}
		
		public enum FluidFilter{
			ANY, EMPTY, FULL
		}
	}
}
