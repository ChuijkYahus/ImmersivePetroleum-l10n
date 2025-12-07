package flaxbeard.immersivepetroleum.common.data.recipes.builders;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.common.data.recipes.IPMultiblockRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author TwistedGate
 */
public class DistillationTowerRecipeBuilder extends IPMultiblockRecipeBuilder<DistillationTowerRecipeBuilder, DistillationTowerRecipe>{
	
	public static DistillationTowerRecipeBuilder builder(@Nonnull FluidStack... fluidOutput){
		Objects.requireNonNull(fluidOutput);
		if(fluidOutput.length == 0)
			throw new IllegalArgumentException("Must contain at least one output fluid");
		
		return new DistillationTowerRecipeBuilder(Arrays.asList(fluidOutput));
	}
	
	private final List<FluidStack> fluidOutput;
	
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private Optional<List<StackWithChance>> itemOutput = Optional.empty();
	
	private SizedFluidIngredient input;
	
	private DistillationTowerRecipeBuilder(List<FluidStack> fluidOutput){
		this.fluidOutput = fluidOutput;
	}
	
	@Override
	protected DistillationTowerRecipe makeInstance(){
		Objects.requireNonNull(this.input, "DistillationTowerRecipe must have an input fluid.");
		
		if(this.fluidOutput.isEmpty())
			throw new IllegalStateException("DistillationTowerRecipe must contain at least one output fluid.");
		
		validateTimeAndEnergy();
		
		return new DistillationTowerRecipe(this.fluidOutput, this.itemOutput, this.input, this.energy, this.time);
	}
	
	public DistillationTowerRecipeBuilder setInput(TagKey<Fluid> tag, int amount){
		this.input = fluidIngredient(tag, amount);
		return this;
	}
	
	public DistillationTowerRecipeBuilder addByproduct(@Nonnull ItemStack stack, double chance){
		return addByproduct(stack, (float) chance);
	}
	
	public DistillationTowerRecipeBuilder addByproduct(@Nonnull ItemStack stack, float chance){
		if(this.itemOutput.isEmpty())
			this.itemOutput = Optional.of(new ArrayList<>());
		
		this.itemOutput.ifPresent(list -> list.add(new StackWithChance(stack, chance)));
		return this;
	}
}
