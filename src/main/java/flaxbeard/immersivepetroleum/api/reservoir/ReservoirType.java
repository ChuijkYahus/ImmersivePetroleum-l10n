package flaxbeard.immersivepetroleum.api.reservoir;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.crafting.Serializers;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListBiome;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListDimension;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReservoirType extends IESerializableRecipe{
	static final Lazy<ItemStack> EMPTY_LAZY = Lazy.of(() -> ItemStack.EMPTY);
	static final TagOutput EMPTY = new TagOutput(ItemStack.EMPTY);
	
	public static Map<ResourceLocation, RecipeHolder<ReservoirType>> map = new HashMap<>();
	
	public final String name;
	public final ResourceLocation fluidLocation;
	public final int weight;
	
	public final int minSize;
	public final int maxSize;
	public final int residual;
	public final int equilibrium;
	
	private final Fluid fluid;
	
	private BWListBiome biomes = new BWListBiome(false);
	private BWListDimension dimensions = new BWListDimension(false);
	
	/**
	 * Creates a new reservoir.
	 *
	 * @param name          The name of this reservoir type
	 * @param fluidLocation The registry name of the fluid this reservoir is containing
	 * @param minSize       Minimum amount of fluid in this reservoir
	 * @param maxSize       Maximum amount of fluid in this reservoir
	 * @param residual      Leftover fluid amount after depletion
	 * @param equilibrium   Maximum amount of fluid that residuals regenerate at
	 * @param weight        The weight for this reservoir
	 */
	public ReservoirType(String name, ResourceLocation fluidLocation, int minSize, int maxSize, int residual, int equilibrium, int weight){
		this(name, BuiltInRegistries.FLUID.get(fluidLocation), minSize, maxSize, residual, equilibrium, weight);
	}
	
	/**
	 * Creates a new reservoir.
	 * 
	 * @param name     The name of this reservoir type
	 * @param fluid    The fluid this reservoir is containing
	 * @param minSize  Minimum amount of fluid in this reservoir
	 * @param maxSize  Maximum amount of fluid in this reservoir
	 * @param residual      Leftover fluid amount after depletion
	 * @param equilibrium   Maximum amount of fluid that residuals regenerate at
	 * @param weight   The weight for this reservoir
	 */
	public ReservoirType(String name, Fluid fluid, int minSize, int maxSize, int residual, int equilibrium, int weight){
		super(EMPTY, IPRecipeTypes.RESERVOIR);
		this.name = name;
		this.fluidLocation = RegistryUtils.getRegistryNameOf(fluid);
		this.fluid = fluid;
		this.residual = residual;
		this.equilibrium = equilibrium;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.weight = weight;
	}
	
	public ReservoirType(CompoundTag nbt){
		super(EMPTY, IPRecipeTypes.RESERVOIR);
		
		this.name = nbt.getString("name");
		
		this.fluidLocation = ResourceLocation.parse(nbt.getString("fluid"));
		this.fluid = BuiltInRegistries.FLUID.get(this.fluidLocation);
		
		this.minSize = nbt.getInt("minSize");
		this.maxSize = nbt.getInt("maxSize");
		this.residual = nbt.getInt("residual");
		this.equilibrium = nbt.getInt("equilibrium");
		
		this.biomes.readFromNbt(nbt.getCompound("biomes"));
		this.dimensions.readFromNbt(nbt.getCompound("dimensions"));
		
		this.weight = nbt.getInt("weight");
	}
	
	@Override
	protected IERecipeSerializer<ReservoirType> getIESerializer(){
		return Serializers.RESERVOIR_SERIALIZER.get();
	}
	
	public CompoundTag writeToNBT(){
		return writeToNBT(new CompoundTag());
	}
	
	public CompoundTag writeToNBT(CompoundTag nbt){
		nbt.putString("name", this.name);
		nbt.putString("id", this.type.toString());
		nbt.putString("fluid", this.fluidLocation.toString());
		
		nbt.putInt("minSize", this.minSize);
		nbt.putInt("maxSize", this.maxSize);
		nbt.putInt("residual", this.residual);
		nbt.putInt("equilibrium", this.equilibrium);
		
		nbt.put("biomes", this.biomes.writeToNbt());
		nbt.put("dimensions", this.dimensions.writeToNbt());
		
		nbt.putInt("weight", this.weight);
		
		return nbt;
	}
	
	public void setBiomes(@Nonnull BWListBiome list){
		Objects.requireNonNull(list);
		
		this.biomes = list;
	}
	
	public void setDimensions(@Nonnull BWListDimension list){
		Objects.requireNonNull(list);
		
		this.dimensions = list;
	}
	
	public BWListDimension getDimensions(){
		return this.dimensions;
	}
	
	public BWListBiome getBiomes(){
		return this.biomes;
	}
	
	@Override
	@Nonnull
	public ItemStack getResultItem(@Nonnull HolderLookup.Provider provider){
		return ItemStack.EMPTY;
	}
	
	public Fluid getFluid(){
		return this.fluid;
	}
	
	@Override
	public String toString(){
		return this.writeToNBT().toString();
	}
}
