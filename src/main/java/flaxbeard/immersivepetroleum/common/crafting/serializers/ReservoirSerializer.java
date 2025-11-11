package flaxbeard.immersivepetroleum.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;

public class ReservoirSerializer extends IERecipeSerializer<ReservoirType>{
	
	//@formatter:off
	public static final DualMapCodec<RegistryFriendlyByteBuf, ReservoirType> CODEC = DualCompositeMapCodecs.composite(
		DualCodecs.STRING.fieldOf("name"),				r -> r.name,
		DualCodecs.RESOURCE_LOCATION.fieldOf("fluid"),	r -> r.fluidLocation,
		DualCodecs.INT.fieldOf("fluidminimum"),			r -> r.minSize,
		DualCodecs.INT.fieldOf("fluidcapacity"),		r -> r.maxSize,
		DualCodecs.INT.fieldOf("fluidtrace"),			r -> r.residual,
		DualCodecs.INT.fieldOf("equilibrium"),			r -> r.equilibrium,
		DualCodecs.INT.fieldOf("weight"),				r -> r.weight,
		
		ReservoirType.BWList.CODECS.optionalFieldOf("dimensions"),	r -> Optional.of(r.getDimensions()),
		ReservoirType.BWList.CODECS.optionalFieldOf("biomes"),		r -> Optional.of(r.getBiomes()),
		
		(name, f, min, max, trace, equilibrium, weight, dimensions, biomes) -> {
			Fluid fluid = RegistryUtils.getFluidFromRegistryName(f);
			
			if(fluid == null)
				throw new RuntimeException(f+" is an invalid/unknown fluid");
			
			ReservoirType type = new ReservoirType(name, fluid, min, max, trace, equilibrium, weight);
			
			dimensions.ifPresent(type::setDimensions);
			biomes.ifPresent(type::setBiomes);
			
			return type;
		}
	);
	//@formatter:on
	
	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, ReservoirType> codecs(){
		return CODEC;
	}
	
	@Override
	public ItemStack getIcon(){
		return ItemStack.EMPTY;
	}
	
	/*
	public ReservoirType readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context){
		String name = GsonHelper.getAsString(json, "name");
		ResourceLocation fluid = ResourceLocation.parse(GsonHelper.getAsString(json, "fluid"));
		int min = GsonHelper.getAsInt(json, "fluidminimum");
		int max = GsonHelper.getAsInt(json, "fluidcapacity");
		int trace = GsonHelper.getAsInt(json, "fluidtrace");
		int equilibrium = GsonHelper.getAsInt(json, "equilibrium", 0);
		int weight = GsonHelper.getAsInt(json, "weight");
		
		ReservoirType reservoir = new ReservoirType(name, recipeId, fluid, min, max, trace, equilibrium, weight);
		
		ImmersivePetroleum.log.debug("Loaded reservoir {} as {}, with {}mB to {}mB of {} and {}mB trace at {}mB equilibrium, with {} of weight.",
				recipeId, name, min, max, fluid, trace, equilibrium, weight);
		
		if(GsonHelper.isValidNode(json, "dimensions")){
			JsonObject dimensions = GsonHelper.getAsJsonObject(json, "dimensions");
			
			boolean isBlacklist = GsonHelper.getAsBoolean(dimensions, "isBlacklist");
			
			if(GsonHelper.isValidNode(dimensions, "list")){
				JsonArray array = GsonHelper.getAsJsonArray(dimensions, "list");
				
				List<ResourceLocation> list = new ArrayList<>();
				array.forEach(rl -> list.add(ResourceLocation.parse(rl.getAsString())));
				reservoir.setDimensions(isBlacklist, list);
			}
		}
		
		if(GsonHelper.isValidNode(json, "biomes")){
			JsonObject biomes = GsonHelper.getAsJsonObject(json, "biomes");
			
			boolean isBlacklist = GsonHelper.getAsBoolean(biomes, "isBlacklist");
			
			if(GsonHelper.isValidNode(biomes, "list")){
				JsonArray array = GsonHelper.getAsJsonArray(biomes, "list");
				
				List<ResourceLocation> list = new ArrayList<>();
				array.forEach(rl -> list.add(ResourceLocation.parse(rl.getAsString())));
				reservoir.setBiomes(isBlacklist, list);
			}
		}
		
		return reservoir;
	}
	
	public ReservoirType fromNetwork(@Nonnull ResourceLocation recipeId, FriendlyByteBuf buffer){
		return new ReservoirType(buffer.readNbt()); // Very convenient having the NBT stuff already.
	}
	
	public void toNetwork(FriendlyByteBuf buffer, ReservoirType recipe){
		buffer.writeNbt(recipe.writeToNBT());
	}
	*/
}
