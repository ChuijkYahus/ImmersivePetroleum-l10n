package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DistillationTowerRecipe extends IPMultiblockRecipe{
	public static Map<ResourceLocation, RecipeHolder<DistillationTowerRecipe>> recipes = new HashMap<>();
	
	private static final RandomSource RANDOM = RandomSource.create();
	
	@Nullable
	public static RecipeHolder<DistillationTowerRecipe> findRecipe(FluidStack input){
		if(recipes.isEmpty())
			return null;
		
		for(RecipeHolder<DistillationTowerRecipe> holder: recipes.values()){
			DistillationTowerRecipe recipe = holder.value();
			
			if(recipe.input != null && recipe.input.ingredient().test(input)){
				return holder;
			}
		}
		
		return null;
	}
	
	private final @Nullable StackWithChance[] itemOutput;
	
	private final SizedFluidIngredient input;
	
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public DistillationTowerRecipe(List<FluidStack> fluidOutput, Optional<List<StackWithChance>> itemOutput, SizedFluidIngredient input, int energy, int time){
		super(IPRecipeTypes.DISTILLATION, time, energy);
		
		this.input = input;
		
		this.itemOutput = itemOutput.map(list -> list.toArray(StackWithChance[]::new)).orElse(null);
		
		this.fluidInputList = Collections.singletonList(input);
		this.fluidOutputList = fluidOutput;
		
		modifyTimeAndEnergy(IPServerConfig.REFINING.distillationTower_timeModifier::get, IPServerConfig.REFINING.distillationTower_energyModifier::get);
	}
	
	public SizedFluidIngredient getInputFluid(){
		return this.input;
	}
	
	public List<StackWithChance> getItemOutput(){
		if(this.itemOutput == null)
			return Collections.emptyList();
		
		return List.of(this.itemOutput);
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
	public ItemStack getResultItem(HolderLookup.Provider access){
		NonNullList<ItemStack> outputs = getItemOutputs();
		if(outputs != null && !outputs.isEmpty())
			return outputs.getFirst().copy();
		return ItemStack.EMPTY;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(){
		if(this.itemOutput == null || this.itemOutput.length == 0)
			return NonNullList.create();
		
		NonNullList<ItemStack> output = NonNullList.create();
		
		for(StackWithChance chancedStack: this.itemOutput){
			if(RANDOM.nextFloat() <= chancedStack.chance()){
				output.add(chancedStack.stack().get().copy());
			}
		}
		
		return output;
	}
}
