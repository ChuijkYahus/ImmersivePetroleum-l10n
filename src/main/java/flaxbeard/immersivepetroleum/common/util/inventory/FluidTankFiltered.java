package flaxbeard.immersivepetroleum.common.util.inventory;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Predicate;

public class FluidTankFiltered implements IFluidHandler, IFluidTank{
	
	@Nonnull
	protected FluidStack fluid = FluidStack.EMPTY;
	protected final int capacity;
	protected final Predicate<FluidStack> validator;
	
	public FluidTankFiltered(int capacity){
		this(capacity, fs -> true);
	}
	
	public FluidTankFiltered(int capacity, @Nonnull Predicate<FluidStack> validator){
		this.capacity = capacity;
		this.validator = Objects.requireNonNull(validator);
	}
	
	public FluidTankFiltered readFromNBT(CompoundTag tag, HolderLookup.Provider provider){
		this.fluid = FluidStack.parseOptional(provider, tag.getCompound("Fluid"));
		return this;
	}
	
	public CompoundTag writeToNBT(CompoundTag tag, HolderLookup.Provider provider){
		if(!this.fluid.isEmpty()){
			tag.put("Fluid", this.fluid.save(provider));
		}
		
		return tag;
	}
	
	public GenericContainerData<FluidStack> getContainerData(){
		GetterAndSetter<FluidStack> getterAndSetter = new GetterAndSetter<>(this::getFluid, this::setFluid);
		return new GenericContainerData<>(GenericDataSerializers.FLUID_STACK, getterAndSetter);
	}
	
	private void setFluid(FluidStack fluid){
		this.fluid = Objects.requireNonNullElse(fluid, FluidStack.EMPTY);
	}
	
	@Nonnull
	@Override
	public FluidStack getFluid(){
		return this.fluid;
	}
	
	@Override
	public int getFluidAmount(){
		return this.fluid.getAmount();
	}
	
	@Override
	public int getCapacity(){
		return this.capacity;
	}
	
	@Override
	public boolean isFluidValid(@Nonnull FluidStack fluid){
		return this.validator.test(fluid);
	}
	
	@Override
	public int getTanks(){
		return 1;
	}
	
	@Nonnull
	@Override
	public FluidStack getFluidInTank(int i){
		return getFluid();
	}
	
	@Override
	public int getTankCapacity(int i){
		return getCapacity();
	}
	
	@Override
	public boolean isFluidValid(int i, @Nonnull FluidStack fluid){
		return isFluidValid(fluid);
	}
	
	@Override
	public int fill(@Nonnull FluidStack fluid, @Nonnull FluidAction action){
		if(fluid.has(IEApiDataComponents.FLUID_PRESSURIZED)){
			fluid = fluid.copy();
			fluid.remove(IEApiDataComponents.FLUID_PRESSURIZED);
		}
		
		if(!fluid.isEmpty() && this.isFluidValid(fluid)){
			if(action.simulate()){
				if(this.fluid.isEmpty()){
					return Math.min(this.capacity, fluid.getAmount());
				}else{
					return !FluidStack.isSameFluidSameComponents(this.fluid, fluid) ? 0 : Math.min(this.capacity - this.fluid.getAmount(), fluid.getAmount());
				}
			}else if(this.fluid.isEmpty()){
				this.fluid = fluid.copyWithAmount(Math.min(this.capacity, fluid.getAmount()));
				return this.fluid.getAmount();
			}else if(!FluidStack.isSameFluidSameComponents(this.fluid, fluid)){
				return 0;
			}else{
				int filled = this.capacity - this.fluid.getAmount();
				if(fluid.getAmount() < filled){
					this.fluid.grow(fluid.getAmount());
					filled = fluid.getAmount();
				}else{
					this.fluid.setAmount(this.capacity);
				}
				
				return filled;
			}
		}else{
			return 0;
		}
	}
	
	@Nonnull
	@Override
	public FluidStack drain(@Nonnull FluidStack fluid, @Nonnull FluidAction action){
		return !fluid.isEmpty() && FluidStack.isSameFluidSameComponents(fluid, this.fluid) ? this.drain(fluid.getAmount(), action) : FluidStack.EMPTY;
	}
	
	@Nonnull
	@Override
	public FluidStack drain(int i, @Nonnull FluidAction action){
		int drained = i;
		if(this.fluid.getAmount() < drained){
			drained = this.fluid.getAmount();
		}
		
		FluidStack stack = this.fluid.copyWithAmount(drained);
		if(action.execute() && drained > 0){
			this.fluid.shrink(drained);
		}
		
		return stack;
	}
}
