package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HighPressureRefineryRecipe extends IPMultiblockRecipe{
	
	public static Map<ResourceLocation, HighPressureRefineryRecipe> recipes = new HashMap<>();
	
	private static final RandomSource RANDOM = RandomSource.create();
	
	public static HighPressureRefineryRecipe findRecipe(@Nonnull FluidStack input, @Nonnull FluidStack secondary){
		Objects.requireNonNull(input);
		Objects.requireNonNull(secondary);
		
		for(HighPressureRefineryRecipe recipe: recipes.values()){
			if(secondary.isEmpty()){
				if(recipe.inputFluidSecondary == null && (recipe.inputFluid != null && recipe.inputFluid.test(input))){
					return recipe;
				}
			}else{
				if((recipe.inputFluid != null && recipe.inputFluid.test(input)) && (recipe.inputFluidSecondary != null && recipe.inputFluidSecondary.test(secondary))){
					return recipe;
				}
			}
		}
		return null;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(HighPressureRefineryRecipe recipe: recipes.values()){
				if(recipe.inputFluid != null && test(recipe.inputFluid, fluid, ignoreAmount)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean hasRecipeWithSecondaryInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(HighPressureRefineryRecipe recipe: recipes.values()){
				if(recipe.inputFluidSecondary != null && test(recipe.inputFluidSecondary, fluid, ignoreAmount)){
					return true;
				}
			}
		}
		return false;
	}
	
	public final FluidStack output;
	public final @Nullable StackWithChance outputItem;
	
	public final SizedFluidIngredient inputFluid;
	public final @Nullable SizedFluidIngredient inputFluidSecondary;
	
	/**
	 * @param output              {@link FluidStack} to output
	 * @param outputItem          {@link StackWithChance} to output
	 * @param inputFluid          {@link FluidStack} to input
	 * @param inputFluidSecondary {@link FluidStack} for secondary input
	 * @param energy              amount of FE to consume
	 * @param time                duration of the recipe
	 */
	public HighPressureRefineryRecipe(FluidStack output, @Nullable StackWithChance outputItem, SizedFluidIngredient inputFluid, @Nullable SizedFluidIngredient inputFluidSecondary, int energy, int time){
		super(IPRecipeTypes.HYDROTREATER, time, energy);
		this.output = output;
		this.outputItem = outputItem;
		this.inputFluid = inputFluid;
		this.inputFluidSecondary = inputFluidSecondary;
		
		this.fluidOutputList = Collections.singletonList(output);
		this.fluidInputList = Arrays.asList(inputFluidSecondary != null ? new SizedFluidIngredient[]{
			inputFluid,
			inputFluidSecondary
		} : new SizedFluidIngredient[]{inputFluid});
		
		modifyTimeAndEnergy(IPServerConfig.REFINING.hydrotreater_timeModifier::get, IPServerConfig.REFINING.hydrotreater_energyModifier::get);
	}
	
	public boolean hasSecondaryItem(){
		return this.outputItem != null;
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	public SizedFluidIngredient getInputFluid(){
		return this.inputFluid;
	}
	
	@Nullable
	public SizedFluidIngredient getSecondaryInputFluid(){
		return this.inputFluidSecondary;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(){
		NonNullList<ItemStack> list = NonNullList.create();
		
		if(this.outputItem != null && RANDOM.nextFloat() <= this.outputItem.chance()){
			list.add(this.outputItem.stack().get());
		}
		
		return list;
	}
	
	@Override
	protected IERecipeSerializer<HighPressureRefineryRecipe> getIESerializer(){
		return Serializers.HYDROTREATER_SERIALIZER.get();
	}
}
