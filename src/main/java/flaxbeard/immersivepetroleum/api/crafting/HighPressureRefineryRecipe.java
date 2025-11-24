package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
				if(recipe.inputFluid != null){
					if((!ignoreAmount && recipe.inputFluid.test(fluid)) || (ignoreAmount && recipe.inputFluid.testIgnoringAmount(fluid))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean hasRecipeWithSecondaryInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(HighPressureRefineryRecipe recipe: recipes.values()){
				if(recipe.inputFluidSecondary != null){
					if((!ignoreAmount && recipe.inputFluidSecondary.test(fluid)) || (ignoreAmount && recipe.inputFluidSecondary.testIgnoringAmount(fluid))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private final ItemStack outputItem;
	private final double chance;
	
	private final FluidStack output;
	
	private final FluidTagInput inputFluid;
	@Nullable
	private final FluidTagInput inputFluidSecondary;
	
	/**
	 * @param id                  {@link ResourceLocation} ID to create the recipe with
	 * @param output              {@link FluidStack} to output
	 * @param outputItem          {@link ItemStack} to output
	 * @param inputFluid          {@link FluidStack} to input
	 * @param inputFluidSecondary {@link FluidStack} for secondary input
	 * @param chance              double chance of the {@link ItemStack} output
	 * @param energy              amount of FE to consume
	 * @param time                duration of the recipe
	 */
	public HighPressureRefineryRecipe(ResourceLocation id, FluidStack output, ItemStack outputItem, FluidTagInput inputFluid, @Nullable FluidTagInput inputFluidSecondary, double chance, int energy, int time){
		super(IPRecipeTypes.HYDROTREATER, id, time, energy);
		this.output = output;
		this.outputItem = outputItem;
		this.inputFluid = inputFluid;
		this.inputFluidSecondary = inputFluidSecondary;
		this.chance = Mth.clamp(chance, 0.0F, 1.0F);
		
		this.fluidOutputList = Collections.singletonList(output);
		
		List<FluidTagInput> inputs = new ArrayList<>(2);
		inputs.add(inputFluid);
		if(inputFluidSecondary != null)
			inputs.add(inputFluidSecondary);
		
		this.fluidInputList = inputs;
		
		modifyTimeAndEnergy(IPServerConfig.REFINING.hydrotreater_timeModifier::get, IPServerConfig.REFINING.hydrotreater_energyModifier::get);
	}
	
	public FluidStack getOutputFluid(){
		return this.output.copy();
	}
	
	public FluidTagInput getPrimaryInputFluid(){
		return this.inputFluid;
	}
	
	public boolean hasSecondaryItem(){
		return this.outputItem != null && !this.outputItem.isEmpty();
	}
	
	@Nullable
	public FluidTagInput getSecondaryInputFluid(){
		return this.inputFluidSecondary;
	}
	
	public ItemStack getOutputItem(){
		if(!hasSecondaryItem())
			return ItemStack.EMPTY;
		
		return this.outputItem.copy();
	}
	
	public double getOutputItemChance(){
		return this.chance;
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(){
		NonNullList<ItemStack> list = NonNullList.create();
		if(hasSecondaryItem() && (this.chance == 1.0F || RANDOM.nextFloat() <= this.chance)){
			list.add(this.outputItem.copy());
		}
		return list;
	}
	
	@Override
	protected IERecipeSerializer<HighPressureRefineryRecipe> getIESerializer(){
		return Serializers.HYDROTREATER_SERIALIZER.get();
	}
}
