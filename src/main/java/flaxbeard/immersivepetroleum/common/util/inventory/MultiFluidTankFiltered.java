package flaxbeard.immersivepetroleum.common.util.inventory;

import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class MultiFluidTankFiltered implements IFluidHandler, IFluidTank{
	
	protected final List<FluidStack> fluids = new ArrayList<>();
	protected final int capacity;
	protected final Predicate<FluidStack> validator;
	
	public MultiFluidTankFiltered(int capacity){
		this(capacity, fs -> true);
	}
	
	public MultiFluidTankFiltered(int capacity, @Nonnull Predicate<FluidStack> validator){
		this.capacity = capacity;
		this.validator = Objects.requireNonNull(validator);
	}
	
	public MultiFluidTankFiltered readFromNBT(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider){
		if(tag.contains("fluids", Tag.TAG_LIST)){
			this.fluids.clear();
			ListTag tagList = tag.getList("fluids", Tag.TAG_COMPOUND);
			for(int i = 0;i < tagList.size();i++){
				FluidStack fs = FluidStack.parseOptional(provider, tagList.getCompound(i));
				if(!fs.isEmpty())
					this.fluids.add(fs);
			}
		}
		return this;
	}
	
	@Nonnull
	public CompoundTag writeToNBT(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider){
		ListTag tagList = new ListTag();
		for(FluidStack fs: this.fluids)
			if(!fs.isEmpty())
				tagList.add(fs.save(provider));
		tag.put("fluids", tagList);
		return tag;
	}
	
	public GenericContainerData<List<FluidStack>> getContainerData(){
		GetterAndSetter<List<FluidStack>> getterAndSetter = new GetterAndSetter<>(() -> this.fluids, fList -> {
			this.fluids.clear();
			if(!fList.isEmpty())
				this.fluids.addAll(fList);
		});
		return new GenericContainerData<>(GenericDataSerializers.FLUID_STACKS, getterAndSetter);
	}
	
	@Nonnull
	@Override
	public FluidStack getFluid(){
		return this.fluids.isEmpty() ? FluidStack.EMPTY : this.fluids.getLast();
	}
	
	@Override
	public int getFluidAmount(){
		int total = 0;
		for(FluidStack fs: this.fluids)
			total += fs.getAmount();
		return total;
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
		return this.fluids.size();
	}
	
	@Nonnull
	@Override
	public FluidStack getFluidInTank(int tank){
		if(tank < 0 || tank >= this.fluids.size())
			return FluidStack.EMPTY;
		
		return this.fluids.get(tank);
	}
	
	@Override
	public int getTankCapacity(int tank){
		if(tank < 0 || tank >= this.fluids.size())
			return this.capacity - getFluidAmount();
		
		return this.fluids.get(tank).getAmount();
	}
	
	@Override
	public boolean isFluidValid(int tank, @Nonnull FluidStack fluid){
		return isFluidValid(fluid);
	}
	
	@Override
	public int fill(@Nonnull FluidStack fluid, @Nonnull FluidAction action){
		int filled = Math.min(fluid.getAmount(), this.capacity - getFluidAmount());
		if(action.simulate())
			return filled;
		
		for(FluidStack fs: this.fluids){
			if(FluidStack.isSameFluidSameComponents(fs, fluid)){
				fs.grow(filled);
				return filled;
			}
		}
		
		this.fluids.addFirst(fluid.copyWithAmount(filled));
		return filled;
	}
	
	@Nonnull
	@Override
	public FluidStack drain(@Nonnull FluidStack fluid, @Nonnull FluidAction action){
		if(this.fluids.isEmpty())
			return FluidStack.EMPTY;
		
		// TODO Needs deep testing
		return this.fluids.stream().filter(fs -> FluidStack.isSameFluidSameComponents(fs, fluid)).findFirst().map(fs -> {
			int amount = Math.min(fluid.getAmount(), fs.getAmount());
			if(action.execute()){
				fs.shrink(amount);
				if(fs.getAmount() <= 0){
					fs.shrink(amount);
					if(fs.getAmount() <= 0)
						this.fluids.removeIf(f -> f == fs);
				}
			}
			return fluid.copyWithAmount(amount);
		}).orElse(FluidStack.EMPTY);
	}
	
	@Nonnull
	@Override
	public FluidStack drain(int drainAmount, @Nonnull FluidAction action){
		return drain(getFluid().copyWithAmount(drainAmount), action);
	}
}
