package flaxbeard.immersivepetroleum.common.data.recipes;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import flaxbeard.immersivepetroleum.api.crafting.IPMultiblockRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author TwistedGate
 */
@SuppressWarnings("unchecked")
public abstract class IPMultiblockRecipeBuilder<B extends IPMultiblockRecipeBuilder<B, R>, R extends IPMultiblockRecipe> extends IPGenericBuilder<R>{
	
	protected final List<ICondition> conditions = new ArrayList<>();
	protected int energy;
	protected int time;
	
	protected abstract R makeInstance();
	
	@Override
	public B addCondition(ICondition condition){
		this.conditions.add(condition);
		return (B) this;
	}
	
	public B setTimeAndEnergy(int time, int energy){
		this.energy = energy;
		this.time = time;
		
		validateTimeAndEnergy();
		
		return (B) this;
	}
	
	protected void validateTimeAndEnergy(){
		if(this.energy <= 0)
			throw new IllegalStateException("Energy consumption must be between 1 - " + Integer.MAX_VALUE);
		
		if(this.time <= 0)
			throw new IllegalStateException("Time must be between 1 - " + Integer.MAX_VALUE);
	}
	
	// ########################################################################################################
	
	protected static IngredientWithSize itemIngredient(ItemStack stack){
		return itemIngredient(Ingredient.of(stack.getItem()), stack.getCount());
	}
	
	protected static IngredientWithSize itemIngredient(ItemLike itemLike, int amount){
		return itemIngredient(Ingredient.of(itemLike), amount);
	}
	
	protected static IngredientWithSize itemIngredient(Ingredient ingredient, int amount){
		return new IngredientWithSize(ingredient, amount);
	}
	
	protected static IngredientWithSize itemIngredient(TagKey<Item> tag, int amount){
		return new IngredientWithSize(tag, amount);
	}
	
	protected static SizedFluidIngredient fluidIngredient(FluidStack fluidStack){
		return fluidIngredient(fluidStack.getFluid(), fluidStack.getAmount());
	}
	
	protected static SizedFluidIngredient fluidIngredient(Fluid fluid, int amount){
		return new SizedFluidIngredient(FluidIngredient.single(fluid), amount);
	}
	
	protected static SizedFluidIngredient fluidIngredient(TagKey<Fluid> tag, int amount){
		return new SizedFluidIngredient(FluidIngredient.tag(tag), amount);
	}
}
