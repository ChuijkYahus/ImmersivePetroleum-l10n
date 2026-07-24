package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HighPressureRefineryRecipe extends IPMultiblockRecipe{
	
	public static Map<ResourceLocation, RecipeHolder<HighPressureRefineryRecipe>> recipes = new HashMap<>();
	
	private static final RandomSource RANDOM = RandomSource.create();
	
	@Nullable
	public static RecipeHolder<HighPressureRefineryRecipe> findRecipe(@Nonnull FluidStack input, @Nonnull FluidStack secondary){
		Objects.requireNonNull(input);
		Objects.requireNonNull(secondary);
		
		for(RecipeHolder<HighPressureRefineryRecipe> holder: recipes.values()){
			HighPressureRefineryRecipe recipe = holder.value();
			
			if(secondary.isEmpty()){
				if(recipe.inputFluidSecondary == null && (recipe.inputFluid != null && recipe.inputFluid.test(input)))
					return holder;
				
			}else{
				if((recipe.inputFluid != null && recipe.inputFluid.test(input)) && (recipe.inputFluidSecondary != null && recipe.inputFluidSecondary.test(secondary)))
					return holder;
			}
		}
		
		return null;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(RecipeHolder<HighPressureRefineryRecipe> holder: recipes.values()){
				HighPressureRefineryRecipe recipe = holder.value();
				
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
			for(RecipeHolder<HighPressureRefineryRecipe> holder: recipes.values()){
				HighPressureRefineryRecipe recipe = holder.value();
				
				if(recipe.inputFluidSecondary != null && test(recipe.inputFluidSecondary, fluid, ignoreAmount)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private final FluidStack output;
	private final @Nullable StackWithChance outputItem;
	
	private final SizedFluidIngredient inputFluid;
	private final @Nullable SizedFluidIngredient inputFluidSecondary;
	
	/**
	 * @param output              {@link FluidStack} to output
	 * @param outputItem          {@link StackWithChance} to output
	 * @param inputFluid          {@link FluidStack} to input
	 * @param inputFluidSecondary {@link FluidStack} for secondary input
	 * @param energy              amount of FE to consume
	 * @param time                duration of the recipe
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public HighPressureRefineryRecipe(FluidStack output, Optional<StackWithChance> outputItem, SizedFluidIngredient inputFluid, Optional<SizedFluidIngredient> inputFluidSecondary, Integer energy, Integer time){
		super(IPRecipeTypes.HYDROTREATER, time, energy);
		this.output = output;
		this.outputItem = outputItem.orElse(null);
		this.inputFluid = inputFluid;
		this.inputFluidSecondary = inputFluidSecondary.orElse(null);
		
		this.fluidOutputList = Collections.singletonList(output);
		
		List<SizedFluidIngredient> list = new ArrayList<>(2);
		list.add(inputFluid);
		if(this.inputFluidSecondary != null)
			list.add(this.inputFluidSecondary);
		this.fluidInputList = list;
		
		modifyTimeAndEnergy(IPServerConfig.REFINING.hydrotreater_timeModifier::get, IPServerConfig.REFINING.hydrotreater_energyModifier::get);
	}
	
	public FluidStack getOutputFluid(){
		return this.output.copy();
	}
	
	@Nullable
	public StackWithChance getSecondaryItem(){
		return this.outputItem;
	}
	
	public SizedFluidIngredient getPrimaryInputFluid(){
		return this.inputFluid;
	}
	
	@Nullable
	public SizedFluidIngredient getSecondaryInputFluid(){
		return this.inputFluidSecondary;
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(){
		NonNullList<ItemStack> list = NonNullList.create();
		
		if(this.outputItem != null && RANDOM.nextFloat() <= this.outputItem.chance()){
			list.add(this.outputItem.stack().get().copy());
		}
		
		return list;
	}
	
	@Override
	protected IERecipeSerializer<HighPressureRefineryRecipe> getIESerializer(){
		return Serializers.HYDROTREATER_SERIALIZER.get();
	}
}
