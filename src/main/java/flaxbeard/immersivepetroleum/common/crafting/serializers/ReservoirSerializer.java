package flaxbeard.immersivepetroleum.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListBiome;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListDimension;
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
		
		BWListDimension.CODECS.optionalFieldOf("dimensions"),	r -> Optional.of(r.getDimensions()),
		BWListBiome.CODECS.optionalFieldOf("biomes"),		r -> Optional.of(r.getBiomes()),
		
		(name, f, min, max, trace, equilibrium, weight, dimensions, biomes) -> {
			Fluid fluid = RegistryUtils.getFluidFromRegistryName(f);
			
			if(fluid == null)
				throw new RuntimeException(f + " is an invalid/unknown fluid");
			
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
}
