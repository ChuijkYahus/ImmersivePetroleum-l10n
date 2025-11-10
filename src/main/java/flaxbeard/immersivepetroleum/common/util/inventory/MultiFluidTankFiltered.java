package flaxbeard.immersivepetroleum.common.util.inventory;

import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class MultiFluidTankFiltered extends MultiFluidTank{
	
	protected Function<FluidStack, Boolean> validator;
	public MultiFluidTankFiltered(int capacity){
		this(capacity, fs -> true);
	}
	
	public MultiFluidTankFiltered(int capacity, @Nonnull Function<FluidStack, Boolean> validator){
		super(capacity);
		this.validator = validator;
	}
	
	@Override
	public boolean isFluidValid(FluidStack stack){
		return this.validator.apply(stack);
	}
	
	@Override
	public boolean isFluidValid(int tank, @Nonnull FluidStack stack){
		return isFluidValid(stack);
	}
	
	@Override
	public int fill(FluidStack resource, FluidAction action){
		if(resource.isEmpty() || !isFluidValid(resource)){
			return 0;
		}
		
		return super.fill(resource, action);
	}
}
