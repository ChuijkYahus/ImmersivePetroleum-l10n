package flaxbeard.immersivepetroleum.common.world;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPRegisters;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.GAME)
public class WorldGenFeatures{
	public static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/WorldGen");
	
	public static final DeferredHolder<Feature<?>, FeatureReservoir> RESERVOIR_FEATURE = IPRegisters.registerFeature("reservoir", FeatureReservoir::new);
	
	private static Optional<PlacedFeature> reservoirFeature = Optional.empty();
	private static final Map<ResourceKey<Level>, List<ChunkPos>> regenChunks = new HashMap<>();
	
	public static void configChanged(){
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null)
			return;
		
		Registry<PlacedFeature> reg = server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
		for(PlacedFeature f: reg){
			if(f.feature().value().feature() instanceof FeatureReservoir && IPServerConfig.WORLDGEN.generateMissingReservoirs.get()){
				reservoirFeature = Optional.of(f);
				log.debug("Prepared for Regen.");
			}
		}
	}
	
	@SubscribeEvent
	public static void chunkDataSave(ChunkDataEvent.Save event){
		// TODO Could be of some use later.
	}
	
	@SubscribeEvent
	public static void chunkDataLoad(ChunkDataEvent.Load event){
		if(reservoirFeature.isEmpty())
			return;
		else if(!IPServerConfig.WORLDGEN.generateMissingReservoirs.get() && reservoirFeature.isPresent()){
			reservoirFeature = Optional.empty();
			synchronized(regenChunks){
				regenChunks.clear();
			}
			return;
		}
		
		if(!(event.getLevel() instanceof Level level) || event.getChunk().getPersistedStatus() != ChunkStatus.FULL)
			return;
		
		ResourceKey<Level> dim = level.dimension();
		synchronized(regenChunks){
			ChunkPos chunkPos = event.getChunk().getPos();
			regenChunks.computeIfAbsent(dim, d -> new ArrayList<>()).add(chunkPos);
			log.debug("Added Chunk({}, {}) to Regen-Queue.", chunkPos.x, chunkPos.z);
		}
	}
	
	@SubscribeEvent
	public static void serverLevelTick(LevelTickEvent.Post event){
		if(!(event.getLevel() instanceof ServerLevel level) || reservoirFeature.isEmpty())
			return;
		
		ResourceKey<Level> dimension = level.dimension();
		synchronized(regenChunks){
			final List<ChunkPos> chunks = regenChunks.get(dimension);
			if(chunks == null || chunks.isEmpty())
				return;
			
			// Process only a few per tick to keep lag to a minimum.
			final int maxRegensPerTick = 16;
			int completed = 0, skipped = 0;
			for(int i = 0;i < maxRegensPerTick && i < chunks.size();i++){
				if(chunks.isEmpty())
					break;
				
				ChunkPos cPos = chunks.getFirst();
				if(level.hasChunk(cPos.x, cPos.z)){
					long seed = level.getSeed();
					RandomSource rand = RandomSource.create(seed);
					long xSeed = rand.nextLong() >> 3;
					long zSeed = rand.nextLong() >> 3;
					rand.setSeed(xSeed * cPos.x + zSeed * cPos.z ^ seed);
					
					if(reservoirFeature.isPresent()){
						if(reservoirFeature.get().place(level, level.getChunkSource().getGenerator(), rand, new BlockPos(16 * cPos.x, 0, 16 * cPos.z))){
							log.debug("Chunk({}, {}) has its reservoir regenerated.", cPos.x, cPos.z);
							completed++;
						}else{
							log.debug("Chunk({}, {}) may already have one or more reservoirs.", cPos.x, cPos.z);
							skipped++;
						}
					}
				}
				chunks.removeFirst();
			}
			
			if(completed > 0 || skipped > 0)
				log.debug("Regen completed {} and skipped {} chunks.", completed, skipped);
		}
	}
	
	public static void forceClassLoad(){
	}
}
