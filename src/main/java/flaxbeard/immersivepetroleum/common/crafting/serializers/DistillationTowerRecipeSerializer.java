package flaxbeard.immersivepetroleum.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DistillationTowerRecipeSerializer extends IERecipeSerializer<DistillationTowerRecipe>{
	
	//@formatter:off
	public static final DualMapCodec<RegistryFriendlyByteBuf, DistillationTowerRecipe> CODEC = DualCompositeMapCodecs.composite(
		IEDualCodecs.FLUID_STACK.listOf().fieldOf("results"), MultiblockRecipe::getFluidOutputs,
		CHANCE_LIST_CODECS.optionalFieldOf("byproducts"), r -> Optional.of(r.getItemOutput()),
		IEDualCodecs.SIZED_FLUID_INGREDIENT.fieldOf("input"), DistillationTowerRecipe::getInputFluid,
		DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
		DualCodecs.INT.fieldOf("time"), MultiblockRecipe::getBaseTime,
		DistillationTowerRecipe::new
	);
	//@formatter:on
	
	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, DistillationTowerRecipe> codecs(){
		return CODEC;
	}
	
	@Override
	public ItemStack getIcon(){
		return IPContent.Multiblock.DISTILLATIONTOWER.iconStack();
	}
}
