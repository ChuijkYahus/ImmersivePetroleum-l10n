package flaxbeard.immersivepetroleum.common.util.compat.jei;

import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.client.gui.machines.IPContainerScreen_CokerUnit;
import flaxbeard.immersivepetroleum.client.gui.machines.IPContainerScreen_DistillationTower;
import flaxbeard.immersivepetroleum.client.gui.machines.IPContainerScreen_Hydrotreater;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class JEIStuff implements IModPlugin{
	private static final ResourceLocation ID = ResourceUtils.ip("main");
	
	private RecipeType<DistillationTowerRecipe> distillation_type;
	private RecipeType<CokerUnitRecipe> coker_type;
	private RecipeType<HighPressureRefineryRecipe> recovery_type;
	
	protected static IDrawableStatic tankOverlay;
	
	@Override
	@Nonnull
	public ResourceLocation getPluginUid(){
		return ID;
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration){
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		
		tankOverlay = guiHelper.drawableBuilder(ResourceUtils.ip("textures/gui/sprites/parts.png"), 20, 0, 20, 51)
			.setTextureSize(128, 128)
			.build();
		
		DistillationRecipeCategory distillation = new DistillationRecipeCategory(guiHelper);
		CokerUnitRecipeCategory coker = new CokerUnitRecipeCategory(guiHelper);
		HighPressureRefineryRecipeCategory recovery = new HighPressureRefineryRecipeCategory(guiHelper);
		
		this.distillation_type = distillation.getRecipeType();
		this.coker_type = coker.getRecipeType();
		this.recovery_type = recovery.getRecipeType();
		
		registration.addRecipeCategories(distillation, coker, recovery);
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration){
		registration.addRecipes(this.distillation_type, listOf(DistillationTowerRecipe.recipes));
		registration.addRecipes(this.coker_type, listOf(CokerUnitRecipe.recipes));
		registration.addRecipes(this.recovery_type, listOf(HighPressureRefineryRecipe.recipes));
	}
	
	private <T extends Recipe<?>> List<T> listOf(Map<ResourceLocation, RecipeHolder<T>> map){
		return map.values().stream().map(RecipeHolder::value).toList();
	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration){
		registration.addRecipeCatalyst(new ItemStack(IPContent.Multiblock.DISTILLATIONTOWER.block().get()), this.distillation_type);
		registration.addRecipeCatalyst(new ItemStack(IPContent.Multiblock.COKERUNIT.block().get()), this.coker_type);
		registration.addRecipeCatalyst(new ItemStack(IPContent.Multiblock.HYDROTREATER.block().get()), this.recovery_type);
	}
	
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration){
		registration.addRecipeClickArea(IPContainerScreen_DistillationTower.class, 85, 19, 18, 51, this.distillation_type);
		
		//Have to use four of these so that they don't overlap
		registration.addRecipeClickArea(IPContainerScreen_CokerUnit.class, 59, 21, 15, 67, this.coker_type);
		registration.addRecipeClickArea(IPContainerScreen_CokerUnit.class, 64, 63, 73, 25, this.coker_type);
		registration.addRecipeClickArea(IPContainerScreen_CokerUnit.class, 127, 21, 15, 67, this.coker_type);
		registration.addRecipeClickArea(IPContainerScreen_CokerUnit.class, 81, 21, 39, 42, this.coker_type);
		
		registration.addRecipeClickArea(IPContainerScreen_Hydrotreater.class, 55, 9, 32, 51, this.recovery_type);
	}
}
