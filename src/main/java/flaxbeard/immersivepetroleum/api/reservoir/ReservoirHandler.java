package flaxbeard.immersivepetroleum.api.reservoir;

import com.google.common.collect.ImmutableList;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * This takes care of dealing with generating, storing and caching (Faster access for regulary queried positions) reservoirs.
 *
 * @author TwistedGate
 */
public class ReservoirHandler{
	private static final Map<Pair<ResourceKey<Level>, ColumnPos>, Reservoir> CACHE = new HashMap<>();
	
	private static final Map<ResourceLocation, Map<ResourceLocation, Integer>> totalWeightMap = new HashMap<>();
	
	/**
	 * Gets the total weight of reservoir types for the given dimension ID and biome type
	 *
	 * @param dimension The dimension to check
	 * @param biome     The biome to check
	 * @return The total weight associated with the dimension/biome pair
	 */
	public static int getTotalWeight(ResourceKey<Level> dimension, Holder<Biome> biome){
		final ResourceLocation dimensionRL = dimension.location();
		final ResourceLocation biomeRL = biome.getKey().location();
		
		Map<ResourceLocation, Integer> map = totalWeightMap.computeIfAbsent(dimensionRL, k -> new HashMap<>());
		
		return map.computeIfAbsent(biomeRL, r -> {
			int totalWeight = 0;
			for(RecipeHolder<ReservoirType> holder: ReservoirType.map.values()){
				ReservoirType reservoir = holder.value();
				
				if(reservoir.getDimensions().isValid(dimension) && reservoir.getBiomes().isValid(biome)){
					totalWeight += reservoir.weight;
				}
			}
			return totalWeight;
		});
	}
	
	/** May only be called on the server-side. Returns null on client-side. */
	public static Reservoir getReservoir(Level world, BlockPos pos){
		return getReservoir(world, Utils.toColumnPos(pos));
	}
	
	/** May only be called on the server-side. Returns null on client-side. */
	public static Reservoir getReservoir(Level world, ColumnPos pos){
		if(world.isClientSide)
			return null;
		
		ResourceKey<Level> dimension = world.dimension();
		Pair<ResourceKey<Level>, ColumnPos> cacheKey = Pair.of(dimension, pos);
		synchronized(CACHE){
			Reservoir ret = CACHE.get(cacheKey);
			
			if(ret == null){
				Reservoir reservoir = ReservoirRegionDataStorage.get().getReservoir(world, pos);
				CACHE.put(cacheKey, reservoir);
				return reservoir;
			}
			
			return ret;
		}
	}
	
	/** <i>This should not be called too much.</i> May only be called on the server-side, returns null on client-side. */
	public static Reservoir getReservoirNoCache(Level world, BlockPos pos){
		return getReservoirNoCache(world, Utils.toColumnPos(pos));
	}
	
	/** <i>This should not be called too much.</i> May only be called on the server-side, returns null on client-side. */
	public static Reservoir getReservoirNoCache(Level world, ColumnPos pos){
		if(world.isClientSide)
			return null;
		
		return ReservoirRegionDataStorage.get().getReservoir(world, pos);
	}
	
	/**
	 * Adds a reservoir type to the pool of valid reservoirs
	 *
	 * @param id        The "recipeId" of the reservoir type
	 * @param reservoir The {@link ReservoirType} type to add
	 * @return The {@link ReservoirType} passed in
	 */
	public static RecipeHolder<ReservoirType> addReservoir(ResourceLocation id, RecipeHolder<ReservoirType> reservoir){
		ReservoirType.map.put(id, reservoir);
		return reservoir;
	}
	
	private static long lastSeed;
	private static PerlinSimplexNoise generator;
	
	static final double scale = 0.015625D;
	static final double d0 = 2 / 3D;
	static final double d1 = 1 / 3D;
	
	/**
	 * <i>Only call on server side!</i>
	 *
	 * @param level {@link Level} to run query on
	 * @param x     Block Position
	 * @param z     Block Position
	 * @return -1 (Nothing/Empty), >=0.0 means there's <i>something</i>
	 */
	public static double getValueOf(@Nonnull Level level, int x, int z){
		if(!level.isClientSide && level instanceof WorldGenLevel worldGen)
			initGenerator(worldGen);
		
		double noise = Math.abs(generator.getValue(x * scale, z * scale, false));
		if(noise > d0)
			return (noise - d0) / d1;
		
		return -1D;
	}
	
	public static void initGenerator(WorldGenLevel world){
		if(generator == null || world.getSeed() != lastSeed){
			lastSeed = world.getSeed();
			generator = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(lastSeed)), ImmutableList.of(0));
		}
	}
	
	public static PerlinSimplexNoise getGenerator(){
		return generator;
	}
	
	public static void clearCache(){
		synchronized(CACHE){
			CACHE.clear();
		}
	}
	
	public static void recalculateChances(){
		totalWeightMap.clear();
	}
}
