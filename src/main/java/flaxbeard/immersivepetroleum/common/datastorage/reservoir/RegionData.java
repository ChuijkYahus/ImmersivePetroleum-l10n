package flaxbeard.immersivepetroleum.common.datastorage.reservoir;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Contains reservoirs within a particular region.
 *
 * @author TwistedGate
 */
public class RegionData extends SavedData{
	protected static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/RegionData");
	
	final RegionPos regionPos;
	final Multimap<ResourceKey<Level>, Reservoir> reservoirlist = ArrayListMultimap.create();
	RegionData(RegionPos regionPos){
		this.regionPos = regionPos;
	}
	RegionData(RegionPos regionPos, CompoundTag nbt, HolderLookup.Provider provider){
		this.regionPos = regionPos;
		load(nbt, provider);
	}
	
	@Override
	public void save(File pFile, @Nonnull HolderLookup.Provider provider){
		if(!pFile.getParentFile().exists()){
			pFile.getParentFile().mkdirs();
		}
		super.save(pFile, provider);
	}
	
	@Nonnull
	@Override
	public CompoundTag save(@Nonnull CompoundTag nbt, @Nonnull HolderLookup.Provider provider){
		nbt.putInt("version", ReservoirRegionDataStorage.VERSION);
		
		ListTag reservoirs = new ListTag();
		synchronized(this.reservoirlist){
			for(ResourceKey<Level> dimension: this.reservoirlist.keySet()){
				CompoundTag dim = new CompoundTag();
				dim.putString("dimension", dimension.location().toString());
				
				ListTag islands = new ListTag();
				for(Reservoir reservoir: this.reservoirlist.get(dimension)){
					islands.add(reservoir.writeToNBT());
				}
				dim.put("islands", islands);
				
				reservoirs.add(dim);
			}
		}
		nbt.put("reservoirs", reservoirs);
		
		ReservoirRegionDataStorage.log.debug("{} Saved with {} Reservoirs.", this, this.reservoirlist.size());
		return nbt;
	}
	
	private void load(CompoundTag nbt, HolderLookup.Provider provider){
		int version = nbt.getInt("version");
		if(version != ReservoirRegionDataStorage.VERSION){
			// TODO Trigger Full Storage Update?
		}
		
		ListTag reservoirs = nbt.getList("reservoirs", Tag.TAG_COMPOUND);
		if(!reservoirs.isEmpty()){
			synchronized(this.reservoirlist){
				for(int i = 0;i < reservoirs.size();i++){
					CompoundTag dim = reservoirs.getCompound(i);
					ResourceLocation rl = ResourceLocation.parse(dim.getString("dimension"));
					ResourceKey<Level> dimType = ResourceKey.create(Registries.DIMENSION, rl);
					ListTag islands = dim.getList("islands", Tag.TAG_COMPOUND);
					
					List<Reservoir> list = islands.stream().map(inbt -> Reservoir.readFromNBT((CompoundTag) inbt)).filter(Objects::nonNull).collect(Collectors.toList());
					list.forEach(reservoir -> reservoir.setRegion(this));
					this.reservoirlist.putAll(dimType, list);
				}
			}
			ReservoirRegionDataStorage.log.debug("{} Loaded with {} Reservoirs.", this, this.reservoirlist.size());
		}
	}
	
	/** Position of this RegionData instance. */
	public RegionPos position(){
		return this.regionPos;
	}
	
	/**
	 * Attempts to find a reservoir.
	 *
	 * @param dimension The world-dimension to look in.
	 * @param pos       The position of the possible reservoir.
	 * @return a {@link Reservoir} instance, or null if none was found at <code>pos</code>.
	 */
	@Nullable
	public Reservoir get(ResourceKey<Level> dimension, ColumnPos pos){
		synchronized(this.reservoirlist){
			for(Reservoir reservoir: this.reservoirlist.get(dimension)){
				if(reservoir.getPolygon().contains(pos)){
					// There's no such thing as overlapping islands, so just return what was found directly
					return reservoir;
				}
			}
			return null;
		}
	}
	
	/**
	 * List of all reservoirs. (Read-Only)
	 *
	 * @return {@link ImmutableMultimap}<{@link ResourceKey}<{@link Level}>, {@link Reservoir}>
	 */
	public Multimap<ResourceKey<Level>, Reservoir> getReservoirList(){
		synchronized(this.reservoirlist){
			return ImmutableMultimap.copyOf(this.reservoirlist);
		}
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(this.regionPos);
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		
		if(!(obj instanceof RegionData other))
			return false;
		
		return Objects.equals(this.regionPos, other.regionPos);
	}
	
	@Override
	public String toString(){
		return String.format("RegionData[%d, %d]", this.regionPos.x(), this.regionPos.z());
	}
}
