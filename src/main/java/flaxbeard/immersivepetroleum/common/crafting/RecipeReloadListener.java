package flaxbeard.immersivepetroleum.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.client.render.dyn.DynamicTextureWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class RecipeReloadListener implements ResourceManagerReloadListener{
	private final ReloadableServerResources dataPackRegistries;
	public RecipeReloadListener(ReloadableServerResources dataPackRegistries){
		this.dataPackRegistries = dataPackRegistries;
	}
	
	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager){
		if(dataPackRegistries != null){
			lists(dataPackRegistries.getRecipeManager());
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void recipesUpdated(RecipesUpdatedEvent event){
		if(!Minecraft.getInstance().hasSingleplayerServer()){
			lists(event.getRecipeManager());
		}
		
		DynamicTextureWrapper.clearCache();
	}
	
	static void lists(RecipeManager recipeManager){
		Collection<RecipeHolder<?>> recipes = recipeManager.getRecipes();
		
		if(recipes.isEmpty())
			return;
		
		ImmersivePetroleum.log.info("Loading Reservoirs.");
		ReservoirType.map = filterRecipes(recipes, ReservoirType.class, IPRecipeTypes.RESERVOIR);
		
		ImmersivePetroleum.log.info("Loading Distillation Recipes.");
		DistillationTowerRecipe.recipes = filterRecipes(recipes, DistillationTowerRecipe.class, IPRecipeTypes.DISTILLATION);
		
		ImmersivePetroleum.log.info("Loading Coker Recipes.");
		CokerUnitRecipe.recipes = filterRecipes(recipes, CokerUnitRecipe.class, IPRecipeTypes.COKER);
		
		ImmersivePetroleum.log.info("Loading High-Pressure Refinery Recipes.");
		HighPressureRefineryRecipe.recipes = filterRecipes(recipes, HighPressureRefineryRecipe.class, IPRecipeTypes.HYDROTREATER);
	}
	
	@SuppressWarnings("unchecked")
	static <R extends Recipe<?>, H extends RecipeHolder<R>> Map<ResourceLocation, H> filterRecipes(Collection<RecipeHolder<?>> recipes, Class<R> recipeClass, IERecipeTypes.TypeWithClass<R> recipeType){
		//@formatter:off
		return (Map<ResourceLocation, H>) recipes.stream()
			.filter(holder -> holder.value().getType() == recipeType.get())
			.map(holder -> new Test(holder.id(), holder))
			.collect(Collectors.toMap(Test::id, Test::holder));
		//@formatter:on
	}
	
	private record Test(ResourceLocation id, RecipeHolder<?> holder){
	}
}
