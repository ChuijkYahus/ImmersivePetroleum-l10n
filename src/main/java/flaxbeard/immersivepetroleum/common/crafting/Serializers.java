package flaxbeard.immersivepetroleum.common.crafting;

import flaxbeard.immersivepetroleum.common.IPRegisters;
import flaxbeard.immersivepetroleum.common.crafting.serializers.CokerUnitRecipeSerializer;
import flaxbeard.immersivepetroleum.common.crafting.serializers.DistillationTowerRecipeSerializer;
import flaxbeard.immersivepetroleum.common.crafting.serializers.HighPressureRefineryRecipeSerializer;
import flaxbeard.immersivepetroleum.common.crafting.serializers.ReservoirSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class Serializers{
	public static final DeferredHolder<RecipeSerializer<?>, DistillationTowerRecipeSerializer> DISTILLATION_SERIALIZER = IPRegisters.registerSerializer(
			"distillation", DistillationTowerRecipeSerializer::new
	);
	
	public static final DeferredHolder<RecipeSerializer<?>, CokerUnitRecipeSerializer> COKER_SERIALIZER = IPRegisters.registerSerializer(
			"coker", CokerUnitRecipeSerializer::new
	);
	
	public static final DeferredHolder<RecipeSerializer<?>, HighPressureRefineryRecipeSerializer> HYDROTREATER_SERIALIZER = IPRegisters.registerSerializer(
			"hydrotreater", HighPressureRefineryRecipeSerializer::new
	);
	
	public static final DeferredHolder<RecipeSerializer<?>, ReservoirSerializer> RESERVOIR_SERIALIZER = IPRegisters.registerSerializer(
			"reservoirs", ReservoirSerializer::new
	);
	
	public static void forceClassLoad(){
	}
}
