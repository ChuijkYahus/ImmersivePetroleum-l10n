package flaxbeard.immersivepetroleum.common.data.recipes.builders;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.data.recipes.IPMultiblockRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author TwistedGate
 */
public class CokerUnitRecipeBuilder extends IPMultiblockRecipeBuilder<CokerUnitRecipeBuilder, CokerUnitRecipe>{
	
	public static CokerUnitRecipeBuilder builder(@Nonnull ItemStack outputItem, @Nonnull FluidStack outputFluid){
		return new CokerUnitRecipeBuilder(outputItem, outputFluid);
	}
	
	private final ItemStack outputItem;
	private final FluidStack outputFluid;
	
	private IngredientWithSize inputItem;
	private SizedFluidIngredient inputFluid;
	
	private CokerUnitRecipeBuilder(@Nonnull ItemStack outputItem, @Nonnull FluidStack outputFluid){
		this.outputItem = outputItem;
		this.outputFluid = outputFluid;
	}
	
	@Override
	protected CokerUnitRecipe makeInstance(){
		Objects.requireNonNull(this.outputItem, "CokerUnitRecipe must have an output item.");
		Objects.requireNonNull(this.outputFluid, "CokerUnitRecipe must have an output fluid.");
		Objects.requireNonNull(this.inputItem, "CokerUnitRecipe must have an input item.");
		Objects.requireNonNull(this.inputFluid, "CokerUnitRecipe must have an input fluid.");
		
		validateTimeAndEnergy();
		
		return new CokerUnitRecipe(this.outputItem, this.outputFluid, this.inputItem, this.inputFluid, this.energy, this.time);
	}
	
	public CokerUnitRecipeBuilder setInputItem(TagKey<Item> tag, int amount){
		this.inputItem = itemIngredient(tag, amount);
		return this;
	}
	
	public CokerUnitRecipeBuilder setInputFluid(TagKey<Fluid> tag, int amount){
		this.inputFluid = fluidIngredient(tag, amount);
		return this;
	}
}
