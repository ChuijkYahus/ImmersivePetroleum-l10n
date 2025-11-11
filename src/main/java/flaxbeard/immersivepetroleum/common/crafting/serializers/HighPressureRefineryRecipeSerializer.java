package flaxbeard.immersivepetroleum.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class HighPressureRefineryRecipeSerializer extends IERecipeSerializer<HighPressureRefineryRecipe>{
	
	//@formatter:off
	public static final DualMapCodec<RegistryFriendlyByteBuf, HighPressureRefineryRecipe> CODEC = DualCompositeMapCodecs.composite(
		IEDualCodecs.FLUID_STACK.fieldOf("result"), r -> r.output,
		StackWithChance.CODECS.optionalFieldOf("secondary_result"), r -> {
			if(r.outputItem == null)
				return Optional.empty();
			return Optional.of(r.outputItem);
		},
		IEDualCodecs.SIZED_FLUID_INGREDIENT.fieldOf("input"), r -> r.inputFluid,
		IEDualCodecs.SIZED_FLUID_INGREDIENT.optionalFieldOf("secondary_input"), r -> {
			if(r.inputFluid == null)
				return Optional.empty();
			return Optional.of(r.inputFluid);
		},
		DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
		DualCodecs.INT.fieldOf("time"), MultiblockRecipe::getBaseTime,
		(output, outputItem, inputFluid, inputFluidSecondary, energy, time)->{
			return new HighPressureRefineryRecipe(output, outputItem.orElse(null), inputFluid, inputFluidSecondary.orElse(null), energy, time);
		}
	);
	//@formatter:on
	
	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, HighPressureRefineryRecipe> codecs(){
		return CODEC;
	}
	
	@Override
	public ItemStack getIcon(){
		return IPContent.Multiblock.HYDROTREATER.iconStack();
	}
}
