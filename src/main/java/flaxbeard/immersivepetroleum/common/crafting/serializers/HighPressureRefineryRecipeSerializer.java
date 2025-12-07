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
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Optional;

public class HighPressureRefineryRecipeSerializer extends IERecipeSerializer<HighPressureRefineryRecipe>{
	
	//@formatter:off
	public static final DualMapCodec<RegistryFriendlyByteBuf, HighPressureRefineryRecipe> CODEC = DualCompositeMapCodecs.composite(
		IEDualCodecs.FLUID_STACK.fieldOf("result"), HighPressureRefineryRecipe::getOutputFluid,
		StackWithChance.CODECS.optionalFieldOf("secondary_result"), HighPressureRefineryRecipeSerializer::itemResult,
		IEDualCodecs.SIZED_FLUID_INGREDIENT.fieldOf("input"), HighPressureRefineryRecipe::getPrimaryInputFluid,
		IEDualCodecs.SIZED_FLUID_INGREDIENT.optionalFieldOf("secondary_input"), HighPressureRefineryRecipeSerializer::secondaryInputFluid,
		DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
		DualCodecs.INT.fieldOf("time"), MultiblockRecipe::getBaseTime,
		HighPressureRefineryRecipe::new
	);
	//@formatter:on
	
	private static Optional<StackWithChance> itemResult(HighPressureRefineryRecipe r){
		if(r.getSecondaryItem() != null)
			return Optional.of(r.getSecondaryItem());
		
		return Optional.empty();
	}
	
	private static Optional<SizedFluidIngredient> secondaryInputFluid(HighPressureRefineryRecipe r){
		if(r.getSecondaryInputFluid() == null)
			return Optional.empty();
		
		return Optional.of(r.getSecondaryInputFluid());
	}
	
	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, HighPressureRefineryRecipe> codecs(){
		return CODEC;
	}
	
	@Override
	public ItemStack getIcon(){
		return IPContent.Multiblock.HYDROTREATER.iconStack();
	}
}
