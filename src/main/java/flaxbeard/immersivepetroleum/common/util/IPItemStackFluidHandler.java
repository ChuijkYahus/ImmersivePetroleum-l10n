package flaxbeard.immersivepetroleum.common.util;

import flaxbeard.immersivepetroleum.common.IPDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class IPItemStackFluidHandler extends FluidHandlerItemStack{
	private final Predicate<FluidStack> isFluidValid;
	
	public IPItemStackFluidHandler(ItemStack container, int capacity){
		this(container, capacity, f -> true);
	}
	
	public IPItemStackFluidHandler(ItemStack container, int capacity, Predicate<FluidStack> isFluidValid){
		super(IPDataComponents.FLUID_ITEM, container, capacity);
		this.isFluidValid = isFluidValid;
	}
	
	@Override
	public boolean canFillFluidType(@Nonnull FluidStack fluid){
		return this.isFluidValid.test(fluid);
	}
}
