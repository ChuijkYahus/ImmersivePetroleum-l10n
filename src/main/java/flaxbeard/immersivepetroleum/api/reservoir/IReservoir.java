package flaxbeard.immersivepetroleum.api.reservoir;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;

public interface IReservoir{
	
	void setDirty();
	
	/**
	 * @return amount of fluid currently in this Reservoir.
	 */
	long getAmount();
	
	/**
	 * @return The current capacity of this Reservoir.
	 */
	long getCapacity();
	
	boolean isInfinite();
	
	/** Only relevant if {@link #isInfinite()} returns true */
	int getInfinityFlowRate();
	
	@Nonnull
	RecipeHolder<ReservoirType> getType();
	
	Fluid getFluid();
	
	ReservoirBoundingBox getBoundingBox();
	
	ReservoirPolygon getPolygon();
	
	boolean isEmpty();
	
	/**
	 * @param amount      to extract
	 * @param fluidAction the {@link FluidAction} to extract with
	 * @return how much has been extracted
	 */
	int extract(int amount, FluidAction fluidAction);
	
	/**
	 * @param x x-coordinate to extract from
	 * @param z z-coordinate to extract from
	 * @return How much was extracted
	 */
	int extractWithPressure(@Nonnull Level world, int x, int z);
	
	CompoundTag writeToNBT();
}
