package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public abstract class IPMultiblockRecipe extends MultiblockRecipe{
	private static final Supplier<RecipeMultiplier> NONE = () -> null;
	
	private final TimeAndEnergy processingCost;
	
	protected <T extends Recipe<?>> IPMultiblockRecipe(IERecipeTypes.TypeWithClass<T> type, int time, int energy){
		this(TagOutput.EMPTY, type, time, energy);
	}
	
	protected <T extends Recipe<?>> IPMultiblockRecipe(TagOutput outputDummy, IERecipeTypes.TypeWithClass<T> type, int time, int energy){
		super(outputDummy, type, time, energy, NONE);
		this.processingCost = new TimeAndEnergy(time, energy);
	}
	
	public void modifyTimeAndEnergy(DoubleSupplier timeModifier, DoubleSupplier energyModifier){
		this.processingCost.modifyTimeAndEnergy(timeModifier, energyModifier);
	}
	
	@Override
	public int getTotalProcessTime(){
		return this.processingCost.getTime();
	}
	
	@Override
	public int getTotalProcessEnergy(){
		return this.processingCost.getEnergy();
	}
	
	protected static boolean test(IngredientWithSize a, ItemStack b, boolean ignoreAmount){
		return (!ignoreAmount && a.test(b)) || (ignoreAmount && a.testIgnoringSize(b));
	}
	
	protected static boolean test(SizedFluidIngredient a, FluidStack b, boolean ignoreAmount){
		boolean equal = a.test(b);
		return (!ignoreAmount && equal && a.amount() == b.getAmount()) || (ignoreAmount && equal);
	}
	
	private static class TimeAndEnergy{
		private final int baseTime;
		private final int baseEnergy;
		
		private Lazy<Integer> processingTime;
		private Lazy<Integer> processingEnergy;
		
		public TimeAndEnergy(int baseTime, int baseEnergy){
			this.baseTime = baseTime;
			this.baseEnergy = baseEnergy;
			
			this.processingTime = Lazy.of(() -> this.baseTime);
			this.processingEnergy = Lazy.of(() -> this.baseEnergy);
		}
		
		private void modifyTimeAndEnergy(DoubleSupplier timeModifier, DoubleSupplier energyModifier){
			this.processingTime = Lazy.of(() -> (int) Math.max(1.0D, this.baseTime * timeModifier.getAsDouble()));
			this.processingEnergy = Lazy.of(() -> (int) Math.max(1.0D, this.baseEnergy * energyModifier.getAsDouble()));
		}
		
		public int getTime(){
			return this.processingTime.get();
		}
		
		public int getEnergy(){
			return this.processingEnergy.get();
		}
	}
}
