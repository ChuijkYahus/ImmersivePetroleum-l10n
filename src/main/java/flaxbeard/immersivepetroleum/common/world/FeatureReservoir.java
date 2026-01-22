package flaxbeard.immersivepetroleum.common.world;

import com.google.common.collect.HashMultimap;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirPolygon;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class FeatureReservoir extends Feature<NoneFeatureConfiguration>{
	public static HashMultimap<ResourceKey<Level>, ChunkPos> generatedReservoirChunks = HashMultimap.create();
	
	public FeatureReservoir(){
		super(NoneFeatureConfiguration.CODEC);
	}
	
	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext){
		WorldGenLevel reader = pContext.level();
		BlockPos pos = pContext.origin();
		ReservoirHandler.initGenerator(reader);
		
		ServerLevel level = reader.getLevel();
		ChunkPos chunkPos = reader.getChunk(pos).getPos();
		
		ResourceKey<Level> dimension = level.dimension();
		if(generatedReservoirChunks.containsEntry(dimension, chunkPos))
			return false;
		
		scanChunkForNewReservoirs(level, chunkPos, pContext.random());
		generatedReservoirChunks.put(dimension, chunkPos);
		
		return true;
	}
	
	public static void scanChunkForNewReservoirs(ServerLevel world, ChunkPos chunkPos, RandomSource randomSource){
		int chunkX = chunkPos.getMinBlockX();
		int chunkZ = chunkPos.getMinBlockZ();
		
		ResourceKey<Level> dimension = world.dimension();
		
		final ReservoirRegionDataStorage storage = ReservoirRegionDataStorage.get();
		
		for(int j = 0;j < 16;j++){
			for(int i = 0;i < 16;i++){
				int x = chunkX + i;
				int z = chunkZ + j;
				
				// This is visually more pleasing to me
				if(ReservoirHandler.getValueOf(world, x, z) <= -1)
					continue;
				
				final ColumnPos current = new ColumnPos(x, z);
				if(storage.existsAt(current))
					return;
				
				// Getting the biome now to prevent lockups
				Holder<Biome> biome = world.getBiome(new BlockPos(x, 64, z));
				
				RecipeHolder<ReservoirType> type = null;
				int totalWeight = ReservoirHandler.getTotalWeight(dimension, biome);
				if(totalWeight > 0){
					int weight = Math.abs(randomSource.nextInt() % totalWeight);
					for(RecipeHolder<ReservoirType> holder:ReservoirType.map.values()){
						ReservoirType res = holder.value();
						
						if(res.getDimensions().isValid(dimension) && res.getBiomes().isValid(biome)){
							weight -= res.weight;
							if(weight < 0){
								type = holder;
								break;
							}
						}
					}
					
					if(type != null){
						ReservoirPolygon reservoirPolygon = ReservoirPolygon.make(world, new ColumnPos(x, z));
						
						if(!reservoirPolygon.isEmpty()){
							int amount = (int) Mth.lerp(randomSource.nextFloat(), type.value().minSize, type.value().maxSize);
							
							Reservoir reservoir = new Reservoir(reservoirPolygon, type, amount);
							storage.addReservoir(dimension, reservoir);
						}
					}
				}
			}
		}
	}
}
