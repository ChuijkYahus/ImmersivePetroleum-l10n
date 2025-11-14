package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Document("mods/immersivepetroleum/DistillationTowerAlt")
@Name("mods.immersivepetroleum.DistillationTowerAlt")
public class DistillationTweaker implements IRecipeManager<DistillationTowerRecipe>{
	@Override
	public RecipeType<DistillationTowerRecipe> getRecipeType(){
		return IPRecipeTypes.DISTILLATION.get();
	}
	
	@Method
	public void addRecipe(String name, Many<KnownTag<Fluid>> inputFluidTag, IFluidStack fluidsOutput, IItemStack byproduct, double byproductChance, int energy, int time){
		addRecipe(name, inputFluidTag, new IFluidStack[]{fluidsOutput}, new IItemStack[]{byproduct}, new double[]{byproductChance}, energy, time);
	}
	
	@Method
	public void addRecipe(String name, Many<KnownTag<Fluid>> inputFluidTag, IFluidStack[] fluidsOutput, IItemStack[] byproducts, double[] byproductChances, int energy, int time){
		name = fixRecipeName(name);
		
		ResourceLocation id = ResourceUtils.ct("distillation/" + name);
		
		FluidTagInput inputFluid = new FluidTagInput(inputFluidTag.getData().getTagKey(), inputFluidTag.getAmount());
		
		if(byproductChances.length != byproducts.length){
			throw new IllegalArgumentException("Byproducts and ByproductChances arrays must be equal in size.");
		}
		
		FluidStack[] outputFluids = new FluidStack[fluidsOutput.length];
		for(int i = 0;i < fluidsOutput.length;i++){
			outputFluids[i] = fluidsOutput[i].getInternal();
		}
		
		ItemStack[] outputItems = new ItemStack[byproducts.length];
		for(int i = 0;i < byproducts.length;i++){
			outputItems[i] = byproducts[i].getInternal();
		}
		
		DistillationTowerRecipe recipe = new DistillationTowerRecipe(id, outputFluids, outputItems, inputFluid, energy, time, byproductChances);
		
		// Does NOT work with this
		//CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe));
		
		// This however does, while it may not be the safest thing to do..
		DistillationTowerRecipe.recipes.put(id, recipe);
	}
}
