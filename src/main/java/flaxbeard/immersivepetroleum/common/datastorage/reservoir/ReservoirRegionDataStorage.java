package flaxbeard.immersivepetroleum.common.datastorage.reservoir;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for {@link RegionData}s
 * 
 * @author TwistedGate
 */
public class ReservoirRegionDataStorage extends SavedData{
	protected static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/ReservoirRegionDataStorage");
	
	public static final int VERSION = 2;
	
	public static final String DATA_NAME = "ImmersivePetroleum-ReservoirRegions";
	
	private static ReservoirRegionDataStorage active_instance;
	public static ReservoirRegionDataStorage get(){
		return active_instance;
	}
	
	public static void init(final DimensionDataStorage dimData){
		active_instance = dimData.computeIfAbsent(new Factory<>(() -> {
			log.debug("Creating ReservoirRegionDataStorage instance.");
			return new ReservoirRegionDataStorage(dimData);
		}, (t, p) -> {
			log.debug("Creating and Loading Data for ReservoirRegionDataStorage instance.");
			return new ReservoirRegionDataStorage(dimData, t, p);
		}), DATA_NAME);
	}
	
	// -----------------------------------------------------------------------------
	
	/** Contains existing reservoir-region files */
	final Map<RegionPos, RegionData> regions = new HashMap<>();
	final DimensionDataStorage dimData;
	public ReservoirRegionDataStorage(DimensionDataStorage dimData){
		this.dimData = dimData;
	}
	
	public ReservoirRegionDataStorage(DimensionDataStorage dimData, CompoundTag nbt, HolderLookup.Provider provider){
		this(dimData);
		load(nbt, provider);
	}
	
	@Nonnull
	@Override
	public CompoundTag save(CompoundTag nbt, @Nonnull HolderLookup.Provider provider){
		nbt.putInt("version", VERSION);
		
		ListTag list = new ListTag();
		this.regions.forEach((key, entry) -> {
			CompoundTag tag = new CompoundTag();
			tag.putInt("x", key.x());
			tag.putInt("z", key.z());
			list.add(tag);
		});
		nbt.put("regions", list);
		
		log.debug("Saved regions file.");
		return nbt;
	}
	
	private void load(CompoundTag nbt, HolderLookup.Provider provider){
		int version = nbt.getInt("version");
		if(version != VERSION){
			// TODO Trigger Full Storage Update?
		}
		
		ListTag regions = nbt.getList("regions", Tag.TAG_COMPOUND);
		for(int i = 0;i < regions.size();i++){
			CompoundTag tag = regions.getCompound(i);
			int x = tag.getInt("x");
			int z = tag.getInt("z");
			
			RegionPos rPos = new RegionPos(x, z);
			RegionData rData = getOrCreateRegionData(rPos);
			this.regions.put(rPos, rData);
		}
		
		log.debug("Loaded regions file.");
	}
	
	/** Marks itself and all regions as dirty. (Only to be used by {@link CommonEventHandler#onUnload(LevelEvent.Unload)}) */
	public void markAllDirty(){
		setDirty();
		this.regions.values().forEach(RegionData::setDirty);
	}
	
	public void addReservoir(ResourceKey<Level> dimensionKey, Reservoir reservoir){
		RegionPos regionPos = new RegionPos(reservoir.getBoundingBox().getCenter());
		
		RegionData regionData = getOrCreateRegionData(regionPos);
		synchronized(regionData.reservoirlist){
			if(!regionData.reservoirlist.containsEntry(dimensionKey, reservoir)){
				regionData.reservoirlist.put(dimensionKey, reservoir);
				reservoir.setRegion(regionData);
				regionData.setDirty();
			}
		}
	}
	
	/** May only be called on the server-side. Returns null on client-side. */
	@Nullable
	public Reservoir getReservoir(Level level, BlockPos pos){
		return getReservoir(level, Utils.toColumnPos(pos));
	}
	
	/** May only be called on the server-side. Returns null on client-side. */
	@Nullable
	public Reservoir getReservoir(Level level, ColumnPos pos){
		if(level.isClientSide){
			return null;
		}
		
		final ResourceKey<Level> dimKey = level.dimension();
		
		Reservoir ret;
		if((ret = getReservoir(dimKey, pos, 1, -1)) == null){
			if((ret = getReservoir(dimKey, pos, 1, 1)) == null){
				if((ret = getReservoir(dimKey, pos, -1, -1)) == null){
					ret = getReservoir(dimKey, pos, -1, 1);
				}
			}
		}
		
		return ret;
	}
	
	private Reservoir getReservoir(ResourceKey<Level> dimKey, ColumnPos pos, int regionXOff, int regionZOff){
		RegionData regionData = getRegionData(new RegionPos(pos, regionXOff, regionZOff));
		return regionData != null ? regionData.get(dimKey, pos) : null;
	}
	
	public boolean existsAt(ColumnPos pos){
		boolean ret;
		if(!(ret = existsAt(pos, 1, -1))){
			if(!(ret = existsAt(pos, 1, 1))){
				if(!(ret = existsAt(pos, -1, -1))){
					ret = existsAt(pos, -1, 1);
				}
			}
		}
		return ret;
	}
	
	private boolean existsAt(ColumnPos pos, int regionXOff, int regionZOff){
		RegionData regionData = getRegionData(new RegionPos(pos, regionXOff, regionZOff));
		if(regionData == null)
			return false;
		
		boolean ret = false;
		synchronized(regionData.reservoirlist){
			ret = regionData.reservoirlist.values().stream().anyMatch(reservoir -> reservoir.getPolygon().contains(pos));
		}
		return ret;
	}
	
	@Nullable
	public RegionData getRegionData(BlockPos pos){
		return getRegionData(new RegionPos(pos));
	}
	
	@Nullable
	public RegionData getRegionData(RegionPos regionPos){
		RegionData ret = this.regions.getOrDefault(regionPos, null);
		return ret;
	}
	
	private RegionData getOrCreateRegionData(RegionPos regionPos){
		RegionData ret = this.regions.computeIfAbsent(regionPos, pos -> {
			String fn = getRegionFileName(pos);
			RegionData data = this.dimData.computeIfAbsent(new Factory<>(() -> new RegionData(pos), (t, p) -> new RegionData(pos, t, p)), fn);
			setDirty();
			log.debug("Created RegionData[{}, {}]", regionPos.x(), regionPos.z());
			return data;
		});
		return ret;
	}
	
	private String getRegionFileName(RegionPos regionPos){
		return DATA_NAME + File.separatorChar + regionPos.x() + "_" + regionPos.z();
	}
	
	// -----------------------------------------------------------------------------
	
}
