package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CokerUnitRecipe extends IPMultiblockRecipe{
	public static Map<ResourceLocation, CokerUnitRecipe> recipes = new HashMap<>();
	
	public static CokerUnitRecipe findRecipe(ItemStack stack, FluidStack fluid){
		for(CokerUnitRecipe recipe: recipes.values()){
			if((recipe.inputItem != null && recipe.inputItem.test(stack)) && (recipe.inputFluid != null && recipe.inputFluid.test(fluid))){
				return recipe;
			}
		}
		
		return null;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull ItemStack stack, @Nonnull FluidStack fluid){
		Objects.requireNonNull(stack);
		Objects.requireNonNull(fluid);
		
		if(!stack.isEmpty() && !fluid.isEmpty()){
			for(CokerUnitRecipe recipe: recipes.values()){
				if(recipe.inputItem != null && recipe.inputFluid != null && recipe.inputItem.test(stack) && recipe.inputFluid.test(fluid)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull ItemStack stack, boolean ignoreAmount){
		Objects.requireNonNull(stack);
		
		if(!stack.isEmpty()){
			for(CokerUnitRecipe recipe: recipes.values()){
				if(recipe.inputItem != null && test(recipe.inputItem, stack, ignoreAmount)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean hasRecipeWithInput(@Nonnull FluidStack fluid, boolean ignoreAmount){
		Objects.requireNonNull(fluid);
		
		if(!fluid.isEmpty()){
			for(CokerUnitRecipe recipe: recipes.values()){
				if(recipe.inputFluid != null && test(recipe.inputFluid, fluid, ignoreAmount)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public final ItemStack outputItem;
	public final FluidStack outputFluid;
	
	public final IngredientWithSize inputItem;
	public final SizedFluidIngredient inputFluid;
	
	public CokerUnitRecipe(ItemStack outputItem, FluidStack outputFluid, IngredientWithSize inputItem, SizedFluidIngredient inputFluid, int energy, int time){
		super(IPRecipeTypes.COKER, time, energy);
		this.outputFluid = outputFluid;
		this.outputItem = outputItem;
		this.inputFluid = inputFluid;
		this.inputItem = inputItem;
		
		modifyTimeAndEnergy(IPServerConfig.REFINING.cokerUnit_timeModifier::get, IPServerConfig.REFINING.cokerUnit_energyModifier::get);
	}
	
	@Override
	public int getMultipleProcessTicks(){
		return 0;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(){
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(this.outputItem);
		return list;
	}
	
	@Override
	protected IERecipeSerializer<CokerUnitRecipe> getIESerializer(){
		return Serializers.COKER_SERIALIZER.get();
	}
}
