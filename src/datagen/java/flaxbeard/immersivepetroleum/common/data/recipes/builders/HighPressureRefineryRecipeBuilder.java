package flaxbeard.immersivepetroleum.common.data.recipes.builders;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.common.data.recipes.IPMultiblockRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author TwistedGate
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class HighPressureRefineryRecipeBuilder extends IPMultiblockRecipeBuilder<HighPressureRefineryRecipeBuilder, HighPressureRefineryRecipe>{
	
	public static HighPressureRefineryRecipeBuilder builder(FluidStack fluidOutput){
		return new HighPressureRefineryRecipeBuilder(fluidOutput);
	}
	
	private final FluidStack output;
	private Optional<StackWithChance> outputItem = Optional.empty();
	
	private SizedFluidIngredient inputFluid;
	private Optional<SizedFluidIngredient> inputFluidSecondary = Optional.empty();
	
	private HighPressureRefineryRecipeBuilder(FluidStack fluidOutput){
		this.output = fluidOutput;
	}
	
	@Override
	protected HighPressureRefineryRecipe makeInstance(){
		Objects.requireNonNull(this.output, "HighPressureRefineryRecipe must have an output fluid.");
		Objects.requireNonNull(this.inputFluid, "HighPressureRefineryRecipe must have an input fluid.");
		
		validateTimeAndEnergy();
		
		return new HighPressureRefineryRecipe(this.output, this.outputItem, this.inputFluid, this.inputFluidSecondary, this.energy, this.time);
	}
	
	public HighPressureRefineryRecipeBuilder addInputFluid(TagKey<Fluid> tag, int amount){
		this.inputFluid = fluidIngredient(tag, amount);
		return this;
	}
	
	public HighPressureRefineryRecipeBuilder addSecondaryInputFluid(TagKey<Fluid> tag, int amount){
		this.inputFluidSecondary = Optional.of(fluidIngredient(tag, amount));
		return this;
	}
	
	public HighPressureRefineryRecipeBuilder addItemWithChance(@Nonnull ItemStack stack, double chance){
		return addItemWithChance(stack, (float) chance);
	}
	
	public HighPressureRefineryRecipeBuilder addItemWithChance(@Nonnull ItemStack stack, float chance){
		this.outputItem = Optional.of(new StackWithChance(stack, chance));
		return this;
	}
}
