package flaxbeard.immersivepetroleum.common;

import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricatedTileInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;

public class IPSaveData extends SavedData{
	public static final String dataName = "ImmersivePetroleum-SaveData";
	
	private static IPSaveData INSTANCE;
	
	public IPSaveData(){
		INSTANCE = this;
	}
	
	public IPSaveData(CompoundTag nbt, HolderLookup.Provider provider){
		INSTANCE = this;
		load(nbt, provider);
	}
	
	@Override
	@Nonnull
	public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider){
		ListTag lubricatedList = new ListTag();
		for(LubricatedTileInfo info: LubricatedHandler.lubricatedTiles){
			if(info != null){
				CompoundTag tag = info.writeToNBT();
				lubricatedList.add(tag);
			}
		}
		nbt.put("lubricated", lubricatedList);
		
		return nbt;
	}
	
	private void load(CompoundTag nbt, HolderLookup.Provider provider){
		ListTag lubricatedList = nbt.getList("lubricated", Tag.TAG_COMPOUND);
		
		LubricatedHandler.lubricatedTiles.clear();
		lubricatedList.stream()
			.map(tag -> new LubricatedTileInfo((CompoundTag) tag))
			.forEach(info -> LubricatedHandler.lubricatedTiles.add(info));
	}
	
	public static void markDirty(){
		if(INSTANCE != null){
			INSTANCE.setDirty();
		}
	}
}
