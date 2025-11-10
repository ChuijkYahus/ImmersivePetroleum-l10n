package flaxbeard.immersivepetroleum.common.crafting.serializers;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutputList;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import cpw.mods.util.Lazy;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.IPContent;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class CokerUnitRecipeSerializer extends IERecipeSerializer<CokerUnitRecipe>{
	
	public static final DualMapCodec<RegistryFriendlyByteBuf, CokerUnitRecipe> CODEC = DualCompositeMapCodecs.composite(
		TagOutputList.CODEC.fieldOf("results"), r -> r.outputItem,
		IEDualCodecs.FLUID_STACK.fieldOf("resultfluid"), r -> r.outputFluid,
		IngredientWithSize.CODEC.fieldOf("input"), r -> r.inputItem,
		CokerUnitRecipe::new
	);
	
	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, CokerUnitRecipe> codecs(){
		return CODEC;
	}
	
	@Override
	public ItemStack getIcon(){
		return new ItemStack(IPContent.Multiblock.COKERUNIT.iconStack().getItem());
	}
	
	public CokerUnitRecipe readFromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context){
		FluidStack outputFluid = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "resultfluid"));
		FluidTagInput inputFluid = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "inputfluid"));
		
		Lazy<ItemStack> outputItem = readOutput(json.get("result"));
		IngredientWithSize inputItem = IngredientWithSize.deserialize(GsonHelper.getAsJsonObject(json, "input"));
		
		int energy = GsonHelper.getAsInt(json, "energy");
		int time = GsonHelper.getAsInt(json, "time");
		
		return new CokerUnitRecipe(recipeId, outputItem, outputFluid, inputItem, inputFluid, energy, time);
	}
	
	public CokerUnitRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer){
		IngredientWithSize inputItem = IngredientWithSize.read(buffer);
		ItemStack outputItem = buffer.readItem();
		
		FluidTagInput inputFluid = FluidTagInput.read(buffer);
		FluidStack outputFluid = FluidStack.readFromPacket(buffer);
		
		int energy = buffer.readInt();
		int time = buffer.readInt();
		
		return new CokerUnitRecipe(recipeId, Lazy.of(() -> outputItem), outputFluid, inputItem, inputFluid, energy, time);
	}
	
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, CokerUnitRecipe recipe){
		recipe.inputItem.write(buffer);
		buffer.writeItem(recipe.outputItem.copy());
		
		recipe.inputFluid.write(buffer);
		recipe.outputFluid.writeToPacket(buffer);
		
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.getTotalProcessTime());
	}
}
