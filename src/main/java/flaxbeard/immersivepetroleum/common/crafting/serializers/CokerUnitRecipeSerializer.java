package flaxbeard.immersivepetroleum.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPMultiblockRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class CokerUnitRecipeSerializer extends IERecipeSerializer<CokerUnitRecipe>{
	
	//@formatter:off
	public static final DualMapCodec<RegistryFriendlyByteBuf, CokerUnitRecipe> CODECS = DualCompositeMapCodecs.composite(
		DualCodecs.ITEM_STACK.fieldOf("result"), CokerUnitRecipe::getOutputItem,
		IEDualCodecs.FLUID_STACK.fieldOf("resultfluid"), CokerUnitRecipe::getOutputFluid,
		IngredientWithSize.CODECS.fieldOf("input"), CokerUnitRecipe::getInputItem,
		IEDualCodecs.SIZED_FLUID_INGREDIENT.fieldOf("inputfluid"), CokerUnitRecipe::getInputFluid,
		DualCodecs.INT.fieldOf("energy"), IPMultiblockRecipe::getBaseEnergy,
		DualCodecs.INT.fieldOf("time"), IPMultiblockRecipe::getBaseTime,
		CokerUnitRecipe::new
	);
	//@formatter:on
	
	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, CokerUnitRecipe> codecs(){
		return CODECS;
	}
	
	@Override
	public ItemStack getIcon(){
		return IPContent.Multiblock.COKERUNIT.iconStack();
	}
}
