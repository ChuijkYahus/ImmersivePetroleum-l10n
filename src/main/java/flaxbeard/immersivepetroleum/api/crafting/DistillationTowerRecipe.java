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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistillationTowerRecipe extends IPMultiblockRecipe{
	public static Map<ResourceLocation, RecipeHolder<DistillationTowerRecipe>> recipes = new HashMap<>();
	
	private static final RandomSource RANDOM = RandomSource.create();
	
	/** May return null! */
	public static RecipeHolder<DistillationTowerRecipe> findRecipe(FluidStack input){
		if(!recipes.isEmpty()){
			for(RecipeHolder<DistillationTowerRecipe> holder: recipes.values()){
				DistillationTowerRecipe recipe = holder.value();
				
				if(recipe.input != null && recipe.input.ingredient().test(input)){
					return holder;
				}
			}
		}
		
		return null;
	}
	
	public final FluidStack[] fluidOutput;
	public final @Nullable StackWithChance[] itemOutput;
	
	public final SizedFluidIngredient input;
	
	public DistillationTowerRecipe(FluidStack[] fluidOutput, @Nullable List<StackWithChance> itemOutput, SizedFluidIngredient input, int energy, int time){
		super(IPRecipeTypes.DISTILLATION, time, energy);
		this.fluidOutput = fluidOutput;
		
		this.itemOutput = (itemOutput != null && !itemOutput.isEmpty()) ? itemOutput.toArray(StackWithChance[]::new) : null;
		
		this.input = input;
		this.fluidInputList = Collections.singletonList(input);
		this.fluidOutputList = Arrays.asList(this.fluidOutput);
		
		modifyTimeAndEnergy(IPServerConfig.REFINING.distillationTower_timeModifier::get, IPServerConfig.REFINING.distillationTower_energyModifier::get);
	}
	
	@Override
	protected IERecipeSerializer<DistillationTowerRecipe> getIESerializer(){
		return Serializers.DISTILLATION_SERIALIZER.get();
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(){
		if(this.itemOutput == null || this.itemOutput.length == 0)
			return NonNullList.create();
		
		NonNullList<ItemStack> output = NonNullList.create();
		
		for(StackWithChance chancedStack: this.itemOutput){
			if(RANDOM.nextFloat() <= chancedStack.chance()){
				output.add(chancedStack.stack().get());
			}
		}
		
		return output;
	}
	
	public SizedFluidIngredient getInputFluid(){
		return this.input;
	}
	
	@Deprecated(forRemoval = true)
	public double[] chances(){
		return new double[0];
	}
}
